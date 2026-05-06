package com.aidotnet.erp.crm.client;

import com.aidotnet.erp.common.api.Result;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * OMS域Feign客户端(CRM→OMS)
 * <p>
 * 描述: CRM域调用OMS域的远程接口，用于查询订单信息和发起退款请求。
 *       遵循内部域间调用规范，走OpenFeign不经过外部网关。
 * </p>
 * <p>
 * 调用场景:
 *   1. 售后退款 - 查询订单信息、发起退款请求
 *   2. 客户画像 - 查询买家订单历史
 * </p>
 *
 * @author ERP系统
 */
@FeignClient(name = "erp-app", contextId = "oms-client-crm", path = "/oms/api/in/v1")
public interface OmsClient {

    /** 查询订单详情 */
    @GetMapping("/orders/{orderId}")
    Result<Map<String, Object>> getOrder(@PathVariable String orderId);

    /** 按买家名称查询订单列表 */
    @GetMapping("/orders/buyer/{buyerName}")
    Result<Map<String, Object>> getOrdersByBuyer(@PathVariable String buyerName);

    /** 发起退款请求 */
    @PostMapping("/orders/{orderId}/refunds")
    Result<Map<String, Object>> requestRefund(@PathVariable String orderId, @RequestBody RefundRequest request);

    /** 退款请求参数 */
    record RefundRequest(String reason, BigDecimal refundAmount, String refundType) {}
}
