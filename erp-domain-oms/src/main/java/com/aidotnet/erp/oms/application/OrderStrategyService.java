package com.aidotnet.erp.oms.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.oms.domain.OrderStrategy;
import com.aidotnet.erp.oms.infrastructure.OrderStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    public OrderStrategyService(OrderStore orderStore, ObjectMapper objectMapper) {
        this.orderStore = orderStore;
        this.objectMapper = objectMapper;
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

    /**
     * 评估策略规则是否命中
     * <p>
     * 策略的rules字段为JSON格式的条件配置，示例:
     * <pre>
     * {
     *   "conditions": [
     *     {"field": "orderAmount", "operator": "gt", "value": 500},
     *     {"field": "isNewBuyer", "operator": "eq", "value": true}
     *   ],
     *   "logic": "AND",
     *   "action": "MANUAL_REVIEW"
     * }
     * </pre>
     * 支持的operator: gt(大于)/gte(大于等于)/lt(小于)/lte(小于等于)/eq(等于)/contains(包含)
     * 支持的logic: AND(所有条件满足)/OR(任一条件满足)
     * </p>
     * <p>
     * 未配置rules时使用默认的硬编码阈值作为兜底。
     * </p>
     */
    private boolean evaluateRules(OrderStrategy strategy, EvaluateStrategyCommand command) {
        // 优先使用配置化的JSON规则
        Map<String, Object> ruleConfig = parseRulesJson(strategy.rules());
        if (ruleConfig != null) {
            return evaluateJsonRules(ruleConfig, command);
        }
        // 兜底: 使用硬编码规则
        return evaluateDefaultRules(strategy, command);
    }

    /**
     * 解析策略的JSON规则配置
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseRulesJson(String rules) {
        if (rules == null || rules.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(rules, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * 评估JSON配置化的规则
     */
    @SuppressWarnings("unchecked")
    private boolean evaluateJsonRules(Map<String, Object> ruleConfig, EvaluateStrategyCommand command) {
        List<Map<String, Object>> conditions = (List<Map<String, Object>>) ruleConfig.get("conditions");
        if (conditions == null || conditions.isEmpty()) {
            return false;
        }
        String logic = (String) ruleConfig.getOrDefault("logic", "AND");
        boolean isAnd = "AND".equalsIgnoreCase(logic);
        for (Map<String, Object> condition : conditions) {
            boolean matched = evaluateSingleCondition(condition, command);
            if (isAnd && !matched) return false;
            if (!isAnd && matched) return true;
        }
        return isAnd;
    }

    /**
     * 评估单条条件
     * <p>
     * condition格式: {"field": "orderAmount", "operator": "gt", "value": 500}
     * 从EvaluateStrategyCommand中提取field对应的值，按operator进行比较。
     * </p>
     */
    private boolean evaluateSingleCondition(Map<String, Object> condition, EvaluateStrategyCommand command) {
        String field = (String) condition.get("field");
        String operator = (String) condition.get("operator");
        Object expectedValue = condition.get("value");
        Object actualValue = extractFieldValue(field, command);
        if (actualValue == null || expectedValue == null) return false;
        return switch (operator) {
            case "eq" -> actualValue.equals(expectedValue);
            case "gt" -> compareComparable(actualValue, expectedValue) > 0;
            case "gte" -> compareComparable(actualValue, expectedValue) >= 0;
            case "lt" -> compareComparable(actualValue, expectedValue) < 0;
            case "lte" -> compareComparable(actualValue, expectedValue) <= 0;
            case "contains" -> actualValue.toString().toLowerCase().contains(expectedValue.toString().toLowerCase());
            default -> false;
        };
    }

    /**
     * 从命令对象中提取字段值
     */
    private Object extractFieldValue(String field, EvaluateStrategyCommand command) {
        return switch (field) {
            case "orderAmount" -> command.orderAmount();
            case "isNewBuyer" -> command.isNewBuyer();
            case "warehouseId" -> command.warehouseId();
            case "destinationCountry" -> command.destinationCountry();
            case "preferredChannel" -> command.preferredChannel();
            default -> null;
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private int compareComparable(Object a, Object b) {
        try {
            if (a instanceof Comparable ca && b instanceof Comparable cb) {
                return ca.compareTo(cb);
            }
            // 数字类型统一转为Double比较
            if (a instanceof Number na && b instanceof Number nb) {
                return Double.compare(na.doubleValue(), nb.doubleValue());
            }
        } catch (Exception e) {
            // 比较失败时视为不匹配
        }
        return -1;
    }

    /**
     * 从JSON规则中提取action字段，未配置时使用兜底策略
     */
    private String determineAction(OrderStrategy strategy, EvaluateStrategyCommand command) {
        Map<String, Object> ruleConfig = parseRulesJson(strategy.rules());
        if (ruleConfig != null && ruleConfig.containsKey("action")) {
            return (String) ruleConfig.get("action");
        }
        // 兜底: 使用默认策略
        return determineDefaultAction(strategy, command);
    }

    /**
     * 兜底规则: 当未配置JSON规则时使用的硬编码阈值
     */
    private boolean evaluateDefaultRules(OrderStrategy strategy, EvaluateStrategyCommand command) {
        return switch (strategy.strategyType()) {
            case "AUDIT" -> (command.orderAmount() != null && command.orderAmount() > 500)
                    || (command.isNewBuyer() != null && command.isNewBuyer());
            case "ALLOCATION" -> command.warehouseId() != null;
            case "LOGISTICS" -> command.destinationCountry() != null;
            default -> false;
        };
    }

    /**
     * 兜底动作: 当未配置JSON规则时使用的默认动作
     */
    private String determineDefaultAction(OrderStrategy strategy, EvaluateStrategyCommand command) {
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

    /**
     * 注: 插头国标规则已迁移至SYS域BusinessRuleService管理，
     *     OMS通过跨域Feign客户端调用SYS的matchPlugRule接口。
     *     自定义发票设置已迁移至FMS域InvoiceVoucherService管理。
     */
}
