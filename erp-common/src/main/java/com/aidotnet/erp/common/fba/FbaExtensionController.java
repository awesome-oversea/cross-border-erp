package com.aidotnet.erp.common.fba;

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
@RequestMapping("/fba/api/v1")
public class FbaExtensionController {

    private final FbaExtensionService fbaExtService;

    public FbaExtensionController(FbaExtensionService fbaExtService) {
        this.fbaExtService = fbaExtService;
    }

    @PostMapping("/inbound-plans")
    public Result<FbaExtensionService.InboundPlan> createInboundPlan(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lineMaps = (List<Map<String, Object>>) request.getOrDefault("lines", List.of());
        List<FbaExtensionService.InboundPlanLine> lines = lineMaps.stream()
                .map(m -> new FbaExtensionService.InboundPlanLine(
                        (String) m.get("sku"),
                        ((Number) m.getOrDefault("quantity", 0)).intValue(),
                        (String) m.getOrDefault("fnsku", ""),
                        (String) m.getOrDefault("prepCategory", "NONE")))
                .toList();
        return Result.ok(fbaExtService.createInboundPlan(
                (String) request.get("tenantId"),
                (String) request.get("planName"),
                (String) request.getOrDefault("marketplace", "AMAZON_US"),
                (String) request.getOrDefault("destinationFulfillmentCenter", ""),
                lines
        ));
    }

    @PutMapping("/inbound-plans/{planId}/submit")
    public Result<FbaExtensionService.InboundPlan> submitInboundPlan(@PathVariable String planId) {
        return Result.ok(fbaExtService.submitInboundPlan(planId));
    }

    @PatchMapping("/inbound-plans/{planId}/status")
    public Result<FbaExtensionService.InboundPlan> updateInboundPlanStatus(
            @PathVariable String planId, @RequestBody Map<String, Object> request) {
        return Result.ok(fbaExtService.updateInboundPlanStatus(planId, (String) request.get("status")));
    }

    @GetMapping("/inbound-plans")
    public Result<List<FbaExtensionService.InboundPlan>> listInboundPlans(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String status) {
        return Result.ok(fbaExtService.listInboundPlans(tenantId, status));
    }

    @PostMapping("/shipments")
    public Result<FbaExtensionService.Shipment> createShipment(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lineMaps = (List<Map<String, Object>>) request.getOrDefault("lines", List.of());
        List<FbaExtensionService.ShipmentLine> lines = lineMaps.stream()
                .map(m -> new FbaExtensionService.ShipmentLine(
                        (String) m.get("sku"),
                        ((Number) m.getOrDefault("quantity", 0)).intValue(),
                        ((Number) m.getOrDefault("cartonCount", 0)).intValue()))
                .toList();
        return Result.ok(fbaExtService.createShipment(
                (String) request.get("tenantId"),
                (String) request.getOrDefault("planId", ""),
                (String) request.getOrDefault("shipmentId", ""),
                (String) request.getOrDefault("originAddress", ""),
                (String) request.getOrDefault("carrier", ""),
                (String) request.getOrDefault("trackingNo", ""),
                lines
        ));
    }

    @PatchMapping("/shipments/{internalId}/status")
    public Result<FbaExtensionService.Shipment> updateShipmentStatus(
            @PathVariable String internalId, @RequestBody Map<String, Object> request) {
        return Result.ok(fbaExtService.updateShipmentStatus(
                internalId, (String) request.get("status"), (String) request.get("trackingNo")));
    }

    @GetMapping("/shipments")
    public Result<List<FbaExtensionService.Shipment>> listShipments(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String planId) {
        return Result.ok(fbaExtService.listShipments(tenantId, planId));
    }

    @PostMapping("/cartons")
    public Result<FbaExtensionService.Carton> createCarton(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemMaps = (List<Map<String, Object>>) request.getOrDefault("items", List.of());
        List<FbaExtensionService.CartonItem> items = itemMaps.stream()
                .map(m -> new FbaExtensionService.CartonItem(
                        (String) m.get("sku"),
                        ((Number) m.getOrDefault("quantity", 0)).intValue()))
                .toList();
        return Result.ok(fbaExtService.createCarton(
                (String) request.get("tenantId"),
                (String) request.get("shipmentId"),
                (String) request.getOrDefault("cartonId", ""),
                new BigDecimal(request.getOrDefault("length", "0").toString()),
                new BigDecimal(request.getOrDefault("width", "0").toString()),
                new BigDecimal(request.getOrDefault("height", "0").toString()),
                new BigDecimal(request.getOrDefault("weight", "0").toString()),
                items
        ));
    }

    @GetMapping("/cartons")
    public Result<List<FbaExtensionService.Carton>> listCartons(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String shipmentId) {
        return Result.ok(fbaExtService.listCartons(tenantId, shipmentId));
    }

    @PostMapping("/inventory")
    public Result<FbaExtensionService.FbaInventory> updateFbaInventory(@RequestBody Map<String, Object> request) {
        return Result.ok(fbaExtService.updateFbaInventory(
                (String) request.get("tenantId"),
                (String) request.get("sku"),
                (String) request.getOrDefault("fnsku", ""),
                (String) request.getOrDefault("fulfillmentCenter", ""),
                ((Number) request.getOrDefault("quantity", 0)).intValue(),
                ((Number) request.getOrDefault("reservedQuantity", 0)).intValue(),
                ((Number) request.getOrDefault("inboundQuantity", 0)).intValue(),
                (String) request.getOrDefault("condition", "NEW")
        ));
    }

    @GetMapping("/inventory")
    public Result<List<FbaExtensionService.FbaInventory>> listFbaInventory(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String sku) {
        return Result.ok(fbaExtService.listFbaInventory(tenantId, sku));
    }
}
