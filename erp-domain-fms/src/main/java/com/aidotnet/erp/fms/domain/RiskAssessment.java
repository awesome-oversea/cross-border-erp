package com.aidotnet.erp.fms.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record RiskAssessment(
        String assessmentId,
        String tenantId,
        String targetType,
        String targetId,
        BigDecimal riskScore,
        String riskLevel,
        Map<String, Object> riskFactors,
        String recommendation,
        Instant assessedAt
) {
    public enum RiskLevel {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }

    public enum TargetType {
        ORDER,
        BUYER,
        TRANSACTION,
        SHIPMENT,
        RETURN
    }
}
