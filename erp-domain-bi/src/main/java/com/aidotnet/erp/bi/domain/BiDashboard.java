package com.aidotnet.erp.bi.domain;

import java.time.Instant;
import java.util.Map;

public record BiDashboard(
        String dashboardId,
        String tenantId,
        String dashboardName,
        String dashboardType,
        Map<String, Object> config,
        String owner,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {}
