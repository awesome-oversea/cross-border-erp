package com.aidotnet.erp.crm.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 服务工单数据对象
 * <p>
 * 描述: 对应crm_service_ticket表，存储客服工单记录。
 *       status字段以枚举名称字符串存储(OPEN/IN_PROGRESS/RESOLVED/CLOSED)。
 * </p>
 *
 * @author ERP系统
 */
@TableName("crm_service_ticket")
public class ServiceTicketDO {

    /** 工单ID(主键) */
    @TableId(type = IdType.ASSIGN_ID)
    private String ticketId;
    /** 租户ID */
    private String tenantId;
    /** 客户ID */
    private String customerId;
    /** 工单主题 */
    private String subject;
    /** 问题描述 */
    private String description;
    /** 处理人 */
    private String assignee;
    /** 工单状态(枚举名称) */
    private String status;
    /** 解决方案 */
    private String resolution;
    /** 创建时间 */
    private Instant createdAt;
    /** 更新时间 */
    private Instant updatedAt;

    public ServiceTicketDO() {}

    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getAssignee() { return assignee; }
    public void setAssignee(String assignee) { this.assignee = assignee; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
