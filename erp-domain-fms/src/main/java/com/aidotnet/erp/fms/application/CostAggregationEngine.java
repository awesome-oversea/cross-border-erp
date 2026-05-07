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
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(CostAggregationEngine.class);

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

    /**
     * 归集成本事件到目标维度
     * <p>
     * 核心逻辑:
     *   1. 查找匹配的归集规则(按成本类型或全局规则)
     *   2. 对PRODUCT_COST类型的成本事件，使用FIFO先进先出算法计算实际消耗成本
     *   3. 对其他类型成本，按规则分配方式归集
     * </p>
     * <p>
     * FIFO算法说明:
     *   - 适用于销售出库时计算COGS(商品销售成本)
     *   - 按采购入库的时间顺序，先入库的批次先出库
     *   - 成本层(CostLayer)记录了每批次入库的数量和单价
     *   - 出库时按层依次消耗，确保成本计算准确
     * </p>
     *
     * @param tenantId    租户ID
     * @param costEventId 成本事件ID
     * @return 归集结果列表
     */
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

        // PRODUCT_COST类型且源类型为销售出库时，使用FIFO计算实际消耗成本
        if ("PRODUCT_COST".equals(event.costType()) && isSalesOutbound(event.sourceType())) {
            return aggregateFifoCost(tenantId, event, rules);
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

    /**
     * FIFO先进先出成本计算
     * <p>
     * 算法步骤:
     *   1. 获取该SKU在手库存的所有成本层(按入库时间升序排列)
     *   2. 按出库数量依次从最早的成本层扣减
     *   3. 每层的消耗数量×该层单价=该层消耗成本
     *   4. 所有层消耗成本之和=本次出库的COGS(商品销售成本)
     *   5. 更新库存成本层: 消耗完的层移除，部分消耗的层更新剩余数量
     * </p>
     * <p>
     * 示例:
     *   采购批次1: 100个×10元(1月1日入库)
     *   采购批次2: 100个×12元(1月15日入库)
     *   销售出库: 120个
     *   FIFO计算: 100×10 + 20×12 = 1000+240 = 1240元(COGS)
     *   剩余库存: 80个×12元 = 960元
     * </p>
     *
     * @param tenantId 租户ID
     * @param event    成本事件(销售出库)
     * @param rules    归集规则列表
     * @return 按FIFO计算的归集结果
     */
    private List<CostAllocationResult> aggregateFifoCost(String tenantId, CostEvent event, List<CostAggregationRule> rules) {
        List<CostLayer> costLayers = loadCostLayers(tenantId, event.sellerSku());
        if (costLayers.isEmpty()) {
            log.warn("FIFO cost layers empty for SKU={}, falling back to direct allocation", event.sellerSku());
            return allocateDirectly(tenantId, event, rules);
        }
        // 按入库时间升序排列(最早的先消耗)
        costLayers = costLayers.stream()
                .sorted(Comparator.comparing(CostLayer::inboundTime))
                .toList();

        BigDecimal remainingQty = event.amount();
        BigDecimal totalFifoCost = BigDecimal.ZERO;
        List<FifoConsumption> consumptions = new ArrayList<>();
        List<CostLayer> remainingLayers = new ArrayList<>();

        for (CostLayer layer : costLayers) {
            if (remainingQty.compareTo(BigDecimal.ZERO) <= 0) {
                remainingLayers.add(layer);
                continue;
            }
            BigDecimal availableQty = BigDecimal.valueOf(layer.availableQuantity());
            if (availableQty.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            BigDecimal consumeQty = remainingQty.min(availableQty);
            BigDecimal consumedCost = consumeQty.multiply(layer.unitCost());
            totalFifoCost = totalFifoCost.add(consumedCost);

            consumptions.add(new FifoConsumption(layer.layerId(), consumeQty, layer.unitCost(), consumedCost));
            remainingQty = remainingQty.subtract(consumeQty);

            int newAvailable = layer.availableQuantity() - consumeQty.intValue();
            if (newAvailable > 0) {
                remainingLayers.add(new CostLayer(layer.layerId(), layer.tenantId(), layer.sellerSku(),
                        layer.unitCost(), layer.inboundQuantity(), newAvailable, layer.inboundTime()));
            }
        }

        if (remainingQty.compareTo(BigDecimal.ZERO) > 0) {
            log.warn("FIFO inventory insufficient for SKU={}, shortage={}, totalFifoCost={}",
                    event.sellerSku(), remainingQty, totalFifoCost);
        }

        // 保存更新后的FIFO成本层
        saveCostLayers(tenantId, event.sellerSku(), remainingLayers);

        // 根据FIFO计算的COGS创建归集结果
        List<CostAllocationResult> results = new ArrayList<>();
        for (CostAggregationRule rule : rules) {
            Map<String, String> dimensions = new HashMap<>();
            dimensions.put("costType", event.costType());
            dimensions.put("sourceType", event.sourceType());
            dimensions.put("sourceId", event.sourceId());
            dimensions.put("sellerSku", event.sellerSku() != null ? event.sellerSku() : "");
            dimensions.put("storeId", event.storeId() != null ? event.storeId() : "");
            dimensions.put("fifoMethod", "TRUE");
            dimensions.put("fifoConsumptions", consumptions.toString());

            CostAllocationResult result = new CostAllocationResult(
                    UUID.randomUUID().toString(), tenantId, rule.ruleId(), event.costEventId(),
                    rule.targetDimension(), resolveTargetId(rule, event),
                    totalFifoCost, event.currency(), BigDecimal.ONE, totalFifoCost,
                    dimensions, Instant.now());
            results.add(extStore.saveCostAllocationResult(result));
        }
        return results;
    }

    /**
     * 加载SKU的FIFO成本层
     * <p>
     * 从现有成本事件中按PRODUCT_COST类型和入库时间提取成本层。
     * 每批采购入库记录为一个独立的成本层。
     * </p>
     */
    private List<CostLayer> loadCostLayers(String tenantId, String sellerSku) {
        // 从数据库中查询该SKU的PRODUCT_COST类型成本事件作为成本层
        // 按时间升序确保先进先出
        return extStore.listCostLayers(tenantId, sellerSku);
    }

    /**
     * 保存更新后的FIFO成本层
     * <p>
     * 消耗后的剩余库存记录更新回数据库，供下次出库时使用。
     * </p>
     */
    private void saveCostLayers(String tenantId, String sellerSku, List<CostLayer> layers) {
        extStore.saveCostLayers(tenantId, sellerSku, layers);
    }

    /**
     * 判断是否为销售出库类型的来源
     */
    private boolean isSalesOutbound(String sourceType) {
        return "SALES_OUTBOUND".equals(sourceType) || "ORDER_SHIPMENT".equals(sourceType);
    }

    /**
     * 兜底: 直接按原金额分配(当FIFO无法计算时)
     */
    private List<CostAllocationResult> allocateDirectly(String tenantId, CostEvent event, List<CostAggregationRule> rules) {
        List<CostAllocationResult> results = new ArrayList<>();
        for (CostAggregationRule rule : rules) {
            CostAllocationResult result = allocate(tenantId, rule, event, List.of());
            if (result != null) {
                results.add(extStore.saveCostAllocationResult(result));
            }
        }
        return results;
    }

    /**
     * FIFO成本层(FIFO Cost Layer) - 库存批次成本记录
     * <p>
     * 每批采购入库时创建一个成本层，记录:
     *   - unitCost: 该批次采购单价
     *   - inboundQuantity: 入库总数量
     *   - availableQuantity: 当前可用数量(已消耗的部分从FIFO层扣减)
     *   - inboundTime: 入库时间(用于FIFO排序)
     * </p>
     */
    public record CostLayer(
            String layerId,
            String tenantId,
            String sellerSku,
            BigDecimal unitCost,
            int inboundQuantity,
            int availableQuantity,
            Instant inboundTime
    ) {}

    /**
     * FIFO消耗记录 - 记录每次出库时对各成本层的消耗明细
     */
    public record FifoConsumption(
            String layerId,
            BigDecimal consumedQuantity,
            BigDecimal unitCost,
            BigDecimal consumedCost
    ) {}

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
        dimensions.put("storeId", event.storeId() != null ? event.storeId() : "");
        dimensions.put("channelCode", event.channelCode() != null ? event.channelCode() : "");
        dimensions.put("marketplaceId", event.marketplaceId() != null ? event.marketplaceId() : "");
        return new CostAllocationResult(
                UUID.randomUUID().toString(), tenantId, rule.ruleId(), event.costEventId(),
                rule.targetDimension(), resolveTargetId(rule, event),
                amountToAllocate, event.currency(), BigDecimal.ONE, amountToAllocate,
                dimensions, Instant.now());
    }

    private String resolveTargetId(CostAggregationRule rule, CostEvent event) {
        return switch (rule.targetDimension()) {
            case "ORDER" -> firstNonBlank(event.sourceId());
            case "SKU" -> firstNonBlank(event.sellerSku(), event.sourceId());
            case "STORE" -> firstNonBlank(event.storeId(), event.marketplaceId(), event.sourceId());
            case "CHANNEL" -> firstNonBlank(event.channelCode(), event.marketplaceId(), event.sourceId());
            case "MARKETPLACE" -> firstNonBlank(event.marketplaceId());
            case "SHIPMENT" -> firstNonBlank(event.sourceId());
            default -> firstNonBlank(event.sourceId());
        };
    }

    private String firstNonBlank(String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate;
            }
        }
        return "";
    }

    public record CreateAggregationRuleCommand(
            String ruleName, String costSource, String costCategory,
            String allocationMethod, String allocationBasis,
            String targetDimension, int priority) {}
}
