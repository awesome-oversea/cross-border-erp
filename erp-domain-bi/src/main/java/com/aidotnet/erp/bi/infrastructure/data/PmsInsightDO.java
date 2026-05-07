package com.aidotnet.erp.bi.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

@TableName("bi_pms_insight_recommendation")
public class PmsInsightDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String insightId;

    private String tenantId;

    private String erpReferenceId;

    private String idempotencyKey;

    private String submissionType;

    private String title;

    private String summary;

    private String insightType;

    private String severity;

    private String category;

    private String metricCode;

    private String metricName;

    private String targetUserId;

    private String trendPeriod;

    private String predictionPointsJson;

    private String insightDataJson;

    private String suggestion;

    private String actionUrl;

    private Instant validUntil;

    private String status;

    private String submittedBy;

    private String submittedActorType;

    private String approvedBy;

    private Instant approvedAt;

    private String generatedTrendId;

    private String dashboardCardId;

    private String traceId;

    private String purpose;

    private String rawScope;

    private Instant createdAt;

    private Instant updatedAt;

    public PmsInsightDO() {}

    public String getInsightId() { return insightId; }
    public void setInsightId(String insightId) { this.insightId = insightId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getErpReferenceId() { return erpReferenceId; }
    public void setErpReferenceId(String erpReferenceId) { this.erpReferenceId = erpReferenceId; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public String getSubmissionType() { return submissionType; }
    public void setSubmissionType(String submissionType) { this.submissionType = submissionType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getInsightType() { return insightType; }
    public void setInsightType(String insightType) { this.insightType = insightType; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getMetricCode() { return metricCode; }
    public void setMetricCode(String metricCode) { this.metricCode = metricCode; }
    public String getMetricName() { return metricName; }
    public void setMetricName(String metricName) { this.metricName = metricName; }
    public String getTargetUserId() { return targetUserId; }
    public void setTargetUserId(String targetUserId) { this.targetUserId = targetUserId; }
    public String getTrendPeriod() { return trendPeriod; }
    public void setTrendPeriod(String trendPeriod) { this.trendPeriod = trendPeriod; }
    public String getPredictionPointsJson() { return predictionPointsJson; }
    public void setPredictionPointsJson(String predictionPointsJson) { this.predictionPointsJson = predictionPointsJson; }
    public String getInsightDataJson() { return insightDataJson; }
    public void setInsightDataJson(String insightDataJson) { this.insightDataJson = insightDataJson; }
    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
    public String getActionUrl() { return actionUrl; }
    public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }
    public Instant getValidUntil() { return validUntil; }
    public void setValidUntil(Instant validUntil) { this.validUntil = validUntil; }
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
    public String getGeneratedTrendId() { return generatedTrendId; }
    public void setGeneratedTrendId(String generatedTrendId) { this.generatedTrendId = generatedTrendId; }
    public String getDashboardCardId() { return dashboardCardId; }
    public void setDashboardCardId(String dashboardCardId) { this.dashboardCardId = dashboardCardId; }
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
