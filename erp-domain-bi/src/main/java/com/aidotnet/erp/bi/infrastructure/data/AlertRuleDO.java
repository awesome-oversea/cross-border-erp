package com.aidotnet.erp.bi.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 告警规则数据对象
 * <p>
 * 描述: 对应bi_alert_rule表，用于存储BI域告警规则的配置数据。
 *       告警规则定义了指标异常的检测条件和通知方式，
 *       当指标值满足触发条件时自动生成告警并通知相关人员。
 * </p>
 * <p>
 * 业务规则:
 *   1. 告警规则关联指标编码(metricCode)，支持跨域指标告警
 *   2. 触发条件(condition)支持: GT(大于)/LT(小于)/GTE(大于等于)/LTE(小于等于)/EQ(等于)
 *   3. 严重级别(severity)支持: INFO/WARNING/CRITICAL
 *   4. 通知渠道(notifyChannel)支持: email/sms/webhook/dingtalk
 *   5. 通知目标(notifyTargets)为逗号分隔的用户ID或群组ID
 *   6. 禁用的规则不参与告警检测
 * </p>
 *
 * @author ERP系统
 * @see com.aidotnet.erp.bi.domain.AlertRule
 */
@TableName("bi_alert_rule")
public class AlertRuleDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String ruleId;
    private String tenantId;
    private String ruleName;
    private String metricCode;
    private String domain;
    private String condition;
    private String threshold;
    private String severity;
    private boolean enabled;
    private String notifyChannel;
    private String notifyTargets;
    private Instant createdAt;
    private Instant updatedAt;

    public AlertRuleDO() {}

    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public String getMetricCode() { return metricCode; }
    public void setMetricCode(String metricCode) { this.metricCode = metricCode; }
    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public String getThreshold() { return threshold; }
    public void setThreshold(String threshold) { this.threshold = threshold; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getNotifyChannel() { return notifyChannel; }
    public void setNotifyChannel(String notifyChannel) { this.notifyChannel = notifyChannel; }
    public String getNotifyTargets() { return notifyTargets; }
    public void setNotifyTargets(String notifyTargets) { this.notifyTargets = notifyTargets; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
