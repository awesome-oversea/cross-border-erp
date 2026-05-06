package com.aidotnet.erp.sys.domain;

import java.time.Instant;
import java.util.List;

public record ApprovalFlowDefinition(
        String flowId,
        String tenantId,
        String flowCode,
        String flowName,
        String businessType,
        String description,
        List<ApprovalStep> steps,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {}
