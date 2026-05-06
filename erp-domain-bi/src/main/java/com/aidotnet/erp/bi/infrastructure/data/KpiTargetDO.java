package com.aidotnet.erp.bi.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * KPI目标数据对象
 * <p>
 * 描述: 对应bi_kpi_target表，用于存储KPI考核目标设定数据。
 *       KPI目标定义了某部门/角色在特定考核周期内的指标目标值、
 *       预警值、优秀值、权重和评分规则，是KPI考核的基础。
 * </p>
 * <p>
 * 业务规则:
 *   1. 同一kpiCode+department+period组合应唯一
 *   2. 目标值(targetValue)必须大于0
 *   3. 权重(weight)范围0-100
 *   4. applicableRoles以JSON数组格式存储
 *   5. 评分规则(scoringRule)支持LINEAR/THRESHOLD两种模式
 * </p>
 *
 * @author ERP系统
 * @see com.aidotnet.erp.bi.domain.KpiTarget
 */
@TableName("bi_kpi_target")
public class KpiTargetDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String targetId;

    private String tenantId;

    private String kpiCode;

    private String kpiName;

    private String department;

    private String role;

    private String period;

    private BigDecimal targetValue;

    private BigDecimal warningValue;

    private BigDecimal excellentValue;

    private String unit;

    private String metricCode;

    private String caliberId;

    private String applicableRoles;

    private String scoringRule;

    private BigDecimal weight;

    private boolean enabled;

    private Instant createdAt;

    private Instant updatedAt;

    public KpiTargetDO() {}

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getKpiCode() { return kpiCode; }
    public void setKpiCode(String kpiCode) { this.kpiCode = kpiCode; }
    public String getKpiName() { return kpiName; }
    public void setKpiName(String kpiName) { this.kpiName = kpiName; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public BigDecimal getTargetValue() { return targetValue; }
    public void setTargetValue(BigDecimal targetValue) { this.targetValue = targetValue; }
    public BigDecimal getWarningValue() { return warningValue; }
    public void setWarningValue(BigDecimal warningValue) { this.warningValue = warningValue; }
    public BigDecimal getExcellentValue() { return excellentValue; }
    public void setExcellentValue(BigDecimal excellentValue) { this.excellentValue = excellentValue; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getMetricCode() { return metricCode; }
    public void setMetricCode(String metricCode) { this.metricCode = metricCode; }
    public String getCaliberId() { return caliberId; }
    public void setCaliberId(String caliberId) { this.caliberId = caliberId; }
    public String getApplicableRoles() { return applicableRoles; }
    public void setApplicableRoles(String applicableRoles) { this.applicableRoles = applicableRoles; }
    public String getScoringRule() { return scoringRule; }
    public void setScoringRule(String scoringRule) { this.scoringRule = scoringRule; }
    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
