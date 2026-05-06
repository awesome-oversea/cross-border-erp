package com.aidotnet.erp.common.strategy;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LogisticsStrategyService {

    private static final Logger log = LoggerFactory.getLogger(LogisticsStrategyService.class);
    private final Map<String, FreightRule> freightRules = new ConcurrentHashMap<>();

    public void addFreightRule(String carrierCode, String serviceCode, String zone,
                                BigDecimal firstKgRate, BigDecimal additionalKgRate,
                                int estimatedDays) {
        String key = carrierCode + ":" + serviceCode + ":" + zone;
        freightRules.put(key, new FreightRule(carrierCode, serviceCode, zone, firstKgRate, additionalKgRate, estimatedDays));
    }

    public FreightEstimation estimateFreight(String originCountry, String destinationCountry,
                                              BigDecimal weight, BigDecimal length, BigDecimal width, BigDecimal height) {
        BigDecimal volumetricWeight = length.multiply(width).multiply(height)
                .divide(BigDecimal.valueOf(5000), 2, java.math.RoundingMode.HALF_UP);
        BigDecimal chargeableWeight = weight.max(volumetricWeight);

        FreightEstimation best = freightRules.values().stream()
                .filter(r -> r.zone().equals(destinationCountry))
                .map(r -> new FreightEstimation(
                        r.carrierCode(), r.serviceCode(),
                        r.firstKgRate().add(r.additionalKgRate().multiply(chargeableWeight.subtract(BigDecimal.ONE).max(BigDecimal.ZERO))),
                        r.estimatedDays()
                ))
                .min(Comparator.comparing(FreightEstimation::cost))
                .orElse(null);

        if (best == null) {
            log.warn("No freight rule found: destination={}", destinationCountry);
        }
        return best;
    }

    public record FreightRule(String carrierCode, String serviceCode, String zone,
                               BigDecimal firstKgRate, BigDecimal additionalKgRate, int estimatedDays) {}
    public record FreightEstimation(String carrierCode, String serviceCode, BigDecimal cost, int estimatedDays) {}
}
