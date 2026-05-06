package com.aidotnet.erp.bi.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * KPI模板数据对象
 * <p>
 * 描述: 对应bi_kpi_template表，用于存储KPI指标模板定义数据。
 *       KPI模板定义了指标的标准编码、分类、默认单位和目标公式，
 *       作为创建KPI目标和考核的标准化基础。
 * </p>
 * <p>
 * 业务规则:
 *   1. 模板编码(templateCode)在租户内唯一
 *   2. 新建模板默认启用(enabled=true)
 *   3. 模板创建后可被KPI目标引用，不可随意删除
 * </p>
 *
 * @author ERP系统
 * @see com.aidotnet.erp.bi.domain.KpiTemplate
 */
@TableName("bi_kpi_template")
public class KpiTemplateDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String templateId;

    private String tenantId;

    private String templateCode;

    private String templateName;

    private String category;

    private String defaultUnit;

    private String defaultTargetFormula;

    private String description;

    private boolean enabled;

    private Instant createdAt;

    private Instant updatedAt;

    public KpiTemplateDO() {}

    public String getTemplateId() { return templateId; }
    public void setTemplateId(String templateId) { this.templateId = templateId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }
    public String getTemplateName() { return templateName; }
    public void setTemplateName(String templateName) { this.templateName = templateName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDefaultUnit() { return defaultUnit; }
    public void setDefaultUnit(String defaultUnit) { this.defaultUnit = defaultUnit; }
    public String getDefaultTargetFormula() { return defaultTargetFormula; }
    public void setDefaultTargetFormula(String defaultTargetFormula) { this.defaultTargetFormula = defaultTargetFormula; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
