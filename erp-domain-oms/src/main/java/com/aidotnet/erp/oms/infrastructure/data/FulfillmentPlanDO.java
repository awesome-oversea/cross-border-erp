package com.aidotnet.erp.oms.infrastructure.data;

import java.math.BigDecimal;
import java.time.Instant;

public class FulfillmentPlanDO {

    private String planId;
    private String tenantId;
    private String orderId;
    private String status;
    private Boolean splitShipment;
    private Boolean partialShipment;
    private BigDecimal estimatedShippingCost;
    private Instant createdAt;
    private Instant updatedAt;

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getSplitShipment() {
        return splitShipment;
    }

    public void setSplitShipment(Boolean splitShipment) {
        this.splitShipment = splitShipment;
    }

    public Boolean getPartialShipment() {
        return partialShipment;
    }

    public void setPartialShipment(Boolean partialShipment) {
        this.partialShipment = partialShipment;
    }

    public BigDecimal getEstimatedShippingCost() {
        return estimatedShippingCost;
    }

    public void setEstimatedShippingCost(BigDecimal estimatedShippingCost) {
        this.estimatedShippingCost = estimatedShippingCost;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
