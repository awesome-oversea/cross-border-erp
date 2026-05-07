package com.aidotnet.erp.iam.client;

import com.aidotnet.erp.common.api.Result;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * OMS客户端 - IAM域调用OMS域
 * <p>
 * 描述: IAM域通过此客户端调用OMS域的内部API，获取订单相关信息。
 *       用于用户数据范围校验和审计日志关联。
 * </p>
 *
 * @author ERP系统
 */
@FeignClient(name = "erp-app", contextId = "iam-oms-client", path = "/oms/api/in/v1")
public interface OmsClient {

    @GetMapping("/orders/{orderId}")
    Result<OrderResponse> getOrder(@PathVariable String orderId);

    @GetMapping("/orders")
    Result<List<OrderResponse>> listOrders(@RequestParam String tenantId);

    record OrderResponse(String orderId, String tenantId, String customerId, String status, String channel) {}
}
