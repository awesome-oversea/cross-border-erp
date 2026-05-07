package com.aidotnet.erp.fms.application;

import com.aidotnet.erp.common.event.DomainEventPublisher;
import com.aidotnet.erp.common.event.StandardDomainEvent;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.fms.domain.CostAllocationResult;
import com.aidotnet.erp.fms.domain.CostAnomaly;
import com.aidotnet.erp.fms.domain.CostEvent;
import com.aidotnet.erp.fms.infrastructure.FinanceStore;
import com.aidotnet.erp.fms.infrastructure.FmsExtStore;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 成本异常应用服务
 * <p>
 * 关键业务逻辑:
 * 1. PMS 只负责给出异常建议，真正生效必须经过 FMS 财务审批。
 * 2. 审批通过后先落地正式成本事件，再按建议维度生成归集结果，保证利润核算只消费已生效数据。
 * 3. 审批完成后回发 PMS 执行事件，由 SYS 统一沉淀待投递反馈。
 * </p>
 */
@Service
public class CostAnomalyService {

    private static final String MANUAL_AI_RULE_ID = "AI_COST_ANOMALY_DRAFT";

    private final FmsExtStore extStore;
    private final FinanceStore financeStore;
    private final DomainEventPublisher eventPublisher;

    public CostAnomalyService(FmsExtStore extStore,
                              FinanceStore financeStore,
                              DomainEventPublisher eventPublisher) {
        this.extStore = extStore;
        this.financeStore = financeStore;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public CostAnomaly submit(String tenantId, SubmitCostAnomalyCommand command) {
        extStore.findCostAnomalyByIdempotencyKey(tenantId, command.idempotencyKey())
                .ifPresent(existing -> {
                    throw new ExistingCostAnomalyException(existing);
                });

        validateCommand(command);
        Instant now = Instant.now();
        CostAnomaly anomaly = new CostAnomaly(
                UUID.randomUUID().toString(),
                tenantId,
                trimToNull(command.erpReferenceId()),
                command.idempotencyKey().trim(),
                normalizeToken(command.anomalyType()),
                normalizeToken(command.sourceType()),
                command.sourceId().trim(),
                normalizeNullableToken(command.dimensionType()),
                trimToNull(command.dimensionId()),
                command.sellerSku().trim(),
                trimToNull(command.storeId()),
                normalizeNullableToken(command.channelCode()),
                normalizeNullableToken(command.marketplaceId()),
                normalizeCostType(command.costType()),
                command.suggestedAmount(),
                normalizeToken(command.currency()),
                command.autoAggregate(),
                trimToNull(command.reason()),
                command.evidence() == null ? Map.of() : command.evidence(),
                CostAnomaly.Status.PENDING_APPROVAL.name(),
                command.submittedBy().trim(),
                normalizeToken(command.submittedActorType()),
                null,
                null,
                null,
                List.of(),
                trimToNull(command.traceId()),
                trimToNull(command.purpose()),
                trimToNull(command.rawScope()),
                now,
                now);
        CostAnomaly saved = extStore.saveCostAnomaly(anomaly);
        publishCostAnomalyEvent(saved);
        return saved;
    }

    public CostAnomaly submitOrGet(String tenantId, SubmitCostAnomalyCommand command) {
        try {
            return submit(tenantId, command);
        } catch (ExistingCostAnomalyException existing) {
            return existing.existing();
        }
    }

    public CostAnomaly get(String tenantId, String anomalyId) {
        return extStore.findCostAnomaly(tenantId, anomalyId)
                .orElseThrow(() -> new BizException("FMS_COST_ANOMALY_NOT_FOUND", "成本异常建议不存在"));
    }

    public List<CostAnomaly> list(String tenantId, String status, String sellerSku, String storeId) {
        return extStore.listCostAnomalies(tenantId, normalizeNullableToken(status), trimToNull(sellerSku), trimToNull(storeId));
    }

    @Transactional
    public CostAnomaly approve(String tenantId, String anomalyId, ApproveCostAnomalyCommand command) {
        CostAnomaly anomaly = get(tenantId, anomalyId);
        if (!CostAnomaly.Status.PENDING_APPROVAL.name().equals(anomaly.status())) {
            throw new BizException("FMS_COST_ANOMALY_STATUS_INVALID", "只有待审批的成本异常建议才能生效");
        }

        Instant approvedAt = command.approvedAt() != null ? command.approvedAt() : Instant.now();
        CostEvent costEvent = financeStore.saveCostEvent(new CostEvent(
                UUID.randomUUID().toString(),
                tenantId,
                anomaly.costType(),
                anomaly.sourceType(),
                anomaly.sourceId(),
                anomaly.sellerSku(),
                anomaly.storeId(),
                anomaly.channelCode(),
                anomaly.marketplaceId(),
                anomaly.currency(),
                anomaly.suggestedAmount(),
                approvedAt,
                approvedAt));

        List<String> allocationResultIds = new ArrayList<>();
        if (anomaly.autoAggregate()) {
            allocationResultIds.addAll(applyManualAllocationDraft(tenantId, anomaly, costEvent, approvedAt));
        }

        CostAnomaly applied = new CostAnomaly(
                anomaly.anomalyId(),
                anomaly.tenantId(),
                anomaly.erpReferenceId(),
                anomaly.idempotencyKey(),
                anomaly.anomalyType(),
                anomaly.sourceType(),
                anomaly.sourceId(),
                anomaly.dimensionType(),
                anomaly.dimensionId(),
                anomaly.sellerSku(),
                anomaly.storeId(),
                anomaly.channelCode(),
                anomaly.marketplaceId(),
                anomaly.costType(),
                anomaly.suggestedAmount(),
                anomaly.currency(),
                anomaly.autoAggregate(),
                anomaly.reason(),
                anomaly.evidence(),
                CostAnomaly.Status.APPLIED.name(),
                anomaly.submittedBy(),
                anomaly.submittedActorType(),
                command.approvedBy().trim(),
                approvedAt,
                costEvent.costEventId(),
                allocationResultIds,
                anomaly.traceId(),
                anomaly.purpose(),
                anomaly.rawScope(),
                anomaly.createdAt(),
                approvedAt);
        CostAnomaly saved = extStore.saveCostAnomaly(applied);
        publishPmsExecutionEvent(saved, allocationResultIds.size());
        return saved;
    }

    private List<String> applyManualAllocationDraft(String tenantId, CostAnomaly anomaly, CostEvent costEvent, Instant allocatedAt) {
        if (!hasText(anomaly.dimensionType()) || !hasText(anomaly.dimensionId())) {
            throw new BizException("FMS_COST_ANOMALY_DIMENSION_REQUIRED", "启用归集草稿时必须指定归集维度");
        }
        Map<String, String> dimensions = new LinkedHashMap<>();
        dimensions.put("costType", anomaly.costType());
        dimensions.put("sourceType", anomaly.sourceType());
        dimensions.put("sourceId", anomaly.sourceId());
        dimensions.put("sellerSku", defaultString(anomaly.sellerSku()));
        dimensions.put("storeId", defaultString(anomaly.storeId()));
        dimensions.put("channelCode", defaultString(anomaly.channelCode()));
        dimensions.put("marketplaceId", defaultString(anomaly.marketplaceId()));
        dimensions.put("anomalyId", anomaly.anomalyId());
        dimensions.put("allocationMode", "AI_APPROVED_DRAFT");

        CostAllocationResult result = new CostAllocationResult(
                UUID.randomUUID().toString(),
                tenantId,
                MANUAL_AI_RULE_ID,
                costEvent.costEventId(),
                anomaly.dimensionType(),
                anomaly.dimensionId(),
                anomaly.suggestedAmount(),
                anomaly.currency(),
                BigDecimal.ONE,
                anomaly.suggestedAmount(),
                dimensions,
                allocatedAt);
        return List.of(extStore.saveCostAllocationResult(result).resultId());
    }

    private void publishCostAnomalyEvent(CostAnomaly anomaly) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("erpReferenceId", anomaly.erpReferenceId());
        payload.put("anomalyType", anomaly.anomalyType());
        payload.put("costType", anomaly.costType());
        payload.put("sellerSku", anomaly.sellerSku());
        payload.put("sourceType", anomaly.sourceType());
        payload.put("sourceId", anomaly.sourceId());
        payload.put("status", anomaly.status());
        payload.put("suggestedAmount", anomaly.suggestedAmount());
        payload.put("currency", anomaly.currency());
        eventPublisher.publish(new StandardDomainEvent(
                UUID.randomUUID().toString(),
                anomaly.tenantId(),
                anomaly.traceId(),
                "erp.fms.cost.anomaly",
                anomaly.anomalyId(),
                Instant.now(),
                payload));
    }

    private void publishPmsExecutionEvent(CostAnomaly anomaly, int allocationResultCount) {
        if (!hasText(anomaly.erpReferenceId())) {
            return;
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("domain", "FMS");
        payload.put("anomalyId", anomaly.anomalyId());
        payload.put("costEventId", anomaly.effectiveCostEventId());
        payload.put("allocationResultCount", allocationResultCount);
        payload.put("approvedBy", anomaly.approvedBy());
        payload.put("status", anomaly.status());
        eventPublisher.publish(new StandardDomainEvent(
                UUID.randomUUID().toString(),
                anomaly.tenantId(),
                anomaly.traceId(),
                "erp.pms.recommendation.executed",
                anomaly.erpReferenceId(),
                Instant.now(),
                payload));
    }

    private void validateCommand(SubmitCostAnomalyCommand command) {
        if (command.suggestedAmount() == null || command.suggestedAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("FMS_COST_ANOMALY_AMOUNT_INVALID", "建议成本金额必须大于0");
        }
        if (command.autoAggregate() && (!hasText(command.dimensionType()) || !hasText(command.dimensionId()))) {
            throw new BizException("FMS_COST_ANOMALY_DIMENSION_REQUIRED", "启用归集草稿时必须指定归集维度");
        }
        normalizeCostType(command.costType());
    }

    private String normalizeCostType(String costType) {
        String normalized = normalizeToken(costType);
        try {
            CostEvent.CostType.valueOf(normalized);
            return normalized;
        } catch (Exception ex) {
            throw new BizException("COST_TYPE_INVALID", "成本类型无效");
        }
    }

    private String normalizeToken(String value) {
        if (!hasText(value)) {
            throw new BizException("BAD_REQUEST", "关键字段不能为空");
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeNullableToken(String value) {
        return hasText(value) ? value.trim().toUpperCase(Locale.ROOT) : null;
    }

    private String trimToNull(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }

    public record SubmitCostAnomalyCommand(
            String erpReferenceId,
            String idempotencyKey,
            String anomalyType,
            String sourceType,
            String sourceId,
            String dimensionType,
            String dimensionId,
            String sellerSku,
            String storeId,
            String channelCode,
            String marketplaceId,
            String costType,
            BigDecimal suggestedAmount,
            String currency,
            boolean autoAggregate,
            String reason,
            Map<String, Object> evidence,
            String submittedBy,
            String submittedActorType,
            String traceId,
            String purpose,
            String rawScope) {}

    public record ApproveCostAnomalyCommand(String approvedBy, Instant approvedAt) {}

    private static final class ExistingCostAnomalyException extends RuntimeException {
        private final CostAnomaly existing;

        private ExistingCostAnomalyException(CostAnomaly existing) {
            this.existing = existing;
        }

        private CostAnomaly existing() {
            return existing;
        }
    }
}
