package com.aidotnet.erp.common.event;

import java.time.Instant;

public record OutboxMessage(
        String messageId,
        String aggregateType,
        String aggregateId,
        String eventType,
        String payload,
        String tenantId,
        Instant createdAt,
        boolean published
) {
    public OutboxMessage {
        if (messageId == null) {
            messageId = java.util.UUID.randomUUID().toString();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public OutboxMessage markPublished() {
        return new OutboxMessage(messageId, aggregateType, aggregateId, eventType, payload, tenantId, createdAt, true);
    }
}
