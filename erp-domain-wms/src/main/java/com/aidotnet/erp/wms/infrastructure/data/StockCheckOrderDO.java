package com.aidotnet.erp.wms.infrastructure.data;

import java.time.Instant;

public class StockCheckOrderDO {
    private String checkOrderId;
    private String tenantId;
    private String warehouseId;
    private String checkType;
    private String status;
    private String checkedBy;
    private String remark;
    private Instant createdAt;
    private Instant completedAt;

    public String getCheckOrderId() { return checkOrderId; }
    public void setCheckOrderId(String checkOrderId) { this.checkOrderId = checkOrderId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getWarehouseId() { return warehouseId; }
    public void setWarehouseId(String warehouseId) { this.warehouseId = warehouseId; }
    public String getCheckType() { return checkType; }
    public void setCheckType(String checkType) { this.checkType = checkType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCheckedBy() { return checkedBy; }
    public void setCheckedBy(String checkedBy) { this.checkedBy = checkedBy; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
