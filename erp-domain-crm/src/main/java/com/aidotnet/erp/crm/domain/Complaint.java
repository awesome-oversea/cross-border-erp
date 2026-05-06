package com.aidotnet.erp.crm.domain;

import java.time.Instant;

/**
 * 投诉领域模型
 * <p>
 * 描述: 客户投诉记录，包含投诉类型、严重等级和处理状态。
 * </p>
 *
 * @author ERP系统
 */
public record Complaint(String complaintId, String tenantId, String customerId, String orderId, String channel,
                        ComplaintType type, ComplaintSeverity severity, ComplaintStatus status, String subject,
                        String description, String handlerId, String resolution, Instant complainedAt,
                        Instant resolvedAt, Instant createdAt, Instant updatedAt) {}
