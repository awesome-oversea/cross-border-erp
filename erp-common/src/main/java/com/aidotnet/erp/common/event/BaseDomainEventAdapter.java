package com.aidotnet.erp.common.event;

import com.aidotnet.erp.common.context.TraceContext;
import java.time.Instant;
import java.util.UUID;

public abstract class BaseDomainEventAdapter implements DomainEvent {

    private final String eventId;
    private final String aggregateId;
    private final String eventType;
    private final Instant occurredAt;
    private final String tenantId;
    private final String traceId;

    protected BaseDomainEventAdapter(String aggregateId, String eventType, String tenantId) {
        this.eventId = UUID.randomUUID().toString();
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.occurredAt = Instant.now();
        this.tenantId = tenantId;
        this.traceId = TraceContext.getTraceId() != null ? TraceContext.getTraceId() : UUID.randomUUID().toString();
    }

    @Override public String eventId() { return eventId; }
    @Override public String aggregateId() { return aggregateId; }
    @Override public String eventType() { return eventType; }
    @Override public Instant occurredAt() { return occurredAt; }
    @Override public String tenantId() { return tenantId; }
    @Override public String traceId() { return traceId; }
}
