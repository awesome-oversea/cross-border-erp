package com.aidotnet.erp.common.audit;

import com.aidotnet.erp.common.context.TraceContext;
import com.aidotnet.erp.common.security.SecurityContext;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuditAspect {

    private final AuditLogStore auditLogStore;

    public AuditAspect(AuditLogStore auditLogStore) {
        this.auditLogStore = auditLogStore;
    }

    @Around("@annotation(audited)")
    public Object audit(ProceedingJoinPoint joinPoint, Audited audited) throws Throwable {
        String tenantId = SecurityContext.getTenantId();
        String userId = SecurityContext.getUserId();
        String username = SecurityContext.getUsername();
        String traceId = TraceContext.getTraceId();

        String action = audited.action().isEmpty()
                ? ((MethodSignature) joinPoint.getSignature()).getMethod().getName()
                : audited.action();
        String resource = audited.resource().isEmpty()
                ? resolveResourceFromRequest()
                : audited.resource();

        Object result;
        try {
            result = joinPoint.proceed();
            auditLogStore.save(AuditLog.of(tenantId, userId, username, action, resource,
                    audited.description(), traceId));
        } catch (Throwable ex) {
            auditLogStore.save(AuditLog.of(tenantId, userId, username, action + "_FAILED",
                    resource, audited.description() + " | Error: " + ex.getMessage(), traceId));
            throw ex;
        }
        return result;
    }

    private String resolveResourceFromRequest() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                return request.getMethod() + " " + request.getRequestURI();
            }
        } catch (Exception ignored) {
        }
        return "unknown";
    }
}
