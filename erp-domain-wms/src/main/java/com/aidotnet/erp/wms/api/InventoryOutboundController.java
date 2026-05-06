package com.aidotnet.erp.wms.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.wms.application.InventoryService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * WMS域对外接口控制器
 * <p>
 * 描述: WMS域对外(Outbound)REST API，供外部系统查询库存可用性和同步库存。
 *       路径前缀: /wms/api/out/v1 (外部接口)
 * </p>
 * <p>
 * 接口列表:
 *   - POST /inventory/sync              - 同步库存到平台
 *   - GET  /inventory/{sellerSku}/availability - 查询SKU库存可用性
 *   - POST /inventory/adjustment-notify  - 通知库存调整
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/wms/api/out/v1")
public class InventoryOutboundController {

    private final InventoryService inventoryService;

    /**
     * 构造函数 - 依赖注入库存服务
     *
     * @param inventoryService 库存管理应用服务
     */
    public InventoryOutboundController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/inventory/sync")
    public Result<Map<String, Object>> syncInventoryToPlatform(@RequestBody Map<String, Object> request) {
        return Result.ok(Map.of("status", "SYNC_REQUESTED", "warehouseId", request.getOrDefault("warehouseId", "")));
    }

    @GetMapping("/inventory/{sellerSku}/availability")
    public Result<Map<String, Object>> checkInventoryAvailability(@PathVariable String sellerSku) {
        InventoryService.InventoryAvailability availability = inventoryService.getAvailability(currentTenant(), sellerSku);
        return Result.ok(Map.of(
                "sellerSku", availability.sellerSku(),
                "onHand", availability.onHand(),
                "reserved", availability.reserved(),
                "available", availability.available()));
    }

    @PostMapping("/inventory/adjustment-notify")
    public Result<Map<String, Object>> notifyInventoryAdjustment(@RequestBody Map<String, Object> request) {
        return Result.ok(Map.of("status", "NOTIFIED", "adjustmentId", request.getOrDefault("adjustmentId", "")));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }
}
