package com.aidotnet.erp.fms.client;

import com.aidotnet.erp.common.api.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * TMS客户端 - FMS域调用TMS域
 * <p>
 * 描述: FMS域通过此客户端调用TMS域的内部API，获取物流费用信息。
 *       用于物流费用对账和成本核算。
 * </p>
 *
 * @author ERP系统
 */
@FeignClient(name = "erp-app", contextId = "fms-tms-client", path = "/tms/api/v1")
public interface TmsClient {

    @GetMapping("/shipments/{shipmentId}")
    Result<ShipmentResponse> getShipment(@PathVariable String shipmentId);

    @GetMapping("/shipping-rates")
    Result<java.util.List<ShippingRateResponse>> listShippingRates(@RequestParam String tenantId);

    record ShipmentResponse(String shipmentId,
                            String tenantId,
                            String orderId,
                            String carrierId,
                            String shippingMethodId,
                            String trackingNo,
                            String destinationCountry,
                            String status,
                            java.math.BigDecimal estimatedFreight,
                            String estimatedFreightCurrency) {}

    record ShippingRateResponse(String rateId,
                                String methodId,
                                String originCountry,
                                String destinationCountry,
                                java.math.BigDecimal baseCost,
                                java.math.BigDecimal costPerKg,
                                String currency) {}
}
