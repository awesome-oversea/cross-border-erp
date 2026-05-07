package com.aidotnet.erp.fms.infrastructure.data;

import java.math.BigDecimal;
import java.time.Instant;

public class CostAnomalyDO {
    private String anomalyId;
    private String tenantId;
    private String erpReferenceId;
    private String idempotencyKey;
    private String anomalyType;
    private String sourceType;
    private String sourceId;
    private String dimensionType;
    private String dimensionId;
    private String sellerSku;
    private String storeId;
    private String channelCode;
    private String marketplaceId;
    private String costType;
    private BigDecimal suggestedAmount;
    private String currency;
    private Boolean autoAggregate;
    private String reason;
    private String evidenceJson;
    private String status;
    private String submittedBy;
    private String submittedActorType;
    private String approvedBy;
    private Instant approvedAt;
    private String effectiveCostEventId;
    private String allocationResultIdsJson;
    private String traceId;
    private String purpose;
    private String rawScope;
    private Instant createdAt;
    private Instant updatedAt;

    public String getAnomalyId() { return anomalyId; }
    public void setAnomalyId(String anomalyId) { this.anomalyId = anomalyId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getErpReferenceId() { return erpReferenceId; }
    public void setErpReferenceId(String erpReferenceId) { this.erpReferenceId = erpReferenceId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getAnomalyType() { return anomalyType; }
    public void setAnomalyType(String anomalyType) { this.anomalyType = anomalyType; }
    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }
    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }
    public String getDimensionType() { return dimensionType; }
    public void setDimensionType(String dimensionType) { this.dimensionType = dimensionType; }
    public String getDimensionId() { return dimensionId; }
    public void setDimensionId(String dimensionId) { this.dimensionId = dimensionId; }
    public String getSellerSku() { return sellerSku; }
    public void setSellerSku(String sellerSku) { this.sellerSku = sellerSku; }
    public String getStoreId() { return storeId; }
    public void setStoreId(String storeId) { this.storeId = storeId; }
    public String getChannelCode() { return channelCode; }
    public void setChannelCode(String channelCode) { this.channelCode = channelCode; }
    public String getMarketplaceId() { return marketplaceId; }
    public void setMarketplaceId(String marketplaceId) { this.marketplaceId = marketplaceId; }
    public String getCostType() { return costType; }
    public void setCostType(String costType) { this.costType = costType; }
    public BigDecimal getSuggestedAmount() { return suggestedAmount; }
    public void setSuggestedAmount(BigDecimal suggestedAmount) { this.suggestedAmount = suggestedAmount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Boolean getAutoAggregate() { return autoAggregate; }
    public void setAutoAggregate(Boolean autoAggregate) { this.autoAggregate = autoAggregate; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getEvidenceJson() { return evidenceJson; }
    public void setEvidenceJson(String evidenceJson) { this.evidenceJson = evidenceJson; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(String submittedBy) { this.submittedBy = submittedBy; }
    public String getSubmittedActorType() { return submittedActorType; }
    public void setSubmittedActorType(String submittedActorType) { this.submittedActorType = submittedActorType; }
    public String getApprovedBy() { return approvedBy; }
    public void setApprovedBy(String approvedBy) { this.approvedBy = approvedBy; }
    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }
    public String getEffectiveCostEventId() { return effectiveCostEventId; }
    public void setEffectiveCostEventId(String effectiveCostEventId) { this.effectiveCostEventId = effectiveCostEventId; }
    public String getAllocationResultIdsJson() { return allocationResultIdsJson; }
    public void setAllocationResultIdsJson(String allocationResultIdsJson) { this.allocationResultIdsJson = allocationResultIdsJson; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public String getRawScope() { return rawScope; }
    public void setRawScope(String rawScope) { this.rawScope = rawScope; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
