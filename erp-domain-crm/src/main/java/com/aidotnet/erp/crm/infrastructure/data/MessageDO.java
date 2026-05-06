package com.aidotnet.erp.crm.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 消息数据对象
 * <p>
 * 描述: 对应crm_message表，存储客户消息(站内信/邮件)。
 *       direction、status字段以枚举名称字符串存储。
 * </p>
 *
 * @author ERP系统
 */
@TableName("crm_message")
public class MessageDO {

    /** 消息ID(主键) */
    @TableId(type = IdType.ASSIGN_ID)
    private String messageId;
    /** 租户ID */
    private String tenantId;
    /** 消息平台 */
    private String platform;
    /** 客户ID */
    private String customerId;
    /** 消息主题 */
    private String subject;
    /** 消息正文 */
    private String body;
    /** 消息方向(INBOUND/OUTBOUND) */
    private String direction;
    /** 分配处理人 */
    private String assignedTo;
    /** 消息状态(UNREAD/READ/REPLIED/CLOSED) */
    private String status;
    /** 消息日期 */
    private Instant messageDate;
    /** 创建时间 */
    private Instant createdAt;
    /** 更新时间 */
    private Instant updatedAt;

    public MessageDO() {}

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getMessageDate() { return messageDate; }
    public void setMessageDate(Instant messageDate) { this.messageDate = messageDate; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
