package com.aidotnet.erp.sys.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.sys.domain.PmsRecommendation;
import com.aidotnet.erp.sys.domain.PmsRecommendationStatus;
import com.aidotnet.erp.sys.domain.PmsWriteObjectType;
import com.aidotnet.erp.sys.infrastructure.PmsRecommendationStore;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PmsRecommendationService {

    private static final Set<String> SUPPORTED_DOMAINS = Set.of(
            "IAM", "PDM", "SOM", "ADS", "OMS", "SCM", "WMS", "FBA", "TMS", "CRM", "FMS", "BI", "SYS", "DASHBOARD");
    private static final Set<PmsWriteObjectType> WRITE_WHITELIST = EnumSet.allOf(PmsWriteObjectType.class);
    private static final Map<PmsRecommendationStatus, Set<PmsRecommendationStatus>> TRANSITIONS = transitions();

    private final PmsRecommendationStore store;

    public PmsRecommendationService(PmsRecommendationStore store) {
        this.store = store;
    }

    public PmsRecommendation submit(PmsSubmitCommand command, PmsCallContext context) {
        validateContext(context);
        validateSubmit(command);
        return store.findByIdempotencyKey(context.tenantId(), command.domain(), context.idempotencyKey())
                .orElseGet(() -> createAccepted(command, context));
    }

    public PmsRecommendation submitForApproval(String tenantId, String erpReferenceId, String approvalPolicy) {
        PmsRecommendation recommendation = get(tenantId, erpReferenceId);
        requireTransition(recommendation.status(), PmsRecommendationStatus.PENDING_APPROVAL);
        return saveWithStatus(recommendation, PmsRecommendationStatus.PENDING_APPROVAL, null, null, null,
                normalizePolicy(approvalPolicy));
    }

    public PmsRecommendation approve(String tenantId, String erpReferenceId) {
        PmsRecommendation recommendation = get(tenantId, erpReferenceId);
        requireTransition(recommendation.status(), PmsRecommendationStatus.APPROVED);
        return saveWithStatus(recommendation, PmsRecommendationStatus.APPROVED, null, null, null, recommendation.approvalPolicy());
    }

    public PmsRecommendation rejectApproval(String tenantId, String erpReferenceId, String reason) {
        PmsRecommendation recommendation = get(tenantId, erpReferenceId);
        requireTransition(recommendation.status(), PmsRecommendationStatus.APPROVAL_REJECTED);
        return saveWithStatus(recommendation, PmsRecommendationStatus.APPROVAL_REJECTED, requireText(reason, "拒绝原因不能为空"), null, null,
                recommendation.approvalPolicy());
    }

    public PmsRecommendation startExecution(String tenantId, String erpReferenceId) {
        PmsRecommendation recommendation = get(tenantId, erpReferenceId);
        requireTransition(recommendation.status(), PmsRecommendationStatus.EXECUTING);
        return saveWithStatus(recommendation, PmsRecommendationStatus.EXECUTING, null, null, null, recommendation.approvalPolicy());
    }

    public PmsRecommendation completeExecution(String tenantId, String erpReferenceId, String executionResult) {
        PmsRecommendation recommendation = get(tenantId, erpReferenceId);
        requireTransition(recommendation.status(), PmsRecommendationStatus.EXECUTED);
        return saveWithStatus(recommendation, PmsRecommendationStatus.EXECUTED, null, requireText(executionResult, "执行结果不能为空"), null,
                recommendation.approvalPolicy());
    }

    public PmsRecommendation failExecution(String tenantId, String erpReferenceId, String executionResult) {
        PmsRecommendation recommendation = get(tenantId, erpReferenceId);
        requireTransition(recommendation.status(), PmsRecommendationStatus.FAILED);
        return saveWithStatus(recommendation, PmsRecommendationStatus.FAILED, null, requireText(executionResult, "失败原因不能为空"), null,
                recommendation.approvalPolicy());
    }

    public PmsRecommendation measure(String tenantId, String erpReferenceId, String measuredResult) {
        PmsRecommendation recommendation = get(tenantId, erpReferenceId);
        requireTransition(recommendation.status(), PmsRecommendationStatus.MEASURED);
        return saveWithStatus(recommendation, PmsRecommendationStatus.MEASURED, null, recommendation.executionResult(),
                requireText(measuredResult, "评估结果不能为空"), recommendation.approvalPolicy());
    }

    public PmsRecommendation get(String tenantId, String erpReferenceId) {
        return store.find(tenantId, erpReferenceId)
                .orElseThrow(() -> new BizException("PMS_RECOMMENDATION_NOT_FOUND", "建议不存在"));
    }

    public List<PmsRecommendation> list(String tenantId, String domain) {
        return store.list(tenantId, domain);
    }

    private PmsRecommendation createAccepted(PmsSubmitCommand command, PmsCallContext context) {
        Instant now = Instant.now();
        return store.save(new PmsRecommendation(
                UUID.randomUUID().toString(),
                command.recommendationId(),
                context.tenantId(),
                command.domain().toUpperCase(),
                command.recommendationType(),
                command.objectType(),
                command.targetObjectType(),
                command.targetObjectId(),
                command.content(),
                command.score(),
                command.confidence(),
                command.evidenceChainId(),
                command.dataSources() == null ? List.of() : List.copyOf(command.dataSources()),
                command.riskFlags() == null ? List.of() : List.copyOf(command.riskFlags()),
                command.explainability(),
                command.requestedAction(),
                defaultApprovalPolicy(command.objectType()),
                PmsRecommendationStatus.ACCEPTED,
                null,
                null,
                null,
                context.traceId(),
                context.idempotencyKey(),
                context.actorId(),
                context.actorType(),
                context.agentId(),
                context.scope(),
                context.purpose(),
                context.sourceSystem(),
                UUID.randomUUID().toString(),
                now,
                now));
    }

    private void validateContext(PmsCallContext context) {
        requireText(context.tenantId(), "租户不能为空");
        requireText(context.actorId(), "actor_id不能为空");
        requireText(context.actorType(), "actor_type不能为空");
        requireText(context.scope(), "scope不能为空");
        requireText(context.purpose(), "purpose不能为空");
        requireText(context.traceId(), "trace_id不能为空");
        requireText(context.idempotencyKey(), "idempotency_key不能为空");
        requireText(context.sourceSystem(), "source_system不能为空");
        requireText(context.signature(), "signature不能为空");
        if (!"PMS".equalsIgnoreCase(context.sourceSystem())) {
            throw new BizException("PMS_SOURCE_INVALID", "source_system必须为PMS");
        }
        if ("agent".equalsIgnoreCase(context.actorType())) {
            requireText(context.agentId(), "agent_id不能为空");
        }
    }

    private void validateSubmit(PmsSubmitCommand command) {
        requireText(command.recommendationId(), "recommendation_id不能为空");
        requireText(command.domain(), "domain不能为空");
        requireText(command.recommendationType(), "recommendation_type不能为空");
        requireText(command.content(), "content不能为空");
        requireText(command.evidenceChainId(), "evidence_chain_id不能为空");
        requireText(command.requestedAction(), "requested_action不能为空");
        if (!SUPPORTED_DOMAINS.contains(command.domain().toUpperCase())) {
            throw new BizException("PMS_DOMAIN_UNSUPPORTED", "不支持的ERP域");
        }
        if (command.objectType() == null || !WRITE_WHITELIST.contains(command.objectType())) {
            throw new BizException("PMS_OBJECT_TYPE_FORBIDDEN", "PMS只能写入建议、草稿、待审批动作、风险预警或洞察卡片");
        }
    }

    private PmsRecommendation saveWithStatus(PmsRecommendation source, PmsRecommendationStatus status, String rejectionReason,
            String executionResult, String measuredResult, String approvalPolicy) {
        return store.save(new PmsRecommendation(source.erpReferenceId(), source.recommendationId(), source.tenantId(), source.domain(),
                source.recommendationType(), source.objectType(), source.targetObjectType(), source.targetObjectId(), source.content(),
                source.score(), source.confidence(), source.evidenceChainId(), source.dataSources(), source.riskFlags(),
                source.explainability(), source.requestedAction(), approvalPolicy, status, rejectionReason, executionResult,
                measuredResult, source.traceId(), source.idempotencyKey(), source.actorId(), source.actorType(), source.agentId(),
                source.scope(), source.purpose(), source.sourceSystem(), source.auditId(), source.createdAt(), Instant.now()));
    }

    private void requireTransition(PmsRecommendationStatus current, PmsRecommendationStatus next) {
        if (!TRANSITIONS.getOrDefault(current, Set.of()).contains(next)) {
            throw new BizException("PMS_STATUS_TRANSITION_INVALID", "建议状态不允许流转");
        }
    }

    private static Map<PmsRecommendationStatus, Set<PmsRecommendationStatus>> transitions() {
        Map<PmsRecommendationStatus, Set<PmsRecommendationStatus>> map = new EnumMap<>(PmsRecommendationStatus.class);
        map.put(PmsRecommendationStatus.ACCEPTED, EnumSet.of(PmsRecommendationStatus.PENDING_APPROVAL, PmsRecommendationStatus.REJECTED));
        map.put(PmsRecommendationStatus.PENDING_APPROVAL, EnumSet.of(PmsRecommendationStatus.APPROVED, PmsRecommendationStatus.APPROVAL_REJECTED));
        map.put(PmsRecommendationStatus.APPROVED, EnumSet.of(PmsRecommendationStatus.EXECUTING));
        map.put(PmsRecommendationStatus.EXECUTING, EnumSet.of(PmsRecommendationStatus.PARTIALLY_EXECUTED, PmsRecommendationStatus.EXECUTED,
                PmsRecommendationStatus.FAILED));
        map.put(PmsRecommendationStatus.PARTIALLY_EXECUTED, EnumSet.of(PmsRecommendationStatus.EXECUTED, PmsRecommendationStatus.FAILED,
                PmsRecommendationStatus.ROLLED_BACK));
        map.put(PmsRecommendationStatus.FAILED, EnumSet.of(PmsRecommendationStatus.EXECUTING, PmsRecommendationStatus.ROLLED_BACK));
        map.put(PmsRecommendationStatus.EXECUTED, EnumSet.of(PmsRecommendationStatus.MEASURED, PmsRecommendationStatus.ROLLED_BACK));
        map.put(PmsRecommendationStatus.MEASURED, EnumSet.of(PmsRecommendationStatus.REVIEWED));
        return map;
    }

    private static String defaultApprovalPolicy(PmsWriteObjectType objectType) {
        return switch (objectType) {
            case RECOMMENDATION, DRAFT, PENDING_ACTION -> "MANUAL_APPROVAL_REQUIRED";
            case RISK_ALERT, INSIGHT_CARD -> "CONFIGURABLE";
        };
    }

    private static String normalizePolicy(String approvalPolicy) {
        return approvalPolicy == null || approvalPolicy.isBlank() ? "MANUAL_APPROVAL_REQUIRED" : approvalPolicy.trim();
    }

    private static String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BizException("PMS_REQUEST_INVALID", message);
        }
        return value.trim();
    }

    public record PmsCallContext(String tenantId, String actorId, String actorType, String agentId, String scope, String purpose,
            String traceId, String idempotencyKey, String sourceSystem, String signature) {}

    public record PmsSubmitCommand(String recommendationId, String domain, String recommendationType, PmsWriteObjectType objectType,
            String targetObjectType, String targetObjectId, String content, BigDecimal score, BigDecimal confidence,
            String evidenceChainId, List<String> dataSources, List<String> riskFlags, String explainability, String requestedAction) {}
}
