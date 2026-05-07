package com.aidotnet.erp.bi.application;

import com.aidotnet.erp.bi.domain.CockpitTrend;
import com.aidotnet.erp.bi.domain.PmsInsight;
import com.aidotnet.erp.bi.infrastructure.BiExtStore;
import com.aidotnet.erp.bi.infrastructure.BiPmsStore;
import com.aidotnet.erp.common.event.DomainEventPublisher;
import com.aidotnet.erp.common.event.StandardDomainEvent;
import com.aidotnet.erp.common.exception.BizException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * BI PMS 趋势预测闭环应用服务。
 * <p>
 * 业务语义:
 * 1. PMS 仅负责提交趋势洞察建议，BI 审批通过后才会生成正式趋势数据。
 * 2. Dashboard 卡片通过领域事件异步沉淀，避免 BI 与 Dashboard 持久化直接耦合。
 * 3. 审批完成后统一发送 PMS 执行完成事件，由 SYS 负责反馈回传。
 * </p>
 */
@Service
public class BiPmsService {

    private static final String SUBMISSION_TYPE = "AI_TREND_PREDICTION";
    private static final String STATUS_PENDING_APPROVAL = "PENDING_APPROVAL";
    private static final String STATUS_APPLIED = "APPLIED";
    private static final Set<String> ALLOWED_SEVERITIES = Set.of("critical", "high", "medium", "low");

    private final BiPmsStore pmsStore;
    private final BiExtStore biExtStore;
    private final DomainEventPublisher eventPublisher;

    public BiPmsService(BiPmsStore pmsStore,
                        BiExtStore biExtStore,
                        DomainEventPublisher eventPublisher) {
        this.pmsStore = pmsStore;
        this.biExtStore = biExtStore;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public PmsInsight submit(String tenantId, SubmitTrendPredictionCommand command) {
        pmsStore.findByIdempotencyKey(tenantId, command.idempotencyKey())
                .ifPresent(existing -> {
                    throw new ExistingPmsInsightException(existing);
                });

        validateCommand(command);
        Instant now = Instant.now();
        PmsInsight insight = new PmsInsight(
                UUID.randomUUID().toString(),
                tenantId,
                requireText(command.erpReferenceId(), "BI_PMS_ERP_REFERENCE_REQUIRED", "erpReferenceId不能为空"),
                requireText(command.idempotencyKey(), "BI_PMS_IDEMPOTENCY_REQUIRED", "idempotencyKey不能为空"),
                SUBMISSION_TYPE,
                requireText(command.title(), "BI_PMS_TITLE_REQUIRED", "title不能为空"),
                trimToNull(command.summary()),
                normalizeInsightType(command.insightType()),
                normalizeSeverity(command.severity()),
                requireText(command.category(), "BI_PMS_CATEGORY_REQUIRED", "category不能为空"),
                requireText(command.metricCode(), "BI_PMS_METRIC_CODE_REQUIRED", "metricCode不能为空"),
                requireText(command.metricName(), "BI_PMS_METRIC_NAME_REQUIRED", "metricName不能为空"),
                requireText(command.targetUserId(), "BI_PMS_TARGET_USER_REQUIRED", "targetUserId不能为空"),
                hasText(command.trendPeriod()) ? command.trendPeriod().trim() : "P7D",
                List.copyOf(command.predictionPoints()),
                command.insightData() == null ? Map.of() : new LinkedHashMap<>(command.insightData()),
                trimToNull(command.suggestion()),
                trimToNull(command.actionUrl()),
                command.validUntil(),
                STATUS_PENDING_APPROVAL,
                requireText(command.submittedBy(), "BI_PMS_SUBMITTED_BY_REQUIRED", "submittedBy不能为空"),
                requireText(command.submittedActorType(), "BI_PMS_SUBMITTED_ACTOR_TYPE_REQUIRED", "submittedActorType不能为空"),
                null,
                null,
                null,
                null,
                trimToNull(command.traceId()),
                trimToNull(command.purpose()),
                trimToNull(command.rawScope()),
                now,
                now);
        return pmsStore.save(insight);
    }

    public PmsInsight submitOrGet(String tenantId, SubmitTrendPredictionCommand command) {
        try {
            return submit(tenantId, command);
        } catch (ExistingPmsInsightException existing) {
            return existing.existing();
        }
    }

    public PmsInsight get(String tenantId, String insightId) {
        return pmsStore.find(tenantId, insightId)
                .orElseThrow(() -> new BizException("BI_PMS_INSIGHT_NOT_FOUND", "BI趋势洞察建议不存在"));
    }

    @Transactional
    public PmsInsight approve(String tenantId, String insightId, ApproveTrendPredictionCommand command) {
        PmsInsight insight = get(tenantId, insightId);
        if (!STATUS_PENDING_APPROVAL.equals(insight.status())) {
            throw new BizException("BI_PMS_INSIGHT_STATUS_INVALID", "只有待审批的BI趋势洞察建议才能生效");
        }

        String approvedBy = requireText(command.approvedBy(), "BI_PMS_APPROVED_BY_REQUIRED", "approvedBy不能为空");
        Instant approvedAt = command.approvedAt() != null ? command.approvedAt() : Instant.now();
        String generatedTrendId = UUID.randomUUID().toString();
        String dashboardCardId = UUID.randomUUID().toString();

        biExtStore.saveCockpitTrend(new CockpitTrend(
                generatedTrendId,
                tenantId,
                insight.metricCode(),
                insight.metricName(),
                insight.trendPeriod(),
                insight.predictionPoints(),
                approvedAt));

        PmsInsight applied = new PmsInsight(
                insight.insightId(),
                insight.tenantId(),
                insight.erpReferenceId(),
                insight.idempotencyKey(),
                insight.submissionType(),
                insight.title(),
                insight.summary(),
                insight.insightType(),
                insight.severity(),
                insight.category(),
                insight.metricCode(),
                insight.metricName(),
                insight.targetUserId(),
                insight.trendPeriod(),
                insight.predictionPoints(),
                insight.insightData(),
                insight.suggestion(),
                insight.actionUrl(),
                insight.validUntil(),
                STATUS_APPLIED,
                insight.submittedBy(),
                insight.submittedActorType(),
                approvedBy,
                approvedAt,
                generatedTrendId,
                dashboardCardId,
                insight.traceId(),
                insight.purpose(),
                insight.rawScope(),
                insight.createdAt(),
                approvedAt);
        PmsInsight saved = pmsStore.save(applied);
        publishDashboardCardCreated(saved);
        publishPmsExecutionEvent(saved);
        return saved;
    }

    private void publishDashboardCardCreated(PmsInsight insight) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("insightId", insight.insightId());
        data.put("metricCode", insight.metricCode());
        data.put("metricName", insight.metricName());
        data.put("category", insight.category());
        data.put("trendPeriod", insight.trendPeriod());
        data.put("generatedTrendId", insight.generatedTrendId());
        if (insight.insightData() != null && !insight.insightData().isEmpty()) {
            data.putAll(insight.insightData());
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("cardId", insight.dashboardCardId());
        payload.put("userId", insight.targetUserId());
        payload.put("title", insight.title());
        payload.put("insightType", insight.insightType());
        payload.put("severity", insight.severity());
        payload.put("data", data);
        payload.put("sourceDomain", "BI");
        putIfNotNull(payload, "summary", insight.summary());
        putIfNotNull(payload, "suggestion", insight.suggestion());
        putIfNotNull(payload, "actionUrl", insight.actionUrl());
        putIfNotNull(payload, "validUntil", insight.validUntil());

        eventPublisher.publish(new StandardDomainEvent(
                UUID.randomUUID().toString(),
                insight.tenantId(),
                insight.traceId(),
                "erp.bi.insight.card.created",
                insight.dashboardCardId(),
                Instant.now(),
                payload));
    }

    private void publishPmsExecutionEvent(PmsInsight insight) {
        if (!hasText(insight.erpReferenceId())) {
            return;
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("domain", "BI");
        payload.put("insightId", insight.insightId());
        payload.put("generatedTrendId", insight.generatedTrendId());
        payload.put("dashboardCardId", insight.dashboardCardId());
        payload.put("approvedBy", insight.approvedBy());
        payload.put("status", insight.status());
        eventPublisher.publish(new StandardDomainEvent(
                UUID.randomUUID().toString(),
                insight.tenantId(),
                insight.traceId(),
                "erp.pms.recommendation.executed",
                insight.erpReferenceId(),
                Instant.now(),
                payload));
    }

    private void validateCommand(SubmitTrendPredictionCommand command) {
        if (!hasText(command.idempotencyKey())) {
            throw new BizException("BI_PMS_IDEMPOTENCY_REQUIRED", "idempotencyKey不能为空");
        }
        if (command.predictionPoints() == null || command.predictionPoints().isEmpty()) {
            throw new BizException("BI_PMS_PREDICTION_POINTS_REQUIRED", "predictionPoints不能为空");
        }
        for (CockpitTrend.DataPoint point : command.predictionPoints()) {
            if (point == null || point.timestamp() == null || point.value() == null) {
                throw new BizException("BI_PMS_PREDICTION_POINT_INVALID", "predictionPoints存在无效数据点");
            }
        }
        if (!"trend".equalsIgnoreCase(command.insightType())) {
            throw new BizException("BI_PMS_INSIGHT_TYPE_INVALID", "trend-predict接口仅支持trend类型");
        }
        normalizeSeverity(command.severity());
    }

    private String normalizeInsightType(String insightType) {
        return requireText(insightType, "BI_PMS_INSIGHT_TYPE_REQUIRED", "insightType不能为空")
                .toLowerCase(Locale.ROOT);
    }

    private String normalizeSeverity(String severity) {
        String normalized = requireText(severity, "BI_PMS_SEVERITY_REQUIRED", "severity不能为空")
                .toLowerCase(Locale.ROOT);
        if (!ALLOWED_SEVERITIES.contains(normalized)) {
            throw new BizException("BI_PMS_SEVERITY_INVALID", "severity必须为critical/high/medium/low");
        }
        return normalized;
    }

    private void putIfNotNull(Map<String, Object> payload, String key, Object value) {
        if (value != null) {
            payload.put(key, value);
        }
    }

    private String requireText(String value, String code, String message) {
        if (!hasText(value)) {
            throw new BizException(code, message);
        }
        return value.trim();
    }

    private String trimToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public record SubmitTrendPredictionCommand(
            String erpReferenceId,
            String idempotencyKey,
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
            String submittedBy,
            String submittedActorType,
            String traceId,
            String purpose,
            String rawScope) {}

    public record ApproveTrendPredictionCommand(String approvedBy, Instant approvedAt) {}

    private static final class ExistingPmsInsightException extends RuntimeException {
        private final PmsInsight existing;

        private ExistingPmsInsightException(PmsInsight existing) {
            this.existing = existing;
        }

        private PmsInsight existing() {
            return existing;
        }
    }
}
