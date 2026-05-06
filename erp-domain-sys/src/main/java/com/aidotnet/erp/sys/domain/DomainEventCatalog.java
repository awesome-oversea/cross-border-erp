package com.aidotnet.erp.sys.domain;

import java.time.Instant;
import java.util.List;

public record DomainEventCatalog(
        String eventId,
        String tenantId,
        String eventCode,
        String eventName,
        String domain,
        String aggregateType,
        String eventType,
        String description,
        String payloadSchema,
        List<String> subscribers,
        String version,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {}
