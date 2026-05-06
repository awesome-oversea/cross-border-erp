package com.aidotnet.erp.sys.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PmsDraftDocument(
        String draftId,
        String tenantId,
        String erpReferenceId,
        String domain,
        String draftType,
        String targetBusinessType,
        String targetBusinessId,
        String contentJson,
        DataTrustLevel trustLevel,
        String sourceSystem,
        String actorId,
        String actorType,
        String agentId,
        String scope,
        String purpose,
        String traceId,
        String approvalStatus,
        String approvedBy,
        Instant approvedAt,
        String executionStatus,
        String executionResult,
        Instant createdAt,
        Instant updatedAt) {}
