package com.aidotnet.erp.oms.domain;

import java.time.Instant;

/**
 * 订单策略领域模型
 * <p>
 * 描述: 订单处理策略配置，支持审核、分配、物流、定价等策略类型。
 *       策略按优先级顺序匹配应用。
 * </p>
 *
 * @author ERP系统
 */
public record OrderStrategy(
        String strategyId,
        String tenantId,
        String strategyType,
        String name,
        String description,
        String rules,
        boolean enabled,
        int priority,
        Instant createdAt,
        Instant updatedAt
) {
    /** 策略类型 */
    public enum StrategyType {
        /** 审核策略 - 订单审核规则 */
        AUDIT,
        /** 分配策略 - 仓库分配规则 */
        ALLOCATION,
        /** 物流策略 - 物流渠道选择规则 */
        LOGISTICS,
        /** 定价策略 - 价格调整规则 */
        PRICING
    }
}
