package com.aidotnet.erp.sys.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * PMS草稿单据数据对象(PmsDraftDocumentDO)
 * <p>
 * 描述: PMS草稿单据数据对象，对应sys_pms_draft_document表。
 *       存储AI建议生成的草稿单据，需审批后才可执行写入ERP业务数据。
 * </p>
 *
 * @author ERP系统
 */
@TableName("sys_pms_draft_document")
public class PmsDraftDocumentDO {
    @TableId(type = IdType.ASSIGN_ID)
    private String draftId;
    private String tenantId;
    private String erpReferenceId;
    private String domain;
    private String draftType;
    private String targetBusinessType;
    private String targetBusinessId;
    private String contentJson;
    private String trustLevel;
    private String sourceSystem;
    private String actorId;
    private String actorType;
    private String agentId;
    private String scope;
    private String purpose;
    private String traceId;
    private String approvalStatus;
    private String approvedBy;
    private Instant approvedAt;
    private String executionStatus;
    private String executionResult;
    private Instant createdAt;
    private Instant updatedAt;

    public PmsDraftDocumentDO() {}

    public String getDraftId() { return draftId; }
    public void setDraftId(String draftId) { this.draftId = draftId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getErpReferenceId() { return erpReferenceId; }
    public void setErpReferenceId(String erpReferenceId) { this.erpReferenceId = erpReferenceId; }
    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    public String getDraftType() { return draftType; }
    public void setDraftType(String draftType) { this.draftType = draftType; }
    public String getTargetBusinessType() { return targetBusinessType; }
    public void setTargetBusinessType(String targetBusinessType) { this.targetBusinessType = targetBusinessType; }
    public String getTargetBusinessId() { return targetBusinessId; }
    public void setTargetBusinessId(String targetBusinessId) { this.targetBusinessId = targetBusinessId; }
    public String getContentJson() { return contentJson; }
    public void setContentJson(String contentJson) { this.contentJson = contentJson; }
    public String getTrustLevel() { return trustLevel; }
    public void setTrustLevel(String trustLevel) { this.trustLevel = trustLevel; }
    public String getSourceSystem() { return sourceSystem; }
    public void setSourceSystem(String sourceSystem) { this.sourceSystem = sourceSystem; }
    public String getActorId() { return actorId; }
    public void setActorId(String actorId) { this.actorId = actorId; }
    public String getActorType() { return actorType; }
    public void setActorType(String actorType) { this.actorType = actorType; }
    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public String getApprovalStatus() { return approvalStatus; }
    public void setApprovalStatus(String approvalStatus) { this.approvalStatus = approvalStatus; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }
    public String getExecutionStatus() { return executionStatus; }
    public void setExecutionStatus(String executionStatus) { this.executionStatus = executionStatus; }
    public String getExecutionResult() { return executionResult; }
    public void setExecutionResult(String executionResult) { this.executionResult = executionResult; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
