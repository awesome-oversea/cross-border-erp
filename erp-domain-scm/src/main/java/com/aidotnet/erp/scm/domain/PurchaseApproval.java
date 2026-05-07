package com.aidotnet.erp.scm.domain;

import java.time.Instant;

/**
 * 采购审批领域模型
 * <p>
 * 描述: 采购订单审批记录，记录审批人、审批状态和审批意见。
 * </p>
 *
 * @author ERP系统
 */
public record PurchaseApproval(
        String approvalId,
        String tenantId,
        String poId,
        int approvalLevel,
        ApprovalStatus status,
        String approverId,
        String comment,
        Instant approvedAt,
        Instant createdAt) {}
