package com.aidotnet.erp.tms.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.tms.domain.Carrier;
import com.aidotnet.erp.tms.domain.CarrierSelectionResult;
import com.aidotnet.erp.tms.domain.CarrierSelectionResult.CarrierOption;
import com.aidotnet.erp.tms.domain.LogisticsStrategy;
import com.aidotnet.erp.tms.domain.ShippingMethod;
import com.aidotnet.erp.tms.domain.ShippingRate;
import com.aidotnet.erp.tms.infrastructure.TmsExtStore;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("tmsLogisticsStrategyService")
public class LogisticsStrategyService {

    private final TmsExtStore tmsExtStore;

    public LogisticsStrategyService(TmsExtStore tmsExtStore) {
        this.tmsExtStore = tmsExtStore;
    }

    @Transactional
    public LogisticsStrategy createStrategy(String tenantId, CreateLogisticsStrategyCommand command) {
        Instant now = Instant.now();
        LogisticsStrategy strategy = new LogisticsStrategy(
                UUID.randomUUID().toString(), tenantId, command.strategyName(), command.strategyType(),
                command.originCountry(), command.destinationCountry(), command.preferredCarrier(),
                command.rules(), true, command.priority(), now, now);
        return tmsExtStore.saveLogisticsStrategy(strategy);
    }

    @Transactional
    public LogisticsStrategy updateStrategy(String tenantId, String strategyId, CreateLogisticsStrategyCommand command) {
        LogisticsStrategy existing = getStrategy(tenantId, strategyId);
        Instant now = Instant.now();
        LogisticsStrategy updated = new LogisticsStrategy(
                existing.strategyId(), existing.tenantId(), command.strategyName(), command.strategyType(),
                command.originCountry(), command.destinationCountry(), command.preferredCarrier(),
                command.rules(), existing.enabled(), command.priority(), existing.createdAt(), now);
        return tmsExtStore.saveLogisticsStrategy(updated);
    }

    @Transactional
    public LogisticsStrategy toggleStrategy(String tenantId, String strategyId, boolean enabled) {
        LogisticsStrategy existing = getStrategy(tenantId, strategyId);
        Instant now = Instant.now();
        LogisticsStrategy updated = new LogisticsStrategy(
                existing.strategyId(), existing.tenantId(), existing.strategyName(), existing.strategyType(),
                existing.originCountry(), existing.destinationCountry(), existing.preferredCarrier(),
                existing.rules(), enabled, existing.priority(), existing.createdAt(), now);
        return tmsExtStore.saveLogisticsStrategy(updated);
    }

    public LogisticsStrategy getStrategy(String tenantId, String strategyId) {
        return tmsExtStore.findLogisticsStrategy(tenantId, strategyId)
                .orElseThrow(() -> new BizException("STRATEGY_NOT_FOUND", "物流策略不存在"));
    }

    public List<LogisticsStrategy> listStrategies(String tenantId, String strategyType) {
        return tmsExtStore.listLogisticsStrategies(tenantId, strategyType);
    }

    public CarrierSelectionResult selectCarrier(String tenantId, CarrierSelectionCommand command) {
        List<LogisticsStrategy> strategies = tmsExtStore.listLogisticsStrategies(tenantId, "CARRIER_SELECTION");
        strategies = strategies.stream().filter(LogisticsStrategy::enabled)
                .filter(s -> matchDestination(s, command.destinationCountry()))
                .sorted((a, b) -> b.priority() - a.priority()).toList();
        List<Carrier> carriers = tmsExtStore.listCarriers(tenantId);
        List<ShippingRate> rates = tmsExtStore.listShippingRates(tenantId, command.originCountry(), command.destinationCountry());
        List<CarrierOption> options = new ArrayList<>();
        for (ShippingRate rate : rates) {
            Carrier carrier = carriers.stream().filter(c -> c.code().equals(rate.methodId())).findFirst().orElse(null);
            if (carrier == null || !carrier.isActive()) continue;
            BigDecimal cost = calculateCost(rate, command.weight(), command.volume());
            BigDecimal score = calculateScore(cost, 7, command.priorityFactor());
            String reason = buildReason(rate, cost, score);
            options.add(new CarrierOption(carrier.code(), rate.methodId(), cost, 7, score, reason));
        }
        if (!strategies.isEmpty() && strategies.get(0).preferredCarrier() != null) {
            String preferred = strategies.get(0).preferredCarrier();
            options.sort((a, b) -> {
                if (a.carrierCode().equals(preferred)) return -1;
                if (b.carrierCode().equals(preferred)) return 1;
                return b.score().compareTo(a.score());
            });
        } else {
            options.sort(Comparator.comparing(CarrierOption::score).reversed());
        }
        if (options.isEmpty()) {
            return new CarrierSelectionResult(null, null, BigDecimal.ZERO, 0, BigDecimal.ZERO, List.of());
        }
        CarrierOption best = options.get(0);
        return new CarrierSelectionResult(best.carrierCode(), best.methodName(), best.cost(),
                best.estimatedDays(), best.score(), options.subList(1, options.size()));
    }

    public BigDecimal calculateFreight(String tenantId, FreightCalculationCommand command) {
        List<ShippingRate> rates = tmsExtStore.listShippingRates(tenantId, command.originCountry(), command.destinationCountry());
        if (rates.isEmpty()) return BigDecimal.ZERO;
        ShippingRate bestRate = rates.stream()
                .min(Comparator.comparing(r -> calculateCost(r, command.weight(), command.volume())))
                .orElse(rates.get(0));
        return calculateCost(bestRate, command.weight(), command.volume());
    }

    private boolean matchDestination(LogisticsStrategy strategy, String destinationCountry) {
        if (strategy.destinationCountry() == null || strategy.destinationCountry().isEmpty()) return true;
        return strategy.destinationCountry().equals(destinationCountry);
    }

    private BigDecimal calculateCost(ShippingRate rate, BigDecimal weight, BigDecimal volume) {
        BigDecimal weightCost = weight.multiply(rate.rate() != null ? rate.rate() : BigDecimal.ZERO);
        BigDecimal baseCost = rate.rate() != null ? rate.rate() : BigDecimal.ZERO;
        return baseCost.add(weightCost).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateScore(BigDecimal cost, int estimatedDays, String priorityFactor) {
        BigDecimal costScore = BigDecimal.ONE.divide(cost.max(BigDecimal.ONE), 4, RoundingMode.HALF_UP);
        BigDecimal timeScore = BigDecimal.ONE.divide(BigDecimal.valueOf(Math.max(estimatedDays, 1)), 4, RoundingMode.HALF_UP);
        return switch (priorityFactor != null ? priorityFactor : "BALANCED") {
            case "COST" -> costScore.multiply(BigDecimal.valueOf(0.7)).add(timeScore.multiply(BigDecimal.valueOf(0.3)));
            case "TIME" -> costScore.multiply(BigDecimal.valueOf(0.3)).add(timeScore.multiply(BigDecimal.valueOf(0.7)));
            default -> costScore.multiply(BigDecimal.valueOf(0.5)).add(timeScore.multiply(BigDecimal.valueOf(0.5)));
        };
    }

    private String buildReason(ShippingRate rate, BigDecimal cost, BigDecimal score) {
        return "Method=" + rate.methodId() + ",Cost=" + cost + ",Score=" + score;
    }

    public record CreateLogisticsStrategyCommand(
            String strategyName, String strategyType, String originCountry,
            String destinationCountry, String preferredCarrier, String rules, int priority) {}

    public record CarrierSelectionCommand(
            String originCountry, String destinationCountry, BigDecimal weight,
            BigDecimal volume, String priorityFactor) {}

    public record FreightCalculationCommand(
            String originCountry, String destinationCountry, BigDecimal weight, BigDecimal volume) {}
}
