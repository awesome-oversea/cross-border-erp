package com.aidotnet.erp.sys.domain;

import java.time.Instant;

public record AuditFlowConfig(
        String flowId,
        String tenantId,
        String flowCode,
        String flowName,
        String businessType,
        int requiredApprovals,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {}
