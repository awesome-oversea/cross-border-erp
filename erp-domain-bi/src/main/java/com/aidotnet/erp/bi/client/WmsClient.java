package com.aidotnet.erp.bi.client;

import com.aidotnet.erp.common.api.Result;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * WMS客户端 - BI域调用WMS域
 * <p>
 * 描述: BI域通过此客户端调用WMS域的内部API，获取库存数据用于库存分析报表。
 * </p>
 *
 * @author ERP系统
 */
@FeignClient(name = "erp-app", contextId = "bi-wms-client", path = "/wms/api/in/v1")
public interface WmsClient {

    @GetMapping("/warehouses")
    Result<List<WarehouseResponse>> listWarehouses(@RequestParam String tenantId);

    @GetMapping("/inventory/{sellerSku}/availability")
    Result<InventoryAvailabilityResponse> checkAvailability(@PathVariable String sellerSku);

    record WarehouseResponse(String warehouseId, String tenantId, String code, String name, String countryCode) {}

    record InventoryAvailabilityResponse(String sellerSku, int onHand, int reserved, int available) {}
}
