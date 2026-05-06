package com.aidotnet.erp.common.billing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class BillingStrategyService {

    private static final Logger log = LoggerFactory.getLogger(BillingStrategyService.class);
    private final Map<String, PlatformFeeRule> platformFeeRules = new ConcurrentHashMap<>();
    private final Map<String, WarehouseFeeRule> warehouseFeeRules = new ConcurrentHashMap<>();
    private final Map<String, PackagingCost> packagingCosts = new ConcurrentHashMap<>();

    public void addPlatformFeeRule(String platform, String category, BigDecimal commissionRate,
                                    BigDecimal fixedFee, BigDecimal perItemFee) {
        String key = platform + ":" + category;
        platformFeeRules.put(key, new PlatformFeeRule(platform, category, commissionRate, fixedFee, perItemFee));
    }

    public FeeSimulation simulate(String platform, String category, BigDecimal salePrice,
                                   int quantity, BigDecimal shippingCost, BigDecimal purchaseCost) {
        String key = platform + ":" + category;
        PlatformFeeRule rule = platformFeeRules.get(key);

        BigDecimal commission = BigDecimal.ZERO;
        BigDecimal fixedFee = BigDecimal.ZERO;
        BigDecimal perItemFee = BigDecimal.ZERO;

        if (rule != null) {
            commission = salePrice.multiply(rule.commissionRate()).setScale(2, RoundingMode.HALF_UP);
            fixedFee = rule.fixedFee();
            perItemFee = rule.perItemFee().multiply(BigDecimal.valueOf(quantity));
        }

        BigDecimal totalRevenue = salePrice.multiply(BigDecimal.valueOf(quantity));
        BigDecimal totalCost = commission.add(fixedFee).add(perItemFee)
                .add(shippingCost).add(purchaseCost);
        BigDecimal profit = totalRevenue.subtract(totalCost);
        BigDecimal profitRate = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                ? profit.divide(totalRevenue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return new FeeSimulation(totalRevenue, commission, fixedFee, perItemFee,
                shippingCost, purchaseCost, totalCost, profit, profitRate);
    }

    public BigDecimal calculateWarehouseFee(String warehouseType, BigDecimal volume, int days) {
        WarehouseFeeRule rule = warehouseFeeRules.get(warehouseType);
        if (rule == null) return BigDecimal.ZERO;
        return volume.multiply(rule.dailyRate()).multiply(BigDecimal.valueOf(days)).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getPackagingCost(String packagingType) {
        PackagingCost cost = packagingCosts.get(packagingType);
        return cost != null ? cost.unitCost() : BigDecimal.ZERO;
    }

    public void addWarehouseFeeRule(String warehouseType, BigDecimal dailyRate) {
        warehouseFeeRules.put(warehouseType, new WarehouseFeeRule(warehouseType, dailyRate));
    }

    public void addPackagingCost(String type, BigDecimal unitCost) {
        packagingCosts.put(type, new PackagingCost(type, unitCost));
    }

    public record PlatformFeeRule(String platform, String category, BigDecimal commissionRate,
                                   BigDecimal fixedFee, BigDecimal perItemFee) {}
    public record WarehouseFeeRule(String warehouseType, BigDecimal dailyRate) {}
    public record PackagingCost(String type, BigDecimal unitCost) {}
    public record FeeSimulation(BigDecimal totalRevenue, BigDecimal commission, BigDecimal fixedFee,
                                BigDecimal perItemFee, BigDecimal shippingCost, BigDecimal purchaseCost,
                                BigDecimal totalCost, BigDecimal profit, BigDecimal profitRate) {}
}
