package com.aidotnet.erp.common.wms;

import com.aidotnet.erp.common.api.Result;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wms/api/v1")
public class WmsExtensionController {

    private final WmsExtensionService wmsExtService;

    public WmsExtensionController(WmsExtensionService wmsExtService) {
        this.wmsExtService = wmsExtService;
    }

    @PostMapping("/warehouses")
    public Result<WmsExtensionService.Warehouse> createWarehouse(@RequestBody Map<String, Object> request) {
        return Result.ok(wmsExtService.createWarehouse(
                (String) request.get("tenantId"),
                (String) request.get("warehouseName"),
                (String) request.get("warehouseCode"),
                (String) request.getOrDefault("address", ""),
                (String) request.getOrDefault("type", "LOCAL"),
                !request.containsKey("active") || (Boolean) request.get("active")
        ));
    }

    @GetMapping("/warehouses")
    public Result<List<WmsExtensionService.Warehouse>> listWarehouses(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String type) {
        return Result.ok(wmsExtService.listWarehouses(tenantId, type));
    }

    @PostMapping("/storage-locations")
    public Result<WmsExtensionService.StorageLocation> createStorageLocation(@RequestBody Map<String, Object> request) {
        BigDecimal maxWeight = request.containsKey("maxWeight") ? new BigDecimal(request.get("maxWeight").toString()) : null;
        return Result.ok(wmsExtService.createStorageLocation(
                (String) request.get("warehouseId"),
                (String) request.get("locationCode"),
                (String) request.getOrDefault("zone", ""),
                (String) request.getOrDefault("aisle", ""),
                (String) request.getOrDefault("rack", ""),
                (String) request.getOrDefault("bin", ""),
                (String) request.getOrDefault("locationType", "STORAGE"),
                maxWeight
        ));
    }

    @GetMapping("/storage-locations")
    public Result<List<WmsExtensionService.StorageLocation>> listStorageLocations(
            @RequestParam(required = false) String warehouseId) {
        return Result.ok(wmsExtService.listStorageLocations(warehouseId));
    }

    @PostMapping("/inbound-orders")
    public Result<WmsExtensionService.InboundOrder> createInboundOrder(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lineMaps = (List<Map<String, Object>>) request.getOrDefault("lines", List.of());
        List<WmsExtensionService.InboundLine> lines = lineMaps.stream()
                .map(m -> new WmsExtensionService.InboundLine(
                        (String) m.get("sku"),
                        ((Number) m.getOrDefault("quantity", 0)).intValue(),
                        (String) m.getOrDefault("locationCode", "")))
                .toList();
        return Result.ok(wmsExtService.createInboundOrder(
                (String) request.get("tenantId"),
                (String) request.get("warehouseId"),
                (String) request.getOrDefault("sourceType", "PURCHASE"),
                (String) request.getOrDefault("sourceId", ""),
                lines
        ));
    }

    @PutMapping("/inbound-orders/{orderId}/confirm")
    public Result<WmsExtensionService.InboundOrder> confirmInbound(@PathVariable String orderId) {
        return Result.ok(wmsExtService.confirmInbound(orderId));
    }

    @GetMapping("/inbound-orders")
    public Result<List<WmsExtensionService.InboundOrder>> listInboundOrders(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String warehouseId) {
        return Result.ok(wmsExtService.listInboundOrders(tenantId, warehouseId));
    }

    @PostMapping("/outbound-orders")
    public Result<WmsExtensionService.OutboundOrder> createOutboundOrder(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lineMaps = (List<Map<String, Object>>) request.getOrDefault("lines", List.of());
        List<WmsExtensionService.OutboundLine> lines = lineMaps.stream()
                .map(m -> new WmsExtensionService.OutboundLine(
                        (String) m.get("sku"),
                        ((Number) m.getOrDefault("quantity", 0)).intValue(),
                        (String) m.getOrDefault("locationCode", "")))
                .toList();
        return Result.ok(wmsExtService.createOutboundOrder(
                (String) request.get("tenantId"),
                (String) request.get("warehouseId"),
                (String) request.getOrDefault("orderType", "SALES"),
                (String) request.getOrDefault("referenceId", ""),
                lines
        ));
    }

    @PutMapping("/outbound-orders/{orderId}/confirm")
    public Result<WmsExtensionService.OutboundOrder> confirmOutbound(@PathVariable String orderId) {
        return Result.ok(wmsExtService.confirmOutbound(orderId));
    }

    @GetMapping("/outbound-orders")
    public Result<List<WmsExtensionService.OutboundOrder>> listOutboundOrders(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String warehouseId) {
        return Result.ok(wmsExtService.listOutboundOrders(tenantId, warehouseId));
    }

    @PostMapping("/quality-checks")
    public Result<WmsExtensionService.QualityCheck> createQualityCheck(@RequestBody Map<String, Object> request) {
        return Result.ok(wmsExtService.createQualityCheck(
                (String) request.get("tenantId"),
                (String) request.get("warehouseId"),
                (String) request.getOrDefault("inboundOrderId", ""),
                (String) request.get("sku"),
                ((Number) request.getOrDefault("quantity", 0)).intValue(),
                (String) request.getOrDefault("checkType", "INBOUND"),
                (String) request.getOrDefault("result", "PASS"),
                (String) request.getOrDefault("inspector", ""),
                (String) request.getOrDefault("remarks", "")
        ));
    }

    @GetMapping("/quality-checks")
    public Result<List<WmsExtensionService.QualityCheck>> listQualityChecks(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String warehouseId) {
        return Result.ok(wmsExtService.listQualityChecks(tenantId, warehouseId));
    }
}
