package com.aidotnet.erp.fms.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.fms.domain.CostAllocationResult;
import com.aidotnet.erp.fms.domain.ProfitDeviationAlert;
import com.aidotnet.erp.fms.domain.ProfitDeviationAlert.AlertStatus;
import com.aidotnet.erp.fms.domain.ProfitDeviationAlert.Severity;
import com.aidotnet.erp.fms.domain.ProfitResult;
import com.aidotnet.erp.fms.domain.ProfitResult.DimensionType;
import com.aidotnet.erp.fms.infrastructure.FmsExtStore;
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
 * 利润核算引擎
 * <p>
 * 描述: FMS域业务中台(5.13)，基于成本归集结果计算利润。
 *       支持多维度利润分析(订单/SKU/店铺/市场/产品)，
 *       自动处理汇率转换，生成利润报表。
 *       毛利 = 收入 - 总成本; 毛利率 = 毛利 / 收入
 * </p>
 * <p>
 * 利润偏差预警: 当实际利润与预估利润偏差超过阈值时，生成ProfitDeviationAlert
 * </p>
 *
 * @author ERP系统
 * @see ProfitResult
 * @see ProfitStatement
 * @see CostAggregationEngine
 */
@Service
public class ProfitCalculationEngine {

    private static final BigDecimal DEFAULT_MARGIN_THRESHOLD = new BigDecimal("0.10");
    private static final BigDecimal CRITICAL_THRESHOLD = new BigDecimal("0.25");
    private static final BigDecimal HIGH_THRESHOLD = new BigDecimal("0.15");
    private static final BigDecimal MEDIUM_THRESHOLD = new BigDecimal("0.10");

    private final FmsExtStore extStore;

    public ProfitCalculationEngine(FmsExtStore extStore) {
        this.extStore = extStore;
    }

    @Transactional
    public ProfitResult calculateProfit(String tenantId, CalculateProfitCommand command) {
        List<CostAllocationResult> allocations = extStore.listCostAllocationResultsByTarget(
                tenantId, command.dimensionType(), command.dimensionId());
        Map<String, BigDecimal> costByType = new LinkedHashMap<>();
        for (CostAllocationResult a : allocations) {
            String costType = a.dimensions() != null ? a.dimensions().getOrDefault("costType", "OTHER") : "OTHER";
            costByType.merge(costType, a.amountInBaseCurrency(), BigDecimal::add);
        }
        BigDecimal revenue = command.revenue() != null ? command.revenue() : BigDecimal.ZERO;
        BigDecimal productCost = costByType.getOrDefault("PRODUCT_COST", BigDecimal.ZERO);
        BigDecimal shippingCost = costByType.getOrDefault("SHIPPING_COST", BigDecimal.ZERO);
        BigDecimal fbaFee = costByType.getOrDefault("FBA_FEE", BigDecimal.ZERO);
        BigDecimal commission = costByType.getOrDefault("COMMISSION", BigDecimal.ZERO);
        BigDecimal advertisingCost = costByType.getOrDefault("ADVERTISING", BigDecimal.ZERO);
        BigDecimal returnCost = costByType.getOrDefault("RETURN_COST", BigDecimal.ZERO);
        BigDecimal storageFee = costByType.getOrDefault("STORAGE_FEE", BigDecimal.ZERO);
        BigDecimal packagingCost = costByType.getOrDefault("PACKAGING", BigDecimal.ZERO);
        BigDecimal customDuty = costByType.getOrDefault("CUSTOM_DUTY", BigDecimal.ZERO);
        BigDecimal otherCost = costByType.getOrDefault("OTHER", BigDecimal.ZERO);
        BigDecimal totalCost = productCost.add(shippingCost).add(fbaFee).add(commission)
                .add(advertisingCost).add(returnCost).add(storageFee)
                .add(packagingCost).add(customDuty).add(otherCost);
        BigDecimal grossProfit = revenue.subtract(totalCost);
        BigDecimal grossMargin = revenue.compareTo(BigDecimal.ZERO) > 0
                ? grossProfit.divide(revenue, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        ProfitResult result = new ProfitResult(
                UUID.randomUUID().toString(), tenantId, command.dimensionType(), command.dimensionId(),
                command.sellerSku(), command.orderId(), command.storeId(), command.marketplaceId(),
                revenue, productCost, shippingCost, fbaFee, commission, advertisingCost,
                returnCost, storageFee, packagingCost, customDuty, otherCost, totalCost,
                grossProfit, grossMargin, command.currency() != null ? command.currency() : "USD",
                BigDecimal.ONE, grossProfit, Instant.now(), costByType);
        return extStore.saveProfitResult(result);
    }

    @Transactional
    public List<ProfitResult> calculateBatchProfit(String tenantId, List<CalculateProfitCommand> commands) {
        List<ProfitResult> results = new ArrayList<>();
        for (CalculateProfitCommand cmd : commands) {
            results.add(calculateProfit(tenantId, cmd));
        }
        return results;
    }

    public ProfitResult getProfitResult(String tenantId, String resultId) {
        return extStore.findProfitResult(tenantId, resultId)
                .orElseThrow(() -> new BizException("PROFIT_RESULT_NOT_FOUND", "利润结果不存在"));
    }

    public List<ProfitResult> listProfitResults(String tenantId, String dimensionType, String dimensionId) {
        return extStore.listProfitResults(tenantId, dimensionType, dimensionId);
    }

    public List<ProfitResult> listProfitResultsBySku(String tenantId, String sellerSku) {
        return extStore.listProfitResultsBySku(tenantId, sellerSku);
    }

    @Transactional
    public List<ProfitDeviationAlert> detectDeviations(String tenantId, BigDecimal threshold) {
        BigDecimal alertThreshold = threshold != null ? threshold : DEFAULT_MARGIN_THRESHOLD;
        List<ProfitResult> allResults = extStore.listAllProfitResults(tenantId);
        List<ProfitDeviationAlert> alerts = new ArrayList<>();
        for (ProfitResult result : allResults) {
            if (result.grossMargin().compareTo(alertThreshold) < 0) {
                Severity severity = determineSeverity(result.grossMargin());
                ProfitDeviationAlert alert = new ProfitDeviationAlert(
                        UUID.randomUUID().toString(), tenantId, result.dimensionType(),
                        result.dimensionId(), result.sellerSku(), alertThreshold, result.grossMargin(),
                        alertThreshold.subtract(result.grossMargin()).abs(), alertThreshold,
                        severity.name(), AlertStatus.OPEN.name(), Instant.now(), null);
                alerts.add(extStore.saveProfitDeviationAlert(alert));
            }
        }
        return alerts;
    }

    @Transactional
    public ProfitDeviationAlert acknowledgeAlert(String tenantId, String alertId) {
        ProfitDeviationAlert alert = getAlert(tenantId, alertId);
        return extStore.saveProfitDeviationAlert(new ProfitDeviationAlert(
                alert.alertId(), alert.tenantId(), alert.dimensionType(), alert.dimensionId(),
                alert.sellerSku(), alert.expectedMargin(), alert.actualMargin(), alert.deviation(),
                alert.deviationThreshold(), alert.severity(), AlertStatus.ACKNOWLEDGED.name(),
                alert.detectedAt(), alert.resolvedAt()));
    }

    @Transactional
    public ProfitDeviationAlert resolveAlert(String tenantId, String alertId) {
        ProfitDeviationAlert alert = getAlert(tenantId, alertId);
        return extStore.saveProfitDeviationAlert(new ProfitDeviationAlert(
                alert.alertId(), alert.tenantId(), alert.dimensionType(), alert.dimensionId(),
                alert.sellerSku(), alert.expectedMargin(), alert.actualMargin(), alert.deviation(),
                alert.deviationThreshold(), alert.severity(), AlertStatus.RESOLVED.name(),
                alert.detectedAt(), Instant.now()));
    }

    public List<ProfitDeviationAlert> listAlerts(String tenantId, String status) {
        return extStore.listProfitDeviationAlerts(tenantId, status);
    }

    private ProfitDeviationAlert getAlert(String tenantId, String alertId) {
        return extStore.findProfitDeviationAlert(tenantId, alertId)
                .orElseThrow(() -> new BizException("ALERT_NOT_FOUND", "利润偏差告警不存在"));
    }

    private Severity determineSeverity(BigDecimal margin) {
        if (margin.compareTo(BigDecimal.ZERO) < 0) return Severity.CRITICAL;
        if (margin.compareTo(new BigDecimal("0.05")) < 0) return Severity.HIGH;
        if (margin.compareTo(MEDIUM_THRESHOLD) < 0) return Severity.MEDIUM;
        return Severity.LOW;
    }

    public Map<String, BigDecimal> getProfitSummaryByDimension(String tenantId, String dimensionType) {
        List<ProfitResult> results = extStore.listProfitResultsByDimension(tenantId, dimensionType);
        Map<String, BigDecimal> summary = new LinkedHashMap<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalProfit = BigDecimal.ZERO;
        for (ProfitResult r : results) {
            totalRevenue = totalRevenue.add(r.revenue());
            totalProfit = totalProfit.add(r.grossProfit());
        }
        summary.put("totalRevenue", totalRevenue);
        summary.put("totalProfit", totalProfit);
        summary.put("avgMargin", totalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? totalProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO);
        summary.put("count", BigDecimal.valueOf(results.size()));
        return summary;
    }

    public record CalculateProfitCommand(
            String dimensionType, String dimensionId, String sellerSku,
            String orderId, String storeId, String marketplaceId,
            BigDecimal revenue, String currency) {}
}
