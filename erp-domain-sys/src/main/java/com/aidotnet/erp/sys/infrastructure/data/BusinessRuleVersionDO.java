package com.aidotnet.erp.sys.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 业务规则版本数据对象(BusinessRuleVersionDO)
 * <p>
 * 描述: 业务规则版本管理数据对象，对应sys_business_rule_version表。
 *       存储业务规则的版本历史，支持版本回滚和模拟回放。
 *       每次规则变更产生新版本，保留完整变更链路。
 * </p>
 *
 * @author ERP系统
 */
@TableName("sys_business_rule_version")
public class BusinessRuleVersionDO {
    @TableId(type = IdType.ASSIGN_ID)
    private String versionId;
    private String tenantId;
    private String ruleId;
    private String ruleType;
    private String ruleName;
    private Integer version;
    private String contentJson;
    private String changeDescription;
    private String changedBy;
    private Instant createdAt;

    public BusinessRuleVersionDO() {}

    public String getVersionId() { return versionId; }
    public void setVersionId(String versionId) { this.versionId = versionId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }
    public String getRuleType() { return ruleType; }
    public void setRuleType(String ruleType) { this.ruleType = ruleType; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public String getContentJson() { return contentJson; }
    public void setContentJson(String contentJson) { this.contentJson = contentJson; }
    public String getChangeDescription() { return changeDescription; }
    public void setChangeDescription(String changeDescription) { this.changeDescription = changeDescription; }
    public String getChangedBy() { return changedBy; }
    public void setChangedBy(String changedBy) { this.changedBy = changedBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
