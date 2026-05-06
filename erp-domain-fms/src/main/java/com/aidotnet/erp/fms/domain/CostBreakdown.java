package com.aidotnet.erp.fms.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record CostBreakdown(String breakdownId, String tenantId, String costEventId, String costType,
                            String costCategory, BigDecimal amount, String currency, BigDecimal exchangeRate,
                            BigDecimal amountInBaseCurrency, String remark, Instant createdAt) {}
