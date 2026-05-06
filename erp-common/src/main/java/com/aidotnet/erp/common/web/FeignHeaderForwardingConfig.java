package com.aidotnet.erp.common.web;

import com.aidotnet.erp.common.context.ActorContext;
import com.aidotnet.erp.common.context.TraceContext;
import com.aidotnet.erp.common.tenant.TenantContext;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignHeaderForwardingConfig {

    private static final String TENANT_HEADER = "X-Tenant-Id";
    private static final String TRACE_HEADER = "X-Trace-Id";
    private static final String ACTOR_HEADER = "X-Actor-Id";

    @Bean
    public RequestInterceptor tenantTraceActorRequestInterceptor() {
        return template -> {
            String tenantId = TenantContext.getTenantId();
            if (tenantId != null && !tenantId.isBlank()) {
                template.header(TENANT_HEADER, tenantId);
            }
            String traceId = TraceContext.getTraceId();
            if (traceId != null && !traceId.isBlank()) {
                template.header(TRACE_HEADER, traceId);
            }
            String actorId = ActorContext.getActorId();
            if (actorId != null && !actorId.isBlank()) {
                template.header(ACTOR_HEADER, actorId);
            }
        };
    }
}
