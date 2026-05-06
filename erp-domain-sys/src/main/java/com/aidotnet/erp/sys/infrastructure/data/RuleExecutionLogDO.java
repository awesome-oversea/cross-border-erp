package com.aidotnet.erp.sys.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 规则执行日志数据对象(RuleExecutionLogDO)
 * <p>
 * 描述: 规则执行日志数据对象，对应sys_rule_execution_log表。
 *       记录每次规则执行的输入、输出、耗时和结果，用于审计和问题排查。
 * </p>
 *
 * @author ERP系统
 */
@TableName("sys_rule_execution_log")
public class RuleExecutionLogDO {
    @TableId(type = IdType.ASSIGN_ID)
    private String logId;
    private String tenantId;
    private String ruleId;
    private Integer ruleVersion;
    private String ruleType;
    private String businessType;
    private String referenceId;
    private String inputContext;
    private String outputResult;
    private Boolean success;
    private String errorMessage;
    private Long executionTimeMs;
    private Instant executedAt;

    public RuleExecutionLogDO() {}

    public String getLogId() { return logId; }
    public void setLogId(String logId) { this.logId = logId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }
    public Integer getRuleVersion() { return ruleVersion; }
    public void setRuleVersion(Integer ruleVersion) { this.ruleVersion = ruleVersion; }
    public String getRuleType() { return ruleType; }
    public void setRuleType(String ruleType) { this.ruleType = ruleType; }
    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
    public String getInputContext() { return inputContext; }
    public void setInputContext(String inputContext) { this.inputContext = inputContext; }
    public String getOutputResult() { return outputResult; }
    public void setOutputResult(String outputResult) { this.outputResult = outputResult; }
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(Long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
    public Instant getExecutedAt() { return executedAt; }
    public void setExecutedAt(Instant executedAt) { this.executedAt = executedAt; }
}
