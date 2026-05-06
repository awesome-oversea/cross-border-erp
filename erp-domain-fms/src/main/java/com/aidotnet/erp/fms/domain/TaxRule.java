package com.aidotnet.erp.fms.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record TaxRule(String ruleId, String tenantId, String countryCode, String taxType, BigDecimal taxRate,
                      String taxCategory, boolean enabled, Instant effectiveFrom, Instant effectiveTo,
                      Instant createdAt, Instant updatedAt) {}
