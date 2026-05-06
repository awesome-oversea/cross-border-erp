package com.aidotnet.erp.bi.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 指标口径数据对象
 * <p>
 * 描述: 对应bi_metric_caliber表，用于存储指标口径定义数据。
 *       指标口径定义了指标的计算方式(直接值/比率/公式)、公式描述、
 *       分子分母指标、数据源、计算范围、维度和排除条件等，
 *       确保全公司指标计算标准统一。
 * </p>
 * <p>
 * 业务规则:
 *   1. 指标编码(metricCode)在租户内唯一
 *   2. 口径类型(caliberType)必须为DIRECT/RATIO/FORMULA之一
 *   3. RATIO类型必须指定分子和分母指标
 *   4. FORMULA类型必须指定计算公式
 *   5. 口径更新时版本号自动递增
 *   6. dimensions和excludeConditions以JSON数组格式存储
 * </p>
 *
 * @author ERP系统
 * @see com.aidotnet.erp.bi.domain.MetricCaliber
 */
@TableName("bi_metric_caliber")
public class MetricCaliberDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String caliberId;

    private String tenantId;

    private String metricCode;

    private String metricName;

    private String category;

    private String caliberType;

    private String formula;

    private String formulaDescription;

    private String numeratorMetric;

    private String denominatorMetric;

    private String unit;

    private String dataSource;

    private String calculationScope;

    private String dimensions;

    private String excludeConditions;

    private String permissionCode;

    private String dataLevel;

    private boolean enabled;

    private String version;

    private String description;

    private Instant createdAt;

    private Instant updatedAt;

    public MetricCaliberDO() {}

    public String getCaliberId() { return caliberId; }
    public void setCaliberId(String caliberId) { this.caliberId = caliberId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getMetricCode() { return metricCode; }
    public void setMetricCode(String metricCode) { this.metricCode = metricCode; }
    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getCaliberType() { return caliberType; }
    public void setCaliberType(String caliberType) { this.caliberType = caliberType; }
    public String getFormula() { return formula; }
    public void setFormula(String formula) { this.formula = formula; }
    public String getFormulaDescription() { return formulaDescription; }
    public void setFormulaDescription(String formulaDescription) { this.formulaDescription = formulaDescription; }
    public String getNumeratorMetric() { return numeratorMetric; }
    public void setNumeratorMetric(String numeratorMetric) { this.numeratorMetric = numeratorMetric; }
    public String getDenominatorMetric() { return denominatorMetric; }
    public void setDenominatorMetric(String denominatorMetric) { this.denominatorMetric = denominatorMetric; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }
    public String getCalculationScope() { return calculationScope; }
    public void setCalculationScope(String calculationScope) { this.calculationScope = calculationScope; }
    public String getDimensions() { return dimensions; }
    public void setDimensions(String dimensions) { this.dimensions = dimensions; }
    public String getExcludeConditions() { return excludeConditions; }
    public void setExcludeConditions(String excludeConditions) { this.excludeConditions = excludeConditions; }
    public String getPermissionCode() { return permissionCode; }
    public void setPermissionCode(String permissionCode) { this.permissionCode = permissionCode; }
    public String getDataLevel() { return dataLevel; }
    public void setDataLevel(String dataLevel) { this.dataLevel = dataLevel; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
