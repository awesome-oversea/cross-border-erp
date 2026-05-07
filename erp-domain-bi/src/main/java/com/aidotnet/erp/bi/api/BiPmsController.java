package com.aidotnet.erp.bi.api;

import com.aidotnet.erp.bi.application.BiPmsService;
import com.aidotnet.erp.bi.application.BiPmsService.ApproveTrendPredictionCommand;
import com.aidotnet.erp.bi.application.BiPmsService.SubmitTrendPredictionCommand;
import com.aidotnet.erp.bi.domain.CockpitTrend;
import com.aidotnet.erp.bi.domain.PmsInsight;
import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.pms.PmsRequestGuard;
import com.aidotnet.erp.common.pms.PmsRequestGuard.PmsDataQueryContext;
import com.aidotnet.erp.common.tenant.TenantContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * BI-PMS 趋势预测建议入站控制器。
 */
@RestController
@RequestMapping("/bi/api/in/v1/pms")
public class BiPmsController {

    private final BiPmsService biPmsService;
    private final PmsRequestGuard pmsRequestGuard;

    public BiPmsController(BiPmsService biPmsService, PmsRequestGuard pmsRequestGuard) {
        this.biPmsService = biPmsService;
        this.pmsRequestGuard = pmsRequestGuard;
    }

    @PostMapping("/trend-predict")
    public Result<PmsInsight> submitTrendPrediction(@RequestHeader Map<String, String> headers,
                                                    @Valid @RequestBody SubmitTrendPredictionRequest request) {
        PmsDataQueryContext context = pmsRequestGuard.validateReadOnlyRequest(headers, "DETAIL");
        validateScope(context, request.category());
        return inTenant(context.tenantId(), () -> Result.ok(biPmsService.submitOrGet(context.tenantId(),
                new SubmitTrendPredictionCommand(
                        request.erpReferenceId(),
                        context.idempotencyKey(),
                        request.title(),
                        request.summary(),
                        request.insightType(),
                        request.severity(),
                        request.category(),
                        request.metricCode(),
                        request.metricName(),
                        request.targetUserId(),
                        request.trendPeriod(),
                        request.predictionPoints(),
                        request.insightData(),
                        request.suggestion(),
                        request.actionUrl(),
                        request.validUntil(),
                        context.actorId(),
                        context.actorType(),
                        context.traceId(),
                        context.purpose(),
                        context.rawScope()))));
    }

    @GetMapping("/trend-predict/{insightId}")
    public Result<PmsInsight> getTrendPrediction(@PathVariable String insightId) {
        return Result.ok(biPmsService.get(currentTenant(), insightId));
    }

    @PostMapping("/trend-predict/{insightId}/approve")
    public Result<PmsInsight> approveTrendPrediction(@PathVariable String insightId,
                                                     @Valid @RequestBody ApproveTrendPredictionRequest request) {
        return Result.ok(biPmsService.approve(currentTenant(), insightId,
                new ApproveTrendPredictionCommand(request.approvedBy(), request.approvedAt())));
    }

    private void validateScope(PmsDataQueryContext context, String category) {
        validateDimensionScope("category", category, context.scopeValues("category"));
    }

    private void validateDimensionScope(String dimension, String actualValue, Set<String> scopedValues) {
        if (scopedValues == null || scopedValues.isEmpty() || actualValue == null || actualValue.isBlank()) {
            return;
        }
        boolean matched = scopedValues.stream().anyMatch(scopeValue -> scopeValue.equalsIgnoreCase(actualValue.trim()));
        if (!matched) {
            throw new BizException("BI_PMS_SCOPE_INVALID", "PMS 请求超出允许的数据范围: " + dimension);
        }
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
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

    public record SubmitTrendPredictionRequest(
            @NotBlank String erpReferenceId,
            @NotBlank String title,
            String summary,
            @NotBlank String insightType,
            @NotBlank String severity,
            @NotBlank String category,
            @NotBlank String metricCode,
            @NotBlank String metricName,
            @NotBlank String targetUserId,
            String trendPeriod,
            @NotEmpty List<CockpitTrend.DataPoint> predictionPoints,
            Map<String, Object> insightData,
            String suggestion,
            String actionUrl,
            Instant validUntil) {}

    public record ApproveTrendPredictionRequest(@NotBlank String approvedBy, Instant approvedAt) {}
}
