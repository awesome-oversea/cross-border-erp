package com.aidotnet.erp.dashboard.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 待办事项数据对象
 * <p>
 * 描述: 对应dashboard_todo_item表，用于存储用户待办事项。
 *       待办事项可关联业务对象(如订单、发货单等)，支持状态流转和到期提醒。
 * </p>
 * <p>
 * 业务规则:
 *   1. 待办状态(status): PENDING(待处理)/IN_PROGRESS(进行中)/COMPLETED(已完成)/CANCELLED(已取消)
 *   2. 状态流转: PENDING → IN_PROGRESS → COMPLETED/CANCELLED
 *   3. businessType+businessId关联业务对象，如: ORDER+orderId
 *   4. dueTime到期时间，用于到期提醒和排序
 * </p>
 *
 * @author ERP系统
 * @see com.aidotnet.erp.dashboard.domain.TodoItem
 */
@TableName("dashboard_todo_item")
public class TodoItemDO {

    /** 待办唯一标识，UUID格式 */
    @TableId(type = IdType.ASSIGN_ID)
    private String todoId;

    /** 租户ID，实现多租户数据隔离 */
    private String tenantId;

    /** 用户ID，关联IAM域用户 */
    private String userId;

    /** 待办类型，如: approval/review/shipment */
    private String type;

    /** 关联业务类型，如: ORDER/SHIPMENT/PURCHASE */
    private String businessType;

    /** 关联业务ID，如: 订单ID/发货单ID */
    private String businessId;

    /** 待办标题 */
    private String title;

    /** 待办状态: PENDING/IN_PROGRESS/COMPLETED/CANCELLED */
    private String status;

    /** 到期时间，用于到期提醒 */
    private Instant dueTime;

    /** 创建时间，UTC时区 */
    private Instant createdAt;

    /** 更新时间，UTC时区 */
    private Instant updatedAt;

    public TodoItemDO() {}

    public String getTodoId() { return todoId; }
    public void setTodoId(String todoId) { this.todoId = todoId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    public String getBusinessId() { return businessId; }
    public void setBusinessId(String businessId) { this.businessId = businessId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getDueTime() { return dueTime; }
    public void setDueTime(Instant dueTime) { this.dueTime = dueTime; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
