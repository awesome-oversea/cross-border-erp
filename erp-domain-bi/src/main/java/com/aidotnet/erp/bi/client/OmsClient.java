package com.aidotnet.erp.bi.client;

import com.aidotnet.erp.common.api.Result;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * OMS客户端 - BI域调用OMS域
 * <p>
 * 描述: BI域通过此客户端调用OMS域的内部API，获取订单数据用于报表分析。
 * </p>
 *
 * @author ERP系统
 */
@FeignClient(name = "erp-app", contextId = "bi-oms-client", path = "/oms/api/in/v1")
public interface OmsClient {

    @GetMapping("/orders")
    Result<List<OrderResponse>> listOrders(@RequestParam String tenantId, @RequestParam(required = false) String status);

    @GetMapping("/orders/{orderId}")
    Result<OrderResponse> getOrder(@PathVariable String orderId);

    record OrderResponse(String orderId, String tenantId, String customerId, String status, String channel, String sellerSku, int quantity, java.math.BigDecimal amount) {}
}
