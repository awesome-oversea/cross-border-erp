package com.aidotnet.erp.common.fba;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class FbaExtensionService {

    private static final Logger log = LoggerFactory.getLogger(FbaExtensionService.class);
    private final Map<String, InboundPlan> inboundPlans = new ConcurrentHashMap<>();
    private final Map<String, Shipment> shipments = new ConcurrentHashMap<>();
    private final Map<String, Carton> cartons = new ConcurrentHashMap<>();
    private final Map<String, FbaInventory> fbaInventories = new ConcurrentHashMap<>();

    public InboundPlan createInboundPlan(String tenantId, String planName, String marketplace,
                                           String destinationFulfillmentCenter, List<InboundPlanLine> lines) {
        String planId = "FBA-PLAN-" + System.currentTimeMillis();
        InboundPlan plan = new InboundPlan(planId, tenantId, planName, marketplace,
                destinationFulfillmentCenter, lines, "DRAFT", Instant.now(), Instant.now());
        inboundPlans.put(planId, plan);
        log.info("Created inbound plan: id={}, name={}, marketplace={}", planId, planName, marketplace);
        return plan;
    }

    public InboundPlan submitInboundPlan(String planId) {
        InboundPlan existing = inboundPlans.get(planId);
        if (existing == null) throw new IllegalArgumentException("Inbound plan not found: " + planId);
        InboundPlan submitted = new InboundPlan(planId, existing.tenantId(), existing.planName(),
                existing.marketplace(), existing.destinationFulfillmentCenter(),
                existing.lines(), "SUBMITTED", existing.createdAt(), Instant.now());
        inboundPlans.put(planId, submitted);
        log.info("Submitted inbound plan: id={}", planId);
        return submitted;
    }

    public InboundPlan updateInboundPlanStatus(String planId, String status) {
        InboundPlan existing = inboundPlans.get(planId);
        if (existing == null) throw new IllegalArgumentException("Inbound plan not found: " + planId);
        InboundPlan updated = new InboundPlan(planId, existing.tenantId(), existing.planName(),
                existing.marketplace(), existing.destinationFulfillmentCenter(),
                existing.lines(), status, existing.createdAt(), Instant.now());
        inboundPlans.put(planId, updated);
        log.info("Updated inbound plan status: id={}, status={}", planId, status);
        return updated;
    }

    public List<InboundPlan> listInboundPlans(String tenantId, String status) {
        return inboundPlans.values().stream()
                .filter(p -> tenantId == null || tenantId.equals(p.tenantId()))
                .filter(p -> status == null || status.equals(p.status()))
                .toList();
    }

    public Shipment createShipment(String tenantId, String planId, String shipmentId,
                                    String originAddress, String carrier, String trackingNo,
                                    List<ShipmentLine> lines) {
        String internalId = "SHP-" + System.currentTimeMillis();
        Shipment shipment = new Shipment(internalId, tenantId, planId, shipmentId,
                originAddress, carrier, trackingNo, lines, "CREATED", Instant.now(), Instant.now());
        shipments.put(internalId, shipment);
        log.info("Created shipment: id={}, plan={}, carrier={}", internalId, planId, carrier);
        return shipment;
    }

    public Shipment updateShipmentStatus(String internalId, String status, String trackingNo) {
        Shipment existing = shipments.get(internalId);
        if (existing == null) throw new IllegalArgumentException("Shipment not found: " + internalId);
        Shipment updated = new Shipment(internalId, existing.tenantId(), existing.planId(),
                existing.shipmentId(), existing.originAddress(), existing.carrier(),
                trackingNo != null ? trackingNo : existing.trackingNo(),
                existing.lines(), status, existing.createdAt(), Instant.now());
        shipments.put(internalId, updated);
        log.info("Updated shipment status: id={}, status={}", internalId, status);
        return updated;
    }

    public List<Shipment> listShipments(String tenantId, String planId) {
        return shipments.values().stream()
                .filter(s -> tenantId == null || tenantId.equals(s.tenantId()))
                .filter(s -> planId == null || planId.equals(s.planId()))
                .toList();
    }

    public Carton createCarton(String tenantId, String shipmentId, String cartonId,
                                BigDecimal length, BigDecimal width, BigDecimal height,
                                BigDecimal weight, List<CartonItem> items) {
        String internalId = "CTN-" + System.currentTimeMillis();
        BigDecimal volume = length.multiply(width).multiply(height);
        Carton carton = new Carton(internalId, tenantId, shipmentId, cartonId,
                length, width, height, weight, volume, items, Instant.now());
        cartons.put(internalId, carton);
        log.info("Created carton: id={}, shipment={}, items={}", internalId, shipmentId, items.size());
        return carton;
    }

    public List<Carton> listCartons(String tenantId, String shipmentId) {
        return cartons.values().stream()
                .filter(c -> tenantId == null || tenantId.equals(c.tenantId()))
                .filter(c -> shipmentId == null || shipmentId.equals(c.shipmentId()))
                .toList();
    }

    public FbaInventory updateFbaInventory(String tenantId, String sku, String fnsku,
                                             String fulfillmentCenter, int quantity,
                                             int reservedQuantity, int inboundQuantity,
                                             String condition) {
        String key = tenantId + ":" + sku + ":" + fulfillmentCenter;
        FbaInventory existing = fbaInventories.get(key);
        int totalQuantity = quantity + (existing != null ? existing.inboundQuantity() : 0) + inboundQuantity;
        FbaInventory inventory = new FbaInventory(key, tenantId, sku, fnsku, fulfillmentCenter,
                quantity, reservedQuantity, inboundQuantity, totalQuantity, condition, Instant.now());
        fbaInventories.put(key, inventory);
        log.info("Updated FBA inventory: sku={}, fc={}, qty={}, reserved={}", sku, fulfillmentCenter, quantity, reservedQuantity);
        return inventory;
    }

    public List<FbaInventory> listFbaInventory(String tenantId, String sku) {
        return fbaInventories.values().stream()
                .filter(i -> tenantId == null || tenantId.equals(i.tenantId()))
                .filter(i -> sku == null || sku.equals(i.sku()))
                .toList();
    }

    public record InboundPlanLine(String sku, int quantity, String fnsku, String prepCategory) {}
    public record InboundPlan(String planId, String tenantId, String planName, String marketplace,
                               String destinationFulfillmentCenter, List<InboundPlanLine> lines,
                               String status, Instant createdAt, Instant updatedAt) {}
    public record ShipmentLine(String sku, int quantity, int cartonCount) {}
    public record Shipment(String internalId, String tenantId, String planId, String shipmentId,
                            String originAddress, String carrier, String trackingNo,
                            List<ShipmentLine> lines, String status, Instant createdAt, Instant updatedAt) {}
    public record CartonItem(String sku, int quantity) {}
    public record Carton(String internalId, String tenantId, String shipmentId, String cartonId,
                          BigDecimal length, BigDecimal width, BigDecimal height,
                          BigDecimal weight, BigDecimal volume, List<CartonItem> items, Instant createdAt) {}
    public record FbaInventory(String inventoryKey, String tenantId, String sku, String fnsku,
                                String fulfillmentCenter, int quantity, int reservedQuantity,
                                int inboundQuantity, int totalQuantity, String condition, Instant updatedAt) {}
}
