package com.aidotnet.erp.sys.domain;

import java.time.Instant;

public record AuditFlowStep(
        String stepId,
        String flowId,
        String tenantId,
        int stepOrder,
        String stepName,
        String approverRole,
        boolean autoApprove,
        Instant createdAt
) {}
