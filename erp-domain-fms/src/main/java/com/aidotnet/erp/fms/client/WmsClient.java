package com.aidotnet.erp.fms.client;

import com.aidotnet.erp.common.api.Result;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * WMS客户端 - FMS域调用WMS域
 * <p>
 * 描述: FMS域通过此客户端调用WMS域的内部API，获取仓库信息。
 *       用于仓库费用分摊和成本核算。
 * </p>
 *
 * @author ERP系统
 */
@FeignClient(name = "erp-app", contextId = "fms-wms-client", path = "/wms/api/in/v1")
public interface WmsClient {

    @GetMapping("/warehouses")
    Result<List<WarehouseResponse>> listWarehouses(@RequestParam String tenantId);

    record WarehouseResponse(String warehouseId, String tenantId, String code, String name, String countryCode) {}
}
