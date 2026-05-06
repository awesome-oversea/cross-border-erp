package com.aidotnet.erp.sys.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 审批流配置数据对象(AuditFlowConfigDO)
 * <p>
 * 描述: 审批流配置数据对象，对应sys_audit_flow_config表。
 *       定义各业务类型的审批流程配置，包括审批步骤数和启用状态。
 *       与AuditFlowStepDO配合使用，构成完整的审批流定义。
 * </p>
 * <p>
 * 业务规则:
 *   1. 同一租户下flowCode必须唯一
 *   2. requiredApprovals必须大于0且与审批步骤数一致
 *   3. enabled=false的审批流不参与业务审批
 * </p>
 * <p>
 * 数据库映射:
 *   - 表名: sys_audit_flow_config
 *   - 主键: flow_id (ASSIGN_ID策略)
 *   - 唯一约束: (tenant_id, flow_code)
 * </p>
 *
 * @author ERP系统
 * @see AuditFlowStepDO
 */
@TableName("sys_audit_flow_config")
public class AuditFlowConfigDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String flowId;
    private String tenantId;
    private String flowCode;
    private String flowName;
    private String businessType;
    private Integer requiredApprovals;
    private Boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;

    public AuditFlowConfigDO() {}

    public String getFlowId() { return flowId; }
    public void setFlowId(String flowId) { this.flowId = flowId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getFlowCode() { return flowCode; }
    public void setFlowCode(String flowCode) { this.flowCode = flowCode; }
    public String getFlowName() { return flowName; }
    public void setFlowName(String flowName) { this.flowName = flowName; }
    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    public Integer getRequiredApprovals() { return requiredApprovals; }
    public void setRequiredApprovals(Integer requiredApprovals) { this.requiredApprovals = requiredApprovals; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
