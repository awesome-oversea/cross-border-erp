package com.aidotnet.erp.bi.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * KPI指标数据对象
 * <p>
 * 描述: 对应bi_kpi_metric表，用于存储KPI指标的测量记录数据。
 *       每次KPI测量生成一条记录，包含当前值、目标值和自动计算的达成状态。
 *       支持按分类和时间维度进行KPI趋势分析和异常检测。
 * </p>
 * <p>
 * 业务规则:
 *   1. KPI编码(kpiCode)标识同一指标的不同时间点测量值
 *   2. 达成状态(status)自动计算: ON_TRACK(≥90%) / AT_RISK(≥70%) / OFF_TRACK(<70%) / NOT_STARTED
 *   3. 每次记录仅插入(insertOnly)，不更新历史数据，保留完整时间序列
 *   4. 按测量时间(measuredAt)倒序排列，最新记录排在前面
 * </p>
 *
 * @author ERP系统
 * @see com.aidotnet.erp.bi.domain.KpiMetric
 */
@TableName("bi_kpi_metric")
public class KpiMetricDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String kpiId;
    private String tenantId;
    private String kpiCode;
    private String kpiName;
    private String category;
    private BigDecimal value;
    private BigDecimal targetValue;
    private String unit;
    private String status;
    private Instant measuredAt;

    public KpiMetricDO() {}

    public String getKpiId() { return kpiId; }
    public void setKpiId(String kpiId) { this.kpiId = kpiId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getKpiCode() { return kpiCode; }
    public void setKpiCode(String kpiCode) { this.kpiCode = kpiCode; }
    public String getKpiName() { return kpiName; }
    public void setKpiName(String kpiName) { this.kpiName = kpiName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }
    public BigDecimal getTargetValue() { return targetValue; }
    public void setTargetValue(BigDecimal targetValue) { this.targetValue = targetValue; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getMeasuredAt() { return measuredAt; }
    public void setMeasuredAt(Instant measuredAt) { this.measuredAt = measuredAt; }
}
