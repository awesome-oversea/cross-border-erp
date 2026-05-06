package com.aidotnet.erp.crm.domain;

import java.time.Instant;

/**
 * 质量问题领域模型
 * <p>
 * 描述: 产品质量问题记录，来源于评价分析或客户投诉。
 * </p>
 *
 * @author ERP系统
 */
public record QualityIssue(
        String issueId,
        String tenantId,
        String productId,
        String source,
        String description,
        String severity,
        String status,
        Instant createdAt,
        Instant updatedAt
) {}
