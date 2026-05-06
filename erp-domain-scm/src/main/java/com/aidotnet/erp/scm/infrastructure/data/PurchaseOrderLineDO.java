package com.aidotnet.erp.scm.infrastructure.data;

import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.Instant;

@TableName("scm_purchase_order_line")
public class PurchaseOrderLineDO {

    private String poId;
    private String lineId;
    private String productId;
    private String sellerSku;
    private Integer quantity;
    private Integer receivedQuantity;
    private BigDecimal unitCost;
    private BigDecimal totalPrice;
    private Instant expectedDate;

    public PurchaseOrderLineDO() {}

    public String getPoId() { return poId; }
    public void setPoId(String poId) { this.poId = poId; }
    public String getLineId() { return lineId; }
    public void setLineId(String lineId) { this.lineId = lineId; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getSellerSku() { return sellerSku; }
    public void setSellerSku(String sellerSku) { this.sellerSku = sellerSku; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Integer getReceivedQuantity() { return receivedQuantity; }
    public void setReceivedQuantity(Integer receivedQuantity) { this.receivedQuantity = receivedQuantity; }
    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    public Instant getExpectedDate() { return expectedDate; }
    public void setExpectedDate(Instant expectedDate) { this.expectedDate = expectedDate; }
}
