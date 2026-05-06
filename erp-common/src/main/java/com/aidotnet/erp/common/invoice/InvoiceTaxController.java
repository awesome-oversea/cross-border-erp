package com.aidotnet.erp.common.invoice;

import com.aidotnet.erp.common.api.Result;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fms/api/v1/invoice")
public class InvoiceTaxController {

    private final InvoiceTaxService invoiceTaxService;

    public InvoiceTaxController(InvoiceTaxService invoiceTaxService) {
        this.invoiceTaxService = invoiceTaxService;
    }

    @PostMapping("/generate")
    public Result<InvoiceTaxService.Invoice> generate(@RequestBody Map<String, Object> request) {
        InvoiceTaxService.Invoice invoice = invoiceTaxService.generateInvoice(
                (String) request.get("orderId"),
                (String) request.getOrDefault("invoiceType", "SALES"),
                (String) request.getOrDefault("templateId", "default"),
                (String) request.get("tenantId"),
                (String) request.getOrDefault("currency", "USD"),
                new BigDecimal(request.get("amount").toString()),
                (String) request.getOrDefault("customerName", ""),
                null
        );
        return Result.ok(invoice);
    }

    @GetMapping("/tax-rates")
    public Result<List<InvoiceTaxService.TaxRate>> getTaxRates(
            @RequestParam(required = false) String country) {
        return Result.ok(invoiceTaxService.listTaxRates(country));
    }

    @GetMapping("/list")
    public Result<List<InvoiceTaxService.Invoice>> getInvoiceList(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String invoiceType) {
        return Result.ok(invoiceTaxService.listInvoices(tenantId, invoiceType));
    }

    @GetMapping("/templates")
    public Result<List<InvoiceTaxService.InvoiceTemplate>> getTemplates(
            @RequestParam(required = false) String templateType) {
        return Result.ok(invoiceTaxService.listTemplates(templateType));
    }

    @PostMapping("/templates")
    public Result<InvoiceTaxService.InvoiceTemplate> createTemplate(@RequestBody Map<String, Object> request) {
        InvoiceTaxService.InvoiceTemplate template = invoiceTaxService.createTemplate(
                (String) request.get("templateId"),
                (String) request.get("templateName"),
                (String) request.getOrDefault("templateType", "SALES"),
                (String) request.getOrDefault("country", ""),
                null
        );
        return Result.ok(template);
    }

    @PutMapping("/{id}/void")
    public Result<InvoiceTaxService.Invoice> voidInvoice(@PathVariable String id,
                                                           @RequestBody(required = false) Map<String, String> request) {
        String reason = request != null ? request.getOrDefault("reason", "Voided by user") : "Voided by user";
        return Result.ok(invoiceTaxService.voidInvoice(id, reason));
    }

    @PostMapping("/{id}/red-flush")
    public Result<InvoiceTaxService.Invoice> redFlush(@PathVariable String id,
                                                        @RequestBody(required = false) Map<String, String> request) {
        String reason = request != null ? request.getOrDefault("reason", "Red flush") : "Red flush";
        return Result.ok(invoiceTaxService.redFlushInvoice(id, reason));
    }
}

@RestController
@RequestMapping("/fms/api/v1/tax")
class TaxController {

    private final InvoiceTaxService invoiceTaxService;

    public TaxController(InvoiceTaxService invoiceTaxService) {
        this.invoiceTaxService = invoiceTaxService;
    }

    @PostMapping("/calculate")
    public Result<InvoiceTaxService.TaxCalculationResult> calculate(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<String> taxTypes = (List<String>) request.getOrDefault("taxTypes", List.of("VAT"));
        InvoiceTaxService.TaxCalculationResult result = invoiceTaxService.calculateTaxWithDetails(
                (String) request.get("country"),
                (String) request.getOrDefault("region", ""),
                new BigDecimal(request.get("amount").toString()),
                taxTypes
        );
        return Result.ok(result);
    }

    @GetMapping("/filing-data")
    public Result<InvoiceTaxService.TaxFilingData> getFilingData(
            @RequestParam String country,
            @RequestParam(required = false, defaultValue = "") String region,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate periodEnd) {
        return Result.ok(invoiceTaxService.getFilingData(country, region, periodStart, periodEnd));
    }
}
