package com.aidotnet.erp.fms.infrastructure.data;

import java.time.Instant;

public class ExternalFinanceVoucherDO {
    private String voucherId;
    private String tenantId;
    private String erpVoucherId;
    private String financeSystem;
    private String voucherType;
    private String voucherNumber;
    private String erpReferenceType;
    private String erpReferenceId;
    private String voucherDataJson;
    private String syncStatus;
    private String syncError;
    private Instant syncedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public String getVoucherId() { return voucherId; }
    public void setVoucherId(String voucherId) { this.voucherId = voucherId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getErpVoucherId() { return erpVoucherId; }
    public void setErpVoucherId(String erpVoucherId) { this.erpVoucherId = erpVoucherId; }
    public String getFinanceSystem() { return financeSystem; }
    public void setFinanceSystem(String financeSystem) { this.financeSystem = financeSystem; }
    public String getVoucherType() { return voucherType; }
    public void setVoucherType(String voucherType) { this.voucherType = voucherType; }
    public String getVoucherNumber() { return voucherNumber; }
    public void setVoucherNumber(String voucherNumber) { this.voucherNumber = voucherNumber; }
    public String getErpReferenceType() { return erpReferenceType; }
    public void setErpReferenceType(String erpReferenceType) { this.erpReferenceType = erpReferenceType; }
    public String getErpReferenceId() { return erpReferenceId; }
    public void setErpReferenceId(String erpReferenceId) { this.erpReferenceId = erpReferenceId; }
    public String getVoucherDataJson() { return voucherDataJson; }
    public void setVoucherDataJson(String voucherDataJson) { this.voucherDataJson = voucherDataJson; }
    public String getSyncStatus() { return syncStatus; }
    public void setSyncStatus(String syncStatus) { this.syncStatus = syncStatus; }
    public String getSyncError() { return syncError; }
    public void setSyncError(String syncError) { this.syncError = syncError; }
    public Instant getSyncedAt() { return syncedAt; }
    public void setSyncedAt(Instant syncedAt) { this.syncedAt = syncedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
