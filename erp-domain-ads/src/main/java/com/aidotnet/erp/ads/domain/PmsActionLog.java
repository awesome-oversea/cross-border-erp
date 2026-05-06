package com.aidotnet.erp.ads.domain;

import java.time.Instant;

public record PmsActionLog(
        String logId,
        String tenantId,
        String campaignId,
        String bidId,
        ActionType actionType,
        String beforeValue,
        String afterValue,
        String pmsReason,
        boolean canRollback,
        boolean rolledBack,
        Instant executedAt,
        Instant rolledBackAt
) {
    public enum ActionType {
        BID_ADJUST,
        KEYWORD_SUGGEST,
        PMS_OPTIMIZATION_TOGGLE
    }
}
