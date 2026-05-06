package com.aidotnet.erp.sys.domain;

import java.time.Instant;

public record PmsFeedback(
        String feedbackId,
        String tenantId,
        String erpReferenceId,
        String recommendationId,
        String domain,
        String feedbackType,
        String executionStatus,
        String businessResult,
        String businessMetricsJson,
        String failureReason,
        String operatorId,
        String traceId,
        boolean delivered,
        int retryCount,
        Instant deliveredAt,
        Instant createdAt) {}
