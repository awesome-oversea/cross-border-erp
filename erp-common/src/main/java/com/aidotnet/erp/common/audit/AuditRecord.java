package com.aidotnet.erp.common.audit;

import java.time.Instant;

public record AuditRecord(
        String auditId,
        String tenantId,
        String actor,
        String action,
        String module,
        String target,
        String traceId,
        boolean success,
        Instant occurredAt
) {
}
