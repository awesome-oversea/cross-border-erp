package com.aidotnet.erp.crm.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * CRM外部方向控制器
 * <p>
 * 描述: CRM域对外接口，提供与电商平台交互的能力，包括：
 *   1. 向平台发起退款请求
 *   2. 从平台拉取评价数据
 *   3. 向客户发送消息
 * </p>
 * <p>
 * 路径规范: /crm/api/out/v1 — 外部方向(out)，v1版本
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/crm/api/out/v1")
public class CrmOutboundController {

    /** 向电商平台发起退款请求 */
    @PostMapping("/returns/refund")
    public Result<Map<String, Object>> processRefundToPlatform(@RequestBody Map<String, Object> request) {
        return Result.ok(Map.of("status", "REFUND_REQUESTED", "returnId", request.getOrDefault("returnId", "")));
    }

    /** 从电商平台拉取评价数据 */
    @GetMapping("/reviews/{sellerSku}/fetch")
    public Result<Map<String, Object>> fetchReviewsFromPlatform(@PathVariable String sellerSku) {
        return Result.ok(Map.of("sellerSku", sellerSku, "reviews", java.util.List.of()));
    }

    /** 向客户发送消息 */
    @PostMapping("/messages/send")
    public Result<Map<String, Object>> sendMessageToCustomer(@RequestBody Map<String, Object> request) {
        return Result.ok(Map.of("status", "SENT", "customerId", request.getOrDefault("customerId", "")));
    }

    /** 获取当前租户ID，为空则抛出业务异常 */
    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }
}
