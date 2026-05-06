package com.aidotnet.erp.bi.domain;

import java.time.Instant;

public record MetricDefinition(
        String metricId,
        String tenantId,
        String metricCode,
        String metricName,
        String category,
        String formula,
        String unit,
        String permissionCode,
        String dataLevel,
        String description,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {}
