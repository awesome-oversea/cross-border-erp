package com.aidotnet.erp.som.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.time.Instant;

@TableName("som_pms_listing_suggestion")
public class PmsListingSuggestionDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String suggestionId;
    private String tenantId;
    private String listingId;
    private String suggestionType;
    private String titleSuggestion;
    private String descriptionSuggestion;
    private String bulletPointsSuggestion;
    private BigDecimal priceSuggestion;
    private String reason;
    private String confidence;
    private String traceId;
    private String idempotencyKey;
    private String status;
    private Instant createdAt;

    public PmsListingSuggestionDO() {}

    public String getSuggestionId() { return suggestionId; }
    public void setSuggestionId(String suggestionId) { this.suggestionId = suggestionId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getListingId() { return listingId; }
    public void setListingId(String listingId) { this.listingId = listingId; }
    public String getSuggestionType() { return suggestionType; }
    public void setSuggestionType(String suggestionType) { this.suggestionType = suggestionType; }
    public String getTitleSuggestion() { return titleSuggestion; }
    public void setTitleSuggestion(String titleSuggestion) { this.titleSuggestion = titleSuggestion; }
    public String getDescriptionSuggestion() { return descriptionSuggestion; }
    public void setDescriptionSuggestion(String descriptionSuggestion) { this.descriptionSuggestion = descriptionSuggestion; }
    public String getBulletPointsSuggestion() { return bulletPointsSuggestion; }
    public void setBulletPointsSuggestion(String bulletPointsSuggestion) { this.bulletPointsSuggestion = bulletPointsSuggestion; }
    public BigDecimal getPriceSuggestion() { return priceSuggestion; }
    public void setPriceSuggestion(BigDecimal priceSuggestion) { this.priceSuggestion = priceSuggestion; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getConfidence() { return confidence; }
    public void setConfidence(String confidence) { this.confidence = confidence; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
