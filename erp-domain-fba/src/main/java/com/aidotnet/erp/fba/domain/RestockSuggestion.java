package com.aidotnet.erp.fba.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 补货建议领域模型
 * <p>
 * 描述: 基于日均销量和交期计算的FBA补货建议，包含本地→FBA和海外→FBA的建议数量。
 * </p>
 *
 * @author ERP系统
 */
public record RestockSuggestion(
        String suggestionId,
        String tenantId,
        String productId,
        String sellerSku,
        BigDecimal avgDailySales,
        int leadTimeDays,
        int suggestedPurchaseQty,
        int suggestedLocalToFbaQty,
        int suggestedOverseasToFbaQty,
        Instant suggestedShipDate,
        Instant createdAt,
        Instant updatedAt
) {}
