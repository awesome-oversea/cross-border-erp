package com.aidotnet.erp.wms.infrastructure.data;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

@TableName("wms_inventory_transaction")
public class InventoryTransactionDO {

    private String transactionId;
    private String tenantId;
    private String warehouseId;
    private String sellerSku;
    private String transactionType;
    private Integer quantity;
    private Integer beforeOnHand;
    private Integer beforeReserved;
    private Integer afterOnHand;
    private Integer afterReserved;
    private String referenceType;
    private String referenceId;
    private String remark;
    private Instant createdAt;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(String warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getSellerSku() {
        return sellerSku;
    }

    public void setSellerSku(String sellerSku) {
        this.sellerSku = sellerSku;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getBeforeOnHand() {
        return beforeOnHand;
    }

    public void setBeforeOnHand(Integer beforeOnHand) {
        this.beforeOnHand = beforeOnHand;
    }

    public Integer getBeforeReserved() {
        return beforeReserved;
    }

    public void setBeforeReserved(Integer beforeReserved) {
        this.beforeReserved = beforeReserved;
    }

    public Integer getAfterOnHand() {
        return afterOnHand;
    }

    public void setAfterOnHand(Integer afterOnHand) {
        this.afterOnHand = afterOnHand;
    }

    public Integer getAfterReserved() {
        return afterReserved;
    }

    public void setAfterReserved(Integer afterReserved) {
        this.afterReserved = afterReserved;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
