package com.aidotnet.erp.pdm.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

@TableName("pdm_quality_standard")
public class QualityStandardDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String standardId;
    private String tenantId;
    private String categoryId;
    private String name;
    private String description;
    private String inspectionItems;
    private String acceptanceCriteria;
    private boolean enabled;
    private Instant createdAt;
    private Instant updatedAt;

    public QualityStandardDO() {}

    public String getStandardId() { return standardId; }
    public void setStandardId(String standardId) { this.standardId = standardId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getInspectionItems() { return inspectionItems; }
    public void setInspectionItems(String inspectionItems) { this.inspectionItems = inspectionItems; }
    public String getAcceptanceCriteria() { return acceptanceCriteria; }
    public void setAcceptanceCriteria(String acceptanceCriteria) { this.acceptanceCriteria = acceptanceCriteria; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
