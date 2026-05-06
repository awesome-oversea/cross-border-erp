package com.aidotnet.erp.wms.client;

import com.aidotnet.erp.common.api.Result;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "erp-app", contextId = "pdm-client", path = "/pdm/api/in/v1")
public interface PdmClient {

    @GetMapping("/products/{productId}")
    Result<Map<String, Object>> getProduct(@PathVariable String productId);

    @GetMapping("/products/sku/{sellerSku}")
    Result<Map<String, Object>> getProductBySku(@PathVariable String sellerSku);
}
