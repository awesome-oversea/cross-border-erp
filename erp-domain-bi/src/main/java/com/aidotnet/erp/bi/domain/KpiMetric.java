package com.aidotnet.erp.bi.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record KpiMetric(
        String kpiId,
        String tenantId,
        String kpiCode,
        String kpiName,
        String category,
        BigDecimal value,
        BigDecimal targetValue,
        String unit,
        KpiStatus status,
        Instant measuredAt
) {}
