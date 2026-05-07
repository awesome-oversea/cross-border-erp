package com.aidotnet.erp.scm.infrastructure.data;

import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

@TableName("scm_purchase_plan_line")
public class PurchasePlanLineDO {

    private String planId;
    private String tenantId;
    private String lineId;
    private String sellerSku;
    private Integer orderDemandQuantity;
    private Integer replenishmentDemandQuantity;
    private Integer availableInventoryQuantity;
    private Integer inPurchasingQuantity;
    private Integer suggestedPurchaseQuantity;
    private String orderSourceRefs;
    private String suggestionSourceRefs;
    private String lineStatus;
    private String linkedPoId;
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

    public String getLineId() {
        return lineId;
    }

    public void setLineId(String lineId) {
        this.lineId = lineId;
    }

    public String getSellerSku() {
        return sellerSku;
    }

    public void setSellerSku(String sellerSku) {
        this.sellerSku = sellerSku;
    }

    public Integer getOrderDemandQuantity() {
        return orderDemandQuantity;
    }

    public void setOrderDemandQuantity(Integer orderDemandQuantity) {
        this.orderDemandQuantity = orderDemandQuantity;
    }

    public Integer getReplenishmentDemandQuantity() {
        return replenishmentDemandQuantity;
    }

    public void setReplenishmentDemandQuantity(Integer replenishmentDemandQuantity) {
        this.replenishmentDemandQuantity = replenishmentDemandQuantity;
    }

    public Integer getAvailableInventoryQuantity() {
        return availableInventoryQuantity;
    }

    public void setAvailableInventoryQuantity(Integer availableInventoryQuantity) {
        this.availableInventoryQuantity = availableInventoryQuantity;
    }

    public Integer getInPurchasingQuantity() {
        return inPurchasingQuantity;
    }

    public void setInPurchasingQuantity(Integer inPurchasingQuantity) {
        this.inPurchasingQuantity = inPurchasingQuantity;
    }

    public Integer getSuggestedPurchaseQuantity() {
        return suggestedPurchaseQuantity;
    }

    public void setSuggestedPurchaseQuantity(Integer suggestedPurchaseQuantity) {
        this.suggestedPurchaseQuantity = suggestedPurchaseQuantity;
    }

    public String getOrderSourceRefs() {
        return orderSourceRefs;
    }

    public void setOrderSourceRefs(String orderSourceRefs) {
        this.orderSourceRefs = orderSourceRefs;
    }

    public String getSuggestionSourceRefs() {
        return suggestionSourceRefs;
    }

    public void setSuggestionSourceRefs(String suggestionSourceRefs) {
        this.suggestionSourceRefs = suggestionSourceRefs;
    }

    public String getLineStatus() {
        return lineStatus;
    }

    public void setLineStatus(String lineStatus) {
        this.lineStatus = lineStatus;
    }

    public String getLinkedPoId() {
        return linkedPoId;
    }

    public void setLinkedPoId(String linkedPoId) {
        this.linkedPoId = linkedPoId;
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
