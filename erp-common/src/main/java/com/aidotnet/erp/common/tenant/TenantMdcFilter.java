package com.aidotnet.erp.common.tenant;

import com.aidotnet.erp.common.context.ActorContext;
import com.aidotnet.erp.common.context.TraceContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class TenantMdcFilter extends OncePerRequestFilter {

    private static final String TENANT_MDC_KEY = "tenantId";
    private static final String TRACE_MDC_KEY = "traceId";
    private static final String ACTOR_MDC_KEY = "actor";
    private static final String TRACE_HEADER = "X-Trace-Id";
    private static final String ACTOR_HEADER = "X-Actor-Id";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String tenantId = request.getHeader(TenantInterceptor.TENANT_HEADER);
            String traceId = request.getHeader(TRACE_HEADER);
            String actorId = request.getHeader(ACTOR_HEADER);
            if (traceId == null || traceId.isBlank()) {
                traceId = UUID.randomUUID().toString();
            }
            TraceContext.setTraceId(traceId.trim());
            MDC.put(TRACE_MDC_KEY, traceId.trim());
            response.setHeader(TRACE_HEADER, traceId.trim());
            if (tenantId != null && !tenantId.isBlank()) {
                String normalizedTenantId = tenantId.trim();
                TenantContext.setTenantId(normalizedTenantId);
                MDC.put(TENANT_MDC_KEY, normalizedTenantId);
            }
            if (actorId != null && !actorId.isBlank()) {
                String normalizedActorId = actorId.trim();
                ActorContext.setActorId(normalizedActorId);
                MDC.put(ACTOR_MDC_KEY, normalizedActorId);
            }
            filterChain.doFilter(request, response);
        } finally {
            TraceContext.clear();
            TenantContext.clear();
            ActorContext.clear();
            MDC.remove(TRACE_MDC_KEY);
            MDC.remove(TENANT_MDC_KEY);
            MDC.remove(ACTOR_MDC_KEY);
        }
    }
}
