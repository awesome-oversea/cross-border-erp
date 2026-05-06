package com.aidotnet.erp.sys.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 数据脱敏规则数据对象(DataMaskingRuleDO)
 * <p>
 * 描述: 数据脱敏规则数据对象，对应sys_data_masking_rule表。
 *       定义敏感数据的脱敏处理规则，支持按字段类型配置不同的脱敏策略。
 *       通过keepPrefix和keepSuffix控制保留前后字符数，中间用replaceChar替换。
 * </p>
 * <p>
 * 业务规则:
 *   1. 同一租户下ruleCode必须唯一
 *   2. keepPrefix+keepSuffix不能超过原始字段长度
 *   3. enabled=false的规则不参与脱敏处理
 *   4. maskPattern支持正则表达式匹配
 * </p>
 * <p>
 * 数据库映射:
 *   - 表名: sys_data_masking_rule
 *   - 主键: rule_id (ASSIGN_ID策略)
 *   - 唯一约束: (tenant_id, rule_code)
 * </p>
 *
 * @author ERP系统
 */
@TableName("sys_data_masking_rule")
public class DataMaskingRuleDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String ruleId;
    private String tenantId;
    private String ruleCode;
    private String ruleName;
    private String fieldType;
    private String maskPattern;
    private String replaceChar;
    private int keepPrefix;
    private int keepSuffix;
    private boolean enabled;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;

    public DataMaskingRuleDO() {}

    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getRuleCode() { return ruleCode; }
    public void setRuleCode(String ruleCode) { this.ruleCode = ruleCode; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public String getFieldType() { return fieldType; }
    public void setFieldType(String fieldType) { this.fieldType = fieldType; }
    public String getMaskPattern() { return maskPattern; }
    public void setMaskPattern(String maskPattern) { this.maskPattern = maskPattern; }
    public String getReplaceChar() { return replaceChar; }
    public void setReplaceChar(String replaceChar) { this.replaceChar = replaceChar; }
    public int getKeepPrefix() { return keepPrefix; }
    public void setKeepPrefix(int keepPrefix) { this.keepPrefix = keepPrefix; }
    public int getKeepSuffix() { return keepSuffix; }
    public void setKeepSuffix(int keepSuffix) { this.keepSuffix = keepSuffix; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
