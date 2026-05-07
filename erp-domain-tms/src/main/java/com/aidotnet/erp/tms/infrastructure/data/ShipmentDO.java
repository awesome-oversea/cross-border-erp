package com.aidotnet.erp.tms.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.Instant;

@TableName("tms_shipment")
public class ShipmentDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String shipmentId;
    private String tenantId;
    private String orderId;
    private String warehouseId;
    private String carrierId;
    private String shippingMethodId;
    private String trackingNo;
    private String destinationCountry;
    private BigDecimal weight;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private Instant estimatedDelivery;
    private Instant actualDelivery;
    private BigDecimal estimatedFreight;
    private String estimatedFreightCurrency;
    private BigDecimal estimatedChargeableWeight;
    private Instant estimatedAt;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public ShipmentDO() {}

    public String getShipmentId() { return shipmentId; }
    public void setShipmentId(String shipmentId) { this.shipmentId = shipmentId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getWarehouseId() { return warehouseId; }
    public void setWarehouseId(String warehouseId) { this.warehouseId = warehouseId; }
    public String getCarrierId() { return carrierId; }
    public void setCarrierId(String carrierId) { this.carrierId = carrierId; }
    public String getShippingMethodId() { return shippingMethodId; }
    public void setShippingMethodId(String shippingMethodId) { this.shippingMethodId = shippingMethodId; }
    public String getTrackingNo() { return trackingNo; }
    public void setTrackingNo(String trackingNo) { this.trackingNo = trackingNo; }
    public String getDestinationCountry() { return destinationCountry; }
    public void setDestinationCountry(String destinationCountry) { this.destinationCountry = destinationCountry; }
    public BigDecimal getWeight() { return weight; }
    public void setWeight(BigDecimal weight) { this.weight = weight; }
    public BigDecimal getLength() { return length; }
    public void setLength(BigDecimal length) { this.length = length; }
    public BigDecimal getWidth() { return width; }
    public void setWidth(BigDecimal width) { this.width = width; }
    public BigDecimal getHeight() { return height; }
    public void setHeight(BigDecimal height) { this.height = height; }
    public Instant getEstimatedDelivery() { return estimatedDelivery; }
    public void setEstimatedDelivery(Instant estimatedDelivery) { this.estimatedDelivery = estimatedDelivery; }
    public Instant getActualDelivery() { return actualDelivery; }
    public void setActualDelivery(Instant actualDelivery) { this.actualDelivery = actualDelivery; }
    public BigDecimal getEstimatedFreight() { return estimatedFreight; }
    public void setEstimatedFreight(BigDecimal estimatedFreight) { this.estimatedFreight = estimatedFreight; }
    public String getEstimatedFreightCurrency() { return estimatedFreightCurrency; }
    public void setEstimatedFreightCurrency(String estimatedFreightCurrency) { this.estimatedFreightCurrency = estimatedFreightCurrency; }
    public BigDecimal getEstimatedChargeableWeight() { return estimatedChargeableWeight; }
    public void setEstimatedChargeableWeight(BigDecimal estimatedChargeableWeight) { this.estimatedChargeableWeight = estimatedChargeableWeight; }
    public Instant getEstimatedAt() { return estimatedAt; }
    public void setEstimatedAt(Instant estimatedAt) { this.estimatedAt = estimatedAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
