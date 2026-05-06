package com.aidotnet.erp.bi.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record KpiAlert(
        String alertId,
        String tenantId,
        String kpiCode,
        String kpiName,
        BigDecimal currentValue,
        BigDecimal targetValue,
        BigDecimal deviationRate,
        KpiStatus status,
        String severity,
        String message,
        Instant detectedAt
) {}
