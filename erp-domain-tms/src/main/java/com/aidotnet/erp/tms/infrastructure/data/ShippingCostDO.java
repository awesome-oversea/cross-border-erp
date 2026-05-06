package com.aidotnet.erp.tms.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.Instant;

@TableName("tms_shipping_cost")
public class ShippingCostDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String costId;
    private String tenantId;
    private String shipmentId;
    private String carrierId;
    private BigDecimal freightCost;
    private BigDecimal fuelSurcharge;
    private BigDecimal otherFees;
    private BigDecimal totalCost;
    private String currency;
    private Instant createdAt;

    public ShippingCostDO() {}

    public String getCostId() { return costId; }
    public void setCostId(String costId) { this.costId = costId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getShipmentId() { return shipmentId; }
    public void setShipmentId(String shipmentId) { this.shipmentId = shipmentId; }
    public String getCarrierId() { return carrierId; }
    public void setCarrierId(String carrierId) { this.carrierId = carrierId; }
    public BigDecimal getFreightCost() { return freightCost; }
    public void setFreightCost(BigDecimal freightCost) { this.freightCost = freightCost; }
    public BigDecimal getFuelSurcharge() { return fuelSurcharge; }
    public void setFuelSurcharge(BigDecimal fuelSurcharge) { this.fuelSurcharge = fuelSurcharge; }
    public BigDecimal getOtherFees() { return otherFees; }
    public void setOtherFees(BigDecimal otherFees) { this.otherFees = otherFees; }
    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
