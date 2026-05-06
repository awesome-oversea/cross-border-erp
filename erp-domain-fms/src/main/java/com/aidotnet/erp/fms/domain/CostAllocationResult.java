package com.aidotnet.erp.fms.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record CostAllocationResult(
        String resultId,
        String tenantId,
        String ruleId,
        String costEventId,
        String targetDimension,
        String targetId,
        BigDecimal allocatedAmount,
        String currency,
        BigDecimal exchangeRate,
        BigDecimal amountInBaseCurrency,
        Map<String, String> dimensions,
        Instant allocatedAt
) {}
