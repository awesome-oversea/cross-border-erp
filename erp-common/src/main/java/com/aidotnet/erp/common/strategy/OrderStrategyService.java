package com.aidotnet.erp.common.strategy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("commonOrderStrategyService")
public class OrderStrategyService {

    private static final Logger log = LoggerFactory.getLogger(OrderStrategyService.class);
    private final Map<String, Strategy> strategies = new ConcurrentHashMap<>();

    public String createStrategy(String type, String name, Map<String, Object> rules, int priority) {
        String id = "strat-" + type + "-" + System.currentTimeMillis();
        strategies.put(id, new Strategy(id, type, name, rules, priority, true));
        log.info("Created strategy: id={}, type={}, name={}", id, type, name);
        return id;
    }

    public WarehouseAllocationResult allocateWarehouse(String orderId, String destinationCountry,
                                                        List<WarehouseInfo> warehouses, List<ItemInfo> items) {
        WarehouseInfo best = warehouses.stream()
                .filter(w -> w.hasInventoryFor(items))
                .min(Comparator.comparingInt(w -> w.distanceTo(destinationCountry)))
                .orElse(null);

        if (best == null) {
            log.warn("No suitable warehouse found for order: orderId={}, country={}", orderId, destinationCountry);
            return new WarehouseAllocationResult(null, "NO_STOCK", "No warehouse has sufficient inventory");
        }
        return new WarehouseAllocationResult(best.warehouseId(), "SUCCESS", "Allocated to " + best.warehouseName());
    }

    public LogisticsSelectionResult selectLogistics(String orderId, String warehouseId, String destinationCountry,
                                                     BigDecimal weight, BigDecimal value,
                                                     List<LogisticsOption> options) {
        LogisticsOption best = options.stream()
                .filter(o -> o.supportsDestination(destinationCountry))
                .filter(o -> o.canCarryWeight(weight))
                .min(Comparator.comparing(o -> o.score(weight, value)))
                .orElse(null);

        if (best == null) {
            log.warn("No suitable logistics found: orderId={}, country={}", orderId, destinationCountry);
            return new LogisticsSelectionResult(null, null, "NO_OPTION", null);
        }
        return new LogisticsSelectionResult(best.carrierCode(), best.serviceCode(), "SUCCESS", best.estimatedCost());
    }

    public record Strategy(String id, String type, String name, Map<String, Object> rules, int priority, boolean enabled) {}
    public record WarehouseInfo(String warehouseId, String warehouseName, String country, Map<String, Integer> inventory) {
        public boolean hasInventoryFor(List<ItemInfo> items) {
            return true;
        }
        public int distanceTo(String dest) { return 100; }
    }
    public record ItemInfo(String sku, int quantity) {}
    public record WarehouseAllocationResult(String warehouseId, String status, String message) {}
    public record LogisticsOption(String carrierCode, String serviceCode, BigDecimal estimatedCost,
                                   int estimatedDays, List<String> supportedCountries,
                                   BigDecimal maxWeight) {
        public boolean supportsDestination(String country) { return supportedCountries.contains(country); }
        public boolean canCarryWeight(BigDecimal w) { return maxWeight == null || w.compareTo(maxWeight) <= 0; }
        public BigDecimal score(BigDecimal weight, BigDecimal value) { return estimatedCost; }
    }
    public record LogisticsSelectionResult(String carrierCode, String serviceCode, String status, BigDecimal cost) {}
}
