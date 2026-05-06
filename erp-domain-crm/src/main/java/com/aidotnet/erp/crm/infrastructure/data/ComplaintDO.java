package com.aidotnet.erp.crm.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 客诉数据对象
 * <p>
 * 描述: 对应crm_complaint表，存储客户投诉记录。
 *       type、severity、status字段以枚举名称字符串存储。
 * </p>
 *
 * @author ERP系统
 */
@TableName("crm_complaint")
public class ComplaintDO {

    /** 客诉ID(主键) */
    @TableId(type = IdType.ASSIGN_ID)
    private String complaintId;
    /** 租户ID */
    private String tenantId;
    /** 客户ID */
    private String customerId;
    /** 关联订单ID */
    private String orderId;
    /** 投诉渠道 */
    private String channel;
    /** 投诉类型(枚举名称) */
    private String type;
    /** 严重等级(枚举名称) */
    private String severity;
    /** 处理状态(枚举名称) */
    private String status;
    /** 投诉主题 */
    private String subject;
    /** 投诉描述 */
    private String description;
    /** 处理人ID */
    private String handlerId;
    /** 解决方案 */
    private String resolution;
    /** 投诉时间 */
    private Instant complainedAt;
    /** 解决时间 */
    private Instant resolvedAt;
    /** 创建时间 */
    private Instant createdAt;
    /** 更新时间 */
    private Instant updatedAt;

    public ComplaintDO() {}

    public String getComplaintId() { return complaintId; }
    public void setComplaintId(String complaintId) { this.complaintId = complaintId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getChannel() { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getHandlerId() { return handlerId; }
    public void setHandlerId(String handlerId) { this.handlerId = handlerId; }
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
    public Instant getComplainedAt() { return complainedAt; }
    public void setComplainedAt(Instant complainedAt) { this.complainedAt = complainedAt; }
    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
