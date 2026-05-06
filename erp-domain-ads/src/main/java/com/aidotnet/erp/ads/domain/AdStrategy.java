package com.aidotnet.erp.ads.domain;

import java.time.Instant;

public record AdStrategy(
        String strategyId,
        String tenantId,
        String strategyCode,
        String strategyName,
        StrategyType strategyType,
        String targetCampaignId,
        String conditionsJson,
        String actionsJson,
        boolean pmsGenerated,
        String scheduleExpression,
        StrategyStatus status,
        Instant lastExecutedAt,
        Instant createdAt,
        Instant updatedAt
) {
    public enum StrategyType {
        BID_ADJUSTMENT,
        BUDGET_ADJUSTMENT,
        KEYWORD_HARVESTING,
        KEYWORD_SUGGESTION
    }

    public enum StrategyStatus {
        DRAFT,
        PENDING,
        ACTIVE,
        DISABLED,
        ARCHIVED
    }
}
