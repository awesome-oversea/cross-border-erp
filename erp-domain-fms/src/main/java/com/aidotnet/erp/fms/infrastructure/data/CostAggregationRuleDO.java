package com.aidotnet.erp.fms.infrastructure.data;

import java.math.BigDecimal;
import java.time.Instant;

public class CostAggregationRuleDO {
    private String ruleId;
    private String tenantId;
    private String ruleName;
    private String costSource;
    private String costCategory;
    private String allocationMethod;
    private String allocationBasis;
    private String targetDimension;
    private boolean enabled;
    private int priority;
    private Instant createdAt;
    private Instant updatedAt;

    public String getRuleId() { return ruleId; }
    public void setRuleId(String ruleId) { this.ruleId = ruleId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public String getCostSource() { return costSource; }
    public void setCostSource(String costSource) { this.costSource = costSource; }
    public String getCostCategory() { return costCategory; }
    public void setCostCategory(String costCategory) { this.costCategory = costCategory; }
    public String getAllocationMethod() { return allocationMethod; }
    public void setAllocationMethod(String allocationMethod) { this.allocationMethod = allocationMethod; }
    public String getAllocationBasis() { return allocationBasis; }
    public void setAllocationBasis(String allocationBasis) { this.allocationBasis = allocationBasis; }
    public String getTargetDimension() { return targetDimension; }
    public void setTargetDimension(String targetDimension) { this.targetDimension = targetDimension; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
