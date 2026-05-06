package com.aidotnet.erp.common.event;

import java.time.Instant;
import java.util.Map;

public record StandardDomainEvent(
        String eventId,
        String tenantId,
        String traceId,
        String eventType,
        String aggregateId,
        Instant occurredAt,
        Map<String, Object> payload) implements DomainEvent {

    public StandardDomainEvent {
        payload = payload == null ? Map.of() : Map.copyOf(payload);
    }
}
