package com.aidotnet.erp.sys.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PmsRecommendation(
        String erpReferenceId,
        String recommendationId,
        String tenantId,
        String domain,
        String recommendationType,
        PmsWriteObjectType objectType,
        String targetObjectType,
        String targetObjectId,
        String content,
        BigDecimal score,
        BigDecimal confidence,
        String evidenceChainId,
        List<String> dataSources,
        List<String> riskFlags,
        String explainability,
        String requestedAction,
        String approvalPolicy,
        PmsRecommendationStatus status,
        String rejectionReason,
        String executionResult,
        String measuredResult,
        String traceId,
        String idempotencyKey,
        String actorId,
        String actorType,
        String agentId,
        String scope,
        String purpose,
        String sourceSystem,
        String auditId,
        Instant createdAt,
        Instant updatedAt) {}
