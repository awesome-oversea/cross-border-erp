package com.aidotnet.erp.dashboard.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * 仪表盘指标数据对象
 * <p>
 * 描述: 对应dashboard_metric表，用于存储仪表盘展示的实时指标数据。
 *       指标数据来源于各业务域的聚合计算结果，为AI看板提供数据驱动展示。
 * </p>
 * <p>
 * 业务规则:
 *   1. 指标编码(metricCode)在同一租户下唯一，由UNIQUE约束保证
 *   2. 指标值(metricValue)采用BigDecimal(18,4)保证金额和比率精度
 *   3. 指标更新时自动刷新updatedAt时间戳
 *   4. 典型指标编码: DAILY_SALES(日销售额)、ORDER_COUNT(订单量)、
 *      INVENTORY_TURNOVER(库存周转率)、ADS_ROAS(广告投入产出比)
 * </p>
 *
 * @author ERP系统
 * @see com.aidotnet.erp.dashboard.domain.DashboardMetric
 */
@TableName("dashboard_metric")
public class DashboardMetricDO {

    /** 指标唯一标识，UUID格式，主键 */
    @TableId(type = IdType.ASSIGN_ID)
    private String metricId;

    /** 租户ID，实现多租户数据隔离 */
    private String tenantId;

    /** 指标编码，租户内唯一，如 DAILY_SALES、ORDER_COUNT */
    private String metricCode;

    /** 指标名称，用于前端展示，如 "日销售额"、"订单量" */
    private String metricName;

    /** 指标值，BigDecimal保证金额和比率精度 */
    private BigDecimal metricValue;

    /** 指标单位，如 CNY、USD、%、件 */
    private String unit;

    /** 最后更新时间，UTC时区 */
    private Instant updatedAt;

    public DashboardMetricDO() {}

    public String getMetricId() { return metricId; }
    public void setMetricId(String metricId) { this.metricId = metricId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getMetricCode() { return metricCode; }
    public void setMetricCode(String metricCode) { this.metricCode = metricCode; }
    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }
    public BigDecimal getMetricValue() { return metricValue; }
    public void setMetricValue(BigDecimal metricValue) { this.metricValue = metricValue; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
