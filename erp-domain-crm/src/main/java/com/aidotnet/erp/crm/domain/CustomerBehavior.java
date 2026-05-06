package com.aidotnet.erp.crm.domain;

import java.time.Instant;
import java.util.Map;

/**
 * 客户行为领域模型
 * <p>
 * 描述: 客户行为事件，记录浏览、加购、下单、评价等行为。
 * </p>
 *
 * @author ERP系统
 */
public record CustomerBehavior(
        String behaviorId,
        String tenantId,
        String customerId,
        String behaviorType,
        String channel,
        String objectType,
        String objectId,
        Map<String, Object> context,
        Instant occurredAt,
        Instant createdAt
) {}
