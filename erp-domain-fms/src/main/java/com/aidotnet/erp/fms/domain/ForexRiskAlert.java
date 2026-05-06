package com.aidotnet.erp.fms.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ForexRiskAlert(
        String fromCurrency,
        String toCurrency,
        BigDecimal latestRate,
        BigDecimal previousRate,
        BigDecimal changeRatio,
        LocalDate latestEffectiveDate,
        LocalDate previousEffectiveDate,
        String level
) {
}
