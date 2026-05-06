package com.aidotnet.erp.common.event;

import java.time.Instant;

public interface DomainEvent {

    String eventId();

    String tenantId();

    String traceId();

    String eventType();

    String aggregateId();

    Instant occurredAt();
}
