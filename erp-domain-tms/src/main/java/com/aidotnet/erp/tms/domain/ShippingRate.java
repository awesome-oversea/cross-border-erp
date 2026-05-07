package com.aidotnet.erp.tms.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * 杩愯垂璐圭巼棰嗗煙妯″瀷
 * <p>
 * 鎻忚堪: 鐗╂祦娓犻亾鐨勮繍璐硅垂鐜囷紝鎸夎捣濮嬪湴銆佺洰鐨勫湴鍜岄噸閲忓尯闂村畾浠枫€? * </p>
 *
 * @author ERP绯荤粺
 */
public record ShippingRate(
        String rateId,
        String tenantId,
        String methodId,
        String originCountry,
        String destinationCountry,
        String zoneCode,
        BigDecimal weightMinKg,
        BigDecimal weightMaxKg,
        BigDecimal baseCost,
        BigDecimal costPerKg,
        String currency,
        Instant effectiveFrom,
        Instant effectiveTo,
        Instant createdAt,
        Instant updatedAt
) {
    public boolean matchesWeight(BigDecimal chargeableWeight) {
        if (chargeableWeight == null) {
            return true;
        }
        boolean aboveMin = weightMinKg == null || chargeableWeight.compareTo(weightMinKg) >= 0;
        boolean belowMax = weightMaxKg == null || chargeableWeight.compareTo(weightMaxKg) <= 0;
        return aboveMin && belowMax;
    }

    /**
     * 渠道规则报价算法：基础费 + 续重单价 * 计费重量。
     */
    public BigDecimal calculateCost(BigDecimal chargeableWeight) {
        BigDecimal safeWeight = chargeableWeight != null ? chargeableWeight : BigDecimal.ZERO;
        BigDecimal safeBaseCost = baseCost != null ? baseCost : BigDecimal.ZERO;
        BigDecimal safeCostPerKg = costPerKg != null ? costPerKg : BigDecimal.ZERO;
        return safeBaseCost.add(safeCostPerKg.multiply(safeWeight));
    }
}
