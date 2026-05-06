package com.aidotnet.erp.crm.domain;

import java.time.Instant;
import java.util.Map;

/**
 * 邮件规则领域模型
 * <p>
 * 描述: 邮件自动分配规则，按条件匹配后分配给指定客服。
 * </p>
 *
 * @author ERP系统
 */
public record EmailRule(
        String ruleId,
        String tenantId,
        Map<String, Object> conditions,
        String assignTo,
        int priority,
        String status,
        Instant createdAt,
        Instant updatedAt
) {}
