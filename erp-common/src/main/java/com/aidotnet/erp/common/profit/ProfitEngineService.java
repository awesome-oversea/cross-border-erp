package com.aidotnet.erp.common.profit;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ProfitEngineService {

    private static final Logger log = LoggerFactory.getLogger(ProfitEngineService.class);
    private final Map<String, ProfitResult> orderProfits = new ConcurrentHashMap<>();
    private final Map<String, List<ProfitResult>> skuProfits = new ConcurrentHashMap<>();
    private final Map<String, List<ProfitResult>> storeProfits = new ConcurrentHashMap<>();
    private final Map<String, List<ProfitResult>> channelProfits = new ConcurrentHashMap<>();
    private final Map<String, List<ProfitResult>> marketProfits = new ConcurrentHashMap<>();
    private final Map<String, CustomFeeDefinition> customFeeDefinitions = new ConcurrentHashMap<>();
    private final Map<String, List<CustomFeeEntry>> customFeeEntries = new ConcurrentHashMap<>();

    public ProfitResult calculateOrderProfit(String orderId, String sku, String storeId,
                                              String channel, String market, BigDecimal netRevenue,
                                              BigDecimal purchaseCost, BigDecimal headTransport,
                                              BigDecimal platformFee, BigDecimal advertisingCost,
                                              BigDecimal paymentFee, BigDecimal tailTransport,
                                              BigDecimal taxCost, BigDecimal otherCost) {
        BigDecimal customFeesTotal = calculateCustomFeesForOrder(orderId);
        BigDecimal totalCost = purchaseCost.add(headTransport).add(platformFee)
                .add(advertisingCost).add(paymentFee).add(tailTransport)
                .add(taxCost).add(otherCost).add(customFeesTotal);
        BigDecimal grossProfit = netRevenue.subtract(totalCost);
        BigDecimal profitRate = netRevenue.compareTo(BigDecimal.ZERO) > 0
                ? grossProfit.divide(netRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        ProfitResult result = new ProfitResult(orderId, sku, storeId, channel, market, netRevenue,
                purchaseCost, headTransport, platformFee, advertisingCost, paymentFee, tailTransport,
                taxCost, otherCost, customFeesTotal, totalCost, grossProfit, profitRate, Instant.now());

        orderProfits.put(orderId, result);
        if (sku != null) skuProfits.computeIfAbsent(sku, k -> new ArrayList<>()).add(result);
        if (storeId != null) storeProfits.computeIfAbsent(storeId, k -> new ArrayList<>()).add(result);
        if (channel != null) channelProfits.computeIfAbsent(channel, k -> new ArrayList<>()).add(result);
        if (market != null) marketProfits.computeIfAbsent(market, k -> new ArrayList<>()).add(result);

        log.debug("Calculated order profit: orderId={}, revenue={}, cost={}, profit={}, rate={}%",
                orderId, netRevenue, totalCost, grossProfit, profitRate);
        return result;
    }

    public ProfitResult calculateOrderProfit(String orderId, BigDecimal netRevenue,
                                              BigDecimal purchaseCost, BigDecimal headTransport,
                                              BigDecimal platformFee, BigDecimal advertisingCost,
                                              BigDecimal paymentFee, BigDecimal tailTransport,
                                              BigDecimal taxCost, BigDecimal otherCost) {
        return calculateOrderProfit(orderId, null, null, null, null, netRevenue,
                purchaseCost, headTransport, platformFee, advertisingCost, paymentFee,
                tailTransport, taxCost, otherCost);
    }

    public ProfitSummary getSkuProfitSummary(String sku) {
        return buildSummary(sku, skuProfits.getOrDefault(sku, List.of()));
    }

    public ProfitSummary getStoreProfitSummary(String storeId) {
        return buildSummary(storeId, storeProfits.getOrDefault(storeId, List.of()));
    }

    public ChannelProfitSummary getChannelProfitSummary(String channel) {
        List<ProfitResult> results = channelProfits.getOrDefault(channel, List.of());
        ProfitSummary summary = buildSummary(channel, results);
        Map<String, BigDecimal> byMarket = new HashMap<>();
        for (ProfitResult r : results) {
            if (r.market() != null) {
                byMarket.merge(r.market(), r.grossProfit(), BigDecimal::add);
            }
        }
        return new ChannelProfitSummary(channel, summary.orderCount(), summary.totalRevenue(),
                summary.totalCost(), summary.totalProfit(), summary.avgProfitRate(), byMarket);
    }

    public MarketProfitSummary getMarketProfitSummary(String market) {
        List<ProfitResult> results = marketProfits.getOrDefault(market, List.of());
        ProfitSummary summary = buildSummary(market, results);
        Map<String, BigDecimal> byChannel = new HashMap<>();
        for (ProfitResult r : results) {
            if (r.channel() != null) {
                byChannel.merge(r.channel(), r.grossProfit(), BigDecimal::add);
            }
        }
        return new MarketProfitSummary(market, summary.orderCount(), summary.totalRevenue(),
                summary.totalCost(), summary.totalProfit(), summary.avgProfitRate(), byChannel);
    }

    public CustomFeeDefinition defineCustomFee(String feeCode, String feeName, BigDecimal defaultAmount,
                                                String calculationMethod, String description) {
        CustomFeeDefinition def = new CustomFeeDefinition(feeCode, feeName, defaultAmount,
                calculationMethod, description, true, Instant.now());
        customFeeDefinitions.put(feeCode, def);
        log.info("Defined custom fee: code={}, name={}, method={}", feeCode, feeName, calculationMethod);
        return def;
    }

    public void applyCustomFee(String orderId, String feeCode, BigDecimal amount, String remark) {
        if (!customFeeDefinitions.containsKey(feeCode)) {
            throw new IllegalArgumentException("Custom fee not defined: " + feeCode);
        }
        CustomFeeEntry entry = new CustomFeeEntry(orderId, feeCode, amount, remark, Instant.now());
        customFeeEntries.computeIfAbsent(orderId, k -> new ArrayList<>()).add(entry);
        log.debug("Applied custom fee: order={}, feeCode={}, amount={}", orderId, feeCode, amount);
    }

    public List<CustomFeeEntry> getCustomFeesForOrder(String orderId) {
        return customFeeEntries.getOrDefault(orderId, List.of());
    }

    public List<CustomFeeDefinition> listCustomFeeDefinitions() {
        return new ArrayList<>(customFeeDefinitions.values());
    }

    private BigDecimal calculateCustomFeesForOrder(String orderId) {
        List<CustomFeeEntry> entries = customFeeEntries.getOrDefault(orderId, List.of());
        return entries.stream().map(CustomFeeEntry::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<ProfitAlert> checkProfitAlerts(BigDecimal threshold) {
        List<ProfitAlert> alerts = new ArrayList<>();
        for (ProfitResult result : orderProfits.values()) {
            if (result.profitRate().compareTo(threshold) < 0) {
                alerts.add(new ProfitAlert(result.orderId(), "LOW_PROFIT",
                        "Profit rate " + result.profitRate() + "% below threshold " + threshold + "%",
                        result.profitRate(), threshold));
            }
            if (result.grossProfit().compareTo(BigDecimal.ZERO) < 0) {
                alerts.add(new ProfitAlert(result.orderId(), "NEGATIVE_PROFIT",
                        "Negative profit: " + result.grossProfit(),
                        result.profitRate(), BigDecimal.ZERO));
            }
        }
        return alerts;
    }

    public AiProfitAnalysis aiAnalyze(String dimension, String dimensionValue) {
        List<ProfitResult> results;
        switch (dimension) {
            case "SKU" -> results = skuProfits.getOrDefault(dimensionValue, List.of());
            case "STORE" -> results = storeProfits.getOrDefault(dimensionValue, List.of());
            case "CHANNEL" -> results = channelProfits.getOrDefault(dimensionValue, List.of());
            case "MARKET" -> results = marketProfits.getOrDefault(dimensionValue, List.of());
            default -> results = List.of();
        }

        if (results.isEmpty()) {
            return new AiProfitAnalysis(dimension, dimensionValue, "NO_DATA",
                    List.of(), List.of(), Map.of());
        }

        BigDecimal totalRevenue = results.stream().map(ProfitResult::netRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCost = results.stream().map(ProfitResult::totalCost).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalProfit = results.stream().map(ProfitResult::grossProfit).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgRate = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? totalProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        List<String> insights = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        Map<String, Object> metrics = new HashMap<>();

        metrics.put("orderCount", results.size());
        metrics.put("totalRevenue", totalRevenue);
        metrics.put("totalCost", totalCost);
        metrics.put("totalProfit", totalProfit);
        metrics.put("avgProfitRate", avgRate);

        if (avgRate.compareTo(new BigDecimal("10")) < 0) {
            insights.add("Profit rate is below 10%, indicating low margin");
            recommendations.add("Review pricing strategy and cost optimization opportunities");
        }
        if (avgRate.compareTo(BigDecimal.ZERO) < 0) {
            insights.add("Negative profit detected - losses are occurring");
            recommendations.add("Immediately review cost structure and consider discontinuing unprofitable items");
        }
        if (avgRate.compareTo(new BigDecimal("30")) > 0) {
            insights.add("Healthy profit margin above 30%");
            recommendations.add("Consider increasing investment in this dimension to scale profitable operations");
        }

        BigDecimal avgRevenue = totalRevenue.divide(BigDecimal.valueOf(results.size()), 2, RoundingMode.HALF_UP);
        long lowPerformers = results.stream()
                .filter(r -> r.profitRate().compareTo(new BigDecimal("5")) < 0)
                .count();
        if (lowPerformers > results.size() / 3) {
            insights.add(lowPerformers + " out of " + results.size() + " orders have profit rate below 5%");
            recommendations.add("Investigate low-performing orders for cost reduction or price adjustment");
        }

        metrics.put("avgRevenuePerOrder", avgRevenue);
        metrics.put("lowPerformerCount", lowPerformers);

        String status = avgRate.compareTo(BigDecimal.ZERO) > 0 ? "PROFITABLE" : "UNPROFITABLE";
        return new AiProfitAnalysis(dimension, dimensionValue, status, insights, recommendations, metrics);
    }

    private ProfitSummary buildSummary(String key, List<ProfitResult> results) {
        BigDecimal totalRevenue = results.stream().map(ProfitResult::netRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCost = results.stream().map(ProfitResult::totalCost).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalProfit = totalRevenue.subtract(totalCost);
        BigDecimal avgRate = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? totalProfit.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
        return new ProfitSummary(key, results.size(), totalRevenue, totalCost, totalProfit, avgRate);
    }

    public record ProfitResult(String orderId, String sku, String storeId, String channel, String market,
                                BigDecimal netRevenue, BigDecimal purchaseCost, BigDecimal headTransport,
                                BigDecimal platformFee, BigDecimal advertisingCost, BigDecimal paymentFee,
                                BigDecimal tailTransport, BigDecimal taxCost, BigDecimal otherCost,
                                BigDecimal customFees, BigDecimal totalCost, BigDecimal grossProfit,
                                BigDecimal profitRate, Instant calculatedAt) {}
    public record ProfitSummary(String key, int orderCount, BigDecimal totalRevenue, BigDecimal totalCost,
                                BigDecimal totalProfit, BigDecimal avgProfitRate) {}
    public record ChannelProfitSummary(String channel, int orderCount, BigDecimal totalRevenue,
                                        BigDecimal totalCost, BigDecimal totalProfit, BigDecimal avgProfitRate,
                                        Map<String, BigDecimal> profitByMarket) {}
    public record MarketProfitSummary(String market, int orderCount, BigDecimal totalRevenue,
                                       BigDecimal totalCost, BigDecimal totalProfit, BigDecimal avgProfitRate,
                                       Map<String, BigDecimal> profitByChannel) {}
    public record ProfitAlert(String orderId, String alertType, String message,
                               BigDecimal profitRate, BigDecimal threshold) {}
    public record CustomFeeDefinition(String feeCode, String feeName, BigDecimal defaultAmount,
                                       String calculationMethod, String description, boolean active,
                                       Instant createdAt) {}
    public record CustomFeeEntry(String orderId, String feeCode, BigDecimal amount,
                                  String remark, Instant appliedAt) {}
    public record AiProfitAnalysis(String dimension, String dimensionValue, String status,
                                    List<String> insights, List<String> recommendations,
                                    Map<String, Object> metrics) {}
}
