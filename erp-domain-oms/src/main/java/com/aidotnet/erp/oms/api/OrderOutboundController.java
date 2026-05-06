package com.aidotnet.erp.oms.api;

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
 * OMS域对外接口控制器
 * <p>
 * 描述: OMS域对外(Outbound)REST API，供外部平台或第三方系统调用。
 *       路径前缀: /oms/api/out/v1 (外部接口)
 * </p>
 * <p>
 * 接口列表:
 *   - POST /platform-orders/sync                      - 触发平台订单同步
 *   - GET  /platform-orders/{platformOrderNo}/status   - 查询平台订单状态
 *   - POST /platform-orders/{platformOrderNo}/acknowledge - 确认平台订单
 *   - POST /platform-orders/{platformOrderNo}/cancel   - 取消平台订单
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/oms/api/out/v1")
public class OrderOutboundController {

    /** 触发平台订单同步 */
    @PostMapping("/platform-orders/sync")
    public Result<Map<String, Object>> syncPlatformOrders(@RequestBody Map<String, Object> request) {
        return Result.ok(Map.of("status", "SYNC_REQUESTED", "platform", request.getOrDefault("platform", "unknown")));
    }

    @GetMapping("/platform-orders/{platformOrderNo}/status")
    public Result<Map<String, Object>> fetchPlatformOrderStatus(@PathVariable String platformOrderNo) {
        return Result.ok(Map.of("platformOrderNo", platformOrderNo, "status", "UNSHIPPED"));
    }

    @PostMapping("/platform-orders/{platformOrderNo}/acknowledge")
    public Result<Map<String, Object>> acknowledgeOrder(@PathVariable String platformOrderNo) {
        return Result.ok(Map.of("platformOrderNo", platformOrderNo, "acknowledged", true));
    }

    @PostMapping("/platform-orders/{platformOrderNo}/cancel")
    public Result<Map<String, Object>> cancelPlatformOrder(@PathVariable String platformOrderNo,
                                                            @RequestBody Map<String, Object> request) {
        return Result.ok(Map.of("platformOrderNo", platformOrderNo, "cancelled", true, "reason", request.getOrDefault("reason", "")));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }
}
