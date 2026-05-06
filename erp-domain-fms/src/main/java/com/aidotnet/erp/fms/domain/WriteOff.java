package com.aidotnet.erp.fms.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record WriteOff(
        String writeoffId,
        String tenantId,
        String type,
        String refType,
        String refId,
        BigDecimal amount,
        String currency,
        String status,
        String approvedBy,
        Instant createdAt,
        Instant updatedAt
) {
}
