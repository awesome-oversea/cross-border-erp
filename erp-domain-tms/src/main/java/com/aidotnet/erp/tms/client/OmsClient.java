package com.aidotnet.erp.tms.client;

import com.aidotnet.erp.common.api.Result;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "erp-app", contextId = "oms-client-tms", path = "/oms/api/in/v1")
public interface OmsClient {

    @GetMapping("/orders/{orderId}")
    Result<Map<String, Object>> getOrder(@PathVariable String orderId);
}
