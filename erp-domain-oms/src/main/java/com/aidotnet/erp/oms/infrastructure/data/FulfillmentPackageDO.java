package com.aidotnet.erp.oms.infrastructure.data;

import java.math.BigDecimal;
import java.time.Instant;

public class FulfillmentPackageDO {

    private String packageId;
    private String planId;
    private String warehouseId;
    private String warehouseCode;
    private String carrierId;
    private String carrierCode;
    private String carrierName;
    private String destinationCountry;
    private String serviceLevel;
    private String status;
    private Integer totalQuantity;
    private BigDecimal totalAmount;
    private BigDecimal estimatedShippingCost;
    private Integer estimatedDeliveryDays;
    private String note;
    private String shipmentId;
    private String trackingNo;
    private Instant shippedAt;
    private String platformSyncStatus;
    private Integer platformSyncAttempts;
    private String platformSyncError;
    private Instant platformSyncedAt;

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(String warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getWarehouseCode() {
        return warehouseCode;
    }

    public void setWarehouseCode(String warehouseCode) {
        this.warehouseCode = warehouseCode;
    }

    public String getCarrierId() {
        return carrierId;
    }

    public void setCarrierId(String carrierId) {
        this.carrierId = carrierId;
    }

    public String getCarrierCode() {
        return carrierCode;
    }

    public void setCarrierCode(String carrierCode) {
        this.carrierCode = carrierCode;
    }

    public String getCarrierName() {
        return carrierName;
    }

    public void setCarrierName(String carrierName) {
        this.carrierName = carrierName;
    }

    public String getDestinationCountry() {
        return destinationCountry;
    }

    public void setDestinationCountry(String destinationCountry) {
        this.destinationCountry = destinationCountry;
    }

    public String getServiceLevel() {
        return serviceLevel;
    }

    public void setServiceLevel(String serviceLevel) {
        this.serviceLevel = serviceLevel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getEstimatedShippingCost() {
        return estimatedShippingCost;
    }

    public void setEstimatedShippingCost(BigDecimal estimatedShippingCost) {
        this.estimatedShippingCost = estimatedShippingCost;
    }

    public Integer getEstimatedDeliveryDays() {
        return estimatedDeliveryDays;
    }

    public void setEstimatedDeliveryDays(Integer estimatedDeliveryDays) {
        this.estimatedDeliveryDays = estimatedDeliveryDays;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getShipmentId() {
        return shipmentId;
    }

    public void setShipmentId(String shipmentId) {
        this.shipmentId = shipmentId;
    }

    public String getTrackingNo() {
        return trackingNo;
    }

    public void setTrackingNo(String trackingNo) {
        this.trackingNo = trackingNo;
    }

    public Instant getShippedAt() {
        return shippedAt;
    }

    public void setShippedAt(Instant shippedAt) {
        this.shippedAt = shippedAt;
    }

    public String getPlatformSyncStatus() {
        return platformSyncStatus;
    }

    public void setPlatformSyncStatus(String platformSyncStatus) {
        this.platformSyncStatus = platformSyncStatus;
    }

    public Integer getPlatformSyncAttempts() {
        return platformSyncAttempts;
    }

    public void setPlatformSyncAttempts(Integer platformSyncAttempts) {
        this.platformSyncAttempts = platformSyncAttempts;
    }

    public String getPlatformSyncError() {
        return platformSyncError;
    }

    public void setPlatformSyncError(String platformSyncError) {
        this.platformSyncError = platformSyncError;
    }

    public Instant getPlatformSyncedAt() {
        return platformSyncedAt;
    }

    public void setPlatformSyncedAt(Instant platformSyncedAt) {
        this.platformSyncedAt = platformSyncedAt;
    }
}
