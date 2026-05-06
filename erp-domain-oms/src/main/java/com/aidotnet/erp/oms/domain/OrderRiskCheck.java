package com.aidotnet.erp.oms.domain;

import java.time.Instant;

/**
 * 订单风控检查领域模型
 * <p>
 * 描述: 订单风控检查结果，由PMS风控服务或本地规则引擎生成。
 *       包含风险等级、类型和建议操作。
 * </p>
 *
 * @author ERP系统
 */
public record OrderRiskCheck(
        String checkId,
        String tenantId,
        String orderId,
        RiskLevel riskLevel,
        String riskType,
        String description,
        String suggestedAction,
        Instant checkedAt
) {
    /** 风险等级 */
    public enum RiskLevel {
        /** 低风险 */
        LOW,
        /** 中风险 */
        MEDIUM,
        /** 高风险 */
        HIGH,
        /** 极高风险 */
        CRITICAL
    }
}
