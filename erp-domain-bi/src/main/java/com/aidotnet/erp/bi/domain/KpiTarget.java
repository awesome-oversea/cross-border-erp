package com.aidotnet.erp.bi.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record KpiTarget(
        String targetId,
        String tenantId,
        String kpiCode,
        String kpiName,
        String department,
        String role,
        String period,
        BigDecimal targetValue,
        BigDecimal warningValue,
        BigDecimal excellentValue,
        String unit,
        String metricCode,
        String caliberId,
        List<String> applicableRoles,
        String scoringRule,
        BigDecimal weight,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt) {}
