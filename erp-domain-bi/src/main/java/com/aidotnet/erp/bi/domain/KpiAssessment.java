package com.aidotnet.erp.bi.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record KpiAssessment(
        String assessmentId,
        String tenantId,
        String targetId,
        String kpiCode,
        String kpiName,
        String department,
        String userId,
        String period,
        BigDecimal actualValue,
        BigDecimal targetValue,
        BigDecimal achievementRate,
        BigDecimal score,
        KpiStatus status,
        String assessorId,
        String comment,
        Instant assessedAt,
        Instant createdAt) {}
