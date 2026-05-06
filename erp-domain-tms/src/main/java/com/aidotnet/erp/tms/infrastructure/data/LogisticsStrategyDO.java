package com.aidotnet.erp.tms.infrastructure.data;

import java.time.Instant;

public class LogisticsStrategyDO {
    private String strategyId;
    private String tenantId;
    private String strategyName;
    private String strategyType;
    private String originCountry;
    private String destinationCountry;
    private String preferredCarrier;
    private String rules;
    private boolean enabled;
    private int priority;
    private Instant createdAt;
    private Instant updatedAt;

    public String getStrategyId() { return strategyId; }
    public void setStrategyId(String strategyId) { this.strategyId = strategyId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getStrategyName() { return strategyName; }
    public void setStrategyName(String strategyName) { this.strategyName = strategyName; }
    public String getStrategyType() { return strategyType; }
    public void setStrategyType(String strategyType) { this.strategyType = strategyType; }
    public String getOriginCountry() { return originCountry; }
    public void setOriginCountry(String originCountry) { this.originCountry = originCountry; }
    public String getDestinationCountry() { return destinationCountry; }
    public void setDestinationCountry(String destinationCountry) { this.destinationCountry = destinationCountry; }
    public String getPreferredCarrier() { return preferredCarrier; }
    public void setPreferredCarrier(String preferredCarrier) { this.preferredCarrier = preferredCarrier; }
    public String getRules() { return rules; }
    public void setRules(String rules) { this.rules = rules; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
