package com.aidotnet.erp.crm.domain;

import java.time.Instant;

/**
 * 消息领域模型
 * <p>
 * 描述: 客户消息，支持站内信和邮件，区分收发方向和阅读状态。
 * </p>
 *
 * @author ERP系统
 */
public record Message(
        String messageId,
        String tenantId,
        String platform,
        String customerId,
        String subject,
        String body,
        MessageDirection direction,
        String assignedTo,
        MessageStatus status,
        Instant messageDate,
        Instant createdAt,
        Instant updatedAt
) {
    /** 消息方向 */
    public enum MessageDirection { /** 收件 */ INBOUND, /** 发件 */ OUTBOUND }
    /** 消息状态 */
    public enum MessageStatus { /** 未读 */ UNREAD, /** 已读 */ READ, /** 已回复 */ REPLIED, /** 已关闭 */ CLOSED }
}
