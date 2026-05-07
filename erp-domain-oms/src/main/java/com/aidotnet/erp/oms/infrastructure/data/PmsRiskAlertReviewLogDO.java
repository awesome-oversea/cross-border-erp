package com.aidotnet.erp.oms.infrastructure.data;

import java.time.Instant;

public class PmsRiskAlertReviewLogDO {

    private String logId;
    private String tenantId;
    private String alertId;
    private String orderId;
    private String action;
    private String reviewerNote;
    private String riskLevel;
    private Instant createdAt;

    public PmsRiskAlertReviewLogDO() {}

    public String getLogId() { return logId; }
    public void setLogId(String logId) { this.logId = logId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getAlertId() { return alertId; }
    public void setAlertId(String alertId) { this.alertId = alertId; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getReviewerNote() { return reviewerNote; }
    public void setReviewerNote(String reviewerNote) { this.reviewerNote = reviewerNote; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
