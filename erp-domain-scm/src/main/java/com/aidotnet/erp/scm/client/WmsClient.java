package com.aidotnet.erp.scm.client;

import com.aidotnet.erp.common.api.Result;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "erp-app", contextId = "wms-client-scm", path = "/wms/api/in/v1")
public interface WmsClient {

    @GetMapping("/warehouses/{warehouseId}")
    Result<Map<String, Object>> getWarehouse(@PathVariable String warehouseId);

    @PostMapping("/inventory/receive")
    Result<Map<String, Object>> receive(@RequestBody StockRequest request);

    record StockRequest(String warehouseId, String sellerSku, int quantity) {}
}
