package com.aidotnet.erp.bi.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 分析维度数据对象
 * <p>
 * 描述: 对应bi_dimension表，用于存储分析维度的定义数据。
 *       维度是数据分析的切面，如时间、地区、品类、仓库等，
 *       支持多维度交叉分析，是BI域报表和可视化的基础配置。
 * </p>
 * <p>
 * 业务规则:
 *   1. 维度编码(dimensionCode)在租户内唯一
 *   2. 维度类型(dimensionType)支持: time/region/category/warehouse/channel等
 *   3. 来源字段(sourceField)指定维度值的数据来源字段
 *   4. 禁用的维度不参与报表和可视化分析
 * </p>
 *
 * @author ERP系统
 * @see com.aidotnet.erp.bi.domain.Dimension
 */
@TableName("bi_dimension")
public class DimensionDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String dimensionId;
    private String tenantId;
    private String dimensionCode;
    private String dimensionName;
    private String dimensionType;
    private String sourceField;
    private String description;
    private Boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;

    public DimensionDO() {}

    public String getDimensionId() { return dimensionId; }
    public void setDimensionId(String dimensionId) { this.dimensionId = dimensionId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getDimensionCode() { return dimensionCode; }
    public void setDimensionCode(String dimensionCode) { this.dimensionCode = dimensionCode; }
    public String getDimensionName() { return dimensionName; }
    public void setDimensionName(String dimensionName) { this.dimensionName = dimensionName; }
    public String getDimensionType() { return dimensionType; }
    public void setDimensionType(String dimensionType) { this.dimensionType = dimensionType; }
    public String getSourceField() { return sourceField; }
    public void setSourceField(String sourceField) { this.sourceField = sourceField; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
