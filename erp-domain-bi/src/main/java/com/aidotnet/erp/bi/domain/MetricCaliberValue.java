package com.aidotnet.erp.bi.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record MetricCaliberValue(
        String valueId,
        String tenantId,
        String caliberId,
        String metricCode,
        BigDecimal calculatedValue,
        String dimensionKey,
        String dimensionValue,
        Instant calculatedAt) {}
