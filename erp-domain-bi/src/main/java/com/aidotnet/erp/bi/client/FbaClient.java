package com.aidotnet.erp.bi.client;

import com.aidotnet.erp.common.api.Result;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * FBA 客户端 - BI 域调用 FBA 域。
 * <p>
 * 描述: BI 通过只读接口获取 FBA 货件和异常数据，用于货件时效、异常和成本分析。
 * </p>
 */
@FeignClient(name = "erp-app", contextId = "bi-fba-client", path = "/fba/api/in/v1")
public interface FbaClient {

    @GetMapping("/shipments")
    Result<List<FbaShipmentResponse>> listShipments(@RequestParam(required = false) String planId);

    @GetMapping("/shipment-exceptions")
    Result<List<ShipmentExceptionResponse>> listShipmentExceptions(@RequestParam String shipmentId);

    record FbaShipmentResponse(String fbaShipmentId,
                               String tenantId,
                               String amazonShipmentId,
                               String destinationFc,
                               String planId,
                               String carrier,
                               String trackingNo,
                               int plannedQuantity,
                               int receivedQuantity,
                               int cartonCount,
                               BigDecimal totalWeight,
                               String status,
                               Instant packedAt,
                               Instant shippedAt,
                               Instant createdAt,
                               Instant updatedAt) {}

    record ShipmentExceptionResponse(String exceptionId,
                                     String tenantId,
                                     String shipmentId,
                                     String type,
                                     int qty,
                                     String status,
                                     String description,
                                     String resolvedBy,
                                     Instant resolvedAt,
                                     Instant createdAt,
                                     Instant updatedAt) {}
}
