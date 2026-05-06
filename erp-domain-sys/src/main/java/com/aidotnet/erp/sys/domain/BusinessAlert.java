package com.aidotnet.erp.sys.domain;

import java.time.Instant;

public record BusinessAlert(
        String alertId,
        String tenantId,
        String alertType,
        String alertCode,
        String severity,
        String domain,
        String title,
        String description,
        String sourceType,
        String sourceId,
        String status,
        String assignedTo,
        String resolution,
        Instant occurredAt,
        Instant resolvedAt,
        Instant createdAt,
        Instant updatedAt
) {}
