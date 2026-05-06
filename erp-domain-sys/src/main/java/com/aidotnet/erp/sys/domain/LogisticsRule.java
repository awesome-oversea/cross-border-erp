package com.aidotnet.erp.sys.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record LogisticsRule(String ruleId, String tenantId, String ruleName, String countryCode, String channel,
                            BigDecimal weightMinKg, BigDecimal weightMaxKg, BigDecimal baseCost,
                            BigDecimal costPerKg, int estimatedDaysMin, int estimatedDaysMax,
                            boolean enabled, int priority, Instant createdAt, Instant updatedAt) {}
