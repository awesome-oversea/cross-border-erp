package com.aidotnet.erp.common.billing;

import com.aidotnet.erp.common.api.Result;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fms/api/v1/billing")
public class BillingStrategyController {

    private final BillingStrategyService billingService;

    public BillingStrategyController(BillingStrategyService billingService) {
        this.billingService = billingService;
    }

    @GetMapping("/platform-fees")
    public Result<Map<String, Object>> getPlatformFees() {
        return Result.ok(Map.of("ruleCount", 0));
    }

    @PostMapping("/simulate")
    public Result<BillingStrategyService.FeeSimulation> simulate(@RequestBody Map<String, Object> request) {
        BillingStrategyService.FeeSimulation result = billingService.simulate(
                (String) request.get("platform"),
                (String) request.get("category"),
                new BigDecimal(request.get("salePrice").toString()),
                request.containsKey("quantity") ? ((Number) request.get("quantity")).intValue() : 1,
                new BigDecimal(request.getOrDefault("shippingCost", "0").toString()),
                new BigDecimal(request.getOrDefault("purchaseCost", "0").toString())
        );
        return Result.ok(result);
    }

    @GetMapping("/warehouse-fees")
    public Result<Map<String, Object>> getWarehouseFees() {
        return Result.ok(Map.of("ruleCount", 0));
    }

    @GetMapping("/freight-pool")
    public Result<Map<String, Object>> getFreightPool() {
        return Result.ok(Map.of());
    }

    @PostMapping("/freight-allocate")
    public Result<Map<String, BigDecimal>> allocateFreight(@RequestBody Map<String, Object> request) {
        return Result.ok(Map.of());
    }

    @GetMapping("/packaging-costs")
    public Result<Map<String, Object>> getPackagingCosts() {
        return Result.ok(Map.of());
    }

    @PostMapping("/fba-head-cost")
    public Result<Map<String, BigDecimal>> calculateFbaHeadCost(@RequestBody Map<String, Object> request) {
        return Result.ok(Map.of("totalCost", BigDecimal.ZERO));
    }
}
