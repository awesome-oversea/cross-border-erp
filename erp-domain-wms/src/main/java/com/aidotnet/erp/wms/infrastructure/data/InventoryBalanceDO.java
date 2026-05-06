package com.aidotnet.erp.wms.infrastructure.data;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

@TableName("wms_inventory_balance")
public class InventoryBalanceDO {

    private String tenantId;
    private String warehouseId;
    private String sellerSku;
    private Integer onHand;
    private Integer reserved;
    private Integer inTransit;
    private Integer frozen;
    private Instant updatedAt;

    public InventoryBalanceDO() {
    }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getWarehouseId() { return warehouseId; }
    public void setWarehouseId(String warehouseId) { this.warehouseId = warehouseId; }
    public String getSellerSku() { return sellerSku; }
    public void setSellerSku(String sellerSku) { this.sellerSku = sellerSku; }
    public Integer getOnHand() { return onHand; }
    public void setOnHand(Integer onHand) { this.onHand = onHand; }
    public Integer getReserved() { return reserved; }
    public void setReserved(Integer reserved) { this.reserved = reserved; }
    public Integer getInTransit() { return inTransit; }
    public void setInTransit(Integer inTransit) { this.inTransit = inTransit; }
    public Integer getFrozen() { return frozen; }
    public void setFrozen(Integer frozen) { this.frozen = frozen; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
