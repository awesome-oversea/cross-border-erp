package com.aidotnet.erp.sys.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 模拟回放数据对象(SimulationReplayDO)
 * <p>
 * 描述: 规则模拟回放数据对象，对应sys_simulation_replay表。
 *       存储规则变更前的模拟回放结果，验证规则变更安全性。
 * </p>
 *
 * @author ERP系统
 */
@TableName("sys_simulation_replay")
public class SimulationReplayDO {
    @TableId(type = IdType.ASSIGN_ID)
    private String replayId;
    private String tenantId;
    private String ruleId;
    private Integer ruleVersion;
    private String ruleType;
    private String inputContext;
    private String outputResult;
    private Boolean passed;
    private String errorMessage;
    private Instant replayedAt;

    public SimulationReplayDO() {}

    public String getReplayId() { return replayId; }
    public void setReplayId(String replayId) { this.replayId = replayId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }
    public Integer getRuleVersion() { return ruleVersion; }
    public void setRuleVersion(Integer ruleVersion) { this.ruleVersion = ruleVersion; }
    public String getRuleType() { return ruleType; }
    public void setRuleType(String ruleType) { this.ruleType = ruleType; }
    public String getInputContext() { return inputContext; }
    public void setInputContext(String inputContext) { this.inputContext = inputContext; }
    public String getOutputResult() { return outputResult; }
    public void setOutputResult(String outputResult) { this.outputResult = outputResult; }
    public Boolean getPassed() { return passed; }
    public void setPassed(Boolean passed) { this.passed = passed; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    public Instant getReplayedAt() { return replayedAt; }
    public void setReplayedAt(Instant replayedAt) { this.replayedAt = replayedAt; }
}
