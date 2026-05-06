package com.aidotnet.erp.tms.domain;

import java.math.BigDecimal;

/**
 * 承运商推荐领域模型
 * <p>
 * 描述: 物流策略引擎输出的承运商推荐结果，包含评分和预估成本。
 * </p>
 *
 * @author ERP系统
 */
public record CarrierRecommendation(
        String carrierId,
        String carrierCode,
        String carrierName,
        String serviceLevel,
        BigDecimal estimatedCost,
        int estimatedDeliveryDays,
        int recommendationScore
) {}
