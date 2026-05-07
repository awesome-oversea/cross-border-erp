package com.aidotnet.erp.pdm.client;

import com.aidotnet.erp.common.api.Result;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * WMS客户端 - PDM域调用WMS域
 * <p>
 * 描述: PDM域通过此客户端调用WMS域的内部API，获取仓库信息。
 *       用于产品质量追溯和仓库关联。
 * </p>
 *
 * @author ERP系统
 */
@FeignClient(name = "erp-app", contextId = "pdm-wms-client", path = "/wms/api/in/v1")
public interface WmsClient {

    @GetMapping("/warehouses")
    Result<List<WarehouseResponse>> listWarehouses(@RequestParam String tenantId);

    @GetMapping("/warehouses/{warehouseId}")
    Result<WarehouseResponse> getWarehouse(@PathVariable String warehouseId);

    record WarehouseResponse(String warehouseId, String tenantId, String code, String name, String countryCode) {}
}
