package com.aidotnet.erp.common.strategy;

import com.aidotnet.erp.common.api.Result;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tms/api/v1/strategy")
public class LogisticsStrategyController {

    private final LogisticsStrategyService logisticsStrategyService;

    public LogisticsStrategyController(LogisticsStrategyService logisticsStrategyService) {
        this.logisticsStrategyService = logisticsStrategyService;
    }

    @PostMapping("/select")
    public Result<Map<String, Object>> selectLogistics(@RequestBody Map<String, Object> request) {
        return Result.ok(Map.of("carrierCode", "DHL", "serviceCode", "EXPRESS", "estimatedDays", 5));
    }

    @PostMapping("/calculate")
    public Result<LogisticsStrategyService.FreightEstimation> calculate(@RequestBody Map<String, Object> request) {
        LogisticsStrategyService.FreightEstimation estimation = logisticsStrategyService.estimateFreight(
                (String) request.get("originCountry"),
                (String) request.get("destinationCountry"),
                new BigDecimal(request.get("weight").toString()),
                new BigDecimal(request.getOrDefault("length", "10").toString()),
                new BigDecimal(request.getOrDefault("width", "10").toString()),
                new BigDecimal(request.getOrDefault("height", "10").toString())
        );
        return Result.ok(estimation);
    }

    @GetMapping("/rules")
    public Result<Map<String, Object>> getRules() {
        return Result.ok(Map.of("ruleCount", 0));
    }
}
