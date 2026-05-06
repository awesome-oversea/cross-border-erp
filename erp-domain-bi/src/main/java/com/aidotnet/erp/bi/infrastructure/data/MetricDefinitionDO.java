package com.aidotnet.erp.bi.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 指标口径定义数据对象
 * <p>
 * 描述: 对应bi_metric_definition表，用于存储指标口径的标准化定义。
 *       指标口径定义了指标的计算公式、单位、数据源和权限控制，
 *       确保不同业务场景下同一指标的计算标准一致。
 * </p>
 * <p>
 * 业务规则:
 *   1. 指标编码(metricCode)在租户内唯一
 *   2. 指标分类(category)支持: sales/profit/inventory/ads/crm/finance等
 *   3. 公式(formula)定义指标计算逻辑，支持简单表达式和复合计算
 *   4. 数据级别(dataLevel)控制指标数据可见范围
 *   5. 启用状态(enabled)控制指标是否参与计算和展示
 * </p>
 *
 * @author ERP系统
 * @see com.aidotnet.erp.bi.domain.MetricDefinition
 */
@TableName("bi_metric_definition")
public class MetricDefinitionDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String metricId;
    private String tenantId;
    private String metricCode;
    private String metricName;
    private String category;
    private String formula;
    private String unit;
    private String permissionCode;
    private String dataLevel;
    private String description;
    private Boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;

    public MetricDefinitionDO() {}

    public String getMetricId() { return metricId; }
    public void setMetricId(String metricId) { this.metricId = metricId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getMetricCode() { return metricCode; }
    public void setMetricCode(String metricCode) { this.metricCode = metricCode; }
    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getFormula() { return formula; }
    public void setFormula(String formula) { this.formula = formula; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getPermissionCode() { return permissionCode; }
    public void setPermissionCode(String permissionCode) { this.permissionCode = permissionCode; }
    public String getDataLevel() { return dataLevel; }
    public void setDataLevel(String dataLevel) { this.dataLevel = dataLevel; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
