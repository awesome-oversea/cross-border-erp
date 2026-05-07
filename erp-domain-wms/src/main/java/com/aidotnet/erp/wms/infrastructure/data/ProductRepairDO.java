package com.aidotnet.erp.wms.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

@TableName("wms_product_repair")
public class ProductRepairDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String repairId;
    private String tenantId;
    private String warehouseId;
    private String supplierId;
    private String sellerSku;
    private int outboundQuantity;
    private int inboundQuantity;
    private String reason;
    private String status;
    private String qcResult;
    private String processedBy;
    private String remark;
    private Instant completedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public String getRepairId() { return repairId; }
    public void setRepairId(String repairId) { this.repairId = repairId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getWarehouseId() { return warehouseId; }
    public void setWarehouseId(String warehouseId) { this.warehouseId = warehouseId; }
    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    public String getSellerSku() { return sellerSku; }
    public void setSellerSku(String sellerSku) { this.sellerSku = sellerSku; }
    public int getOutboundQuantity() { return outboundQuantity; }
    public void setOutboundQuantity(int outboundQuantity) { this.outboundQuantity = outboundQuantity; }
    public int getInboundQuantity() { return inboundQuantity; }
    public void setInboundQuantity(int inboundQuantity) { this.inboundQuantity = inboundQuantity; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getQcResult() { return qcResult; }
    public void setQcResult(String qcResult) { this.qcResult = qcResult; }
    public String getProcessedBy() { return processedBy; }
    public void setProcessedBy(String processedBy) { this.processedBy = processedBy; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
