package com.aidotnet.erp.common.cost;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CostEngineService {

    private static final Logger log = LoggerFactory.getLogger(CostEngineService.class);
    private final Map<String, List<CostEvent>> costEventsByOrder = new ConcurrentHashMap<>();
    private final Map<String, List<CostEvent>> costEventsBySku = new ConcurrentHashMap<>();
    private final Map<String, LinkedList<FifoLayer>> fifoLayersBySku = new ConcurrentHashMap<>();

    public void collectCostEvent(String orderId, String sku, String costType, BigDecimal amount,
                                  String currency, String source, Map<String, Object> metadata) {
        CostEvent event = new CostEvent(orderId, sku, costType, amount, currency, source, metadata, Instant.now());
        costEventsByOrder.computeIfAbsent(orderId, k -> new ArrayList<>()).add(event);
        costEventsBySku.computeIfAbsent(sku, k -> new ArrayList<>()).add(event);

        if ("PURCHASE".equals(costType) && amount.compareTo(BigDecimal.ZERO) > 0) {
            LinkedList<FifoLayer> layers = fifoLayersBySku.computeIfAbsent(sku, k -> new LinkedList<>());
            int quantity = metadata != null && metadata.containsKey("quantity")
                    ? ((Number) metadata.get("quantity")).intValue() : 1;
            layers.addLast(new FifoLayer(sku, quantity, amount.divide(BigDecimal.valueOf(quantity), 2, RoundingMode.HALF_UP), Instant.now()));
        }

        log.debug("Collected cost event: order={}, sku={}, type={}, amount={}", orderId, sku, costType, amount);
    }

    public CostBreakdown generateBreakdown(String orderId) {
        List<CostEvent> events = costEventsByOrder.getOrDefault(orderId, List.of());
        BigDecimal purchaseCost = sumByType(events, "PURCHASE");
        BigDecimal headCost = sumByType(events, "HEAD_TRANSPORT");
        BigDecimal warehouseCost = sumByType(events, "WAREHOUSE");
        BigDecimal platformFee = sumByType(events, "PLATFORM_FEE");
        BigDecimal adCost = sumByType(events, "ADVERTISING");
        BigDecimal paymentFee = sumByType(events, "PAYMENT_FEE");
        BigDecimal tailCost = sumByType(events, "TAIL_TRANSPORT");
        BigDecimal otherCost = sumByType(events, "OTHER");

        BigDecimal totalCost = purchaseCost.add(headCost).add(warehouseCost).add(platformFee)
                .add(adCost).add(paymentFee).add(tailCost).add(otherCost);

        return new CostBreakdown(orderId, purchaseCost, headCost, warehouseCost, platformFee,
                adCost, paymentFee, tailCost, otherCost, totalCost, events.size());
    }

    public SkuCostBreakdown generateSkuBreakdown(String sku) {
        List<CostEvent> events = costEventsBySku.getOrDefault(sku, List.of());
        BigDecimal totalCost = events.stream().map(CostEvent::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new SkuCostBreakdown(sku, totalCost, events.size());
    }

    public void allocateCost(String sourceSku, List<String> targetSkus, BigDecimal totalAmount, String allocationMethod) {
        BigDecimal perSku = totalAmount.divide(BigDecimal.valueOf(targetSkus.size()), 2, RoundingMode.HALF_UP);
        for (String targetSku : targetSkus) {
            collectCostEvent(null, targetSku, "ALLOCATED", perSku, "USD", "ALLOCATION",
                    Map.of("sourceSku", sourceSku, "method", allocationMethod));
        }
        log.info("Allocated cost: source={}, targets={}, total={}, perSku={}",
                sourceSku, targetSkus.size(), totalAmount, perSku);
    }

    public FifoCalculationResult calculateFifo(String sku, int quantitySold) {
        LinkedList<FifoLayer> layers = fifoLayersBySku.get(sku);
        if (layers == null || layers.isEmpty()) {
            log.warn("No FIFO layers found for SKU: {}", sku);
            return new FifoCalculationResult(sku, quantitySold, BigDecimal.ZERO, BigDecimal.ZERO, 0);
        }

        BigDecimal costOfGoodsSold = BigDecimal.ZERO;
        int remaining = quantitySold;
        int layersConsumed = 0;
        LinkedList<FifoLayer> workingCopy = new LinkedList<>(layers);

        while (remaining > 0 && !workingCopy.isEmpty()) {
            FifoLayer oldest = workingCopy.peekFirst();
            if (oldest.quantity() <= remaining) {
                costOfGoodsSold = costOfGoodsSold.add(oldest.unitCost().multiply(BigDecimal.valueOf(oldest.quantity())));
                remaining -= oldest.quantity();
                workingCopy.pollFirst();
                layersConsumed++;
            } else {
                costOfGoodsSold = costOfGoodsSold.add(oldest.unitCost().multiply(BigDecimal.valueOf(remaining)));
                remaining = 0;
                layersConsumed++;
            }
        }

        BigDecimal remainingInventoryValue = workingCopy.stream()
                .map(l -> l.unitCost().multiply(BigDecimal.valueOf(l.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int remainingQuantity = workingCopy.stream().mapToInt(FifoLayer::quantity).sum();

        fifoLayersBySku.put(sku, workingCopy);

        log.info("FIFO calculated: sku={}, sold={}, cogs={}, remainingQty={}", sku, quantitySold, costOfGoodsSold, remainingQuantity);
        return new FifoCalculationResult(sku, quantitySold, costOfGoodsSold, remainingInventoryValue, remainingQuantity);
    }

    public CostTrend getCostTrend(String sku, LocalDate startDate, LocalDate endDate) {
        List<CostEvent> events = costEventsBySku.getOrDefault(sku, List.of());
        List<DailyCostPoint> dailyPoints = new ArrayList<>();
        Map<LocalDate, BigDecimal> dailyTotals = new ConcurrentHashMap<>();

        for (CostEvent event : events) {
            LocalDate eventDate = event.timestamp().atZone(java.time.ZoneOffset.UTC).toLocalDate();
            if (!eventDate.isBefore(startDate) && !eventDate.isAfter(endDate)) {
                dailyTotals.merge(eventDate, event.amount(), BigDecimal::add);
            }
        }

        dailyTotals.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> dailyPoints.add(new DailyCostPoint(e.getKey(), e.getValue())));

        BigDecimal totalCost = dailyPoints.stream().map(DailyCostPoint::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgDailyCost = dailyPoints.isEmpty() ? BigDecimal.ZERO
                : totalCost.divide(BigDecimal.valueOf(dailyPoints.size()), 2, RoundingMode.HALF_UP);

        return new CostTrend(sku, startDate, endDate, dailyPoints, totalCost, avgDailyCost);
    }

    public AnomalyDetectionResult detectAnomaly(String sku, BigDecimal threshold) {
        List<CostEvent> events = costEventsBySku.getOrDefault(sku, List.of());
        List<CostAnomaly> anomalies = new ArrayList<>();

        if (events.size() < 3) {
            return new AnomalyDetectionResult(sku, anomalies, "INSUFFICIENT_DATA");
        }

        BigDecimal avgAmount = events.stream().map(CostEvent::amount).reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(events.size()), 2, RoundingMode.HALF_UP);

        BigDecimal variance = BigDecimal.ZERO;
        for (CostEvent event : events) {
            BigDecimal diff = event.amount().subtract(avgAmount);
            variance = variance.add(diff.multiply(diff));
        }
        BigDecimal stdDev = BigDecimal.valueOf(Math.sqrt(variance.divide(BigDecimal.valueOf(events.size()), 6, RoundingMode.HALF_UP).doubleValue()));

        for (CostEvent event : events) {
            BigDecimal deviation = event.amount().subtract(avgAmount).abs();
            if (stdDev.compareTo(BigDecimal.ZERO) > 0 && deviation.divide(stdDev, 2, RoundingMode.HALF_UP).compareTo(threshold) > 0) {
                anomalies.add(new CostAnomaly(event.eventId(), sku, event.costType(), event.amount(),
                        avgAmount, "COST_ANOMALY", "Cost deviates " + deviation.divide(stdDev, 1, RoundingMode.HALF_UP) + " standard deviations from mean"));
            }
        }

        String status = anomalies.isEmpty() ? "NORMAL" : "ANOMALY_DETECTED";
        log.info("Anomaly detection: sku={}, events={}, anomalies={}, status={}", sku, events.size(), anomalies.size(), status);
        return new AnomalyDetectionResult(sku, anomalies, status);
    }

    private BigDecimal sumByType(List<CostEvent> events, String type) {
        return events.stream()
                .filter(e -> type.equals(e.costType()))
                .map(CostEvent::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public record CostEvent(String eventId, String orderId, String sku, String costType, BigDecimal amount,
                             String currency, String source, Map<String, Object> metadata, Instant timestamp) {
        public CostEvent(String orderId, String sku, String costType, BigDecimal amount,
                          String currency, String source, Map<String, Object> metadata, Instant timestamp) {
            this("CE-" + System.nanoTime(), orderId, sku, costType, amount, currency, source, metadata, timestamp);
        }
    }
    public record CostBreakdown(String orderId, BigDecimal purchaseCost, BigDecimal headTransportCost,
                                BigDecimal warehouseCost, BigDecimal platformFee, BigDecimal advertisingCost,
                                BigDecimal paymentFee, BigDecimal tailTransportCost, BigDecimal otherCost,
                                BigDecimal totalCost, int eventCount) {}
    public record SkuCostBreakdown(String sku, BigDecimal totalCost, int eventCount) {}
    public record FifoLayer(String sku, int quantity, BigDecimal unitCost, Instant acquiredAt) {}
    public record FifoCalculationResult(String sku, int quantitySold, BigDecimal costOfGoodsSold,
                                         BigDecimal remainingInventoryValue, int remainingQuantity) {}
    public record DailyCostPoint(LocalDate date, BigDecimal amount) {}
    public record CostTrend(String sku, LocalDate startDate, LocalDate endDate,
                             List<DailyCostPoint> dailyPoints, BigDecimal totalCost, BigDecimal avgDailyCost) {}
    public record CostAnomaly(String eventId, String sku, String costType, BigDecimal amount,
                               BigDecimal averageAmount, String anomalyType, String description) {}
    public record AnomalyDetectionResult(String sku, List<CostAnomaly> anomalies, String status) {}
}
