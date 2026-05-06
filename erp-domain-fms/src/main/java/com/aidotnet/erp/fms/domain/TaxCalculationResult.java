package com.aidotnet.erp.fms.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record TaxCalculationResult(String calculationId, String tenantId, String countryCode, String regionCode,
                                   String sellerSku, String orderId, BigDecimal taxableAmount,
                                   BigDecimal taxRate, BigDecimal taxAmount, String taxType,
                                   String serviceProvider, Instant calculatedAt) {}
