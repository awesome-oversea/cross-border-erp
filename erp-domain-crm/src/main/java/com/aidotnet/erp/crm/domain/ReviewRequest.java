package com.aidotnet.erp.crm.domain;

import java.time.Instant;

/**
 * 评价请求领域模型
 * <p>
 * 描述: 评价索评请求，可排除已退款和差评客户。
 * </p>
 *
 * @author ERP系统
 */
public record ReviewRequest(
        String requestId,
        String tenantId,
        String orderId,
        String platform,
        boolean excludeRefunded,
        boolean excludeNegative,
        String status,
        Instant createdAt,
        Instant updatedAt
) {}
