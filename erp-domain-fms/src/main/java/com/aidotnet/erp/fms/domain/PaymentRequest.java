package com.aidotnet.erp.fms.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentRequest(
        String requestId,
        String tenantId,
        String poId,
        String supplierId,
        BigDecimal amount,
        String currency,
        String requestType,
        String status,
        String requestedBy,
        String approvalFlow,
        String paidBy,
        Instant paidAt,
        String writeoffStatus,
        BigDecimal writeoffAmount,
        Instant createdAt,
        Instant updatedAt
) {
}
