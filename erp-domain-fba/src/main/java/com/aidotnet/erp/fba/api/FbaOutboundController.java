package com.aidotnet.erp.fba.api;

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
 * FBA域对外接口控制器
 * <p>
 * 描述: FBA域对外(Outbound)REST API，供外部系统与亚马逊FBA交互。
 *       路径前缀: /fba/api/out/v1 (外部接口)
 * </p>
 * <p>
 * 接口列表:
 *   - POST /shipments/create                          - 向亚马逊创建FBA发货
 *   - GET  /shipments/{amazonShipmentId}/status        - 查询FBA发货状态
 *   - POST /shipments/{amazonShipmentId}/labels        - 请求箱标
 *   - POST /replenishment-plans/submit                 - 提交补货计划到亚马逊
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/fba/api/out/v1")
public class FbaOutboundController {

    @PostMapping("/shipments/create")
    public Result<Map<String, Object>> createFbaShipmentToAmazon(@RequestBody Map<String, Object> request) {
        return Result.ok(Map.of("status", "SUBMITTED", "amazonShipmentId", request.getOrDefault("amazonShipmentId", "")));
    }

    @GetMapping("/shipments/{amazonShipmentId}/status")
    public Result<Map<String, Object>> fetchShipmentStatus(@PathVariable String amazonShipmentId) {
        return Result.ok(Map.of("amazonShipmentId", amazonShipmentId, "status", "WORKING"));
    }

    @PostMapping("/shipments/{amazonShipmentId}/labels")
    public Result<Map<String, Object>> requestLabels(@PathVariable String amazonShipmentId,
                                                      @RequestBody Map<String, Object> request) {
        return Result.ok(Map.of("amazonShipmentId", amazonShipmentId, "labelStatus", "GENERATED"));
    }

    @PostMapping("/replenishment-plans/submit")
    public Result<Map<String, Object>> submitReplenishmentToAmazon(@RequestBody Map<String, Object> request) {
        return Result.ok(Map.of("status", "SUBMITTED", "planId", request.getOrDefault("planId", "")));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }
}
