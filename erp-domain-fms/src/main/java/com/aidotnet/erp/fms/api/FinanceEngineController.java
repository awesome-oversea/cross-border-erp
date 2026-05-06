package com.aidotnet.erp.fms.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.fms.application.CostAggregationEngine;
import com.aidotnet.erp.fms.application.ProfitCalculationEngine;
import com.aidotnet.erp.fms.domain.CostAggregationRule;
import com.aidotnet.erp.fms.domain.CostAllocationResult;
import com.aidotnet.erp.fms.domain.ProfitDeviationAlert;
import com.aidotnet.erp.fms.domain.ProfitResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * FMS财务引擎控制器
 * <p>
 * 描述: 财务域引擎接口，提供成本归集、利润核算、计费策略等引擎能力。
 * </p>
 * <p>
 * 路径规范: /fms/api/in/v1/engine — 内部方向(in)，v1版本
 * </p>
 *
 * @author ERP系统
 * @see CostAggregationEngine
 * @see ProfitCalculationEngine
 * @see BillingStrategyService
 */
@RestController
@RequestMapping("/fms/api/in/v1/engine")
public class FinanceEngineController {

    private final CostAggregationEngine costAggregationEngine;
    private final ProfitCalculationEngine profitCalculationEngine;

    public FinanceEngineController(CostAggregationEngine costAggregationEngine,
                                   ProfitCalculationEngine profitCalculationEngine) {
        this.costAggregationEngine = costAggregationEngine;
        this.profitCalculationEngine = profitCalculationEngine;
    }

    @PostMapping("/cost-rules")
    public Result<CostAggregationRule> createRule(@Valid @RequestBody CreateCostRuleRequest request) {
        return Result.ok(costAggregationEngine.createRule(currentTenant(), new CostAggregationEngine.CreateAggregationRuleCommand(
                request.ruleName(), request.costSource(), request.costCategory(), request.allocationMethod(),
                request.allocationBasis(), request.targetDimension(), request.priority())));
    }

    @PostMapping("/cost-events/{costEventId}/aggregate")
    public Result<List<CostAllocationResult>> aggregateCostEvent(@PathVariable String costEventId) {
        return Result.ok(costAggregationEngine.aggregateCostEvent(currentTenant(), costEventId));
    }

    @PostMapping("/profit-results")
    public Result<ProfitResult> calculateProfit(@Valid @RequestBody CalculateProfitRequest request) {
        return Result.ok(profitCalculationEngine.calculateProfit(currentTenant(), new ProfitCalculationEngine.CalculateProfitCommand(
                request.dimensionType(), request.dimensionId(), request.sellerSku(), request.orderId(),
                request.storeId(), request.marketplaceId(), request.revenue(), request.currency())));
    }

    @GetMapping("/profit-results")
    public Result<List<ProfitResult>> listProfitResults(@NotBlank String dimensionType, @NotBlank String dimensionId) {
        return Result.ok(profitCalculationEngine.listProfitResults(currentTenant(), dimensionType, dimensionId));
    }

    @PostMapping("/profit-deviation-alerts/detect")
    public Result<List<ProfitDeviationAlert>> detectAlerts(@Valid @RequestBody DetectDeviationAlertRequest request) {
        return Result.ok(profitCalculationEngine.detectDeviations(currentTenant(), request.threshold()));
    }

    @GetMapping("/profit-deviation-alerts")
    public Result<List<ProfitDeviationAlert>> listAlerts(String status) {
        return Result.ok(profitCalculationEngine.listAlerts(currentTenant(), status));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record CreateCostRuleRequest(@NotBlank String ruleName,
                                        String costSource,
                                        String costCategory,
                                        @NotBlank String allocationMethod,
                                        String allocationBasis,
                                        @NotBlank String targetDimension,
                                        @Min(0) int priority) {}

    public record CalculateProfitRequest(@NotBlank String dimensionType,
                                         @NotBlank String dimensionId,
                                         String sellerSku,
                                         String orderId,
                                         String storeId,
                                         String marketplaceId,
                                         @NotNull BigDecimal revenue,
                                         @NotBlank String currency) {}

    public record DetectDeviationAlertRequest(@NotNull BigDecimal threshold) {}
}
