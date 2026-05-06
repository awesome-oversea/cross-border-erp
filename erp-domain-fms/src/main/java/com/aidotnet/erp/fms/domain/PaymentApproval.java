package com.aidotnet.erp.fms.domain;

import java.time.Instant;

public record PaymentApproval(
        String approvalId,
        String tenantId,
        String requestId,
        String approverId,
        int approvalLevel,
        String status,
        String comment,
        Instant approvedAt,
        Instant createdAt
) {
}
