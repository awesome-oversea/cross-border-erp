package com.aidotnet.erp.wms.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

@TableName("wms_inventory_movement")
public class InventoryMovementDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String movementId;
    private String tenantId;
    private String warehouseId;
    private String sellerSku;
    private String fromLocationId;
    private String toLocationId;
    private int quantity;
    private String type;
    private String referenceType;
    private String referenceId;
    private Instant movedAt;

    public InventoryMovementDO() {}

    public String getMovementId() { return movementId; }
    public void setMovementId(String movementId) { this.movementId = movementId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getWarehouseId() { return warehouseId; }
    public void setWarehouseId(String warehouseId) { this.warehouseId = warehouseId; }
    public String getSellerSku() { return sellerSku; }
    public void setSellerSku(String sellerSku) { this.sellerSku = sellerSku; }
    public String getFromLocationId() { return fromLocationId; }
    public void setFromLocationId(String fromLocationId) { this.fromLocationId = fromLocationId; }
    public String getToLocationId() { return toLocationId; }
    public void setToLocationId(String toLocationId) { this.toLocationId = toLocationId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getReferenceType() { return referenceType; }
    public void setReferenceType(String referenceType) { this.referenceType = referenceType; }
    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }
    public Instant getMovedAt() { return movedAt; }
    public void setMovedAt(Instant movedAt) { this.movedAt = movedAt; }
}
