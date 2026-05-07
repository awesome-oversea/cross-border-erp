package com.aidotnet.erp.common.strategy;

import com.aidotnet.erp.common.api.Result;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/platform/oms/api/v1/strategies")
public class OrderStrategyController {

    private final OrderStrategyService strategyService;

    public OrderStrategyController(OrderStrategyService strategyService) {
        this.strategyService = strategyService;
    }

    @GetMapping
    public Result<List<OrderStrategyService.Strategy>> getStrategies() {
        return Result.ok(List.of());
    }

    @PostMapping
    public Result<Map<String, String>> createStrategy(@RequestBody Map<String, Object> request) {
        String type = (String) request.get("type");
        String name = (String) request.get("name");
        @SuppressWarnings("unchecked")
        Map<String, Object> rules = (Map<String, Object>) request.get("rules");
        int priority = request.containsKey("priority") ? ((Number) request.get("priority")).intValue() : 0;
        String id = strategyService.createStrategy(type, name, rules, priority);
        return Result.ok(Map.of("id", id));
    }

    @PostMapping("/evaluate")
    public Result<Map<String, Object>> evaluate(@RequestBody Map<String, Object> request) {
        String orderId = (String) request.get("orderId");
        String destination = (String) request.get("destinationCountry");
        return Result.ok(Map.of("orderId", orderId, "recommendation", "AUTO_APPROVE"));
    }
}
