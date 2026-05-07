package com.aidotnet.erp.fba.client;

import com.aidotnet.erp.common.api.Result;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * OMS客户端 - FBA域调用OMS域
 * <p>
 * 描述: FBA域通过此客户端调用OMS域的内部API，获取订单信息。
 *       用于FBA发货计划关联订单数据。
 * </p>
 *
 * @author ERP系统
 */
@FeignClient(name = "erp-app", contextId = "fba-oms-client", path = "/oms/api/in/v1")
public interface OmsClient {

    @GetMapping("/orders/{orderId}")
    Result<OrderResponse> getOrder(@PathVariable String orderId);

    @GetMapping("/orders")
    Result<List<OrderResponse>> listOrders(@RequestParam String tenantId, @RequestParam(required = false) String status);

    record OrderResponse(String orderId, String tenantId, String customerId, String status, String channel, String sellerSku, int quantity) {}
}
