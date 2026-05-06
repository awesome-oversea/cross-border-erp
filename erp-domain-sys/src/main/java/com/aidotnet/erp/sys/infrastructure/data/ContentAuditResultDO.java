package com.aidotnet.erp.sys.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 内容审核结果数据对象(ContentAuditResultDO)
 * <p>
 * 描述: 内容审核结果数据对象，对应sys_content_audit_result表。
 *       存储内容审核的执行结果，包含违规项详情。
 * </p>
 *
 * @author ERP系统
 */
@TableName("sys_content_audit_result")
public class ContentAuditResultDO {
    @TableId(type = IdType.ASSIGN_ID)
    private String resultId;
    private String tenantId;
    private String auditType;
    private String sourceType;
    private String sourceId;
    private Boolean passed;
    private String violations;
    private String auditedBy;
    private Instant auditedAt;

    public ContentAuditResultDO() {}

    public String getResultId() { return resultId; }
    public void setResultId(String resultId) { this.resultId = resultId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getAuditType() { return auditType; }
    public void setAuditType(String auditType) { this.auditType = auditType; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }
    public Boolean getPassed() { return passed; }
    public void setPassed(Boolean passed) { this.passed = passed; }
    public String getViolations() { return violations; }
    public void setViolations(String violations) { this.violations = violations; }
    public String getAuditedBy() { return auditedBy; }
    public void setAuditedBy(String auditedBy) { this.auditedBy = auditedBy; }
    public Instant getAuditedAt() { return auditedAt; }
    public void setAuditedAt(Instant auditedAt) { this.auditedAt = auditedAt; }
}
