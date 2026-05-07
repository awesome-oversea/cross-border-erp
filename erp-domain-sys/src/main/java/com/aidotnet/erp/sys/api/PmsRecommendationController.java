package com.aidotnet.erp.sys.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.sys.application.PmsRecommendationService;
import com.aidotnet.erp.sys.application.PmsRecommendationService.PmsCallContext;
import com.aidotnet.erp.sys.application.PmsRecommendationService.PmsSubmitCommand;
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
@RequestMapping("/sys/api/in/v1/pms/recommendations")
public class PmsRecommendationController {

    private final PmsRecommendationService recommendationService;

    public PmsRecommendationController(PmsRecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping
    public Result<PmsRecommendation> submit(@Valid @RequestBody SubmitRecommendationRequest request,
            @RequestHeader Map<String, String> headers) {
        String tenantId = requiredHeader(headers, "tenant_id");
        return inTenant(tenantId, () -> Result.ok(recommendationService.submit(request.toCommand(), context(headers))));
    }

    @GetMapping
    public Result<List<PmsRecommendation>> list(@RequestParam(required = false) String domain,
            @RequestHeader Map<String, String> headers) {
        String tenantId = requiredHeader(headers, "tenant_id");
        return inTenant(tenantId, () -> Result.ok(recommendationService.list(tenantId, domain)));
    }

    @GetMapping("/{erpReferenceId}")
    public Result<PmsRecommendation> detail(@PathVariable String erpReferenceId, @RequestHeader Map<String, String> headers) {
        String tenantId = requiredHeader(headers, "tenant_id");
        return inTenant(tenantId, () -> Result.ok(recommendationService.get(tenantId, erpReferenceId)));
    }

    @PatchMapping("/{erpReferenceId}/submit-approval")
    public Result<PmsRecommendation> submitApproval(@PathVariable String erpReferenceId, @RequestBody ApprovalRequest request,
            @RequestHeader Map<String, String> headers) {
        String tenantId = requiredHeader(headers, "tenant_id");
        return inTenant(tenantId, () -> Result.ok(recommendationService.submitForApproval(tenantId, erpReferenceId,
                request == null ? null : request.approvalPolicy())));
    }

    @PatchMapping("/{erpReferenceId}/approve")
    public Result<PmsRecommendation> approve(@PathVariable String erpReferenceId, @RequestHeader Map<String, String> headers) {
        String tenantId = requiredHeader(headers, "tenant_id");
        return inTenant(tenantId, () -> Result.ok(recommendationService.approve(tenantId, erpReferenceId)));
    }

    @PatchMapping("/{erpReferenceId}/reject-approval")
    public Result<PmsRecommendation> rejectApproval(@PathVariable String erpReferenceId, @RequestBody RejectRequest request,
            @RequestHeader Map<String, String> headers) {
        String tenantId = requiredHeader(headers, "tenant_id");
        return inTenant(tenantId, () -> Result.ok(recommendationService.rejectApproval(tenantId, erpReferenceId,
                request == null ? null : request.reason())));
    }

    @PatchMapping("/{erpReferenceId}/start-execution")
    public Result<PmsRecommendation> startExecution(@PathVariable String erpReferenceId, @RequestHeader Map<String, String> headers) {
        String tenantId = requiredHeader(headers, "tenant_id");
        return inTenant(tenantId, () -> Result.ok(recommendationService.startExecution(tenantId, erpReferenceId)));
    }

    @PatchMapping("/{erpReferenceId}/complete-execution")
    public Result<PmsRecommendation> completeExecution(@PathVariable String erpReferenceId, @RequestBody ExecutionRequest request,
            @RequestHeader Map<String, String> headers) {
        String tenantId = requiredHeader(headers, "tenant_id");
        return inTenant(tenantId, () -> Result.ok(recommendationService.completeExecution(tenantId, erpReferenceId,
                request == null ? null : request.executionResult())));
    }

    @PatchMapping("/{erpReferenceId}/fail-execution")
    public Result<PmsRecommendation> failExecution(@PathVariable String erpReferenceId, @RequestBody ExecutionRequest request,
            @RequestHeader Map<String, String> headers) {
        String tenantId = requiredHeader(headers, "tenant_id");
        return inTenant(tenantId, () -> Result.ok(recommendationService.failExecution(tenantId, erpReferenceId,
                request == null ? null : request.executionResult())));
    }

    @PatchMapping("/{erpReferenceId}/measure")
    public Result<PmsRecommendation> measure(@PathVariable String erpReferenceId, @RequestBody MeasureRequest request,
            @RequestHeader Map<String, String> headers) {
        String tenantId = requiredHeader(headers, "tenant_id");
        return inTenant(tenantId, () -> Result.ok(recommendationService.measure(tenantId, erpReferenceId,
                request == null ? null : request.measuredResult())));
    }

    private PmsCallContext context(Map<String, String> headers) {
        return new PmsCallContext(requiredHeader(headers, "tenant_id"), requiredHeader(headers, "actor_id"),
                requiredHeader(headers, "actor_type"), header(headers, "agent_id"), requiredHeader(headers, "scope"),
                requiredHeader(headers, "purpose"), requiredHeader(headers, "trace_id"),
                requiredHeader(headers, "idempotency_key"), requiredHeader(headers, "source_system"),
                requiredHeader(headers, "signature"));
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
            throw new BizException("PMS_REQUEST_INVALID", name + "不能为空");
        }
        return value.trim();
    }

    private static String header(Map<String, String> headers, String name) {
        return headers.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    public record SubmitRecommendationRequest(
            @JsonProperty("recommendation_id") @NotBlank String recommendationId,
            @NotBlank String domain,
            @JsonProperty("recommendation_type") @NotBlank String recommendationType,
            @JsonProperty("object_type") PmsWriteObjectType objectType,
            @JsonProperty("target_object_type") String targetObjectType,
            @JsonProperty("target_object_id") String targetObjectId,
            @NotBlank String content,
            BigDecimal score,
            BigDecimal confidence,
            @JsonProperty("evidence_chain_id") @NotBlank String evidenceChainId,
            @JsonProperty("data_sources") List<String> dataSources,
            @JsonProperty("risk_flags") List<String> riskFlags,
            String explainability,
            @JsonProperty("requested_action") @NotBlank String requestedAction) {

        PmsSubmitCommand toCommand() {
            return new PmsSubmitCommand(recommendationId, domain, recommendationType, objectType, targetObjectType, targetObjectId,
                    content, score, confidence, evidenceChainId, dataSources, riskFlags, explainability, requestedAction);
        }
    }

    public record ApprovalRequest(@JsonProperty("approval_policy") String approvalPolicy) {}

    public record RejectRequest(String reason) {}

    public record ExecutionRequest(@JsonProperty("execution_result") String executionResult) {}

    public record MeasureRequest(@JsonProperty("measured_result") String measuredResult) {}
}
