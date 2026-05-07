package com.aidotnet.erp.fms.client;

import com.aidotnet.erp.common.api.Result;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * OMS客户端 - FMS域调用OMS域
 * <p>
 * 描述: FMS域通过此客户端调用OMS域的内部API，获取订单信息。
 *       用于应收账款关联订单和结算对账。
 * </p>
 *
 * @author ERP系统
 */
@FeignClient(name = "erp-app", contextId = "fms-oms-client", path = "/oms/api/in/v1")
public interface OmsClient {

    @GetMapping("/orders/{orderId}")
    Result<OrderResponse> getOrder(@PathVariable String orderId);

    @GetMapping("/orders")
    Result<List<OrderResponse>> listOrders(@RequestParam String tenantId, @RequestParam(required = false) String status);

    record OrderResponse(String orderId, String tenantId, String customerId, String status, String channel, java.math.BigDecimal amount) {}
}
