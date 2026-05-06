package com.aidotnet.erp.wms.infrastructure.data;

import java.math.BigDecimal;
import java.time.Instant;

public class TransferOrderDO {
    private String transferId;
    private String tenantId;
    private String fromWarehouseId;
    private String toWarehouseId;
    private String status;
    private String remark;
    private Instant createdAt;
    private Instant updatedAt;

    public String getTransferId() { return transferId; }
    public void setTransferId(String transferId) { this.transferId = transferId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getFromWarehouseId() { return fromWarehouseId; }
    public void setFromWarehouseId(String fromWarehouseId) { this.fromWarehouseId = fromWarehouseId; }
    public String getToWarehouseId() { return toWarehouseId; }
    public void setToWarehouseId(String toWarehouseId) { this.toWarehouseId = toWarehouseId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
