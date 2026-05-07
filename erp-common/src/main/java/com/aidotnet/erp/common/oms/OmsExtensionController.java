package com.aidotnet.erp.common.oms;

import com.aidotnet.erp.common.api.Result;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/platform/oms/api/v1")
public class OmsExtensionController {

    private final OmsExtensionService omsExtService;

    public OmsExtensionController(OmsExtensionService omsExtService) {
        this.omsExtService = omsExtService;
    }

    @PostMapping("/orders/sync")
    public Result<OmsExtensionService.OrderSyncRecord> syncOrders(@RequestBody Map<String, Object> request) {
        Instant startTime = request.containsKey("startTime") ? Instant.parse((String) request.get("startTime")) : Instant.now().minusSeconds(86400);
        Instant endTime = request.containsKey("endTime") ? Instant.parse((String) request.get("endTime")) : Instant.now();
        return Result.ok(omsExtService.syncOrders(
                (String) request.get("tenantId"),
                (String) request.get("platform"),
                (String) request.get("storeId"),
                startTime, endTime
        ));
    }

    @GetMapping("/orders/sync-records")
    public Result<List<OmsExtensionService.OrderSyncRecord>> listSyncRecords(
            @RequestParam String tenantId,
            @RequestParam(required = false) String platform) {
        return Result.ok(omsExtService.listSyncRecords(tenantId, platform));
    }

    @PutMapping("/orders/{orderId}/audit")
    public Result<OmsExtensionService.AuditResult> auditOrder(
            @PathVariable String orderId, @RequestBody Map<String, Object> request) {
        return Result.ok(omsExtService.auditOrder(
                (String) request.get("tenantId"),
                orderId,
                (String) request.get("auditor"),
                (String) request.get("auditAction"),
                (String) request.getOrDefault("reason", "")
        ));
    }

    @PutMapping("/orders/{orderId}/allocate")
    public Result<OmsExtensionService.WarehouseAllocation> allocateWarehouse(
            @PathVariable String orderId, @RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<String> warehouses = (List<String>) request.getOrDefault("availableWarehouses", List.of());
        return Result.ok(omsExtService.allocateWarehouse(
                (String) request.get("tenantId"),
                orderId,
                (String) request.get("sku"),
                ((Number) request.getOrDefault("quantity", 1)).intValue(),
                (String) request.get("countryCode"),
                warehouses
        ));
    }

    @GetMapping("/orders/allocations")
    public Result<List<OmsExtensionService.WarehouseAllocation>> listAllocations(
            @RequestParam String tenantId,
            @RequestParam(required = false) String orderId) {
        return Result.ok(omsExtService.listAllocations(tenantId, orderId));
    }

    @GetMapping("/orders/risk-check")
    public Result<OmsExtensionService.RiskCheckResult> checkRisk(@RequestBody Map<String, Object> request) {
        BigDecimal orderAmount = request.containsKey("orderAmount") ? new BigDecimal(request.get("orderAmount").toString()) : BigDecimal.ZERO;
        return Result.ok(omsExtService.checkRisk(
                (String) request.get("tenantId"),
                (String) request.get("orderId"),
                orderAmount,
                (String) request.get("buyerName"),
                (String) request.get("countryCode"),
                (String) request.get("platform")
        ));
    }

    @PostMapping("/orders/risk-check")
    public Result<OmsExtensionService.RiskCheckResult> performRiskCheck(@RequestBody Map<String, Object> request) {
        BigDecimal orderAmount = request.containsKey("orderAmount") ? new BigDecimal(request.get("orderAmount").toString()) : BigDecimal.ZERO;
        return Result.ok(omsExtService.checkRisk(
                (String) request.get("tenantId"),
                (String) request.get("orderId"),
                orderAmount,
                (String) request.get("buyerName"),
                (String) request.get("countryCode"),
                (String) request.get("platform")
        ));
    }

    @GetMapping("/orders/risk-checks")
    public Result<List<OmsExtensionService.RiskCheckResult>> listRiskChecks(
            @RequestParam String tenantId,
            @RequestParam(required = false) String orderId) {
        return Result.ok(omsExtService.listRiskChecks(tenantId, orderId));
    }
}
