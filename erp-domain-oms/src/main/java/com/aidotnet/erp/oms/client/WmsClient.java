package com.aidotnet.erp.oms.client;

import com.aidotnet.erp.common.api.Result;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "erp-app", contextId = "wms-client", path = "/wms/api/in/v1")
public interface WmsClient {

    @GetMapping("/warehouses")
    Result<List<WarehouseResponse>> listWarehouses();

    @GetMapping("/warehouses/{warehouseId}/inventory")
    Result<List<InventoryBalanceResponse>> listBalances(@PathVariable String warehouseId);

    @GetMapping("/inventory/{sellerSku}/availability")
    Result<InventoryAvailabilityResponse> checkAvailability(@PathVariable String sellerSku);

    @PostMapping("/inventory/reserve")
    Result<InventoryBalanceResponse> reserve(@RequestBody StockRequest request);

    @PostMapping("/inventory/release")
    Result<InventoryBalanceResponse> release(@RequestBody StockRequest request);

    @PostMapping("/inventory/deduct")
    Result<InventoryBalanceResponse> deduct(@RequestBody StockRequest request);

    record WarehouseResponse(String warehouseId, String tenantId, String code, String name, String countryCode) {}

    record InventoryBalanceResponse(String tenantId, String warehouseId, String sellerSku, int onHand, int reserved) {
        public int available() {
            return onHand - reserved;
        }
    }

    record InventoryAvailabilityResponse(String sellerSku, int onHand, int reserved, int available) {}

    record StockRequest(String warehouseId, String sellerSku, int quantity,
                        String referenceType, String referenceId, String remark) {}
}
