package com.aidotnet.erp.scm.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * PMS补货建议领域模型
 * <p>
 * 描述: PMS智能服务生成的补货建议，包含建议采购量、预估成本和风险等级。
 *       支持幂等接收，状态流转: pending → accepted/rejected。
 * </p>
 *
 * @author ERP系统
 */
public record PmsReplenishmentAdvice(
        String adviceId,
        String tenantId,
        String sellerSku,
        String warehouseId,
        int suggestedQuantity,
        BigDecimal estimatedCost,
        String reason,
        String riskLevel,
        String traceId,
        String idempotencyKey,
        String status,
        Instant createdAt
) {
    /** 是否待处理 */
    public boolean isPending() {
        return "pending".equalsIgnoreCase(status);
    }

    /** 是否需要人工审核(HIGH或CRITICAL风险) */
    public boolean requiresReview() {
        return "HIGH".equalsIgnoreCase(riskLevel) || "CRITICAL".equalsIgnoreCase(riskLevel);
    }
}
