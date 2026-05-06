package com.aidotnet.erp.common.ads;

import com.aidotnet.erp.common.api.Result;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ads/api/v1/optimization")
public class AdsOptimizationController {

    private final AdsOptimizationService optimizationService;

    public AdsOptimizationController(AdsOptimizationService optimizationService) {
        this.optimizationService = optimizationService;
    }

    @PostMapping("/budget-allocate")
    public Result<AdsOptimizationService.BudgetAllocationResult> allocateBudget(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        Map<String, BigDecimal> metrics = (Map<String, BigDecimal>) request.get("metrics");
        return Result.ok(optimizationService.allocateBudget(
                (String) request.get("campaignId"),
                new BigDecimal(request.get("totalBudget").toString()),
                (String) request.getOrDefault("strategy", "ACOS_OPTIMIZE"),
                metrics != null ? metrics : Map.of()
        ));
    }

    @PostMapping("/pms/execute")
    public Result<AdsOptimizationService.PmsInstruction> executePmsInstruction(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) request.getOrDefault("params", Map.of());
        return Result.ok(optimizationService.executePmsInstruction(
                (String) request.get("campaignId"),
                (String) request.get("instructionType"),
                params
        ));
    }

    @PostMapping("/pms/{instructionId}/rollback")
    public Result<AdsOptimizationService.PmsInstruction> rollbackPmsInstruction(@PathVariable String instructionId) {
        return Result.ok(optimizationService.rollbackPmsInstruction(instructionId));
    }

    @GetMapping("/pms/{campaignId}/instructions")
    public Result<List<AdsOptimizationService.PmsInstruction>> getCampaignInstructions(@PathVariable String campaignId) {
        return Result.ok(optimizationService.getCampaignInstructions(campaignId));
    }

    @PostMapping("/effect/analyze")
    public Result<AdsOptimizationService.EffectAnalysis> analyzeEffect(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        Map<String, BigDecimal> before = (Map<String, BigDecimal>) request.get("beforeMetrics");
        @SuppressWarnings("unchecked")
        Map<String, BigDecimal> after = (Map<String, BigDecimal>) request.get("afterMetrics");
        return Result.ok(optimizationService.analyzeEffect(
                (String) request.get("campaignId"),
                before != null ? before : Map.of(),
                after != null ? after : Map.of()
        ));
    }

    @GetMapping("/effect/{campaignId}/history")
    public Result<List<AdsOptimizationService.EffectSnapshot>> getEffectHistory(@PathVariable String campaignId) {
        return Result.ok(optimizationService.getEffectHistory(campaignId));
    }
}
