package com.aidotnet.erp.fms.infrastructure.data;

import java.time.Instant;

public class FraudDetectionResultDO {
    private String resultId;
    private String tenantId;
    private String orderId;
    private String buyerId;
    private String detectionType;
    private String severity;
    private String description;
    private String indicatorsJson;
    private String status;
    private Instant detectedAt;

    public String getResultId() { return resultId; }
    public void setResultId(String resultId) { this.resultId = resultId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getBuyerId() { return buyerId; }
    public void setBuyerId(String buyerId) { this.buyerId = buyerId; }
    public String getDetectionType() { return detectionType; }
    public void setDetectionType(String detectionType) { this.detectionType = detectionType; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getIndicatorsJson() { return indicatorsJson; }
    public void setIndicatorsJson(String indicatorsJson) { this.indicatorsJson = indicatorsJson; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getDetectedAt() { return detectedAt; }
    public void setDetectedAt(Instant detectedAt) { this.detectedAt = detectedAt; }
}
