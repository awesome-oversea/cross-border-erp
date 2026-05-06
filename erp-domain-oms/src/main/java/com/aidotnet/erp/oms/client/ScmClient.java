package com.aidotnet.erp.oms.client;

import com.aidotnet.erp.common.api.Result;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "erp-app", contextId = "scm-client", path = "/scm/api/in/v1")
public interface ScmClient {

    @GetMapping("/suppliers/{supplierId}")
    Result<Map<String, Object>> getSupplier(@PathVariable String supplierId);
}
