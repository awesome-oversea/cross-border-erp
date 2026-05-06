package com.aidotnet.erp.fms.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ForexRate(
        String rateId,
        String tenantId,
        String fromCurrency,
        String toCurrency,
        BigDecimal rate,
        LocalDate effectiveDate,
        String source,
        Instant createdAt
) {
}
