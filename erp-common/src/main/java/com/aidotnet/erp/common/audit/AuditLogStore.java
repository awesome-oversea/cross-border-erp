package com.aidotnet.erp.common.audit;

import com.aidotnet.erp.common.context.TraceContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuditLogStore {

    private static final Logger log = LoggerFactory.getLogger(AuditLogStore.class);
    private final Map<String, AuditLog> store = new ConcurrentHashMap<>();

    public void save(AuditLog auditLog) {
        store.put(auditLog.logId(), auditLog);
        log.info("[AUDIT] tenant={} user={} action={} resource={} trace={}",
                auditLog.tenantId(), auditLog.username(), auditLog.action(),
                auditLog.resource(), auditLog.traceId());
    }
}
