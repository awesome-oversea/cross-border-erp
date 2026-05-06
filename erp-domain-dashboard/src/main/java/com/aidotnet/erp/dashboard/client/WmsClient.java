package com.aidotnet.erp.dashboard.client;

import com.aidotnet.erp.common.api.Result;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * WMS域Feign客户端，用于工作台域与仓储域的跨域通信。
 * <p>
 * 描述: 工作台域通过此客户端查询WMS域的库存数据，
 *       用于仪表盘库存指标展示和AI洞察预警。
 * </p>
 * <p>
 * 跨域关联:
 *   - DASHBOARD → WMS: 查询SKU库存(用于库存预警洞察详情)
 *   - DASHBOARD → WMS: 查询库存统计(用于库存指标展示)
 *   - DASHBOARD → WMS: 查询库存预警列表(用于待办事项生成)
 * </p>
 *
 * @author ERP系统
 */
@FeignClient(name = "erp-app", contextId = "wms-client-dashboard", path = "/wms/api/in/v1")
public interface WmsClient {

    /**
     * 按SKU ID查询库存详情。
     *
     * @param tenantId 租户ID
     * @param skuId    SKU ID
     * @return 库存信息(包含可用库存、在途库存、安全库存等)
     */
    @GetMapping("/inventory/{skuId}")
    Result<Map<String, Object>> getInventory(@RequestHeader("X-Tenant-Id") String tenantId,
                                             @PathVariable String skuId);

    /**
     * 查询库存统计摘要。
     * <p>
     * 返回总库存金额、SKU数量、预警数量等统计数据，
     * 用于仪表盘库存指标展示。
     * </p>
     *
     * @param tenantId 租户ID
     * @return 库存统计(包含totalValue、skuCount、warningCount等)
     */
    @GetMapping("/inventory/statistics")
    Result<Map<String, Object>> getInventoryStatistics(@RequestHeader("X-Tenant-Id") String tenantId);

    /**
     * 查询库存预警列表。
     * <p>
     * 返回低于安全库存的SKU列表，用于工作台生成预警待办。
     * </p>
     *
     * @param tenantId 租户ID
     * @param limit    返回数量限制
     * @return 库存预警列表
     */
    @GetMapping("/inventory/warnings")
    Result<Map<String, Object>> getInventoryWarnings(@RequestHeader("X-Tenant-Id") String tenantId,
                                                     @RequestParam(defaultValue = "20") int limit);
}
