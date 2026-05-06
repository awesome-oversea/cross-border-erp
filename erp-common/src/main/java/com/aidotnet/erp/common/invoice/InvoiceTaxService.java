package com.aidotnet.erp.common.invoice;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InvoiceTaxService {

    private static final Logger log = LoggerFactory.getLogger(InvoiceTaxService.class);
    private final Map<String, TaxRate> taxRates = new ConcurrentHashMap<>();
    private final Map<String, InvoiceTemplate> templates = new ConcurrentHashMap<>();
    private final Map<String, Invoice> invoices = new ConcurrentHashMap<>();

    public void addTaxRate(String country, String region, String taxType, BigDecimal rate) {
        String key = country + ":" + region + ":" + taxType;
        taxRates.put(key, new TaxRate(country, region, taxType, rate));
        log.info("Added tax rate: country={}, region={}, type={}, rate={}", country, region, taxType, rate);
    }

    public TaxRate findTaxRate(String country, String region, String taxType) {
        String key = country + ":" + region + ":" + taxType;
        return taxRates.get(key);
    }

    public List<TaxRate> listTaxRates(String country) {
        return taxRates.values().stream()
                .filter(t -> country == null || country.equals(t.country()))
                .toList();
    }

    public BigDecimal calculateTax(String country, String region, String taxType, BigDecimal amount) {
        String key = country + ":" + region + ":" + taxType;
        TaxRate rate = taxRates.get(key);
        if (rate == null) {
            log.warn("No tax rate found: country={}, region={}, type={}", country, region, taxType);
            return BigDecimal.ZERO;
        }
        return amount.multiply(rate.rate()).setScale(2, RoundingMode.HALF_UP);
    }

    public TaxCalculationResult calculateTaxWithDetails(String country, String region, BigDecimal amount,
                                                          List<String> taxTypes) {
        List<TaxLine> taxLines = new ArrayList<>();
        BigDecimal totalTax = BigDecimal.ZERO;
        for (String taxType : taxTypes) {
            BigDecimal taxAmount = calculateTax(country, region, taxType, amount);
            TaxRate rate = findTaxRate(country, region, taxType);
            taxLines.add(new TaxLine(taxType, rate != null ? rate.rate() : BigDecimal.ZERO, taxAmount));
            totalTax = totalTax.add(taxAmount);
        }
        BigDecimal totalWithTax = amount.add(totalTax);
        return new TaxCalculationResult(country, region, amount, taxLines, totalTax, totalWithTax);
    }

    public InvoiceTemplate createTemplate(String templateId, String templateName, String templateType,
                                            String country, Map<String, Object> layout) {
        InvoiceTemplate template = new InvoiceTemplate(templateId, templateName, templateType,
                country, layout, true, Instant.now());
        templates.put(templateId, template);
        log.info("Created invoice template: id={}, name={}, type={}", templateId, templateName, templateType);
        return template;
    }

    public List<InvoiceTemplate> listTemplates(String templateType) {
        return templates.values().stream()
                .filter(t -> templateType == null || templateType.equals(t.templateType()))
                .toList();
    }

    public Invoice generateInvoice(String orderId, String invoiceType, String templateId,
                                     String tenantId, String currency, BigDecimal amount,
                                     String customerName, Map<String, Object> data) {
        String invoiceNumber = "INV-" + System.currentTimeMillis();
        Invoice invoice = new Invoice(invoiceNumber, orderId, invoiceType, templateId,
                tenantId, currency, amount, customerName, data, "ACTIVE", null, null, Instant.now(), Instant.now());
        invoices.put(invoiceNumber, invoice);
        log.info("Generated invoice: number={}, orderId={}, type={}, amount={}", invoiceNumber, orderId, invoiceType, amount);
        return invoice;
    }

    public List<Invoice> listInvoices(String tenantId, String invoiceType) {
        return invoices.values().stream()
                .filter(i -> tenantId == null || tenantId.equals(i.tenantId()))
                .filter(i -> invoiceType == null || invoiceType.equals(i.invoiceType()))
                .toList();
    }

    public Invoice voidInvoice(String invoiceNumber, String reason) {
        Invoice invoice = invoices.get(invoiceNumber);
        if (invoice == null) {
            throw new IllegalArgumentException("Invoice not found: " + invoiceNumber);
        }
        if (!"ACTIVE".equals(invoice.status())) {
            throw new IllegalStateException("Only ACTIVE invoices can be voided");
        }
        Invoice voided = new Invoice(invoice.invoiceNumber(), invoice.orderId(), invoice.invoiceType(),
                invoice.templateId(), invoice.tenantId(), invoice.currency(), invoice.amount(),
                invoice.customerName(), invoice.data(), "VOIDED", reason, null,
                invoice.createdAt(), Instant.now());
        invoices.put(invoiceNumber, voided);
        log.info("Voided invoice: number={}, reason={}", invoiceNumber, reason);
        return voided;
    }

    public Invoice redFlushInvoice(String invoiceNumber, String reason) {
        Invoice original = invoices.get(invoiceNumber);
        if (original == null) {
            throw new IllegalArgumentException("Invoice not found: " + invoiceNumber);
        }
        if (!"ACTIVE".equals(original.status())) {
            throw new IllegalStateException("Only ACTIVE invoices can be red-flushed");
        }
        Invoice redFlushed = new Invoice(original.invoiceNumber(), original.orderId(), original.invoiceType(),
                original.templateId(), original.tenantId(), original.currency(), original.amount().negate(),
                original.customerName(), original.data(), "RED_FLUSHED", reason, invoiceNumber,
                original.createdAt(), Instant.now());
        String redFlushNumber = "RF-" + invoiceNumber;
        invoices.put(redFlushNumber, redFlushed);

        Invoice updatedOriginal = new Invoice(original.invoiceNumber(), original.orderId(), original.invoiceType(),
                original.templateId(), original.tenantId(), original.currency(), original.amount(),
                original.customerName(), original.data(), "RED_FLUSHED", "Red flushed: " + redFlushNumber, null,
                original.createdAt(), Instant.now());
        invoices.put(invoiceNumber, updatedOriginal);
        log.info("Red flushed invoice: original={}, redFlushNumber={}, reason={}", invoiceNumber, redFlushNumber, reason);
        return redFlushed;
    }

    public TaxFilingData getFilingData(String country, String region, LocalDate periodStart, LocalDate periodEnd) {
        List<Invoice> periodInvoices = invoices.values().stream()
                .filter(i -> "ACTIVE".equals(i.status()))
                .filter(i -> i.createdAt().isAfter(periodStart.atStartOfDay().toInstant(java.time.ZoneOffset.UTC)))
                .filter(i -> i.createdAt().isBefore(periodEnd.plusDays(1).atStartOfDay().toInstant(java.time.ZoneOffset.UTC)))
                .toList();
        BigDecimal totalSales = periodInvoices.stream()
                .filter(i -> i.amount().compareTo(BigDecimal.ZERO) > 0)
                .map(Invoice::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalRefunds = periodInvoices.stream()
                .filter(i -> i.amount().compareTo(BigDecimal.ZERO) < 0)
                .map(Invoice::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netSales = totalSales.add(totalRefunds);
        BigDecimal totalTax = calculateTax(country, region, "VAT", netSales);
        return new TaxFilingData(country, region, periodStart, periodEnd,
                periodInvoices.size(), totalSales, totalRefunds, netSales, totalTax);
    }

    public record TaxRate(String country, String region, String taxType, BigDecimal rate) {}
    public record TaxLine(String taxType, BigDecimal rate, BigDecimal amount) {}
    public record TaxCalculationResult(String country, String region, BigDecimal taxableAmount,
                                        List<TaxLine> taxLines, BigDecimal totalTax,
                                        BigDecimal totalWithTax) {}
    public record InvoiceTemplate(String templateId, String templateName, String templateType,
                                   String country, Map<String, Object> layout,
                                   boolean active, Instant createdAt) {}
    public record Invoice(String invoiceNumber, String orderId, String invoiceType,
                           String templateId, String tenantId, String currency,
                           BigDecimal amount, String customerName, Map<String, Object> data,
                           String status, String voidReason, String redFlushRef,
                           Instant createdAt, Instant updatedAt) {}
    public record TaxFilingData(String country, String region, LocalDate periodStart, LocalDate periodEnd,
                                 int invoiceCount, BigDecimal totalSales, BigDecimal totalRefunds,
                                 BigDecimal netSales, BigDecimal estimatedTax) {}
}
