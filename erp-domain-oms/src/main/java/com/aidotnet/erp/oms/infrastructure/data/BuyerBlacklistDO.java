package com.aidotnet.erp.oms.infrastructure.data;

import java.time.Instant;

public class BuyerBlacklistDO {

    private String entryId;
    private String tenantId;
    private String buyerName;
    private String reason;
    private Instant createdAt;

    public BuyerBlacklistDO() {}

    public String getEntryId() { return entryId; }
    public void setEntryId(String entryId) { this.entryId = entryId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
