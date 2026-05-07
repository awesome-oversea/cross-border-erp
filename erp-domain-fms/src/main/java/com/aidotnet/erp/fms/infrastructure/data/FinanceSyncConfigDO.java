package com.aidotnet.erp.fms.infrastructure.data;

import java.time.Instant;

public class FinanceSyncConfigDO {
    private String configId;
    private String tenantId;
    private String financeSystem;
    private String apiUrl;
    private String apiKey;
    private String apiSecret;
    private String accountSet;
    private boolean enabled;
    private String mappingRulesJson;
    private Instant lastSyncAt;
    private Instant createdAt;
    private Instant updatedAt;

    public String getConfigId() { return configId; }
    public void setConfigId(String configId) { this.configId = configId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getFinanceSystem() { return financeSystem; }
    public void setFinanceSystem(String financeSystem) { this.financeSystem = financeSystem; }
    public String getApiUrl() { return apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getApiSecret() { return apiSecret; }
    public void setApiSecret(String apiSecret) { this.apiSecret = apiSecret; }
    public String getAccountSet() { return accountSet; }
    public void setAccountSet(String accountSet) { this.accountSet = accountSet; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getMappingRulesJson() { return mappingRulesJson; }
    public void setMappingRulesJson(String mappingRulesJson) { this.mappingRulesJson = mappingRulesJson; }
    public Instant getLastSyncAt() { return lastSyncAt; }
    public void setLastSyncAt(Instant lastSyncAt) { this.lastSyncAt = lastSyncAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
