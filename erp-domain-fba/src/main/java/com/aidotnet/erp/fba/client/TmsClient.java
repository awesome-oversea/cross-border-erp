package com.aidotnet.erp.fba.client;

import com.aidotnet.erp.common.api.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * TMS客户端 - FBA域调用TMS域
 * <p>
 * 描述: FBA域通过此客户端调用TMS域的内部API，获取物流信息。
 *       用于FBA发货跟踪和物流商选择。
 * </p>
 *
 * @author ERP系统
 */
@FeignClient(name = "erp-app", contextId = "fba-tms-client", path = "/tms/api/v1")
public interface TmsClient {

    @GetMapping("/shipments/{shipmentId}")
    Result<ShipmentResponse> getShipment(@PathVariable String shipmentId);

    @GetMapping("/shipping-methods")
    Result<java.util.List<ShippingMethodResponse>> listShippingMethods(@RequestParam String tenantId);

    record ShipmentResponse(String shipmentId,
                            String tenantId,
                            String orderId,
                            String carrierId,
                            String shippingMethodId,
                            String trackingNo,
                            String destinationCountry,
                            String status) {}

    record ShippingMethodResponse(String methodId,
                                  String carrierId,
                                  String methodCode,
                                  String methodName,
                                  String transportMode,
                                  String rateType,
                                  boolean enabled) {}
}
