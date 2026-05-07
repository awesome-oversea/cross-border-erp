package com.aidotnet.erp.oms.client;

import com.aidotnet.erp.common.api.Result;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "erp-app", contextId = "tms-client-oms")
public interface TmsClient {

    @PostMapping("/tms/api/v1/shipments")
    Result<ShipmentResponse> createShipment(@RequestBody CreateShipmentRequest request);

    @PatchMapping("/tms/api/v1/shipments/{shipmentId}/tracking")
    Result<ShipmentResponse> addTracking(@PathVariable String shipmentId, @RequestBody AddTrackingRequest request);

    @PostMapping("/tms/api/out/v1/carriers/recommendations")
    Result<List<CarrierRecommendationResponse>> recommendCarriers(@RequestBody RecommendCarrierRequest request);

    record CreateShipmentRequest(String orderId, String carrierId, String trackingNo, String destinationCountry) {}

    record AddTrackingRequest(String status, String location, String description) {}

    record RecommendCarrierRequest(String destinationCountry, String warehouseCountry,
                                   int packageQuantity, BigDecimal packageAmount, boolean splitShipment) {}

    record ShipmentResponse(String shipmentId, String tenantId, String orderId, String carrierId, String trackingNo,
                            String destinationCountry, String status) {}

    record CarrierRecommendationResponse(String carrierId, String carrierCode, String carrierName,
                                         String serviceLevel, BigDecimal estimatedCost,
                                         int estimatedDeliveryDays, int recommendationScore) {}
}
