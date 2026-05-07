package com.aidotnet.erp.bi.domain;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * PMS 推送到 BI 的洞察/趋势建议。
 * <p>
 * 业务语义：
 * 1. PMS 只能提交建议，真正落地必须由 BI/运营侧审批后生效。
 * 2. 趋势预测审批后沉淀为 BI 正式趋势数据，洞察卡片通过事件投递给 Dashboard 展示。
 * 3. 建议记录本身需要保留审批痕迹、幂等键和 ERP 引用，保证可追溯。
 * </p>
 */
public record PmsInsight(
        String insightId,
        String tenantId,
        String erpReferenceId,
        String idempotencyKey,
        String submissionType,
        String title,
        String summary,
        String insightType,
        String severity,
        String category,
        String metricCode,
        String metricName,
        String targetUserId,
        String trendPeriod,
        List<CockpitTrend.DataPoint> predictionPoints,
        Map<String, Object> insightData,
        String suggestion,
        String actionUrl,
        Instant validUntil,
        String status,
        String submittedBy,
        String submittedActorType,
        String approvedBy,
        Instant approvedAt,
        String generatedTrendId,
        String dashboardCardId,
        String traceId,
        String purpose,
        String rawScope,
        Instant createdAt,
        Instant updatedAt
) {}
