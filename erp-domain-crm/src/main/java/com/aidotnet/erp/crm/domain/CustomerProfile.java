package com.aidotnet.erp.crm.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/**
 * 客户画像领域模型
 * <p>
 * 描述: 客户360度画像，包含分群、生命周期价值、退货率、偏好渠道和风险等级。
 * </p>
 *
 * @author ERP系统
 */
public record CustomerProfile(
        String profileId,
        String tenantId,
        String customerId,
        String segment,
        BigDecimal lifetimeValue,
        BigDecimal avgOrderValue,
        int totalOrders,
        int totalReturns,
        BigDecimal returnRate,
        String preferredChannel,
        String preferredLanguage,
        String riskLevel,
        Map<String, Object> attributes,
        Instant firstOrderAt,
        Instant lastOrderAt,
        Instant createdAt,
        Instant updatedAt
) {}
