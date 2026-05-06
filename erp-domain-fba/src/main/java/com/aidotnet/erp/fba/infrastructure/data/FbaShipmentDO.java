package com.aidotnet.erp.fba.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.Instant;

@TableName("fba_shipment")
public class FbaShipmentDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String fbaShipmentId;
    private String tenantId;
    private String amazonShipmentId;
    private String destinationFc;
    private String planId;
    private String carrier;
    private String trackingNo;
    private Integer plannedQuantity;
    private Integer receivedQuantity;
    private Integer cartonCount;
    private BigDecimal totalWeight;
    private String status;
    private Instant packedAt;
    private Instant shippedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public FbaShipmentDO() {}

    public String getFbaShipmentId() { return fbaShipmentId; }
    public void setFbaShipmentId(String fbaShipmentId) { this.fbaShipmentId = fbaShipmentId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getAmazonShipmentId() { return amazonShipmentId; }
    public void setAmazonShipmentId(String amazonShipmentId) { this.amazonShipmentId = amazonShipmentId; }
    public String getDestinationFc() { return destinationFc; }
    public void setDestinationFc(String destinationFc) { this.destinationFc = destinationFc; }
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    public String getCarrier() { return carrier; }
    public void setCarrier(String carrier) { this.carrier = carrier; }
    public String getTrackingNo() { return trackingNo; }
    public void setTrackingNo(String trackingNo) { this.trackingNo = trackingNo; }
    public Integer getPlannedQuantity() { return plannedQuantity; }
    public void setPlannedQuantity(Integer plannedQuantity) { this.plannedQuantity = plannedQuantity; }
    public Integer getReceivedQuantity() { return receivedQuantity; }
    public void setReceivedQuantity(Integer receivedQuantity) { this.receivedQuantity = receivedQuantity; }
    public Integer getCartonCount() { return cartonCount; }
    public void setCartonCount(Integer cartonCount) { this.cartonCount = cartonCount; }
    public BigDecimal getTotalWeight() { return totalWeight; }
    public void setTotalWeight(BigDecimal totalWeight) { this.totalWeight = totalWeight; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getPackedAt() { return packedAt; }
    public void setPackedAt(Instant packedAt) { this.packedAt = packedAt; }
    public Instant getShippedAt() { return shippedAt; }
    public void setShippedAt(Instant shippedAt) { this.shippedAt = shippedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
