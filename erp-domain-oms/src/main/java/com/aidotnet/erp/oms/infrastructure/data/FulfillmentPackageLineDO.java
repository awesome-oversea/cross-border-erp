package com.aidotnet.erp.oms.infrastructure.data;

import java.math.BigDecimal;

public class FulfillmentPackageLineDO {

    private String packageLineId;
    private String packageId;
    private String orderLineId;
    private String sellerSku;
    private String title;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineAmount;

    public String getPackageLineId() {
        return packageLineId;
    }

    public void setPackageLineId(String packageLineId) {
        this.packageLineId = packageLineId;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getOrderLineId() {
        return orderLineId;
    }

    public void setOrderLineId(String orderLineId) {
        this.orderLineId = orderLineId;
    }

    public String getSellerSku() {
        return sellerSku;
    }

    public void setSellerSku(String sellerSku) {
        this.sellerSku = sellerSku;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getLineAmount() {
        return lineAmount;
    }

    public void setLineAmount(BigDecimal lineAmount) {
        this.lineAmount = lineAmount;
    }
}
