package com.aidotnet.erp.scm.infrastructure.data;

import java.math.BigDecimal;
import java.time.Instant;

public class PurchaseTrackingDO {
    private String trackingId;
    private String tenantId;
    private String poId;
    private String lineId;
    private String sellerSku;
    private int orderedQuantity;
    private int receivedQuantity;
    private int pendingQuantity;
    private int damagedQuantity;
    private int returnedQuantity;
    private BigDecimal orderedUnitCost;
    private BigDecimal actualUnitCost;
    private String status;
    private Instant lastReceivedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public String getTrackingId() { return trackingId; }
    public void setTrackingId(String trackingId) { this.trackingId = trackingId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getPoId() { return poId; }
    public void setPoId(String poId) { this.poId = poId; }
    public String getLineId() { return lineId; }
    public void setLineId(String lineId) { this.lineId = lineId; }
    public String getSellerSku() { return sellerSku; }
    public void setSellerSku(String sellerSku) { this.sellerSku = sellerSku; }
    public int getOrderedQuantity() { return orderedQuantity; }
    public void setOrderedQuantity(int orderedQuantity) { this.orderedQuantity = orderedQuantity; }
    public int getReceivedQuantity() { return receivedQuantity; }
    public void setReceivedQuantity(int receivedQuantity) { this.receivedQuantity = receivedQuantity; }
    public int getPendingQuantity() { return pendingQuantity; }
    public void setPendingQuantity(int pendingQuantity) { this.pendingQuantity = pendingQuantity; }
    public int getDamagedQuantity() { return damagedQuantity; }
    public void setDamagedQuantity(int damagedQuantity) { this.damagedQuantity = damagedQuantity; }
    public int getReturnedQuantity() { return returnedQuantity; }
    public void setReturnedQuantity(int returnedQuantity) { this.returnedQuantity = returnedQuantity; }
    public BigDecimal getOrderedUnitCost() { return orderedUnitCost; }
    public void setOrderedUnitCost(BigDecimal orderedUnitCost) { this.orderedUnitCost = orderedUnitCost; }
    public BigDecimal getActualUnitCost() { return actualUnitCost; }
    public void setActualUnitCost(BigDecimal actualUnitCost) { this.actualUnitCost = actualUnitCost; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getLastReceivedAt() { return lastReceivedAt; }
    public void setLastReceivedAt(Instant lastReceivedAt) { this.lastReceivedAt = lastReceivedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
