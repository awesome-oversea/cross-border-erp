package com.aidotnet.erp.sys.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * PMS反馈数据对象(PmsFeedbackDO)
 * <p>
 * 描述: PMS反馈数据对象，对应sys_pms_feedback表。
 *       存储ERP向PMS回传的执行结果反馈，支持重试投递。
 * </p>
 *
 * @author ERP系统
 */
@TableName("sys_pms_feedback")
public class PmsFeedbackDO {
    @TableId(type = IdType.ASSIGN_ID)
    private String feedbackId;
    private String tenantId;
    private String erpReferenceId;
    private String recommendationId;
    private String domain;
    private String feedbackType;
    private String executionStatus;
    private String businessResult;
    private String businessMetricsJson;
    private String failureReason;
    private String operatorId;
    private String traceId;
    private Boolean delivered;
    private Integer retryCount;
    private Instant deliveredAt;
    private Instant createdAt;
    private Instant updatedAt;

    public PmsFeedbackDO() {}

    public String getFeedbackId() { return feedbackId; }
    public void setFeedbackId(String feedbackId) { this.feedbackId = feedbackId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getErpReferenceId() { return erpReferenceId; }
    public void setErpReferenceId(String erpReferenceId) { this.erpReferenceId = erpReferenceId; }
    public String getRecommendationId() { return recommendationId; }
    public void setRecommendationId(String recommendationId) { this.recommendationId = recommendationId; }
    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    public String getFeedbackType() { return feedbackType; }
    public void setFeedbackType(String feedbackType) { this.feedbackType = feedbackType; }
    public String getExecutionStatus() { return executionStatus; }
    public void setExecutionStatus(String executionStatus) { this.executionStatus = executionStatus; }
    public String getBusinessResult() { return businessResult; }
    public void setBusinessResult(String businessResult) { this.businessResult = businessResult; }
    public String getBusinessMetricsJson() { return businessMetricsJson; }
    public void setBusinessMetricsJson(String businessMetricsJson) { this.businessMetricsJson = businessMetricsJson; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public String getOperatorId() { return operatorId; }
    public void setOperatorId(String operatorId) { this.operatorId = operatorId; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public Boolean getDelivered() { return delivered; }
    public void setDelivered(Boolean delivered) { this.delivered = delivered; }
    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
