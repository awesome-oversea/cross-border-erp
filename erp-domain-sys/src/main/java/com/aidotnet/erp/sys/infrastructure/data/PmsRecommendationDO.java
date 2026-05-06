package com.aidotnet.erp.sys.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * PMS推荐数据对象(PmsRecommendationDO)
 * <p>
 * 描述: PMS智能推荐系统生成的业务建议数据对象，对应sys_pms_recommendation表。
 *       存储AI智能服务(PMS)推送的业务建议，ERP作为审批和执行主控方。
 *       支持完整的建议生命周期: 提交→审批→执行→评估→回顾。
 * </p>
 * <p>
 * 业务规则:
 *   1. erpReferenceId为ERP侧唯一标识，全局唯一
 *   2. (tenant_id, idempotency_key)实现幂等提交
 *   3. status字段控制状态流转，遵循预定义状态机
 *   4. dataSources/riskFlags存储JSON数组格式
 *   5. score/confidence为0~1之间的精度值(DECIMAL(8,4))
 * </p>
 * <p>
 * 状态流转:
 *   ACCEPTED → PENDING_APPROVAL → APPROVED → EXECUTING → EXECUTED → MEASURED → REVIEWED
 *   ACCEPTED → REJECTED
 *   PENDING_APPROVAL → APPROVAL_REJECTED
 *   EXECUTING → PARTIALLY_EXECUTED / FAILED
 *   PARTIALLY_EXECUTED → EXECUTED / FAILED / ROLLED_BACK
 *   FAILED → EXECUTING / ROLLED_BACK
 *   EXECUTED → MEASURED / ROLLED_BACK
 * </p>
 * <p>
 * 数据库映射:
 *   - 表名: sys_pms_recommendation
 *   - 主键: erp_reference_id (ASSIGN_ID策略)
 *   - 索引: idx_pms_rec_tenant(tenant_id), idx_pms_rec_domain(tenant_id, domain),
 *           idx_pms_rec_status(tenant_id, status), idx_pms_rec_idempotency(tenant_id, idempotency_key)
 * </p>
 *
 * @author ERP系统
 * @see com.aidotnet.erp.sys.domain.PmsRecommendation
 */
@TableName("sys_pms_recommendation")
public class PmsRecommendationDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String erpReferenceId;
    private String tenantId;
    private String recommendationId;
    private String domain;
    private String recommendationType;
    private String objectType;
    private String targetObjectType;
    private String targetObjectId;
    private String content;
    private BigDecimal score;
    private BigDecimal confidence;
    private String evidenceChainId;
    private String dataSources;
    private String riskFlags;
    private String explainability;
    private String requestedAction;
    private String status;
    private String approvalPolicy;
    private String rejectionReason;
    private String executionResult;
    private String measuredResult;
    private String traceId;
    private String idempotencyKey;
    private String actorId;
    private String actorType;
    private String agentId;
    private String scope;
    private String purpose;
    private String sourceSystem;
    private String auditId;
    private Instant createdAt;
    private Instant updatedAt;

    public PmsRecommendationDO() {
    }

    public String getErpReferenceId() { return erpReferenceId; }
    public void setErpReferenceId(String erpReferenceId) { this.erpReferenceId = erpReferenceId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getRecommendationId() { return recommendationId; }
    public void setRecommendationId(String recommendationId) { this.recommendationId = recommendationId; }
    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    public String getRecommendationType() { return recommendationType; }
    public void setRecommendationType(String recommendationType) { this.recommendationType = recommendationType; }
    public String getObjectType() { return objectType; }
    public void setObjectType(String objectType) { this.objectType = objectType; }
    public String getTargetObjectType() { return targetObjectType; }
    public void setTargetObjectType(String targetObjectType) { this.targetObjectType = targetObjectType; }
    public String getTargetObjectId() { return targetObjectId; }
    public void setTargetObjectId(String targetObjectId) { this.targetObjectId = targetObjectId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public BigDecimal getScore() { return score; }
    public void setScore(BigDecimal score) { this.score = score; }
    public BigDecimal getConfidence() { return confidence; }
    public void setConfidence(BigDecimal confidence) { this.confidence = confidence; }
    public String getEvidenceChainId() { return evidenceChainId; }
    public void setEvidenceChainId(String evidenceChainId) { this.evidenceChainId = evidenceChainId; }
    public String getDataSources() { return dataSources; }
    public void setDataSources(String dataSources) { this.dataSources = dataSources; }
    public String getRiskFlags() { return riskFlags; }
    public void setRiskFlags(String riskFlags) { this.riskFlags = riskFlags; }
    public String getExplainability() { return explainability; }
    public void setExplainability(String explainability) { this.explainability = explainability; }
    public String getRequestedAction() { return requestedAction; }
    public void setRequestedAction(String requestedAction) { this.requestedAction = requestedAction; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getApprovalPolicy() { return approvalPolicy; }
    public void setApprovalPolicy(String approvalPolicy) { this.approvalPolicy = approvalPolicy; }
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    public String getExecutionResult() { return executionResult; }
    public void setExecutionResult(String executionResult) { this.executionResult = executionResult; }
    public String getMeasuredResult() { return measuredResult; }
    public void setMeasuredResult(String measuredResult) { this.measuredResult = measuredResult; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
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
    public String getSourceSystem() { return sourceSystem; }
    public void setSourceSystem(String sourceSystem) { this.sourceSystem = sourceSystem; }
    public String getAuditId() { return auditId; }
    public void setAuditId(String auditId) { this.auditId = auditId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
