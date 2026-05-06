package com.aidotnet.erp.common.event;

import com.aidotnet.erp.common.context.TraceContext;
import com.aidotnet.erp.common.tenant.TenantContext;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public final class DomainEvents {

    private DomainEvents() {}

    public static StandardDomainEvent of(String eventType, String aggregateId) {
        return of(eventType, aggregateId, Map.of());
    }

    public static StandardDomainEvent of(String eventType, String aggregateId, Map<String, Object> payload) {
        return new StandardDomainEvent(
                UUID.randomUUID().toString(),
                TenantContext.getTenantId(),
                TraceContext.getTraceId(),
                eventType,
                aggregateId,
                Instant.now(),
                payload);
    }
}
