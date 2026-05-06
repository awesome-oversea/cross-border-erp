package com.aidotnet.erp.fba.infrastructure.data;

import java.time.Instant;

public class RemovalOrderDO {
    private String removalId;
    private String tenantId;
    private String fbaSku;
    private int quantity;
    private String removalType;
    private String status;
    private String returnAddressId;
    private String reason;
    private Instant createdAt;
    private Instant updatedAt;

    public String getRemovalId() { return removalId; }
    public void setRemovalId(String removalId) { this.removalId = removalId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getFbaSku() { return fbaSku; }
    public void setFbaSku(String fbaSku) { this.fbaSku = fbaSku; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getRemovalType() { return removalType; }
    public void setRemovalType(String removalType) { this.removalType = removalType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getReturnAddressId() { return returnAddressId; }
    public void setReturnAddressId(String returnAddressId) { this.returnAddressId = returnAddressId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
