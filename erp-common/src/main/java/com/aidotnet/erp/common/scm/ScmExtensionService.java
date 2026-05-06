package com.aidotnet.erp.common.scm;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ScmExtensionService {

    private static final Logger log = LoggerFactory.getLogger(ScmExtensionService.class);
    private final Map<String, Supplier> suppliers = new ConcurrentHashMap<>();
    private final Map<String, Quote> quotes = new ConcurrentHashMap<>();
    private final Map<String, SupplierScore> supplierScores = new ConcurrentHashMap<>();
    private final Map<String, ProcessingOrder> processingOrders = new ConcurrentHashMap<>();

    public Supplier createSupplier(String tenantId, String supplierName, String contactPerson,
                                    String phone, String email, String address, String category) {
        String supplierId = "SUP-" + System.currentTimeMillis();
        Supplier supplier = new Supplier(supplierId, tenantId, supplierName, contactPerson,
                phone, email, address, category, true, Instant.now(), Instant.now());
        suppliers.put(supplierId, supplier);
        log.info("Created supplier: id={}, name={}, category={}", supplierId, supplierName, category);
        return supplier;
    }

    public Supplier updateSupplier(String supplierId, String supplierName, String contactPerson,
                                    String phone, String email, boolean active) {
        Supplier existing = suppliers.get(supplierId);
        if (existing == null) throw new IllegalArgumentException("Supplier not found: " + supplierId);
        Supplier updated = new Supplier(supplierId, existing.tenantId(),
                supplierName != null ? supplierName : existing.supplierName(),
                contactPerson != null ? contactPerson : existing.contactPerson(),
                phone != null ? phone : existing.phone(),
                email != null ? email : existing.email(),
                existing.address(), existing.category(), active, existing.createdAt(), Instant.now());
        suppliers.put(supplierId, updated);
        log.info("Updated supplier: id={}", supplierId);
        return updated;
    }

    public List<Supplier> listSuppliers(String tenantId, String category) {
        return suppliers.values().stream()
                .filter(s -> tenantId == null || tenantId.equals(s.tenantId()))
                .filter(s -> category == null || category.equals(s.category()))
                .toList();
    }

    public Quote createQuote(String tenantId, String supplierId, String sku,
                              BigDecimal quantity, BigDecimal unitPrice, String currency,
                              String validUntil, String requester) {
        String quoteId = "QUOTE-" + System.currentTimeMillis();
        BigDecimal totalPrice = quantity.multiply(unitPrice).setScale(2, java.math.RoundingMode.HALF_UP);
        Quote quote = new Quote(quoteId, tenantId, supplierId, sku, quantity,
                unitPrice, totalPrice, currency, validUntil, "PENDING", requester, Instant.now());
        quotes.put(quoteId, quote);
        log.info("Created quote: id={}, supplier={}, sku={}, totalPrice={}", quoteId, supplierId, sku, totalPrice);
        return quote;
    }

    public Quote respondQuote(String quoteId, BigDecimal unitPrice, String status) {
        Quote existing = quotes.get(quoteId);
        if (existing == null) throw new IllegalArgumentException("Quote not found: " + quoteId);
        BigDecimal finalPrice = unitPrice != null ? unitPrice : existing.unitPrice();
        BigDecimal totalPrice = existing.quantity().multiply(finalPrice).setScale(2, java.math.RoundingMode.HALF_UP);
        Quote updated = new Quote(quoteId, existing.tenantId(), existing.supplierId(),
                existing.sku(), existing.quantity(), finalPrice, totalPrice,
                existing.currency(), existing.validUntil(), status, existing.requester(), existing.createdAt());
        quotes.put(quoteId, updated);
        log.info("Quote responded: id={}, status={}, unitPrice={}", quoteId, status, finalPrice);
        return updated;
    }

    public List<Quote> listQuotes(String tenantId, String supplierId) {
        return quotes.values().stream()
                .filter(q -> tenantId == null || tenantId.equals(q.tenantId()))
                .filter(q -> supplierId == null || supplierId.equals(q.supplierId()))
                .toList();
    }

    public SupplierScore scoreSupplier(String tenantId, String supplierId,
                                        BigDecimal qualityScore, BigDecimal deliveryScore,
                                        BigDecimal priceScore, BigDecimal serviceScore) {
        BigDecimal qualityWeight = new BigDecimal("0.30");
        BigDecimal deliveryWeight = new BigDecimal("0.25");
        BigDecimal priceWeight = new BigDecimal("0.25");
        BigDecimal serviceWeight = new BigDecimal("0.20");
        BigDecimal totalScore = qualityScore.multiply(qualityWeight)
                .add(deliveryScore.multiply(deliveryWeight))
                .add(priceScore.multiply(priceWeight))
                .add(serviceScore.multiply(serviceWeight))
                .setScale(1, java.math.RoundingMode.HALF_UP);

        String grade;
        if (totalScore.compareTo(new BigDecimal("90")) >= 0) grade = "A";
        else if (totalScore.compareTo(new BigDecimal("75")) >= 0) grade = "B";
        else if (totalScore.compareTo(new BigDecimal("60")) >= 0) grade = "C";
        else grade = "D";

        String scoreId = "SCORE-" + System.currentTimeMillis();
        SupplierScore score = new SupplierScore(scoreId, tenantId, supplierId,
                qualityScore, deliveryScore, priceScore, serviceScore,
                totalScore, grade, Instant.now());
        supplierScores.put(scoreId, score);
        log.info("Scored supplier: id={}, supplier={}, total={}, grade={}", scoreId, supplierId, totalScore, grade);
        return score;
    }

    public List<SupplierScore> listSupplierScores(String tenantId, String supplierId) {
        return supplierScores.values().stream()
                .filter(s -> tenantId == null || tenantId.equals(s.tenantId()))
                .filter(s -> supplierId == null || supplierId.equals(s.supplierId()))
                .toList();
    }

    public ProcessingOrder createProcessingOrder(String tenantId, String supplierId, String sku,
                                                   BigDecimal quantity, String processingType,
                                                   String description, Instant dueDate) {
        String orderId = "PROC-" + System.currentTimeMillis();
        ProcessingOrder order = new ProcessingOrder(orderId, tenantId, supplierId, sku,
                quantity, processingType, description, "PENDING", dueDate, Instant.now(), Instant.now());
        processingOrders.put(orderId, order);
        log.info("Created processing order: id={}, supplier={}, sku={}, type={}", orderId, supplierId, sku, processingType);
        return order;
    }

    public ProcessingOrder updateProcessingOrderStatus(String orderId, String status) {
        ProcessingOrder existing = processingOrders.get(orderId);
        if (existing == null) throw new IllegalArgumentException("Processing order not found: " + orderId);
        ProcessingOrder updated = new ProcessingOrder(orderId, existing.tenantId(), existing.supplierId(),
                existing.sku(), existing.quantity(), existing.processingType(),
                existing.description(), status, existing.dueDate(), existing.createdAt(), Instant.now());
        processingOrders.put(orderId, updated);
        log.info("Updated processing order status: id={}, status={}", orderId, status);
        return updated;
    }

    public List<ProcessingOrder> listProcessingOrders(String tenantId, String supplierId) {
        return processingOrders.values().stream()
                .filter(o -> tenantId == null || tenantId.equals(o.tenantId()))
                .filter(o -> supplierId == null || supplierId.equals(o.supplierId()))
                .toList();
    }

    public record Supplier(String supplierId, String tenantId, String supplierName, String contactPerson,
                            String phone, String email, String address, String category,
                            boolean active, Instant createdAt, Instant updatedAt) {}
    public record Quote(String quoteId, String tenantId, String supplierId, String sku,
                         BigDecimal quantity, BigDecimal unitPrice, BigDecimal totalPrice,
                         String currency, String validUntil, String status, String requester, Instant createdAt) {}
    public record SupplierScore(String scoreId, String tenantId, String supplierId,
                                 BigDecimal qualityScore, BigDecimal deliveryScore,
                                 BigDecimal priceScore, BigDecimal serviceScore,
                                 BigDecimal totalScore, String grade, Instant scoredAt) {}
    public record ProcessingOrder(String orderId, String tenantId, String supplierId, String sku,
                                   BigDecimal quantity, String processingType, String description,
                                   String status, Instant dueDate, Instant createdAt, Instant updatedAt) {}
}
