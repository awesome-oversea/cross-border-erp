package com.aidotnet.erp.common.tms;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TmsExtensionService {

    private static final Logger log = LoggerFactory.getLogger(TmsExtensionService.class);
    private final Map<String, Carrier> carriers = new ConcurrentHashMap<>();
    private final Map<String, ShippingMethod> shippingMethods = new ConcurrentHashMap<>();
    private final Map<String, TrackingInfo> trackings = new ConcurrentHashMap<>();
    private final Map<String, ShippingBatch> shippingBatches = new ConcurrentHashMap<>();
    private final Map<String, CarrierPerformance> carrierPerformances = new ConcurrentHashMap<>();

    public Carrier createCarrier(String tenantId, String carrierName, String carrierCode,
                                  String carrierType, String contactPerson, String phone, boolean active) {
        String carrierId = "CAR-" + System.currentTimeMillis();
        Carrier carrier = new Carrier(carrierId, tenantId, carrierName, carrierCode,
                carrierType, contactPerson, phone, active, Instant.now(), Instant.now());
        carriers.put(carrierId, carrier);
        log.info("Created carrier: id={}, name={}, code={}", carrierId, carrierName, carrierCode);
        return carrier;
    }

    public List<Carrier> listCarriers(String tenantId, String carrierType) {
        return carriers.values().stream()
                .filter(c -> tenantId == null || tenantId.equals(c.tenantId()))
                .filter(c -> carrierType == null || carrierType.equals(c.carrierType()))
                .toList();
    }

    public ShippingMethod createShippingMethod(String carrierId, String methodName, String methodCode,
                                                String originCountry, String destinationCountry,
                                                BigDecimal minWeight, BigDecimal maxWeight,
                                                int estimatedDays, boolean active) {
        String methodId = "SM-" + System.currentTimeMillis();
        ShippingMethod method = new ShippingMethod(methodId, carrierId, methodName, methodCode,
                originCountry, destinationCountry, minWeight, maxWeight, estimatedDays, active, Instant.now());
        shippingMethods.put(methodId, method);
        log.info("Created shipping method: id={}, carrier={}, name={}", methodId, carrierId, methodName);
        return method;
    }

    public List<ShippingMethod> listShippingMethods(String carrierId, String destinationCountry) {
        return shippingMethods.values().stream()
                .filter(m -> carrierId == null || carrierId.equals(m.carrierId()))
                .filter(m -> destinationCountry == null || destinationCountry.equals(m.destinationCountry()))
                .toList();
    }

    public ShippingEstimate estimateShipping(String originCountry, String destinationCountry,
                                              BigDecimal weight, BigDecimal length, BigDecimal width, BigDecimal height) {
        List<ShippingMethod> candidates = shippingMethods.values().stream()
                .filter(m -> m.active())
                .filter(m -> originCountry == null || originCountry.equals(m.originCountry()) || m.originCountry() == null)
                .filter(m -> destinationCountry.equals(m.destinationCountry()) || m.destinationCountry() == null)
                .filter(m -> weight == null || (m.minWeight() == null || weight.compareTo(m.minWeight()) >= 0)
                        && (m.maxWeight() == null || weight.compareTo(m.maxWeight()) <= 0))
                .toList();

        List<ShippingOption> options = new ArrayList<>();
        for (ShippingMethod method : candidates) {
            Carrier carrier = carriers.get(method.carrierId());
            String carrierName = carrier != null ? carrier.carrierName() : "Unknown";
            BigDecimal baseRate = weight != null ? weight.multiply(new BigDecimal("5.0")) : BigDecimal.ZERO;
            BigDecimal volumetricWeight = BigDecimal.ZERO;
            if (length != null && width != null && height != null) {
                volumetricWeight = length.multiply(width).multiply(height).divide(new BigDecimal("5000"), 2, RoundingMode.HALF_UP);
            }
            BigDecimal chargeableWeight = volumetricWeight.compareTo(weight != null ? weight : BigDecimal.ZERO) > 0
                    ? volumetricWeight : (weight != null ? weight : BigDecimal.ZERO);
            BigDecimal estimatedCost = chargeableWeight.multiply(baseRate.divide(weight != null ? weight : BigDecimal.ONE, 2, RoundingMode.HALF_UP));

            options.add(new ShippingOption(method.methodId(), method.carrierId(), carrierName,
                    method.methodName(), estimatedCost.setScale(2, RoundingMode.HALF_UP),
                    method.estimatedDays(), chargeableWeight.setScale(2, RoundingMode.HALF_UP)));
        }

        options.sort(Comparator.comparing(ShippingOption::estimatedCost));
        log.info("Shipping estimate: origin={}, dest={}, weight={}, options={}", originCountry, destinationCountry, weight, options.size());
        return new ShippingEstimate(originCountry, destinationCountry, weight, options, Instant.now());
    }

    public TrackingInfo updateTracking(String trackingNo, String carrierId, String status,
                                        String location, String description) {
        TrackingInfo existing = trackings.get(trackingNo);
        List<TrackingEvent> events = existing != null ? new ArrayList<>(existing.events()) : new ArrayList<>();
        events.add(new TrackingEvent(status, location, description, Instant.now()));
        TrackingInfo info = new TrackingInfo(trackingNo, carrierId, status, events, Instant.now());
        trackings.put(trackingNo, info);
        log.info("Updated tracking: no={}, status={}, location={}", trackingNo, status, location);
        return info;
    }

    public TrackingInfo getTracking(String trackingNo) {
        return trackings.get(trackingNo);
    }

    public ShippingBatch createShippingBatch(String tenantId, String carrierId, String warehouseId,
                                               List<String> shipmentIds, String operator) {
        String batchId = "BATCH-" + System.currentTimeMillis();
        ShippingBatch batch = new ShippingBatch(batchId, tenantId, carrierId, warehouseId,
                shipmentIds, operator, "CREATED", Instant.now(), Instant.now());
        shippingBatches.put(batchId, batch);
        log.info("Created shipping batch: id={}, carrier={}, shipments={}", batchId, carrierId, shipmentIds.size());
        return batch;
    }

    public ShippingBatch updateBatchStatus(String batchId, String status) {
        ShippingBatch existing = shippingBatches.get(batchId);
        if (existing == null) throw new IllegalArgumentException("Shipping batch not found: " + batchId);
        ShippingBatch updated = new ShippingBatch(batchId, existing.tenantId(), existing.carrierId(),
                existing.warehouseId(), existing.shipmentIds(), existing.operator(), status, existing.createdAt(), Instant.now());
        shippingBatches.put(batchId, updated);
        log.info("Updated shipping batch status: id={}, status={}", batchId, status);
        return updated;
    }

    public CarrierPerformance recordCarrierPerformance(String tenantId, String carrierId,
                                                        int totalShipments, int onTimeDeliveries,
                                                        int damagedShipments, int lostShipments,
                                                        BigDecimal avgDeliveryDays) {
        BigDecimal onTimeRate = totalShipments > 0
                ? BigDecimal.valueOf(onTimeDeliveries).divide(BigDecimal.valueOf(totalShipments), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
        BigDecimal damageRate = totalShipments > 0
                ? BigDecimal.valueOf(damagedShipments).divide(BigDecimal.valueOf(totalShipments), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
        BigDecimal lossRate = totalShipments > 0
                ? BigDecimal.valueOf(lostShipments).divide(BigDecimal.valueOf(totalShipments), 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        String perfId = "PERF-" + System.currentTimeMillis();
        CarrierPerformance perf = new CarrierPerformance(perfId, tenantId, carrierId,
                totalShipments, onTimeDeliveries, damagedShipments, lostShipments,
                onTimeRate, damageRate, lossRate, avgDeliveryDays, Instant.now());
        carrierPerformances.put(perfId, perf);
        log.info("Recorded carrier performance: id={}, carrier={}, onTime={}%, damage={}%",
                perfId, carrierId, onTimeRate, damageRate);
        return perf;
    }

    public List<CarrierPerformance> listCarrierPerformances(String tenantId, String carrierId) {
        return carrierPerformances.values().stream()
                .filter(p -> tenantId == null || tenantId.equals(p.tenantId()))
                .filter(p -> carrierId == null || carrierId.equals(p.carrierId()))
                .toList();
    }

    public record Carrier(String carrierId, String tenantId, String carrierName, String carrierCode,
                           String carrierType, String contactPerson, String phone,
                           boolean active, Instant createdAt, Instant updatedAt) {}
    public record ShippingMethod(String methodId, String carrierId, String methodName, String methodCode,
                                  String originCountry, String destinationCountry,
                                  BigDecimal minWeight, BigDecimal maxWeight,
                                  int estimatedDays, boolean active, Instant createdAt) {}
    public record ShippingOption(String methodId, String carrierId, String carrierName,
                                  String methodName, BigDecimal estimatedCost,
                                  int estimatedDays, BigDecimal chargeableWeight) {}
    public record ShippingEstimate(String originCountry, String destinationCountry, BigDecimal weight,
                                    List<ShippingOption> options, Instant estimatedAt) {}
    public record TrackingEvent(String status, String location, String description, Instant eventTime) {}
    public record TrackingInfo(String trackingNo, String carrierId, String status,
                                List<TrackingEvent> events, Instant updatedAt) {}
    public record ShippingBatch(String batchId, String tenantId, String carrierId, String warehouseId,
                                 List<String> shipmentIds, String operator, String status,
                                 Instant createdAt, Instant updatedAt) {}
    public record CarrierPerformance(String perfId, String tenantId, String carrierId,
                                      int totalShipments, int onTimeDeliveries,
                                      int damagedShipments, int lostShipments,
                                      BigDecimal onTimeRate, BigDecimal damageRate,
                                      BigDecimal lossRate, BigDecimal avgDeliveryDays, Instant recordedAt) {}
}
