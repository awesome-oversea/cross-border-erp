package com.aidotnet.erp.common.oms;

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
public class OmsExtensionService {

    private static final Logger log = LoggerFactory.getLogger(OmsExtensionService.class);
    private final Map<String, OrderSyncRecord> syncRecords = new ConcurrentHashMap<>();
    private final Map<String, WarehouseAllocation> allocations = new ConcurrentHashMap<>();
    private final Map<String, RiskCheckResult> riskChecks = new ConcurrentHashMap<>();

    public OrderSyncRecord syncOrders(String tenantId, String platform, String storeId,
                                       Instant startTime, Instant endTime) {
        String syncId = "SYNC-" + System.currentTimeMillis();
        int pulledCount = 0;
        int newCount = 0;
        int updatedCount = 0;
        int failedCount = 0;
        String status = "COMPLETED";

        OrderSyncRecord record = new OrderSyncRecord(syncId, tenantId, platform, storeId,
                startTime, endTime, pulledCount, newCount, updatedCount, failedCount,
                status, Instant.now());
        syncRecords.put(syncId, record);
        log.info("Order sync completed: id={}, platform={}, pulled={}, new={}, updated={}, failed={}",
                syncId, platform, pulledCount, newCount, updatedCount, failedCount);
        return record;
    }

    public OrderSyncRecord getSyncRecord(String syncId) {
        return syncRecords.get(syncId);
    }

    public List<OrderSyncRecord> listSyncRecords(String tenantId, String platform) {
        return syncRecords.values().stream()
                .filter(r -> tenantId == null || tenantId.equals(r.tenantId()))
                .filter(r -> platform == null || platform.equals(r.platform()))
                .toList();
    }

    public AuditResult auditOrder(String tenantId, String orderId, String auditor,
                                   String auditAction, String reason) {
        boolean approved = "APPROVE".equals(auditAction);
        if (approved) {
            log.info("Order audited and approved: tenant={}, orderId={}, auditor={}", tenantId, orderId, auditor);
        } else {
            log.info("Order audited and rejected: tenant={}, orderId={}, auditor={}, reason={}", tenantId, orderId, auditor, reason);
        }
        return new AuditResult(orderId, tenantId, auditor, auditAction,
                approved ? "APPROVED" : "REJECTED", reason, Instant.now());
    }

    public WarehouseAllocation allocateWarehouse(String tenantId, String orderId, String sku,
                                                  int quantity, String countryCode,
                                                  List<String> availableWarehouses) {
        String warehouseId = selectWarehouse(countryCode, availableWarehouses);
        String allocId = "ALLOC-" + System.currentTimeMillis();
        WarehouseAllocation allocation = new WarehouseAllocation(allocId, tenantId, orderId,
                sku, quantity, warehouseId, "ALLOCATED", Instant.now());
        allocations.put(allocId, allocation);
        log.info("Warehouse allocated: id={}, orderId={}, warehouse={}, sku={}", allocId, orderId, warehouseId, sku);
        return allocation;
    }

    public List<WarehouseAllocation> listAllocations(String tenantId, String orderId) {
        return allocations.values().stream()
                .filter(a -> tenantId.equals(a.tenantId()))
                .filter(a -> orderId == null || orderId.equals(a.orderId()))
                .toList();
    }

    private String selectWarehouse(String countryCode, List<String> availableWarehouses) {
        if (availableWarehouses == null || availableWarehouses.isEmpty()) {
            return "DEFAULT-WH";
        }
        return availableWarehouses.get(0);
    }

    public RiskCheckResult checkRisk(String tenantId, String orderId, BigDecimal orderAmount,
                                      String buyerName, String countryCode, String platform) {
        List<String> riskFlags = new ArrayList<>();
        String riskLevel = "LOW";

        if (orderAmount.compareTo(new BigDecimal("10000")) > 0) {
            riskFlags.add("HIGH_AMOUNT");
            riskLevel = "HIGH";
        } else if (orderAmount.compareTo(new BigDecimal("5000")) > 0) {
            riskFlags.add("AMOUNT_WARNING");
            riskLevel = "MEDIUM";
        }

        List<String> highRiskCountries = List.of("XX", "YY");
        if (highRiskCountries.contains(countryCode)) {
            riskFlags.add("HIGH_RISK_COUNTRY");
            riskLevel = "HIGH";
        }

        if (buyerName != null && buyerName.matches(".*\\d{5,}.*")) {
            riskFlags.add("SUSPICIOUS_BUYER");
            if (!"HIGH".equals(riskLevel)) riskLevel = "MEDIUM";
        }

        String checkId = "RISK-" + System.currentTimeMillis();
        RiskCheckResult result = new RiskCheckResult(checkId, tenantId, orderId,
                riskLevel, riskFlags, riskFlags.isEmpty() ? "PASS" : "REVIEW_REQUIRED", Instant.now());
        riskChecks.put(checkId, result);
        log.info("Risk check completed: id={}, orderId={}, level={}, flags={}", checkId, orderId, riskLevel, riskFlags);
        return result;
    }

    public List<RiskCheckResult> listRiskChecks(String tenantId, String orderId) {
        return riskChecks.values().stream()
                .filter(r -> tenantId.equals(r.tenantId()))
                .filter(r -> orderId == null || orderId.equals(r.orderId()))
                .toList();
    }

    public record OrderSyncRecord(String syncId, String tenantId, String platform, String storeId,
                                   Instant startTime, Instant endTime, int pulledCount,
                                   int newCount, int updatedCount, int failedCount,
                                   String status, Instant syncedAt) {}
    public record AuditResult(String orderId, String tenantId, String auditor,
                               String auditAction, String result, String reason, Instant auditedAt) {}
    public record WarehouseAllocation(String allocId, String tenantId, String orderId,
                                       String sku, int quantity, String warehouseId,
                                       String status, Instant allocatedAt) {}
    public record RiskCheckResult(String checkId, String tenantId, String orderId,
                                   String riskLevel, List<String> riskFlags,
                                   String decision, Instant checkedAt) {}
}
