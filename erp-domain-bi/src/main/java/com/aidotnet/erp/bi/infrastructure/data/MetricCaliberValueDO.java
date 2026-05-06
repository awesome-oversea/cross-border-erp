package com.aidotnet.erp.bi.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * 指标口径计算值数据对象
 * <p>
 * 描述: 对应bi_metric_caliber_value表，用于存储按口径计算后的指标值。
 *       每条记录代表某个指标口径在特定维度下的计算结果，
 *       支持按维度键值对进行细粒度的指标追踪。
 * </p>
 * <p>
 * 业务规则:
 *   1. 计算值由口径计算引擎生成，不可手动修改
 *   2. 同一口径+维度组合可存在多条历史计算记录
 *   3. 计算值保留4位小数精度
 * </p>
 *
 * @author ERP系统
 * @see com.aidotnet.erp.bi.domain.MetricCaliberValue
 */
@TableName("bi_metric_caliber_value")
public class MetricCaliberValueDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String valueId;

    private String tenantId;

    private String caliberId;

    private String metricCode;

    private BigDecimal calculatedValue;

    private String dimensionKey;

    private String dimensionValue;

    private Instant calculatedAt;

    public MetricCaliberValueDO() {}

    public String getValueId() { return valueId; }
    public void setValueId(String valueId) { this.valueId = valueId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getCaliberId() { return caliberId; }
    public void setCaliberId(String caliberId) { this.caliberId = caliberId; }
    public String getMetricCode() { return metricCode; }
    public void setMetricCode(String metricCode) { this.metricCode = metricCode; }
    public BigDecimal getCalculatedValue() { return calculatedValue; }
    public void setCalculatedValue(BigDecimal calculatedValue) { this.calculatedValue = calculatedValue; }
    public String getDimensionKey() { return dimensionKey; }
    public void setDimensionKey(String dimensionKey) { this.dimensionKey = dimensionKey; }
    public String getDimensionValue() { return dimensionValue; }
    public void setDimensionValue(String dimensionValue) { this.dimensionValue = dimensionValue; }
    public Instant getCalculatedAt() { return calculatedAt; }
    public void setCalculatedAt(Instant calculatedAt) { this.calculatedAt = calculatedAt; }
}
