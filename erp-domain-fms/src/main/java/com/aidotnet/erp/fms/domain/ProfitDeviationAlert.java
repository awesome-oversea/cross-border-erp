package com.aidotnet.erp.fms.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record ProfitDeviationAlert(
        String alertId,
        String tenantId,
        String dimensionType,
        String dimensionId,
        String sellerSku,
        BigDecimal expectedMargin,
        BigDecimal actualMargin,
        BigDecimal deviation,
        BigDecimal deviationThreshold,
        String severity,
        String status,
        Instant detectedAt,
        Instant resolvedAt
) {
    public enum Severity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public enum AlertStatus {
        OPEN,
        ACKNOWLEDGED,
        RESOLVED,
        IGNORED
    }
}
