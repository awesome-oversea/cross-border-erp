package com.aidotnet.erp.common.tms;

import com.aidotnet.erp.common.api.Result;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tms/api/v1")
public class TmsExtensionController {

    private final TmsExtensionService tmsExtService;

    public TmsExtensionController(TmsExtensionService tmsExtService) {
        this.tmsExtService = tmsExtService;
    }

    @PostMapping("/carriers")
    public Result<TmsExtensionService.Carrier> createCarrier(@RequestBody Map<String, Object> request) {
        return Result.ok(tmsExtService.createCarrier(
                (String) request.get("tenantId"),
                (String) request.get("carrierName"),
                (String) request.get("carrierCode"),
                (String) request.getOrDefault("carrierType", "EXPRESS"),
                (String) request.getOrDefault("contactPerson", ""),
                (String) request.getOrDefault("phone", ""),
                !request.containsKey("active") || (Boolean) request.get("active")
        ));
    }

    @GetMapping("/carriers")
    public Result<List<TmsExtensionService.Carrier>> listCarriers(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String carrierType) {
        return Result.ok(tmsExtService.listCarriers(tenantId, carrierType));
    }

    @PostMapping("/shipping-methods")
    public Result<TmsExtensionService.ShippingMethod> createShippingMethod(@RequestBody Map<String, Object> request) {
        BigDecimal minWeight = request.containsKey("minWeight") ? new BigDecimal(request.get("minWeight").toString()) : null;
        BigDecimal maxWeight = request.containsKey("maxWeight") ? new BigDecimal(request.get("maxWeight").toString()) : null;
        return Result.ok(tmsExtService.createShippingMethod(
                (String) request.get("carrierId"),
                (String) request.get("methodName"),
                (String) request.get("methodCode"),
                (String) request.getOrDefault("originCountry", "CN"),
                (String) request.get("destinationCountry"),
                minWeight, maxWeight,
                request.containsKey("estimatedDays") ? ((Number) request.get("estimatedDays")).intValue() : 15,
                !request.containsKey("active") || (Boolean) request.get("active")
        ));
    }

    @GetMapping("/shipping-methods")
    public Result<List<TmsExtensionService.ShippingMethod>> listShippingMethods(
            @RequestParam(required = false) String carrierId,
            @RequestParam(required = false) String destinationCountry) {
        return Result.ok(tmsExtService.listShippingMethods(carrierId, destinationCountry));
    }

    @PostMapping("/shipping-rates/estimate")
    public Result<TmsExtensionService.ShippingEstimate> estimateShipping(@RequestBody Map<String, Object> request) {
        BigDecimal weight = request.containsKey("weight") ? new BigDecimal(request.get("weight").toString()) : null;
        BigDecimal length = request.containsKey("length") ? new BigDecimal(request.get("length").toString()) : null;
        BigDecimal width = request.containsKey("width") ? new BigDecimal(request.get("width").toString()) : null;
        BigDecimal height = request.containsKey("height") ? new BigDecimal(request.get("height").toString()) : null;
        return Result.ok(tmsExtService.estimateShipping(
                (String) request.getOrDefault("originCountry", "CN"),
                (String) request.get("destinationCountry"),
                weight, length, width, height
        ));
    }

    @PostMapping("/trackings/{trackingNo}")
    public Result<TmsExtensionService.TrackingInfo> updateTracking(
            @PathVariable String trackingNo, @RequestBody Map<String, Object> request) {
        return Result.ok(tmsExtService.updateTracking(
                trackingNo,
                (String) request.get("carrierId"),
                (String) request.get("status"),
                (String) request.getOrDefault("location", ""),
                (String) request.getOrDefault("description", "")
        ));
    }

    @GetMapping("/trackings/{trackingNo}")
    public Result<TmsExtensionService.TrackingInfo> getTracking(@PathVariable String trackingNo) {
        return Result.ok(tmsExtService.getTracking(trackingNo));
    }

    @PostMapping("/shipping-batches")
    public Result<TmsExtensionService.ShippingBatch> createShippingBatch(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<String> shipmentIds = (List<String>) request.getOrDefault("shipmentIds", List.of());
        return Result.ok(tmsExtService.createShippingBatch(
                (String) request.get("tenantId"),
                (String) request.get("carrierId"),
                (String) request.getOrDefault("warehouseId", ""),
                shipmentIds,
                (String) request.getOrDefault("operator", "")
        ));
    }

    @PatchMapping("/shipping-batches/{batchId}/status")
    public Result<TmsExtensionService.ShippingBatch> updateBatchStatus(
            @PathVariable String batchId, @RequestBody Map<String, Object> request) {
        return Result.ok(tmsExtService.updateBatchStatus(batchId, (String) request.get("status")));
    }

    @PostMapping("/carrier-performance")
    public Result<TmsExtensionService.CarrierPerformance> recordCarrierPerformance(@RequestBody Map<String, Object> request) {
        BigDecimal avgDeliveryDays = request.containsKey("avgDeliveryDays") ? new BigDecimal(request.get("avgDeliveryDays").toString()) : BigDecimal.ZERO;
        return Result.ok(tmsExtService.recordCarrierPerformance(
                (String) request.get("tenantId"),
                (String) request.get("carrierId"),
                ((Number) request.getOrDefault("totalShipments", 0)).intValue(),
                ((Number) request.getOrDefault("onTimeDeliveries", 0)).intValue(),
                ((Number) request.getOrDefault("damagedShipments", 0)).intValue(),
                ((Number) request.getOrDefault("lostShipments", 0)).intValue(),
                avgDeliveryDays
        ));
    }

    @GetMapping("/carrier-performance")
    public Result<List<TmsExtensionService.CarrierPerformance>> listCarrierPerformances(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String carrierId) {
        return Result.ok(tmsExtService.listCarrierPerformances(tenantId, carrierId));
    }
}
