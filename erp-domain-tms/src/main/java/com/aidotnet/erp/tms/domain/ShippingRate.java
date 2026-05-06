package com.aidotnet.erp.tms.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 运费费率领域模型
 * <p>
 * 描述: 物流渠道的运费费率，按起始地、目的地和重量区间定价。
 * </p>
 *
 * @author ERP系统
 */
public record ShippingRate(
        String rateId,
        String tenantId,
        String methodId,
        String origin,
        String destination,
        BigDecimal weightMin,
        BigDecimal weightMax,
        BigDecimal rate,
        String currency,
        Instant createdAt,
        Instant updatedAt
) {}
