package com.aidotnet.erp.scm.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.Instant;

@TableName("scm_purchase_order")
public class PurchaseOrderDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String poId;
    private String tenantId;
    private String supplierId;
    private String poNumber;
    private String currency;
    private BigDecimal totalAmount;
    private String status;
    private String paymentTerms;
    private String shippingTerms;
    private String purchaseType;
    private Instant expectedDeliveryDate;
    private Instant actualDeliveryDate;
    private String notes;
    private String createdBy;
    private String approvedBy;
    private Instant createdAt;
    private Instant updatedAt;

    public PurchaseOrderDO() {}

    public String getPoId() { return poId; }
    public void setPoId(String poId) { this.poId = poId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }
    public String getPoNumber() { return poNumber; }
    public void setPoNumber(String poNumber) { this.poNumber = poNumber; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }
    public String getShippingTerms() { return shippingTerms; }
    public void setShippingTerms(String shippingTerms) { this.shippingTerms = shippingTerms; }
    public String getPurchaseType() { return purchaseType; }
    public void setPurchaseType(String purchaseType) { this.purchaseType = purchaseType; }
    public Instant getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public void setExpectedDeliveryDate(Instant expectedDeliveryDate) { this.expectedDeliveryDate = expectedDeliveryDate; }
    public Instant getActualDeliveryDate() { return actualDeliveryDate; }
    public void setActualDeliveryDate(Instant actualDeliveryDate) { this.actualDeliveryDate = actualDeliveryDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
