package com.aidotnet.erp.sys.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 审批流步骤数据对象(AuditFlowStepDO)
 * <p>
 * 描述: 审批流步骤数据对象，对应sys_audit_flow_step表。
 *       定义审批流中每个步骤的审批人角色和审批顺序。
 *       通过flowId关联审批流配置，按stepOrder排列步骤顺序。
 * </p>
 * <p>
 * 业务规则:
 *   1. 同一审批流下stepOrder必须唯一且连续
 *   2. autoApprove=true时系统自动通过该步骤
 *   3. approverRole指定审批人角色编码
 * </p>
 * <p>
 * 数据库映射:
 *   - 表名: sys_audit_flow_step
 *   - 主键: step_id (ASSIGN_ID策略)
 *   - 索引: (flow_id, step_order)
 * </p>
 *
 * @author ERP系统
 * @see AuditFlowConfigDO
 */
@TableName("sys_audit_flow_step")
public class AuditFlowStepDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String stepId;
    private String flowId;
    private String tenantId;
    private Integer stepOrder;
    private String stepName;
    private String approverRole;
    private Boolean autoApprove;
    private Instant createdAt;

    public AuditFlowStepDO() {}

    public String getStepId() { return stepId; }
    public void setStepId(String stepId) { this.stepId = stepId; }
    public String getFlowId() { return flowId; }
    public void setFlowId(String flowId) { this.flowId = flowId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public Integer getStepOrder() { return stepOrder; }
    public void setStepOrder(Integer stepOrder) { this.stepOrder = stepOrder; }
    public String getStepName() { return stepName; }
    public void setStepName(String stepName) { this.stepName = stepName; }
    public String getApproverRole() { return approverRole; }
    public void setApproverRole(String approverRole) { this.approverRole = approverRole; }
    public Boolean getAutoApprove() { return autoApprove; }
    public void setAutoApprove(Boolean autoApprove) { this.autoApprove = autoApprove; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
