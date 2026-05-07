package com.aidotnet.erp.scm.client;

import com.aidotnet.erp.common.api.Result;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * OMS客户端 - SCM域调用OMS域
 * <p>
 * 描述: SCM域通过此客户端调用OMS域的内部API，获取订单信息。
 *       用于采购计划关联销售订单需求。
 * </p>
 *
 * @author ERP系统
 */
@FeignClient(name = "erp-app", contextId = "scm-oms-client", path = "/oms/api/in/v1")
public interface OmsClient {

    @GetMapping("/orders/procurement-demand")
    Result<List<ProcurementDemandResponse>> listProcurementDemand();

    @GetMapping("/orders")
    Result<List<OrderResponse>> listOrders();

    @GetMapping("/orders/{orderId}")
    Result<OrderResponse> getOrder(@PathVariable String orderId);

    record ProcurementDemandResponse(String sellerSku, int orderDemandQuantity, List<String> orderIds) {}

    record OrderResponse(String orderId, String tenantId, String customerId, String status, String channel, String sellerSku, int quantity) {}
}
