package com.aidotnet.erp.tms.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 物流策略领域模型
 * <p>
 * 描述: 物流策略规则，支持承运商选择、运费计算、时效预估和异常处理四种策略类型。
 * </p>
 *
 * @author ERP系统
 */
public record LogisticsStrategy(
        String strategyId,
        String tenantId,
        String strategyName,
        String strategyType,
        String originCountry,
        String destinationCountry,
        String preferredCarrier,
        String rules,
        boolean enabled,
        int priority,
        Instant createdAt,
        Instant updatedAt
) {
    /** 策略类型 */
    public enum StrategyType {
        /** 承运商选择 */
        CARRIER_SELECTION,
        /** 运费计算 */
        FREIGHT_CALCULATION,
        /** 时效预估 */
        DELIVERY_ESTIMATION,
        /** 异常处理 */
        EXCEPTION_HANDLING
    }
}
