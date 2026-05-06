package com.aidotnet.erp.common.wms;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WmsExtensionService {

    private static final Logger log = LoggerFactory.getLogger(WmsExtensionService.class);
    private final Map<String, Warehouse> warehouses = new ConcurrentHashMap<>();
    private final Map<String, StorageLocation> storageLocations = new ConcurrentHashMap<>();
    private final Map<String, InboundOrder> inboundOrders = new ConcurrentHashMap<>();
    private final Map<String, OutboundOrder> outboundOrders = new ConcurrentHashMap<>();
    private final Map<String, QualityCheck> qualityChecks = new ConcurrentHashMap<>();

    public Warehouse createWarehouse(String tenantId, String warehouseName, String warehouseCode,
                                      String address, String type, boolean active) {
        String whId = "WH-" + System.currentTimeMillis();
        Warehouse wh = new Warehouse(whId, tenantId, warehouseName, warehouseCode,
                address, type, active, Instant.now(), Instant.now());
        warehouses.put(whId, wh);
        log.info("Created warehouse: id={}, name={}, code={}", whId, warehouseName, warehouseCode);
        return wh;
    }

    public List<Warehouse> listWarehouses(String tenantId, String type) {
        return warehouses.values().stream()
                .filter(w -> tenantId == null || tenantId.equals(w.tenantId()))
                .filter(w -> type == null || type.equals(w.type()))
                .toList();
    }

    public StorageLocation createStorageLocation(String warehouseId, String locationCode,
                                                  String zone, String aisle, String rack, String bin,
                                                  String locationType, BigDecimal maxWeight) {
        String locId = "LOC-" + System.currentTimeMillis();
        StorageLocation loc = new StorageLocation(locId, warehouseId, locationCode,
                zone, aisle, rack, bin, locationType, maxWeight, true, Instant.now());
        storageLocations.put(locId, loc);
        log.info("Created storage location: id={}, warehouse={}, code={}", locId, warehouseId, locationCode);
        return loc;
    }

    public List<StorageLocation> listStorageLocations(String warehouseId) {
        return storageLocations.values().stream()
                .filter(l -> warehouseId == null || warehouseId.equals(l.warehouseId()))
                .toList();
    }

    public InboundOrder createInboundOrder(String tenantId, String warehouseId, String sourceType,
                                             String sourceId, List<InboundLine> lines) {
        String orderId = "INB-" + System.currentTimeMillis();
        InboundOrder order = new InboundOrder(orderId, tenantId, warehouseId,
                sourceType, sourceId, lines, "PENDING", Instant.now(), Instant.now());
        inboundOrders.put(orderId, order);
        log.info("Created inbound order: id={}, warehouse={}, source={}", orderId, warehouseId, sourceType);
        return order;
    }

    public InboundOrder confirmInbound(String orderId) {
        InboundOrder order = inboundOrders.get(orderId);
        if (order == null) throw new IllegalArgumentException("Inbound order not found: " + orderId);
        InboundOrder confirmed = new InboundOrder(orderId, order.tenantId(), order.warehouseId(),
                order.sourceType(), order.sourceId(), order.lines(), "CONFIRMED", order.createdAt(), Instant.now());
        inboundOrders.put(orderId, confirmed);
        log.info("Confirmed inbound order: id={}", orderId);
        return confirmed;
    }

    public List<InboundOrder> listInboundOrders(String tenantId, String warehouseId) {
        return inboundOrders.values().stream()
                .filter(o -> tenantId == null || tenantId.equals(o.tenantId()))
                .filter(o -> warehouseId == null || warehouseId.equals(o.warehouseId()))
                .toList();
    }

    public OutboundOrder createOutboundOrder(String tenantId, String warehouseId, String orderType,
                                               String referenceId, List<OutboundLine> lines) {
        String orderId = "OUT-" + System.currentTimeMillis();
        OutboundOrder order = new OutboundOrder(orderId, tenantId, warehouseId,
                orderType, referenceId, lines, "PENDING", Instant.now(), Instant.now());
        outboundOrders.put(orderId, order);
        log.info("Created outbound order: id={}, warehouse={}, type={}", orderId, warehouseId, orderType);
        return order;
    }

    public OutboundOrder confirmOutbound(String orderId) {
        OutboundOrder order = outboundOrders.get(orderId);
        if (order == null) throw new IllegalArgumentException("Outbound order not found: " + orderId);
        OutboundOrder confirmed = new OutboundOrder(orderId, order.tenantId(), order.warehouseId(),
                order.orderType(), order.referenceId(), order.lines(), "CONFIRMED", order.createdAt(), Instant.now());
        outboundOrders.put(orderId, confirmed);
        log.info("Confirmed outbound order: id={}", orderId);
        return confirmed;
    }

    public List<OutboundOrder> listOutboundOrders(String tenantId, String warehouseId) {
        return outboundOrders.values().stream()
                .filter(o -> tenantId == null || tenantId.equals(o.tenantId()))
                .filter(o -> warehouseId == null || warehouseId.equals(o.warehouseId()))
                .toList();
    }

    public QualityCheck createQualityCheck(String tenantId, String warehouseId, String inboundOrderId,
                                             String sku, int quantity, String checkType,
                                             String result, String inspector, String remarks) {
        String checkId = "QC-" + System.currentTimeMillis();
        QualityCheck qc = new QualityCheck(checkId, tenantId, warehouseId, inboundOrderId,
                sku, quantity, checkType, result, inspector, remarks, Instant.now());
        qualityChecks.put(checkId, qc);
        log.info("Created quality check: id={}, sku={}, result={}", checkId, sku, result);
        return qc;
    }

    public List<QualityCheck> listQualityChecks(String tenantId, String warehouseId) {
        return qualityChecks.values().stream()
                .filter(q -> tenantId == null || tenantId.equals(q.tenantId()))
                .filter(q -> warehouseId == null || warehouseId.equals(q.warehouseId()))
                .toList();
    }

    public record Warehouse(String whId, String tenantId, String warehouseName, String warehouseCode,
                             String address, String type, boolean active, Instant createdAt, Instant updatedAt) {}
    public record StorageLocation(String locId, String warehouseId, String locationCode,
                                   String zone, String aisle, String rack, String bin,
                                   String locationType, BigDecimal maxWeight, boolean active, Instant createdAt) {}
    public record InboundLine(String sku, int quantity, String locationCode) {}
    public record InboundOrder(String orderId, String tenantId, String warehouseId,
                                String sourceType, String sourceId, List<InboundLine> lines,
                                String status, Instant createdAt, Instant updatedAt) {}
    public record OutboundLine(String sku, int quantity, String locationCode) {}
    public record OutboundOrder(String orderId, String tenantId, String warehouseId,
                                 String orderType, String referenceId, List<OutboundLine> lines,
                                 String status, Instant createdAt, Instant updatedAt) {}
    public record QualityCheck(String checkId, String tenantId, String warehouseId, String inboundOrderId,
                                String sku, int quantity, String checkType, String result,
                                String inspector, String remarks, Instant checkedAt) {}
}
