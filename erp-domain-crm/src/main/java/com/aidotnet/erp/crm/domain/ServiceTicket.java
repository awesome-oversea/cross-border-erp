package com.aidotnet.erp.crm.domain;

import java.time.Instant;

/**
 * 服务工单领域模型
 * <p>
 * 描述: 客户服务工单，记录问题、处理人和解决方案。
 * </p>
 *
 * @author ERP系统
 */
public record ServiceTicket(String ticketId, String tenantId, String customerId, String subject, String description,
                            String assignee, TicketStatus status, String resolution,
                            Instant createdAt, Instant updatedAt) {}
