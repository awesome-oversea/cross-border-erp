package com.aidotnet.erp.fms.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.pms.PmsRequestGuard;
import com.aidotnet.erp.common.pms.PmsRequestGuard.PmsDataQueryContext;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.fms.application.CostAnomalyService;
import com.aidotnet.erp.fms.application.CostAnomalyService.ApproveCostAnomalyCommand;
import com.aidotnet.erp.fms.application.CostAnomalyService.SubmitCostAnomalyCommand;
import com.aidotnet.erp.fms.domain.CostAnomaly;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * FMS-PMS 成本异常接口
 * <p>
 * 描述: 接收 PMS AI 成本异常建议，并在 FMS 内形成财务审批后生效的业务闭环。
 * </p>
 */
@RestController
@RequestMapping("/fms/api/in/v1/pms")
public class FmsPmsController {

    private final CostAnomalyService costAnomalyService;
    private final PmsRequestGuard pmsRequestGuard;

    public FmsPmsController(CostAnomalyService costAnomalyService, PmsRequestGuard pmsRequestGuard) {
        this.costAnomalyService = costAnomalyService;
        this.pmsRequestGuard = pmsRequestGuard;
    }

    @PostMapping("/cost-anomaly")
    public Result<CostAnomaly> submitCostAnomaly(@RequestHeader Map<String, String> headers,
                                                 @Valid @RequestBody SubmitCostAnomalyRequest request) {
        PmsDataQueryContext context = pmsRequestGuard.validateReadOnlyRequest(headers, "DETAIL");
        validateScope(context, request.storeId(), request.marketplaceId());
        return inTenant(context.tenantId(), () -> Result.ok(costAnomalyService.submitOrGet(context.tenantId(),
                new SubmitCostAnomalyCommand(
                        request.erpReferenceId(),
                        context.idempotencyKey(),
                        request.anomalyType(),
                        request.sourceType(),
                        request.sourceId(),
                        request.dimensionType(),
                        request.dimensionId(),
                        request.sellerSku(),
                        request.storeId(),
                        request.channelCode(),
                        request.marketplaceId(),
                        request.costType(),
                        request.suggestedAmount(),
                        request.currency(),
                        request.autoAggregate(),
                        request.reason(),
                        request.evidence(),
                        context.actorId(),
                        context.actorType(),
                        context.traceId(),
                        context.purpose(),
                        context.rawScope()))));
    }

    @GetMapping("/cost-anomaly")
    public Result<List<CostAnomaly>> listCostAnomalies(@RequestParam(required = false) String status,
                                                       @RequestParam(required = false) String sellerSku,
                                                       @RequestParam(required = false) String storeId) {
        return Result.ok(costAnomalyService.list(currentTenant(), status, sellerSku, storeId));
    }

    @GetMapping("/cost-anomaly/{anomalyId}")
    public Result<CostAnomaly> getCostAnomaly(@PathVariable String anomalyId) {
        return Result.ok(costAnomalyService.get(currentTenant(), anomalyId));
    }

    @PostMapping("/cost-anomaly/{anomalyId}/approve")
    public Result<CostAnomaly> approveCostAnomaly(@PathVariable String anomalyId,
                                                  @Valid @RequestBody ApproveCostAnomalyRequest request) {
        return Result.ok(costAnomalyService.approve(currentTenant(), anomalyId,
                new ApproveCostAnomalyCommand(request.approvedBy(), request.approvedAt())));
    }

    private void validateScope(PmsDataQueryContext context, String storeId, String marketplaceId) {
        validateDimensionScope("store", storeId, context.scopeValues("store"));
        validateDimensionScope("marketplace", marketplaceId, context.scopeValues("marketplace"));
    }

    private void validateDimensionScope(String dimension, String actualValue, Set<String> scopedValues) {
        if (scopedValues == null || scopedValues.isEmpty() || actualValue == null || actualValue.isBlank()) {
            return;
        }
        boolean matched = scopedValues.stream().anyMatch(scopeValue -> scopeValue.equalsIgnoreCase(actualValue.trim()));
        if (!matched) {
            throw new BizException("FMS_COST_ANOMALY_SCOPE_INVALID", "PMS 请求超出允许的数据范围: " + dimension);
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

    public record SubmitCostAnomalyRequest(
            String erpReferenceId,
            @NotBlank String anomalyType,
            @NotBlank String sourceType,
            @NotBlank String sourceId,
            String dimensionType,
            String dimensionId,
            @NotBlank String sellerSku,
            String storeId,
            String channelCode,
            String marketplaceId,
            @NotBlank String costType,
            @Positive BigDecimal suggestedAmount,
            @NotBlank String currency,
            boolean autoAggregate,
            String reason,
            Map<String, Object> evidence) {}

    public record ApproveCostAnomalyRequest(@NotBlank String approvedBy, Instant approvedAt) {}
}
