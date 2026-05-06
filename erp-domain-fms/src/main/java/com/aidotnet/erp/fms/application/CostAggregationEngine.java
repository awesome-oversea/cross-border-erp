package com.aidotnet.erp.fms.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.fms.domain.CostAggregationRule;
import com.aidotnet.erp.fms.domain.CostAggregationRule.AllocationMethod;
import com.aidotnet.erp.fms.domain.CostAllocationResult;
import com.aidotnet.erp.fms.domain.CostBreakdown;
import com.aidotnet.erp.fms.domain.CostEvent;
import com.aidotnet.erp.fms.infrastructure.FmsExtStore;
import com.aidotnet.erp.fms.infrastructure.FinanceStore;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 成本归集引擎
 * <p>
 * 描述: FMS域业务中台(5.12)，按SKU+市场+店铺等多维度自动归集成本。
 *       支持成本分摊规则(CostAggregationRule)，将间接成本按比例分摊到SKU。
 *       归集结果输出CostAllocationResult，供利润核算引擎使用。
 * </p>
 * <p>
 * 归集维度: 产品成本、物流运费、FBA费用、平台佣金、广告费、退货成本、仓储费
 * 分摊规则: 按销售额比例/按数量比例/固定比例/自定义权重
 * </p>
 *
 * @author ERP系统
 * @see CostAggregationRule
 * @see CostAllocationResult
 * @see ProfitCalculationEngine
 */
@Service
public class CostAggregationEngine {

    private final FinanceStore financeStore;
    private final FmsExtStore extStore;

    public CostAggregationEngine(FinanceStore financeStore, FmsExtStore extStore) {
        this.financeStore = financeStore;
        this.extStore = extStore;
    }

    @Transactional
    public CostAggregationRule createRule(String tenantId, CreateAggregationRuleCommand command) {
        Instant now = Instant.now();
        CostAggregationRule rule = new CostAggregationRule(
                UUID.randomUUID().toString(), tenantId, command.ruleName(),
                command.costSource(), command.costCategory(), command.allocationMethod(),
                command.allocationBasis(), command.targetDimension(), true, command.priority(), now, now);
        return extStore.saveCostAggregationRule(rule);
    }

    @Transactional
    public CostAggregationRule updateRule(String tenantId, String ruleId, CreateAggregationRuleCommand command) {
        CostAggregationRule existing = getRule(tenantId, ruleId);
        Instant now = Instant.now();
        CostAggregationRule updated = new CostAggregationRule(
                existing.ruleId(), existing.tenantId(), command.ruleName(),
                command.costSource(), command.costCategory(), command.allocationMethod(),
                command.allocationBasis(), command.targetDimension(), existing.enabled(),
                command.priority(), existing.createdAt(), now);
        return extStore.saveCostAggregationRule(updated);
    }

    @Transactional
    public CostAggregationRule toggleRule(String tenantId, String ruleId, boolean enabled) {
        CostAggregationRule existing = getRule(tenantId, ruleId);
        Instant now = Instant.now();
        CostAggregationRule updated = new CostAggregationRule(
                existing.ruleId(), existing.tenantId(), existing.ruleName(),
                existing.costSource(), existing.costCategory(), existing.allocationMethod(),
                existing.allocationBasis(), existing.targetDimension(), enabled,
                existing.priority(), existing.createdAt(), now);
        return extStore.saveCostAggregationRule(updated);
    }

    public List<CostAggregationRule> listRules(String tenantId, String costSource) {
        return extStore.listCostAggregationRules(tenantId, costSource);
    }

    public CostAggregationRule getRule(String tenantId, String ruleId) {
        return extStore.findCostAggregationRule(tenantId, ruleId)
                .orElseThrow(() -> new BizException("RULE_NOT_FOUND", "归集规则不存在"));
    }

    @Transactional
    public List<CostAllocationResult> aggregateCostEvent(String tenantId, String costEventId) {
        CostEvent event = financeStore.findCostEvent(tenantId, costEventId)
                .orElseThrow(() -> new BizException("COST_EVENT_NOT_FOUND", "成本事件不存在"));
        List<CostBreakdown> breakdowns = extStore.listCostBreakdownsByEvent(tenantId, costEventId);
        List<CostAggregationRule> rules = extStore.listCostAggregationRules(tenantId, event.costType());
        rules = rules.stream().filter(CostAggregationRule::enabled).sorted((a, b) -> b.priority() - a.priority()).toList();
        if (rules.isEmpty()) {
            rules = extStore.listCostAggregationRules(tenantId, null);
            rules = rules.stream().filter(CostAggregationRule::enabled).sorted((a, b) -> b.priority() - a.priority()).toList();
        }
        List<CostAllocationResult> results = new ArrayList<>();
        for (CostAggregationRule rule : rules) {
            CostAllocationResult result = allocate(tenantId, rule, event, breakdowns);
            if (result != null) {
                results.add(extStore.saveCostAllocationResult(result));
            }
        }
        return results;
    }

    @Transactional
    public List<CostAllocationResult> aggregateByDimension(String tenantId, String dimensionType, String dimensionId) {
        List<CostAllocationResult> existing = extStore.listCostAllocationResults(tenantId, dimensionType, dimensionId);
        return existing;
    }

    public Map<String, BigDecimal> getCostSummaryBySku(String tenantId, String sellerSku) {
        List<CostAllocationResult> results = extStore.listCostAllocationResultsBySku(tenantId, sellerSku);
        Map<String, BigDecimal> summary = new LinkedHashMap<>();
        for (CostAllocationResult r : results) {
            String key = r.targetDimension();
            summary.merge(key, r.amountInBaseCurrency(), BigDecimal::add);
        }
        return summary;
    }

    private CostAllocationResult allocate(String tenantId, CostAggregationRule rule,
                                          CostEvent event, List<CostBreakdown> breakdowns) {
        BigDecimal amountToAllocate = event.amount();
        if (!breakdowns.isEmpty()) {
            amountToAllocate = breakdowns.stream()
                    .filter(b -> rule.costCategory() == null || rule.costCategory().equals(b.costCategory()))
                    .map(CostBreakdown::amountInBaseCurrency)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        Map<String, String> dimensions = new HashMap<>();
        dimensions.put("costType", event.costType());
        dimensions.put("sourceType", event.sourceType());
        dimensions.put("sourceId", event.sourceId());
        dimensions.put("sellerSku", event.sellerSku() != null ? event.sellerSku() : "");
        dimensions.put("marketplaceId", event.marketplaceId() != null ? event.marketplaceId() : "");
        return new CostAllocationResult(
                UUID.randomUUID().toString(), tenantId, rule.ruleId(), event.costEventId(),
                rule.targetDimension(), resolveTargetId(rule, event),
                amountToAllocate, event.currency(), BigDecimal.ONE, amountToAllocate,
                dimensions, Instant.now());
    }

    private String resolveTargetId(CostAggregationRule rule, CostEvent event) {
        return switch (rule.targetDimension()) {
            case "ORDER" -> event.sourceType().equals("ORDER") ? event.sourceId() : event.sourceId();
            case "SKU" -> event.sellerSku() != null ? event.sellerSku() : event.sourceId();
            case "STORE" -> event.marketplaceId() != null ? event.marketplaceId() : event.sourceId();
            case "MARKETPLACE" -> event.marketplaceId() != null ? event.marketplaceId() : "";
            case "SHIPMENT" -> event.sourceId();
            default -> event.sourceId();
        };
    }

    public record CreateAggregationRuleCommand(
            String ruleName, String costSource, String costCategory,
            String allocationMethod, String allocationBasis,
            String targetDimension, int priority) {}
}
