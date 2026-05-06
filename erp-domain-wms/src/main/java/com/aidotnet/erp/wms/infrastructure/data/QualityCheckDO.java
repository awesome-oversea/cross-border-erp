package com.aidotnet.erp.wms.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

@TableName("wms_quality_check")
public class QualityCheckDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String checkId;
    private String tenantId;
    private String warehouseId;
    private String inboundOrderId;
    private String sellerSku;
    private int sampleQuantity;
    private int passQuantity;
    private int failQuantity;
    private String result;
    private String inspector;
    private String remark;
    private Instant checkedAt;
    private Instant createdAt;

    public QualityCheckDO() {}

    public String getCheckId() { return checkId; }
    public void setCheckId(String checkId) { this.checkId = checkId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getWarehouseId() { return warehouseId; }
    public void setWarehouseId(String warehouseId) { this.warehouseId = warehouseId; }
    public String getInboundOrderId() { return inboundOrderId; }
    public void setInboundOrderId(String inboundOrderId) { this.inboundOrderId = inboundOrderId; }
    public String getSellerSku() { return sellerSku; }
    public void setSellerSku(String sellerSku) { this.sellerSku = sellerSku; }
    public int getSampleQuantity() { return sampleQuantity; }
    public void setSampleQuantity(int sampleQuantity) { this.sampleQuantity = sampleQuantity; }
    public int getPassQuantity() { return passQuantity; }
    public void setPassQuantity(int passQuantity) { this.passQuantity = passQuantity; }
    public int getFailQuantity() { return failQuantity; }
    public void setFailQuantity(int failQuantity) { this.failQuantity = failQuantity; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getInspector() { return inspector; }
    public void setInspector(String inspector) { this.inspector = inspector; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public Instant getCheckedAt() { return checkedAt; }
    public void setCheckedAt(Instant checkedAt) { this.checkedAt = checkedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
