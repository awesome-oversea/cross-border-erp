package com.aidotnet.erp.fms.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record BillingRule(
        String ruleId,
        String tenantId,
        String ruleName,
        String feeType,
        String platform,
        String category,
        BigDecimal rate,
        BigDecimal minAmount,
        BigDecimal maxAmount,
        String calculationMethod,
        boolean enabled,
        int priority,
        Instant createdAt,
        Instant updatedAt
) {
    public enum FeeType {
        PLATFORM_COMMISSION,
        WAREHOUSE_FEE,
        FREIGHT_FEE,
        PACKAGING_COST,
        FBA_HEAD_COST,
        SERVICE_FEE,
        OTHER
    }

    public enum CalculationMethod {
        PERCENTAGE,
        FIXED,
        TIERED,
        WEIGHT_BASED,
        VOLUME_BASED
    }
}
