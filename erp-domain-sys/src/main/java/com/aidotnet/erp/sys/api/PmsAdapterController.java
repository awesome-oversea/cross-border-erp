package com.aidotnet.erp.sys.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.exception.ErrorCode;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.sys.application.PmsIntegrationService;
import com.aidotnet.erp.sys.application.PmsRecommendationService;
import com.aidotnet.erp.sys.application.PmsRecommendationService.PmsCallContext;
import com.aidotnet.erp.sys.application.PmsRecommendationService.PmsSubmitCommand;
import com.aidotnet.erp.sys.domain.AIFeatureToggle;
import com.aidotnet.erp.sys.domain.DataTrustLevel;
import com.aidotnet.erp.sys.domain.PmsDataTrustRule;
import com.aidotnet.erp.sys.domain.PmsDraftDocument;
import com.aidotnet.erp.sys.domain.PmsFeedback;
import com.aidotnet.erp.sys.domain.PmsRecommendation;
import com.aidotnet.erp.sys.domain.PmsWriteObjectType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pms/v1")
public class PmsAdapterController {

    private final PmsIntegrationService integrationService;
    private final PmsRecommendationService recommendationService;

    public PmsAdapterController(PmsIntegrationService integrationService, PmsRecommendationService recommendationService) {
        this.integrationService = integrationService;
        this.recommendationService = recommendationService;
    }

    @PostMapping("/recommendations")
    public Result<PmsRecommendation> submitRecommendation(@Valid @RequestBody SubmitRequest request,
            @RequestHeader Map<String, String> headers) {
        integrationService.validateHeaders(headers);
        integrationService.validateWriteWhitelist(request.objectType());
        integrationService.validateDomain(request.domain());
        String tenantId = requiredHeader(headers, "tenant_id");
        return inTenant(tenantId, () -> {
            integrationService.validateDataSovereignty(tenantId, request.domain(), request.objectType());
            if (!integrationService.checkAIFeatureEnabled(tenantId, request.domain())) {
                throw new BizException(ErrorCode.PMS_FEATURE_DISABLED, "该域AI功能已关闭");
            }
            PmsSubmitCommand command = new PmsSubmitCommand(
                    request.recommendationId(), request.domain(), request.recommendationType(),
                    request.objectType(), request.targetObjectType(), request.targetObjectId(),
                    request.content(), request.score(), request.confidence(),
                    request.evidenceChainId(), request.dataSources(), request.riskFlags(),
                    request.explainability(), request.requestedAction());
            return Result.ok(recommendationService.submit(command, toContext(headers)));
        });
    }

    @GetMapping("/recommendations")
    public Result<List<PmsRecommendation>> listRecommendations(@RequestParam(required = false) String domain,
            @RequestHeader Map<String, String> headers) {
        integrationService.validateHeaders(headers);
        String tenantId = requiredHeader(headers, "tenant_id");
        return inTenant(tenantId, () -> Result.ok(recommendationService.list(tenantId, domain)));
    }

    @GetMapping("/recommendations/{erpReferenceId}")
    public Result<PmsRecommendation> getRecommendation(@PathVariable String erpReferenceId,
            @RequestHeader Map<String, String> headers) {
        integrationService.validateHeaders(headers);
        String tenantId = requiredHeader(headers, "tenant_id");
        return inTenant(tenantId, () -> Result.ok(recommendationService.get(tenantId, erpReferenceId)));
    }

    @PostMapping("/recommendations/{erpReferenceId}/drafts")
    public Result<PmsDraftDocument> generateDraft(@PathVariable String erpReferenceId,
            @Valid @RequestBody CreateDraftRequest request,
            @RequestHeader Map<String, String> headers) {
        integrationService.validateHeaders(headers);
        String tenantId = requiredHeader(headers, "tenant_id");
        return inTenant(tenantId, () -> Result.ok(integrationService.generateDraft(tenantId, erpReferenceId,
                request.domain(), request.draftType(), request.targetBusinessType(),
                request.contentJson(), request.trustLevel(),
                requiredHeader(headers, "actor_id"), header(headers, "actor_type"),
                header(headers, "agent_id"), requiredHeader(headers, "scope"),
                requiredHeader(headers, "purpose"), requiredHeader(headers, "trace_id"))));
    }

    @PatchMapping("/drafts/{draftId}/approve")
    public Result<PmsDraftDocument> approveDraft(@PathVariable String draftId,
            @RequestBody ApproveDraftRequest request,
            @RequestHeader Map<String, String> headers) {
        integrationService.validateHeaders(headers);
        String tenantId = requiredHeader(headers, "tenant_id");
        return inTenant(tenantId, () -> Result.ok(integrationService.approveDraft(
                tenantId, draftId, request.approvedBy())));
    }

    @PatchMapping("/drafts/{draftId}/execute")
    public Result<PmsDraftDocument> executeDraft(@PathVariable String draftId,
            @RequestBody ExecuteDraftRequest request,
            @RequestHeader Map<String, String> headers) {
        integrationService.validateHeaders(headers);
        String tenantId = requiredHeader(headers, "tenant_id");
        return inTenant(tenantId, () -> Result.ok(integrationService.executeDraft(
                tenantId, draftId, request.executionResult())));
    }

    @PostMapping("/recommendations/{erpReferenceId}/feedback")
    public Result<PmsFeedback> sendFeedback(@PathVariable String erpReferenceId,
            @Valid @RequestBody FeedbackRequest request,
            @RequestHeader Map<String, String> headers) {
        integrationService.validateHeaders(headers);
        String tenantId = requiredHeader(headers, "tenant_id");
        return inTenant(tenantId, () -> Result.ok(integrationService.sendFeedback(tenantId, erpReferenceId,
                request.feedbackType(), request.executionStatus(), request.businessResult(),
                request.businessMetricsJson(), request.failureReason(),
                requiredHeader(headers, "actor_id"), requiredHeader(headers, "trace_id"))));
    }

    @GetMapping("/feedbacks/pending")
    public Result<List<PmsFeedback>> listPendingFeedbacks(@RequestHeader Map<String, String> headers) {
        integrationService.validateHeaders(headers);
        String tenantId = requiredHeader(headers, "tenant_id");
        return inTenant(tenantId, () -> Result.ok(integrationService.listPendingFeedbacks(tenantId)));
    }

    @PatchMapping("/feedbacks/{feedbackId}/delivered")
    public Result<PmsFeedback> markFeedbackDelivered(@PathVariable String feedbackId,
            @RequestHeader Map<String, String> headers) {
        integrationService.validateHeaders(headers);
        String tenantId = requiredHeader(headers, "tenant_id");
        return inTenant(tenantId, () -> Result.ok(integrationService.markFeedbackDelivered(
                tenantId, feedbackId)));
    }

    @PostMapping("/data-trust-rules")
    public Result<PmsDataTrustRule> createDataTrustRule(@Valid @RequestBody DataTrustRuleRequest request,
            @RequestHeader Map<String, String> headers) {
        String tenantId = requiredHeader(headers, "tenant_id");
        return inTenant(tenantId, () -> Result.ok(integrationService.createDataTrustRule(tenantId, request.domain(),
                request.objectType(), request.trustLevel(), request.description(),
                request.allowedActions(), request.canOverwriteErp())));
    }

    @GetMapping("/data-trust-rules")
    public Result<List<PmsDataTrustRule>> listDataTrustRules(@RequestHeader Map<String, String> headers) {
        String tenantId = requiredHeader(headers, "tenant_id");
        return inTenant(tenantId, () -> Result.ok(integrationService.listDataTrustRules(tenantId)));
    }

    @PostMapping("/ai-toggles")
    public Result<AIFeatureToggle> setAIFeatureToggle(@Valid @RequestBody AIToggleRequest request,
            @RequestHeader Map<String, String> headers) {
        String tenantId = requiredHeader(headers, "tenant_id");
        return inTenant(tenantId, () -> Result.ok(integrationService.setAIFeatureToggle(tenantId, request.featureCode(),
                request.featureName(), request.domain(), request.enabled(),
                request.description(), request.configJson())));
    }

    @GetMapping("/ai-toggles")
    public Result<List<AIFeatureToggle>> listAIToggles(@RequestHeader Map<String, String> headers) {
        String tenantId = requiredHeader(headers, "tenant_id");
        return inTenant(tenantId, () -> Result.ok(integrationService.listAIFeatureToggles(tenantId)));
    }

    private PmsCallContext toContext(Map<String, String> headers) {
        return new PmsCallContext(
                requiredHeader(headers, "tenant_id"), requiredHeader(headers, "actor_id"),
                requiredHeader(headers, "actor_type"), header(headers, "agent_id"),
                requiredHeader(headers, "scope"), requiredHeader(headers, "purpose"),
                requiredHeader(headers, "trace_id"), requiredHeader(headers, "idempotency_key"),
                requiredHeader(headers, "source_system"), requiredHeader(headers, "signature"));
    }

    private <T> T inTenant(String tenantId, Supplier<T> action) {
        String previousTenantId = TenantContext.getTenantId();
        TenantContext.setTenantId(tenantId);
        try {
            return action.get();
        } finally {
            TenantContext.setTenantId(previousTenantId);
        }
    }

    private static String requiredHeader(Map<String, String> headers, String name) {
        String value = header(headers, name);
        if (value == null || value.isBlank()) {
            throw new BizException(ErrorCode.PMS_HEADER_MISSING, name + "不能为空");
        }
        return value.trim();
    }

    private static String header(Map<String, String> headers, String name) {
        return headers.entrySet().stream()
                .filter(e -> e.getKey().equalsIgnoreCase(name))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    public record SubmitRequest(
            @JsonProperty("recommendation_id") @NotBlank String recommendationId,
            @NotBlank String domain,
            @JsonProperty("recommendation_type") @NotBlank String recommendationType,
            @JsonProperty("object_type") PmsWriteObjectType objectType,
            @JsonProperty("target_object_type") String targetObjectType,
            @JsonProperty("target_object_id") String targetObjectId,
            @NotBlank String content,
            BigDecimal score, BigDecimal confidence,
            @JsonProperty("evidence_chain_id") @NotBlank String evidenceChainId,
            @JsonProperty("data_sources") List<String> dataSources,
            @JsonProperty("risk_flags") List<String> riskFlags,
            String explainability,
            @JsonProperty("requested_action") @NotBlank String requestedAction) {}

    public record CreateDraftRequest(
            @NotBlank String domain,
            @JsonProperty("draft_type") @NotBlank String draftType,
            @JsonProperty("target_business_type") @NotBlank String targetBusinessType,
            @JsonProperty("content_json") @NotBlank String contentJson,
            @JsonProperty("trust_level") DataTrustLevel trustLevel) {}

    public record ApproveDraftRequest(@JsonProperty("approved_by") String approvedBy) {}

    public record ExecuteDraftRequest(@JsonProperty("execution_result") String executionResult) {}

    public record FeedbackRequest(
            @JsonProperty("feedback_type") @NotBlank String feedbackType,
            @JsonProperty("execution_status") @NotBlank String executionStatus,
            @JsonProperty("business_result") String businessResult,
            @JsonProperty("business_metrics_json") String businessMetricsJson,
            @JsonProperty("failure_reason") String failureReason) {}

    public record DataTrustRuleRequest(
            @NotBlank String domain,
            @JsonProperty("object_type") @NotBlank String objectType,
            @JsonProperty("trust_level") DataTrustLevel trustLevel,
            String description,
            @JsonProperty("allowed_actions") List<String> allowedActions,
            @JsonProperty("can_overwrite_erp") boolean canOverwriteErp) {}

    public record AIToggleRequest(
            @JsonProperty("feature_code") @NotBlank String featureCode,
            @JsonProperty("feature_name") String featureName,
            @NotBlank String domain,
            boolean enabled,
            String description,
            @JsonProperty("config_json") String configJson) {}
}
