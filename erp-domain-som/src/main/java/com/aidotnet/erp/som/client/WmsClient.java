package com.aidotnet.erp.som.client;

import com.aidotnet.erp.common.api.Result;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * WMS客户端 - SOM域调用WMS域
 * <p>
 * 描述: SOM域通过此客户端调用WMS域的内部API，获取库存信息。
 *       用于Listing库存同步和可用性检查。
 * </p>
 *
 * @author ERP系统
 */
@FeignClient(name = "erp-app", contextId = "som-wms-client", path = "/wms/api/in/v1")
public interface WmsClient {

    @GetMapping("/inventory/{sellerSku}/availability")
    Result<InventoryAvailabilityResponse> checkAvailability(@PathVariable String sellerSku);

    @GetMapping("/warehouses")
    Result<List<WarehouseResponse>> listWarehouses(@RequestParam String tenantId);

    record InventoryAvailabilityResponse(String sellerSku, int onHand, int reserved, int available) {}

    record WarehouseResponse(String warehouseId, String tenantId, String code, String name, String countryCode) {}
}
