package com.aidotnet.erp.bi.infrastructure;

import com.aidotnet.erp.bi.domain.CockpitTrend;
import com.aidotnet.erp.bi.domain.PmsInsight;
import com.aidotnet.erp.bi.infrastructure.data.PmsInsightDO;
import com.aidotnet.erp.bi.infrastructure.mapper.BiPmsMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * BI-PMS 趋势洞察建议持久化仓储。
 */
@Repository
public class BiPmsStore {

    private final BiPmsMapper mapper;
    private final ObjectMapper objectMapper;

    public BiPmsStore(BiPmsMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    public PmsInsight save(PmsInsight insight) {
        PmsInsightDO existing = mapper.selectPmsInsight(insight.tenantId(), insight.insightId());
        PmsInsightDO data = toData(insight);
        if (existing == null) {
            mapper.insertPmsInsight(data);
        } else {
            mapper.updatePmsInsight(data);
        }
        return insight;
    }

    public Optional<PmsInsight> find(String tenantId, String insightId) {
        return Optional.ofNullable(mapper.selectPmsInsight(tenantId, insightId)).map(this::toDomain);
    }

    public Optional<PmsInsight> findByIdempotencyKey(String tenantId, String idempotencyKey) {
        return Optional.ofNullable(mapper.selectPmsInsightByIdempotencyKey(tenantId, idempotencyKey)).map(this::toDomain);
    }

    private PmsInsightDO toData(PmsInsight insight) {
        PmsInsightDO data = new PmsInsightDO();
        data.setInsightId(insight.insightId());
        data.setTenantId(insight.tenantId());
        data.setErpReferenceId(insight.erpReferenceId());
        data.setIdempotencyKey(insight.idempotencyKey());
        data.setSubmissionType(insight.submissionType());
        data.setTitle(insight.title());
        data.setSummary(insight.summary());
        data.setInsightType(insight.insightType());
        data.setSeverity(insight.severity());
        data.setCategory(insight.category());
        data.setMetricCode(insight.metricCode());
        data.setMetricName(insight.metricName());
        data.setTargetUserId(insight.targetUserId());
        data.setTrendPeriod(insight.trendPeriod());
        data.setPredictionPointsJson(toJson(insight.predictionPoints() == null ? List.of() : insight.predictionPoints()));
        data.setInsightDataJson(toJson(insight.insightData() == null ? Map.of() : insight.insightData()));
        data.setSuggestion(insight.suggestion());
        data.setActionUrl(insight.actionUrl());
        data.setValidUntil(insight.validUntil());
        data.setStatus(insight.status());
        data.setSubmittedBy(insight.submittedBy());
        data.setSubmittedActorType(insight.submittedActorType());
        data.setApprovedBy(insight.approvedBy());
        data.setApprovedAt(insight.approvedAt());
        data.setGeneratedTrendId(insight.generatedTrendId());
        data.setDashboardCardId(insight.dashboardCardId());
        data.setTraceId(insight.traceId());
        data.setPurpose(insight.purpose());
        data.setRawScope(insight.rawScope());
        data.setCreatedAt(insight.createdAt() != null ? insight.createdAt() : Instant.now());
        data.setUpdatedAt(insight.updatedAt() != null ? insight.updatedAt() : Instant.now());
        return data;
    }

    private PmsInsight toDomain(PmsInsightDO data) {
        List<CockpitTrend.DataPoint> predictionPoints = fromJson(
                data.getPredictionPointsJson(), new TypeReference<List<CockpitTrend.DataPoint>>() {});
        Map<String, Object> insightData = fromJson(
                data.getInsightDataJson(), new TypeReference<Map<String, Object>>() {});
        return new PmsInsight(
                data.getInsightId(),
                data.getTenantId(),
                data.getErpReferenceId(),
                data.getIdempotencyKey(),
                data.getSubmissionType(),
                data.getTitle(),
                data.getSummary(),
                data.getInsightType(),
                data.getSeverity(),
                data.getCategory(),
                data.getMetricCode(),
                data.getMetricName(),
                data.getTargetUserId(),
                data.getTrendPeriod(),
                predictionPoints == null ? List.of() : predictionPoints,
                insightData == null ? Map.of() : insightData,
                data.getSuggestion(),
                data.getActionUrl(),
                data.getValidUntil(),
                data.getStatus(),
                data.getSubmittedBy(),
                data.getSubmittedActorType(),
                data.getApprovedBy(),
                data.getApprovedAt(),
                data.getGeneratedTrendId(),
                data.getDashboardCardId(),
                data.getTraceId(),
                data.getPurpose(),
                data.getRawScope(),
                data.getCreatedAt(),
                data.getUpdatedAt());
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize BI PMS insight payload.", ex);
        }
    }

    private <T> T fromJson(String value, TypeReference<T> typeReference) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(value, typeReference);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to deserialize BI PMS insight payload.", ex);
        }
    }
}
