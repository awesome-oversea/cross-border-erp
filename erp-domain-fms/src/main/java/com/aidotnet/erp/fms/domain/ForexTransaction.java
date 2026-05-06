package com.aidotnet.erp.fms.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record ForexTransaction(
        String forexId,
        String tenantId,
        String fromCurrency,
        String toCurrency,
        BigDecimal amount,
        BigDecimal rate,
        BigDecimal fee,
        BigDecimal convertedAmount,
        String refType,
        String refId,
        Instant createdAt
) {
}
