package com.aidotnet.erp.fba.client;

import com.aidotnet.erp.common.api.Result;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "erp-app", contextId = "wms-client-fba", path = "/wms/api/in/v1")
public interface WmsClient {

    @GetMapping("/inventory/{sellerSku}/availability")
    Result<Map<String, Object>> checkAvailability(@PathVariable String sellerSku,
                                                  @RequestParam(required = false) String warehouseId);
}
