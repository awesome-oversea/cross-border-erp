package com.aidotnet.erp.bi.domain;

import java.time.Instant;
import java.util.Map;

public record DataVisualization(
        String vizId,
        String tenantId,
        String reportId,
        String vizName,
        String vizType,
        Map<String, Object> config,
        int sortOrder,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {}
