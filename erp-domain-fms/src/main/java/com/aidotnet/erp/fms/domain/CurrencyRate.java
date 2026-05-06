package com.aidotnet.erp.fms.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record CurrencyRate(
        String rateId,
        String tenantId,
        String fromCurrency,
        String toCurrency,
        BigDecimal rate,
        String source,
        LocalDate rateDate,
        Instant syncedAt
) {}
