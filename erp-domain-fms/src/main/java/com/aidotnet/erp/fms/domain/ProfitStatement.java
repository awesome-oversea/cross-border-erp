package com.aidotnet.erp.fms.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record ProfitStatement(
        String statementId,
        String tenantId,
        String sellerSku,
        String marketplaceId,
        String orderId,
        BigDecimal revenue,
        BigDecimal productCost,
        BigDecimal shippingCost,
        BigDecimal fbaFee,
        BigDecimal commission,
        BigDecimal advertisingCost,
        BigDecimal otherCost,
        BigDecimal totalCost,
        BigDecimal grossProfit,
        BigDecimal grossMargin,
        String currency,
        Instant createdAt
) {}
