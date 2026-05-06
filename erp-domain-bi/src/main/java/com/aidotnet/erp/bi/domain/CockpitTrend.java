package com.aidotnet.erp.bi.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CockpitTrend(
        String trendId,
        String tenantId,
        String metricCode,
        String metricName,
        String period,
        List<DataPoint> dataPoints,
        Instant generatedAt
) {
    public record DataPoint(Instant timestamp, BigDecimal value, BigDecimal targetValue) {}
}
