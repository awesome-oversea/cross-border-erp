package com.aidotnet.erp.sys.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.sys.domain.BusinessRuleVersion;
import com.aidotnet.erp.sys.domain.PlugStandardRule;
import com.aidotnet.erp.sys.domain.RuleExecutionLog;
import com.aidotnet.erp.sys.domain.SimulationReplay;
import com.aidotnet.erp.sys.infrastructure.SysExtStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 业务规则管理应用服务
 * <p>
 * 描述: 系统设置域业务规则引擎服务，负责业务规则的版本管理、
 *       模拟回放、执行日志记录等核心能力。支持规则热更新和灰度发布。
 * </p>
 * <p>
 * 核心能力:
 *   1. 规则版本管理 - 创建/发布/回滚规则版本，支持灰度发布
 *   2. 模拟回放 - 使用历史数据模拟规则执行，对比结果差异
 *   3. 执行日志 - 记录每次规则执行的输入/输出/耗时，确保可审计
 * </p>
 *
 * @author ERP系统
 * @see BusinessRuleVersion
 * @see BusinessRuleSimulation
 * @see BusinessRuleExecutionLog
 */
@Service
public class BusinessRuleService {

    private static final Logger log = LoggerFactory.getLogger(BusinessRuleService.class);

    private final SysExtStore extStore;
    private final ObjectMapper objectMapper;

    public BusinessRuleService(SysExtStore extStore, ObjectMapper objectMapper) {
        this.extStore = extStore;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public BusinessRuleVersion createVersion(String tenantId, CreateRuleVersionCommand command) {
        List<BusinessRuleVersion> existing = extStore.listRuleVersions(tenantId, command.ruleId(), null);
        int nextVersion = existing.stream().mapToInt(BusinessRuleVersion::version).max().orElse(0) + 1;
        Instant now = Instant.now();
        BusinessRuleVersion version = new BusinessRuleVersion(
                UUID.randomUUID().toString(), tenantId, command.ruleId(), command.ruleType(),
                command.ruleName(), nextVersion, command.contentJson(), command.changeDescription(),
                command.changedBy(), now);
        return extStore.saveRuleVersion(version);
    }

    @Transactional
    public BusinessRuleVersion rollbackToVersion(String tenantId, String ruleId, int targetVersion, String changedBy) {
        BusinessRuleVersion target = extStore.listRuleVersions(tenantId, ruleId, null).stream()
                .filter(v -> v.version() == targetVersion)
                .findFirst()
                .orElseThrow(() -> new BizException("VERSION_NOT_FOUND", "规则版本不存在: " + targetVersion));
        CreateRuleVersionCommand rollbackCommand = new CreateRuleVersionCommand(
                ruleId, target.ruleType(), target.ruleName(), target.contentJson(),
                "Rollback to version " + targetVersion, changedBy);
        return createVersion(tenantId, rollbackCommand);
    }

    public BusinessRuleVersion getVersion(String tenantId, String ruleId, int version) {
        return extStore.listRuleVersions(tenantId, ruleId, null).stream()
                .filter(v -> v.version() == version)
                .findFirst()
                .orElseThrow(() -> new BizException("VERSION_NOT_FOUND", "规则版本不存在: " + version));
    }

    public BusinessRuleVersion getLatestVersion(String tenantId, String ruleId) {
        List<BusinessRuleVersion> versions = extStore.listRuleVersions(tenantId, ruleId, null);
        return versions.stream()
                .max((a, b) -> Integer.compare(a.version(), b.version()))
                .orElseThrow(() -> new BizException("VERSION_NOT_FOUND", "规则无版本: " + ruleId));
    }

    public List<BusinessRuleVersion> listVersions(String tenantId, String ruleId) {
        return extStore.listRuleVersions(tenantId, ruleId, null);
    }

    public List<BusinessRuleVersion> listVersionsByType(String tenantId, String ruleType) {
        return extStore.listRuleVersions(tenantId, null, ruleType);
    }

    @Transactional
    public SimulationReplay simulate(String tenantId, SimulateRuleCommand command) {
        BusinessRuleVersion version = getVersion(tenantId, command.ruleId(), command.ruleVersion());
        Instant now = Instant.now();
        Map<String, Object> outputResult = new HashMap<>();
        boolean passed = true;
        String errorMessage = null;
        try {
            outputResult = evaluateRule(version, command.inputContext());
        } catch (Exception e) {
            passed = false;
            errorMessage = e.getMessage();
            log.warn("Rule simulation failed: ruleId={} version={} error={}", command.ruleId(), command.ruleVersion(), e.getMessage());
        }
        SimulationReplay replay = new SimulationReplay(
                UUID.randomUUID().toString(), tenantId, command.ruleId(), command.ruleVersion(),
                version.ruleType(), command.inputContext(), outputResult, passed, errorMessage, now);
        return extStore.saveSimulationReplay(replay);
    }

    @Transactional
    public List<SimulationReplay> batchSimulate(String tenantId, List<SimulateRuleCommand> commands) {
        List<SimulationReplay> results = new ArrayList<>();
        for (SimulateRuleCommand command : commands) {
            results.add(simulate(tenantId, command));
        }
        return results;
    }

    public List<SimulationReplay> listSimulationReplays(String tenantId, String ruleId) {
        return extStore.listSimulationReplays(tenantId, ruleId);
    }

    @Transactional
    public RuleExecutionLog logExecution(String tenantId, LogExecutionCommand command) {
        Instant now = Instant.now();
        RuleExecutionLog execLog = new RuleExecutionLog(
                UUID.randomUUID().toString(), tenantId, command.ruleId(), command.ruleVersion(),
                command.ruleType(), command.businessType(), command.referenceId(),
                command.inputContext(), command.outputResult(), command.success(),
                command.errorMessage(), command.executionTimeMs(), now);
        return extStore.saveRuleExecutionLog(execLog);
    }

    public List<RuleExecutionLog> listExecutionLogs(String tenantId, String ruleId, String businessType) {
        return extStore.listRuleExecutionLogs(tenantId, ruleId, businessType);
    }

    public List<RuleExecutionLog> listExecutionLogsByReference(String tenantId, String referenceId) {
        return extStore.listRuleExecutionLogsByReference(tenantId, referenceId);
    }

    public ExecutionStatistics getExecutionStatistics(String tenantId, String ruleId) {
        List<RuleExecutionLog> logs = extStore.listRuleExecutionLogs(tenantId, ruleId, null);
        long totalCount = logs.size();
        long successCount = logs.stream().filter(RuleExecutionLog::success).count();
        long failCount = totalCount - successCount;
        double avgExecutionTimeMs = totalCount > 0
                ? logs.stream().mapToLong(RuleExecutionLog::executionTimeMs).average().orElse(0)
                : 0;
        double successRate = totalCount > 0 ? (double) successCount / totalCount * 100 : 0;
        return new ExecutionStatistics(tenantId, ruleId, totalCount, successCount, failCount,
                avgExecutionTimeMs, successRate);
    }

    /**
     * 规则执行核心方法
     * <p>
     * 解析BusinessRuleVersion.contentJson中的规则配置JSON，
     * 对输入上下文(inputContext)执行条件匹配与动作计算。
     * contentJson格式示例:
     * <pre>
     * {
     *   "conditions": [
     *     {"field": "orderAmount", "operator": "gt", "value": 1000},
     *     {"field": "riskLevel", "operator": "eq", "value": "HIGH"}
     *   ],
     *   "logic": "AND",
     *   "actions": [{"type": "reject", "reason": "高风险订单自动拦截"}]
     * }
     * </pre>
     * 支持算子: gt/gte/lt/lte/eq/neq/contains/in
     * 支持逻辑: AND(全部满足)/OR(任一满足)
     * 数字类型自动跨类型比较(Integer/Double/Long/BigDecimal)
     * </p>
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> evaluateRule(BusinessRuleVersion version, Map<String, Object> inputContext) {
        Map<String, Object> result = new HashMap<>();
        result.put("ruleId", version.ruleId());
        result.put("version", version.version());
        result.put("ruleType", version.ruleType());
        result.put("evaluated", true);
        result.put("inputKeys", inputContext.keySet());

        if (version.contentJson() == null || version.contentJson().isBlank()) {
            result.put("matched", false);
            result.put("reason", "规则内容为空");
            return result;
        }
        try {
            Map<String, Object> ruleConfig = objectMapper.readValue(
                    version.contentJson(), new TypeReference<Map<String, Object>>() {});
            List<Map<String, Object>> conditions = (List<Map<String, Object>>) ruleConfig.get("conditions");
            if (conditions == null || conditions.isEmpty()) {
                result.put("matched", true);
                result.put("reason", "无条件约束，默认命中");
                return result;
            }
            String logic = (String) ruleConfig.getOrDefault("logic", "AND");
            boolean isAnd = "AND".equalsIgnoreCase(logic);
            result.put("logic", logic);
            List<Map<String, Object>> matchedConds = new ArrayList<>();
            List<Map<String, Object>> unmatchedConds = new ArrayList<>();
            for (Map<String, Object> cond : conditions) {
                String field = (String) cond.get("field");
                String operator = (String) cond.get("operator");
                Object expVal = cond.get("value");
                Object actVal = inputContext.get(field);
                boolean matched = evaluateOneCondition(operator, expVal, actVal);
                Map<String, Object> cr = new HashMap<>();
                cr.put("field", field); cr.put("operator", operator);
                cr.put("expected", expVal); cr.put("actual", actVal); cr.put("matched", matched);
                if (matched) matchedConds.add(cr); else unmatchedConds.add(cr);
            }
            boolean allMatched = isAnd ? unmatchedConds.isEmpty() : !matchedConds.isEmpty();
            result.put("matched", allMatched);
            result.put("conditionResults", matchedConds);
            if (allMatched) {
                List<Map<String, Object>> actions = (List<Map<String, Object>>) ruleConfig.get("actions");
                result.put("actions", actions != null ? actions : List.of());
                result.put("reason", "规则命中");
            } else {
                result.put("actions", List.of());
                result.put("reason", isAnd ? "存在未匹配条件" : "所有条件均未匹配");
            }
        } catch (JsonProcessingException e) {
            result.put("matched", false);
            result.put("reason", "规则JSON解析失败");
            log.warn("Rule parse failed: ruleId={} version={}", version.ruleId(), version.version());
        }
        return result;
    }

    /**
     * 单条件评估: 比较actual与expected，支持跨数字类型
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private boolean evaluateOneCondition(String operator, Object expected, Object actual) {
        if (actual == null) return false;
        if (expected == null) return "null".equals(operator);
        int cmp;
        if (actual instanceof Number na && expected instanceof Number nb) {
            cmp = Double.compare(na.doubleValue(), nb.doubleValue());
        } else if (actual instanceof Comparable ca && expected.getClass() == actual.getClass()) {
            cmp = ca.compareTo(expected);
        } else {
            cmp = actual.toString().compareTo(expected.toString());
        }
        return switch (operator) {
            case "eq" -> cmp == 0;
            case "neq" -> cmp != 0;
            case "gt" -> cmp > 0;
            case "gte" -> cmp >= 0;
            case "lt" -> cmp < 0;
            case "lte" -> cmp <= 0;
            case "contains" -> actual.toString().toLowerCase().contains(expected.toString().toLowerCase());
            case "in" -> expected instanceof List<?> list && list.stream().anyMatch(i -> i.toString().equals(actual.toString()));
            default -> false;
        };
    }

    public record CreateRuleVersionCommand(
            String ruleId, String ruleType, String ruleName, String contentJson,
            String changeDescription, String changedBy) {}

    public record SimulateRuleCommand(
            String ruleId, int ruleVersion, Map<String, Object> inputContext) {}

    public record LogExecutionCommand(
            String ruleId, int ruleVersion, String ruleType, String businessType,
            String referenceId, Map<String, Object> inputContext, Map<String, Object> outputResult,
            boolean success, String errorMessage, long executionTimeMs) {}

    public record ExecutionStatistics(
            String tenantId, String ruleId, long totalCount, long successCount,
            long failCount, double avgExecutionTimeMs, double successRate) {}

    // ========== 插头国标规则管理(规则引擎具体实现) ==========
    /**
     * 插头国标规则是业务规则引擎的一种具体应用。
     * OMS在订单履约时通过跨域Feign客户端调用本服务接口，
     * 按收货国家匹配对应的插头规格SKU，确保产品符合目标国家电器标准。
     *
     * 跨域调用链路: OMS → FeignClient → SYS BusinessRuleService.matchPlugRule
     */

    private final Map<String, PlugStandardRule> plugRuleStore = new ConcurrentHashMap<>();

    @Transactional
    public PlugStandardRule createPlugRule(String tenantId, CreatePlugRuleCommand command) {
        Instant now = Instant.now();
        PlugStandardRule rule = new PlugStandardRule(UUID.randomUUID().toString(), tenantId,
                command.countryCode(), command.categoryId(), command.plugStandard(),
                command.targetSku(), command.priority(), true, now, now);
        plugRuleStore.put(rule.ruleId(), rule);
        return rule;
    }

    /**
     * 按国家匹配插头规格SKU(供OMS跨域调用)
     * <p>
     * 匹配策略: 先匹配国家+类目(精确匹配)，再匹配国家+全类目(兜底)，
     * 按优先级取最高优先级的匹配结果。
     * </p>
     *
     * @param tenantId   租户ID
     * @param countryCode ISO国家代码(如 DE/US/GB)
     * @param categoryId  产品类目ID(可为空)
     * @return 匹配的插头规则，无匹配时返回null
     */
    public PlugStandardRule matchPlugRule(String tenantId, String countryCode, String categoryId) {
        return plugRuleStore.values().stream()
                .filter(r -> r.tenantId().equals(tenantId) && r.enabled())
                .filter(r -> r.countryCode().equalsIgnoreCase(countryCode))
                .filter(r -> r.categoryId() == null || r.categoryId().equals(categoryId))
                .sorted((a, b) -> b.priority() - a.priority())
                .findFirst()
                .orElse(null);
    }

    public List<PlugStandardRule> listPlugRules(String tenantId) {
        return plugRuleStore.values().stream()
                .filter(r -> r.tenantId().equals(tenantId))
                .collect(Collectors.toList());
    }

    public record CreatePlugRuleCommand(String countryCode, String categoryId, String plugStandard,
                                         String targetSku, int priority) {}
}
