package com.aidotnet.erp.sys.infrastructure.data;

import java.time.Instant;

/**
 * 单据编号规则数据对象(DocumentNumberRuleDO)
 * <p>
 * 描述: 单据编号规则数据对象，对应sys_document_number_rule表。
 *       定义各业务单据类型的编号生成规则，支持前缀、日期格式、
 *       序列号长度和自动重置策略。与DocumentNumberSegmentDO配合使用，
 *       实现单据编号的有序生成和防重复。
 * </p>
 * <p>
 * 业务规则:
 *   1. 同一租户下documentType必须唯一
 *   2. 序列号按step递增，currentSequence记录当前值
 *   3. resetDaily/resetMonthly/resetYearly控制序列号重置周期
 *   4. 重置时lastResetAt更新为当前时间
 * </p>
 * <p>
 * 数据库映射:
 *   - 表名: sys_document_number_rule
 *   - 主键: rule_id
 *   - 唯一约束: (tenant_id, document_type)
 * </p>
 *
 * @author ERP系统
 * @see DocumentNumberSegmentDO
 */
public class DocumentNumberRuleDO {
    private String ruleId;
    private String tenantId;
    private String ruleName;
    private String documentType;
    private String prefix;
    private String dateFormat;
    private int sequenceLength;
    private long currentSequence;
    private long step;
    private boolean resetDaily;
    private boolean resetMonthly;
    private boolean resetYearly;
    private Instant lastResetAt;
    private Instant createdAt;
    private Instant updatedAt;

    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }
    public String getDateFormat() { return dateFormat; }
    public void setDateFormat(String dateFormat) { this.dateFormat = dateFormat; }
    public int getSequenceLength() { return sequenceLength; }
    public void setSequenceLength(int sequenceLength) { this.sequenceLength = sequenceLength; }
    public long getCurrentSequence() { return currentSequence; }
    public void setCurrentSequence(long currentSequence) { this.currentSequence = currentSequence; }
    public long getStep() { return step; }
    public void setStep(long step) { this.step = step; }
    public boolean isResetDaily() { return resetDaily; }
    public void setResetDaily(boolean resetDaily) { this.resetDaily = resetDaily; }
    public boolean isResetMonthly() { return resetMonthly; }
    public void setResetMonthly(boolean resetMonthly) { this.resetMonthly = resetMonthly; }
    public boolean isResetYearly() { return resetYearly; }
    public void setResetYearly(boolean resetYearly) { this.resetYearly = resetYearly; }
    public Instant getLastResetAt() { return lastResetAt; }
    public void setLastResetAt(Instant lastResetAt) { this.lastResetAt = lastResetAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
