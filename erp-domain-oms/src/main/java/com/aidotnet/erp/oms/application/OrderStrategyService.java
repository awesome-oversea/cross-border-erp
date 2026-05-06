package com.aidotnet.erp.oms.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.oms.domain.OrderStrategy;
import com.aidotnet.erp.oms.infrastructure.OrderStore;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("omsOrderStrategyService")
public class OrderStrategyService {

    private final OrderStore orderStore;

    public OrderStrategyService(OrderStore orderStore) {
        this.orderStore = orderStore;
    }

    @Transactional
    public OrderStrategy createStrategy(String tenantId, CreateStrategyCommand command) {
        Instant now = Instant.now();
        OrderStrategy strategy = new OrderStrategy(
                UUID.randomUUID().toString(), tenantId, command.strategyType(), command.name(),
                command.description(), command.rules(), true, command.priority(), now, now);
        return orderStore.saveOrderStrategy(strategy);
    }

    @Transactional
    public OrderStrategy updateStrategy(String tenantId, String strategyId, CreateStrategyCommand command) {
        OrderStrategy existing = getStrategy(tenantId, strategyId);
        Instant now = Instant.now();
        OrderStrategy updated = new OrderStrategy(
                existing.strategyId(), existing.tenantId(), command.strategyType(), command.name(),
                command.description(), command.rules(), existing.enabled(), command.priority(),
                existing.createdAt(), now);
        return orderStore.saveOrderStrategy(updated);
    }

    @Transactional
    public OrderStrategy toggleStrategy(String tenantId, String strategyId, boolean enabled) {
        OrderStrategy existing = getStrategy(tenantId, strategyId);
        Instant now = Instant.now();
        OrderStrategy updated = new OrderStrategy(
                existing.strategyId(), existing.tenantId(), existing.strategyType(), existing.name(),
                existing.description(), existing.rules(), enabled, existing.priority(),
                existing.createdAt(), now);
        return orderStore.saveOrderStrategy(updated);
    }

    public OrderStrategy getStrategy(String tenantId, String strategyId) {
        return orderStore.findOrderStrategy(tenantId, strategyId)
                .orElseThrow(() -> new BizException("STRATEGY_NOT_FOUND", "订单策略不存在"));
    }

    public List<OrderStrategy> listStrategies(String tenantId, String strategyType) {
        return orderStore.listOrderStrategies(tenantId, strategyType);
    }

    public StrategyEvaluationResult evaluate(String tenantId, EvaluateStrategyCommand command) {
        List<OrderStrategy> strategies = orderStore.listOrderStrategies(tenantId, command.strategyType());
        strategies = strategies.stream().filter(OrderStrategy::enabled).sorted((a, b) -> b.priority() - a.priority()).toList();
        List<Map<String, Object>> matchedRules = new ArrayList<>();
        String recommendedAction = "MANUAL_REVIEW";
        for (OrderStrategy strategy : strategies) {
            boolean matched = evaluateRules(strategy, command);
            if (matched) {
                Map<String, Object> matchResult = new HashMap<>();
                matchResult.put("strategyId", strategy.strategyId());
                matchResult.put("strategyName", strategy.name());
                matchResult.put("strategyType", strategy.strategyType());
                matchResult.put("action", determineAction(strategy, command));
                matchedRules.add(matchResult);
                if ("APPROVE".equals(matchResult.get("action"))) {
                    recommendedAction = "APPROVE";
                    break;
                } else if ("REJECT".equals(matchResult.get("action"))) {
                    recommendedAction = "REJECT";
                    break;
                }
            }
        }
        return new StrategyEvaluationResult(command.strategyType(), recommendedAction, matchedRules);
    }

    private boolean evaluateRules(OrderStrategy strategy, EvaluateStrategyCommand command) {
        if (strategy.strategyType().equals("AUDIT")) {
            return evaluateAuditRules(strategy, command);
        } else if (strategy.strategyType().equals("ALLOCATION")) {
            return evaluateAllocationRules(strategy, command);
        } else if (strategy.strategyType().equals("LOGISTICS")) {
            return evaluateLogisticsRules(strategy, command);
        }
        return false;
    }

    private boolean evaluateAuditRules(OrderStrategy strategy, EvaluateStrategyCommand command) {
        if (command.orderAmount() != null && command.orderAmount() > 500) return true;
        if (command.isNewBuyer() != null && command.isNewBuyer()) return true;
        return false;
    }

    private boolean evaluateAllocationRules(OrderStrategy strategy, EvaluateStrategyCommand command) {
        if (command.warehouseId() != null) return true;
        return false;
    }

    private boolean evaluateLogisticsRules(OrderStrategy strategy, EvaluateStrategyCommand command) {
        if (command.destinationCountry() != null) return true;
        return false;
    }

    private String determineAction(OrderStrategy strategy, EvaluateStrategyCommand command) {
        return switch (strategy.strategyType()) {
            case "AUDIT" -> command.orderAmount() != null && command.orderAmount() > 1000 ? "MANUAL_REVIEW" : "APPROVE";
            case "ALLOCATION" -> "ALLOCATE";
            case "LOGISTICS" -> "SELECT_CHANNEL";
            default -> "MANUAL_REVIEW";
        };
    }

    public record CreateStrategyCommand(
            String strategyType, String name, String description, String rules, int priority) {}

    public record EvaluateStrategyCommand(
            String strategyType, String orderId, Double orderAmount, Boolean isNewBuyer,
            String warehouseId, String destinationCountry, String preferredChannel) {}

    public record StrategyEvaluationResult(
            String strategyType, String recommendedAction, List<Map<String, Object>> matchedRules) {}
}
