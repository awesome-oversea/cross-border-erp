package com.aidotnet.erp.fba.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

@TableName("fba_replenishment_plan")
public class ReplenishmentPlanDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String planId;
    private String tenantId;
    private String sellerSku;
    private String destinationFc;
    private Integer suggestedQuantity;
    private String sourceWarehouseId;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public ReplenishmentPlanDO() {}

    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getSellerSku() { return sellerSku; }
    public void setSellerSku(String sellerSku) { this.sellerSku = sellerSku; }
    public String getDestinationFc() { return destinationFc; }
    public void setDestinationFc(String destinationFc) { this.destinationFc = destinationFc; }
    public Integer getSuggestedQuantity() { return suggestedQuantity; }
    public void setSuggestedQuantity(Integer suggestedQuantity) { this.suggestedQuantity = suggestedQuantity; }
    public String getSourceWarehouseId() { return sourceWarehouseId; }
    public void setSourceWarehouseId(String sourceWarehouseId) { this.sourceWarehouseId = sourceWarehouseId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
