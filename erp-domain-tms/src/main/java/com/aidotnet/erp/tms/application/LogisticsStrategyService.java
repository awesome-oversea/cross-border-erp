package com.aidotnet.erp.tms.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.tms.domain.Carrier;
import com.aidotnet.erp.tms.domain.CarrierSelectionResult;
import com.aidotnet.erp.tms.domain.CarrierSelectionResult.CarrierOption;
import com.aidotnet.erp.tms.domain.LogisticsStrategy;
import com.aidotnet.erp.tms.domain.ShippingMethod;
import com.aidotnet.erp.tms.domain.ShippingRate;
import com.aidotnet.erp.tms.infrastructure.ShipmentStore;
import com.aidotnet.erp.tms.infrastructure.TmsExtStore;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("tmsLogisticsStrategyService")
public class LogisticsStrategyService {

    private final TmsExtStore tmsExtStore;
    private final ShipmentStore shipmentStore;

    public LogisticsStrategyService(TmsExtStore tmsExtStore, ShipmentStore shipmentStore) {
        this.tmsExtStore = tmsExtStore;
        this.shipmentStore = shipmentStore;
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
        List<Carrier> carriers = shipmentStore.listCarriers(tenantId);
        List<ShippingMethod> methods = shipmentStore.listShippingMethods(tenantId);
        List<ShippingRate> rates = shipmentStore.listShippingRatesByRoute(tenantId, command.originCountry(), command.destinationCountry());
        List<CarrierOption> options = new ArrayList<>();
        for (ShippingRate rate : rates) {
            ShippingMethod method = methods.stream()
                    .filter(candidate -> candidate.methodId().equals(rate.methodId()) && candidate.enabled())
                    .findFirst()
                    .orElse(null);
            if (method == null) {
                continue;
            }
            Carrier carrier = carriers.stream()
                    .filter(candidate -> candidate.carrierId().equals(method.carrierId()))
                    .findFirst()
                    .orElse(null);
            if (carrier == null || !carrier.isActive()) {
                continue;
            }
            BigDecimal cost = calculateCost(rate, command.weight(), command.volume());
            int estimatedDays = method.estimatedDaysMax() != null && method.estimatedDaysMax() > 0 ? method.estimatedDaysMax() : 7;
            BigDecimal score = calculateScore(cost, estimatedDays, command.priorityFactor());
            String reason = buildReason(carrier, method, rate, cost, score);
            options.add(new CarrierOption(carrier.code(), method.methodName(), cost, estimatedDays, score, reason));
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
        FreightQuote quote = estimateFreightQuote(tenantId, new FreightEstimateCommand(
                null,
                null,
                command.originCountry(),
                command.destinationCountry(),
                command.weight(),
                command.volume()));
        return quote != null ? quote.estimatedFreight() : BigDecimal.ZERO;
    }

    /**
     * 运费试算统一走渠道费率表，不直接依赖外部物流商接口，避免内部链路被外部可用性拖垮。
     */
    public FreightQuote estimateFreightQuote(String tenantId, FreightEstimateCommand command) {
        BigDecimal chargeableWeight = resolveChargeableWeight(command.weight(), command.volume());
        Instant now = Instant.now();
        Map<String, Carrier> carrierMap = shipmentStore.listCarriers(tenantId).stream()
                .collect(Collectors.toMap(Carrier::carrierId, Function.identity(), (left, right) -> left));
        Map<String, ShippingMethod> methodMap = shipmentStore.listShippingMethods(tenantId).stream()
                .collect(Collectors.toMap(ShippingMethod::methodId, Function.identity(), (left, right) -> left));
        return shipmentStore.listShippingRatesByRoute(tenantId, command.originCountry(), command.destinationCountry()).stream()
                .filter(rate -> rate.matchesWeight(chargeableWeight))
                .filter(rate -> isRateEffective(rate, now))
                .map(rate -> toFreightQuote(rate, chargeableWeight, carrierMap, methodMap))
                .filter(option -> option != null)
                .filter(option -> command.carrierId() == null || command.carrierId().isBlank()
                        || command.carrierId().equals(option.carrierId()))
                .filter(option -> command.shippingMethodId() == null || command.shippingMethodId().isBlank()
                        || command.shippingMethodId().equals(option.shippingMethodId()))
                .min(Comparator.comparing(FreightQuote::estimatedFreight))
                .orElse(null);
    }

    private boolean matchDestination(LogisticsStrategy strategy, String destinationCountry) {
        if (strategy.destinationCountry() == null || strategy.destinationCountry().isEmpty()) return true;
        return strategy.destinationCountry().equals(destinationCountry);
    }

    private FreightQuote toFreightQuote(ShippingRate rate,
                                        BigDecimal chargeableWeight,
                                        Map<String, Carrier> carrierMap,
                                        Map<String, ShippingMethod> methodMap) {
        ShippingMethod method = methodMap.get(rate.methodId());
        if (method == null || !method.enabled()) {
            return null;
        }
        Carrier carrier = carrierMap.get(method.carrierId());
        if (carrier == null || !carrier.isActive()) {
            return null;
        }
        BigDecimal estimatedFreight = rate.calculateCost(chargeableWeight).setScale(2, RoundingMode.HALF_UP);
        return new FreightQuote(
                carrier.carrierId(),
                carrier.code(),
                carrier.name(),
                method.methodId(),
                method.methodCode(),
                method.methodName(),
                rate.zoneCode(),
                chargeableWeight,
                estimatedFreight,
                rate.currency(),
                method.estimatedDaysMin(),
                method.estimatedDaysMax());
    }

    private boolean isRateEffective(ShippingRate rate, Instant now) {
        if (rate.effectiveFrom() != null && rate.effectiveFrom().isAfter(now)) {
            return false;
        }
        return rate.effectiveTo() == null || !rate.effectiveTo().isBefore(now);
    }

    /**
     * 物流试算按计费重量套用渠道规则，优先使用重量，缺失时退化为体积换算后的近似值。
     */
    private BigDecimal calculateCost(ShippingRate rate, BigDecimal weight, BigDecimal volume) {
        return rate.calculateCost(resolveChargeableWeight(weight, volume)).setScale(2, RoundingMode.HALF_UP);
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

    private String buildReason(Carrier carrier, ShippingMethod method, ShippingRate rate, BigDecimal cost, BigDecimal score) {
        return "Carrier=" + carrier.code()
                + ",Method=" + method.methodCode()
                + ",Zone=" + rate.zoneCode()
                + ",Cost=" + cost
                + ",Score=" + score;
    }

    private BigDecimal resolveChargeableWeight(BigDecimal weight, BigDecimal volume) {
        if (weight != null && volume != null) {
            return weight.max(volume);
        }
        if (weight != null) {
            return weight;
        }
        if (volume != null) {
            return volume;
        }
        return BigDecimal.ZERO;
    }

    public record CreateLogisticsStrategyCommand(
            String strategyName, String strategyType, String originCountry,
            String destinationCountry, String preferredCarrier, String rules, int priority) {}

    public record CarrierSelectionCommand(
            String originCountry, String destinationCountry, BigDecimal weight,
            BigDecimal volume, String priorityFactor) {}

    public record FreightCalculationCommand(
            String originCountry, String destinationCountry, BigDecimal weight, BigDecimal volume) {}

    public record FreightEstimateCommand(
            String carrierId,
            String shippingMethodId,
            String originCountry,
            String destinationCountry,
            BigDecimal weight,
            BigDecimal volume) {}

    public record FreightQuote(
            String carrierId,
            String carrierCode,
            String carrierName,
            String shippingMethodId,
            String shippingMethodCode,
            String shippingMethodName,
            String zoneCode,
            BigDecimal chargeableWeight,
            BigDecimal estimatedFreight,
            String currency,
            Integer estimatedDaysMin,
            Integer estimatedDaysMax) {}
}
