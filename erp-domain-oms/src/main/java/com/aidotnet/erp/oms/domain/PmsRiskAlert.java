package com.aidotnet.erp.oms.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * PMS风控告警领域模型
 * <p>
 * 描述: PMS智能风控服务生成的订单风险告警，包含风险评分、等级和建议操作。
 *       支持幂等接收，状态流转: pending → acknowledged/resolved。
 * </p>
 *
 * @author ERP系统
 */
public record PmsRiskAlert(
        String alertId,
        String tenantId,
        String orderId,
        String riskType,
        BigDecimal riskScore,
        String riskLevel,
        String description,
        String suggestedAction,
        String traceId,
        String idempotencyKey,
        String status,
        Instant createdAt
) {
    /** 是否需要人工审核(HIGH或CRITICAL级别) */
    public boolean requiresReview() {
        return "HIGH".equals(riskLevel) || "CRITICAL".equals(riskLevel);
    }

    /** 是否待处理 */
    public boolean isPending() {
        return "pending".equals(status);
    }
}
