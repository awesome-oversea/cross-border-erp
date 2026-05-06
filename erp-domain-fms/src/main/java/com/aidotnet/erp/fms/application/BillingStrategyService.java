package com.aidotnet.erp.fms.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.fms.domain.BillingRule;
import com.aidotnet.erp.fms.domain.BillingRule.CalculationMethod;
import com.aidotnet.erp.fms.domain.BillingRule.FeeType;
import com.aidotnet.erp.fms.domain.BillingSimulationResult;
import com.aidotnet.erp.fms.domain.BillingSimulationResult.FeeDetail;
import com.aidotnet.erp.fms.infrastructure.FmsExtStore;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 计费策略服务
 * <p>
 * 描述: FMS域业务中台(5.6)，管理平台费用计算和佣金规则引擎。
 *       支持按平台/类目/价格区间配置不同的计费规则(BillingRule)，
 *       提供费用模拟计算能力(BillingSimulationResult)。
 * </p>
 * <p>
 * 计费规则类型: 固定费率/阶梯费率/混合费率
 * 适用平台: Amazon/Shopify/TikTok/Walmart等
 * </p>
 *
 * @author ERP系统
 * @see BillingRule
 * @see BillingSimulationResult
 */
@Service("fmsBillingStrategyService")
public class BillingStrategyService {

    private final FmsExtStore extStore;

    public BillingStrategyService(FmsExtStore extStore) {
        this.extStore = extStore;
    }

    @Transactional
    public BillingRule createRule(String tenantId, CreateBillingRuleCommand command) {
        Instant now = Instant.now();
        BillingRule rule = new BillingRule(
                UUID.randomUUID().toString(), tenantId, command.ruleName(), command.feeType(),
                command.platform(), command.category(), command.rate(), command.minAmount(),
                command.maxAmount(), command.calculationMethod(), true, command.priority(), now, now);
        return extStore.saveBillingRule(rule);
    }

    @Transactional
    public BillingRule updateRule(String tenantId, String ruleId, CreateBillingRuleCommand command) {
        BillingRule existing = getRule(tenantId, ruleId);
        Instant now = Instant.now();
        BillingRule updated = new BillingRule(
                existing.ruleId(), existing.tenantId(), command.ruleName(), command.feeType(),
                command.platform(), command.category(), command.rate(), command.minAmount(),
                command.maxAmount(), command.calculationMethod(), existing.enabled(),
                command.priority(), existing.createdAt(), now);
        return extStore.saveBillingRule(updated);
    }

    @Transactional
    public BillingRule toggleRule(String tenantId, String ruleId, boolean enabled) {
        BillingRule existing = getRule(tenantId, ruleId);
        Instant now = Instant.now();
        BillingRule updated = new BillingRule(
                existing.ruleId(), existing.tenantId(), existing.ruleName(), existing.feeType(),
                existing.platform(), existing.category(), existing.rate(), existing.minAmount(),
                existing.maxAmount(), existing.calculationMethod(), enabled,
                existing.priority(), existing.createdAt(), now);
        return extStore.saveBillingRule(updated);
    }

    public BillingRule getRule(String tenantId, String ruleId) {
        return extStore.findBillingRule(tenantId, ruleId)
                .orElseThrow(() -> new BizException("RULE_NOT_FOUND", "计费规则不存在"));
    }

    public List<BillingRule> listRules(String tenantId, String feeType, String platform) {
        return extStore.listBillingRules(tenantId, feeType, platform);
    }

    public BillingSimulationResult simulate(String tenantId, BillingSimulationCommand command) {
        List<FeeDetail> feeDetails = new ArrayList<>();
        BigDecimal platformCommission = calculateFee(tenantId, FeeType.PLATFORM_COMMISSION,
                command.platform(), command.category(), command.orderAmount(), feeDetails);
        BigDecimal warehouseFee = calculateFee(tenantId, FeeType.WAREHOUSE_FEE,
                command.platform(), command.category(), command.orderAmount(), feeDetails);
        BigDecimal freightFee = calculateFee(tenantId, FeeType.FREIGHT_FEE,
                command.platform(), command.category(), command.orderAmount(), feeDetails);
        BigDecimal packagingCost = calculateFee(tenantId, FeeType.PACKAGING_COST,
                command.platform(), command.category(), command.orderAmount(), feeDetails);
        BigDecimal fbaHeadCost = calculateFee(tenantId, FeeType.FBA_HEAD_COST,
                command.platform(), command.category(), command.orderAmount(), feeDetails);
        BigDecimal serviceFee = calculateFee(tenantId, FeeType.SERVICE_FEE,
                command.platform(), command.category(), command.orderAmount(), feeDetails);
        BigDecimal totalFee = platformCommission.add(warehouseFee).add(freightFee)
                .add(packagingCost).add(fbaHeadCost).add(serviceFee);
        BigDecimal netAmount = command.orderAmount().subtract(totalFee);
        return new BillingSimulationResult(tenantId, command.platform(), command.orderAmount(),
                platformCommission, warehouseFee, freightFee, packagingCost, fbaHeadCost,
                serviceFee, totalFee, netAmount, feeDetails);
    }

    private BigDecimal calculateFee(String tenantId, FeeType feeType, String platform,
                                     String category, BigDecimal orderAmount, List<FeeDetail> feeDetails) {
        List<BillingRule> rules = extStore.listBillingRules(tenantId, feeType.name(), platform);
        rules = rules.stream().filter(BillingRule::enabled).sorted((a, b) -> b.priority() - a.priority()).toList();
        if (rules.isEmpty()) return BigDecimal.ZERO;
        BillingRule rule = rules.get(0);
        BigDecimal amount;
        if (rule.calculationMethod().equals(CalculationMethod.PERCENTAGE.name())) {
            amount = orderAmount.multiply(rule.rate()).setScale(2, RoundingMode.HALF_UP);
        } else if (rule.calculationMethod().equals(CalculationMethod.FIXED.name())) {
            amount = rule.rate();
        } else {
            amount = orderAmount.multiply(rule.rate()).setScale(2, RoundingMode.HALF_UP);
        }
        if (rule.minAmount() != null && amount.compareTo(rule.minAmount()) < 0) {
            amount = rule.minAmount();
        }
        if (rule.maxAmount() != null && amount.compareTo(rule.maxAmount()) > 0) {
            amount = rule.maxAmount();
        }
        feeDetails.add(new FeeDetail(feeType.name(), rule.ruleName(), rule.rate(), amount, rule.calculationMethod()));
        return amount;
    }

    public BigDecimal calculatePlatformCommission(String tenantId, String platform, String category, BigDecimal orderAmount) {
        List<BillingRule> rules = extStore.listBillingRules(tenantId, FeeType.PLATFORM_COMMISSION.name(), platform);
        rules = rules.stream().filter(BillingRule::enabled).sorted((a, b) -> b.priority() - a.priority()).toList();
        if (rules.isEmpty()) return BigDecimal.ZERO;
        BillingRule rule = rules.get(0);
        return orderAmount.multiply(rule.rate()).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateFreightAllocation(String tenantId, String platform, BigDecimal totalFreight,
                                                  List<BigDecimal> orderAmounts) {
        BigDecimal totalOrders = orderAmounts.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalOrders.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        List<BigDecimal> allocations = new ArrayList<>();
        for (BigDecimal orderAmount : orderAmounts) {
            allocations.add(totalFreight.multiply(orderAmount).divide(totalOrders, 2, RoundingMode.HALF_UP));
        }
        return allocations.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public record CreateBillingRuleCommand(
            String ruleName, String feeType, String platform, String category,
            BigDecimal rate, BigDecimal minAmount, BigDecimal maxAmount,
            String calculationMethod, int priority) {}

    public record BillingSimulationCommand(
            String platform, String category, BigDecimal orderAmount) {}
}
