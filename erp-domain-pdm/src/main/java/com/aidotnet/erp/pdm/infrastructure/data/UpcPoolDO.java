package com.aidotnet.erp.pdm.infrastructure.data;

import java.time.Instant;

public class UpcPoolDO {
    private String poolId;
    private String tenantId;
    private String upcCode;
    private String status;
    private String assignedSkuId;
    private Instant assignedAt;
    private Instant createdAt;

    public String getPoolId() { return poolId; }
    public void setPoolId(String poolId) { this.poolId = poolId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getUpcCode() { return upcCode; }
    public void setUpcCode(String upcCode) { this.upcCode = upcCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAssignedSkuId() { return assignedSkuId; }
    public void setAssignedSkuId(String assignedSkuId) { this.assignedSkuId = assignedSkuId; }
    public Instant getAssignedAt() { return assignedAt; }
    public void setAssignedAt(Instant assignedAt) { this.assignedAt = assignedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
