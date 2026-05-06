package com.aidotnet.erp.tms.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

@TableName("tms_shipping_batch")
public class ShippingBatchDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String batchId;
    private String tenantId;
    private String carrierId;
    private String shipmentIds;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public ShippingBatchDO() {}

    public String getBatchId() { return batchId; }
    public void setBatchId(String batchId) { this.batchId = batchId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getCarrierId() { return carrierId; }
    public void setCarrierId(String carrierId) { this.carrierId = carrierId; }
    public String getShipmentIds() { return shipmentIds; }
    public void setShipmentIds(String shipmentIds) { this.shipmentIds = shipmentIds; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
