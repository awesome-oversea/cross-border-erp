package com.aidotnet.erp.sys.client;

import com.aidotnet.erp.common.api.Result;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * WMS域Feign客户端，用于系统设置域与仓储域的跨域通信。
 * <p>
 * 描述: SYS域通过此客户端查询WMS域的库存和入库数据，
 *       用于库存预警关联、物流规则计算和PMS数据源。
 * </p>
 * <p>
 * 跨域关联:
 *   - SYS → WMS: 查询库存详情(用于预警关联和物流计费)
 *   - SYS → WMS: 查询仓库信息(用于连接器同步配置)
 * </p>
 *
 * @author ERP系统
 */
@FeignClient(name = "erp-app", contextId = "wms-client-sys", path = "/wms/api/in/v1")
public interface WmsClient {

    /**
     * 按SKU ID查询库存详情。
     *
     * @param tenantId 租户ID
     * @param skuId    SKU ID
     * @return 库存信息
     */
    @GetMapping("/inventory/{skuId}")
    Result<Map<String, Object>> getInventory(@RequestHeader("X-Tenant-Id") String tenantId,
                                             @PathVariable String skuId);

    /**
     * 查询仓库列表。
     *
     * @param tenantId 租户ID
     * @param warehouseCode 仓库编码(可选)
     * @return 仓库信息列表
     */
    @GetMapping("/warehouses")
    Result<Map<String, Object>> listWarehouses(@RequestHeader("X-Tenant-Id") String tenantId,
                                               @RequestParam(required = false) String warehouseCode);
}
