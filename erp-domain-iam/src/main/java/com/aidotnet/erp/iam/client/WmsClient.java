package com.aidotnet.erp.iam.client;

import com.aidotnet.erp.common.api.Result;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * WMS客户端 - IAM域调用WMS域
 * <p>
 * 描述: IAM域通过此客户端调用WMS域的内部API，获取仓库相关信息。
 *       用于用户数据范围校验和仓库权限验证。
 * </p>
 *
 * @author ERP系统
 */
@FeignClient(name = "erp-app", contextId = "iam-wms-client", path = "/wms/api/in/v1")
public interface WmsClient {

    @GetMapping("/warehouses")
    Result<List<WarehouseResponse>> listWarehouses(@RequestParam String tenantId);

    @GetMapping("/warehouses/{warehouseId}")
    Result<WarehouseResponse> getWarehouse(@PathVariable String warehouseId);

    record WarehouseResponse(String warehouseId, String tenantId, String code, String name, String countryCode) {}
}
