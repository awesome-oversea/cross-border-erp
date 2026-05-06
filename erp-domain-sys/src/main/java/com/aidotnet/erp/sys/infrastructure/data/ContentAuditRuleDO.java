package com.aidotnet.erp.sys.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 内容审核规则数据对象(ContentAuditRuleDO)
 * <p>
 * 描述: 内容审核规则数据对象，对应sys_content_audit_rule表。
 *       定义商品描述、广告文案等内容审核的关键词和规则。
 * </p>
 *
 * @author ERP系统
 */
@TableName("sys_content_audit_rule")
public class ContentAuditRuleDO {
    @TableId(type = IdType.ASSIGN_ID)
    private String ruleId;
    private String tenantId;
    private String ruleType;
    private String category;
    private String keyword;
    private String keywordPattern;
    private Integer severity;
    private String action;
    private String replacement;
    private String description;
    private Boolean enabled;
    private String applicablePlatforms;
    private Instant createdAt;
    private Instant updatedAt;

    public ContentAuditRuleDO() {}

    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getRuleType() { return ruleType; }
    public void setRuleType(String ruleType) { this.ruleType = ruleType; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public String getKeywordPattern() { return keywordPattern; }
    public void setKeywordPattern(String keywordPattern) { this.keywordPattern = keywordPattern; }
    public Integer getSeverity() { return severity; }
    public void setSeverity(Integer severity) { this.severity = severity; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getReplacement() { return replacement; }
    public void setReplacement(String replacement) { this.replacement = replacement; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public String getApplicablePlatforms() { return applicablePlatforms; }
    public void setApplicablePlatforms(String applicablePlatforms) { this.applicablePlatforms = applicablePlatforms; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
