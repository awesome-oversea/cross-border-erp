package com.aidotnet.erp.common.cost;

import com.aidotnet.erp.common.api.Result;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fms/api/v1/cost-engine")
public class CostEngineController {

    private final CostEngineService costEngineService;

    public CostEngineController(CostEngineService costEngineService) {
        this.costEngineService = costEngineService;
    }

    @PostMapping("/events/collect")
    public Result<Void> collectEvent(@RequestBody Map<String, Object> request) {
        costEngineService.collectCostEvent(
                (String) request.get("orderId"),
                (String) request.get("sku"),
                (String) request.get("costType"),
                new BigDecimal(request.get("amount").toString()),
                (String) request.getOrDefault("currency", "USD"),
                (String) request.getOrDefault("source", "MANUAL"),
                null
        );
        return Result.ok(null);
    }

    @GetMapping("/breakdown/{orderId}")
    public Result<CostEngineService.CostBreakdown> getBreakdown(@PathVariable String orderId) {
        return Result.ok(costEngineService.generateBreakdown(orderId));
    }

    @PostMapping("/breakdown/generate")
    public Result<CostEngineService.CostBreakdown> generateBreakdown(@RequestBody Map<String, String> request) {
        return Result.ok(costEngineService.generateBreakdown(request.get("orderId")));
    }

    @GetMapping("/breakdown/sku/{sku}")
    public Result<CostEngineService.SkuCostBreakdown> getSkuBreakdown(@PathVariable String sku) {
        return Result.ok(costEngineService.generateSkuBreakdown(sku));
    }

    @PostMapping("/allocate")
    public Result<Void> allocate(@RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        java.util.List<String> targets = (java.util.List<String>) request.get("targetSkus");
        costEngineService.allocateCost(
                (String) request.get("sourceSku"),
                targets,
                new BigDecimal(request.get("totalAmount").toString()),
                (String) request.getOrDefault("method", "EQUAL")
        );
        return Result.ok(null);
    }

    @PostMapping("/fifo/calculate")
    public Result<CostEngineService.FifoCalculationResult> fifoCalculate(@RequestBody Map<String, Object> request) {
        return Result.ok(costEngineService.calculateFifo(
                (String) request.get("sku"),
                ((Number) request.get("quantitySold")).intValue()
        ));
    }

    @GetMapping("/trend/{sku}")
    public Result<CostEngineService.CostTrend> getTrend(
            @PathVariable String sku,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return Result.ok(costEngineService.getCostTrend(sku, startDate, endDate));
    }

    @PostMapping("/ai/detect-anomaly")
    public Result<CostEngineService.AnomalyDetectionResult> detectAnomaly(@RequestBody Map<String, Object> request) {
        BigDecimal threshold = request.containsKey("threshold")
                ? new BigDecimal(request.get("threshold").toString())
                : new BigDecimal("2.0");
        return Result.ok(costEngineService.detectAnomaly((String) request.get("sku"), threshold));
    }
}
