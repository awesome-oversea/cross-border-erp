package com.aidotnet.erp.fms.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record CostAggregationRule(
        String ruleId,
        String tenantId,
        String ruleName,
        String costSource,
        String costCategory,
        String allocationMethod,
        String allocationBasis,
        String targetDimension,
        boolean enabled,
        int priority,
        Instant createdAt,
        Instant updatedAt
) {
    public enum AllocationMethod {
        DIRECT,
        PROPORTIONAL,
        WEIGHTED,
        EQUAL,
        CUSTOM
    }

    public enum CostSource {
        PURCHASE,
        SHIPPING_OUTBOUND,
        FBA_FEE,
        PLATFORM_COMMISSION,
        ADVERTISING,
        RETURN_REFUND,
        STORAGE,
        PACKAGING,
        CUSTOM_DUTY,
        OTHER
    }

    public enum TargetDimension {
        ORDER,
        SKU,
        STORE,
        MARKETPLACE,
        SHIPMENT
    }
}
