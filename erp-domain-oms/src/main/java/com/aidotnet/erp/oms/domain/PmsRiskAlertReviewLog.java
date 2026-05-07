package com.aidotnet.erp.oms.domain;

import java.time.Instant;

public record PmsRiskAlertReviewLog(
        String logId,
        String tenantId,
        String alertId,
        String orderId,
        String action,
        String reviewerNote,
        String riskLevel,
        Instant createdAt
) {}
