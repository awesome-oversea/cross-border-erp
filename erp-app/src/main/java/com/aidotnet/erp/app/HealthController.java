package com.aidotnet.erp.app;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.context.TraceContext;
import com.aidotnet.erp.common.tenant.TenantContext;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys/api/in/v1/health")
public class HealthController {

    @GetMapping
    public Result<Map<String, String>> health() {
        return Result.ok(Map.of(
                "status", "UP",
                "tenantId", valueOrEmpty(TenantContext.getTenantId()),
                "traceId", valueOrEmpty(TraceContext.getTraceId())));
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
