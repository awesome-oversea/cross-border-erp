package com.aidotnet.erp.common.audit;

import java.time.Instant;
import java.util.UUID;

public record AuditLog(
        String logId,
        String tenantId,
        String userId,
        String username,
        String action,
        String resource,
        String description,
        String traceId,
        Instant occurredAt
) {
    public static AuditLog of(String tenantId, String userId, String username,
                               String action, String resource, String description, String traceId) {
        return new AuditLog(
                UUID.randomUUID().toString(),
                tenantId,
                userId,
                username,
                action,
                resource,
                description,
                traceId,
                Instant.now()
        );
    }
}
