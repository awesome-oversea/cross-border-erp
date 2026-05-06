package com.aidotnet.erp.bi.domain;

import java.time.Instant;
import java.util.Map;

public record DashboardWidget(
        String widgetId,
        String tenantId,
        String widgetCode,
        String widgetName,
        String widgetType,
        Map<String, Object> config,
        int sortOrder,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {}
