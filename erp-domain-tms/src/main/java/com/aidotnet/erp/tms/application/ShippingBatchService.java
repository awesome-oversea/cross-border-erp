package com.aidotnet.erp.tms.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.tms.domain.Shipment;
import com.aidotnet.erp.tms.domain.ShipmentStatus;
import com.aidotnet.erp.tms.domain.ShippingBatch;
import com.aidotnet.erp.tms.domain.ShippingBatch.ShippingBatchStatus;
import com.aidotnet.erp.tms.infrastructure.ShipmentStore;
import com.aidotnet.erp.tms.infrastructure.TmsExtStore;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShippingBatchService {

    private final TmsExtStore extStore;
    private final ShipmentStore shipmentStore;

    public ShippingBatchService(TmsExtStore extStore, ShipmentStore shipmentStore) {
        this.extStore = extStore;
        this.shipmentStore = shipmentStore;
    }

    @Transactional
    public ShippingBatch createBatch(String tenantId, CreateBatchCommand command) {
        shipmentStore.findCarrier(tenantId, command.carrierId())
                .orElseThrow(() -> new BizException("CARRIER_NOT_FOUND", "Carrier does not exist"));
        List<String> shipmentIds = deduplicateShipmentIds(command.shipmentIds());
        for (String shipmentId : shipmentIds) {
            Shipment shipment = shipmentStore.findShipment(tenantId, shipmentId)
                    .orElseThrow(() -> new BizException("SHIPMENT_NOT_FOUND", "Shipment does not exist: " + shipmentId));
            if (!command.carrierId().equals(shipment.carrierId())) {
                throw new BizException("CARRIER_SHIPMENT_MISMATCH", "Shipment carrier does not match batch carrier: " + shipmentId);
            }
            if (shipment.status() != ShipmentStatus.CREATED) {
                throw new BizException("SHIPMENT_STATUS_INVALID", "Shipment status does not allow batching: " + shipmentId);
            }
        }
        Instant now = Instant.now();
        ShippingBatch batch = new ShippingBatch(UUID.randomUUID().toString(), tenantId, command.carrierId(),
                shipmentIds, ShippingBatchStatus.DRAFT, now, now);
        return extStore.saveShippingBatch(batch);
    }

    @Transactional
    public ShippingBatch submitBatch(String tenantId, String batchId) {
        ShippingBatch batch = getBatch(tenantId, batchId);
        if (batch.status() != ShippingBatchStatus.DRAFT) {
            throw new BizException("BATCH_STATUS_INVALID", "Batch status does not allow submit");
        }
        return extStore.saveShippingBatch(new ShippingBatch(batch.batchId(), batch.tenantId(), batch.carrierId(),
                batch.shipmentIds(), ShippingBatchStatus.SUBMITTED, batch.createdAt(), Instant.now()));
    }

    @Transactional
    public ShippingBatch markInTransit(String tenantId, String batchId) {
        ShippingBatch batch = getBatch(tenantId, batchId);
        if (batch.status() != ShippingBatchStatus.SUBMITTED) {
            throw new BizException("BATCH_STATUS_INVALID", "Batch status does not allow in-transit");
        }
        Instant now = Instant.now();
        // Keep shipment main status aligned with the batch handover milestone.
        for (String shipmentId : batch.shipmentIds()) {
            Shipment shipment = shipmentStore.findShipment(tenantId, shipmentId)
                    .orElseThrow(() -> new BizException("SHIPMENT_NOT_FOUND", "Shipment does not exist: " + shipmentId));
            if (shipment.status() == ShipmentStatus.CREATED) {
                shipmentStore.saveShipment(changeShipmentStatus(shipment, ShipmentStatus.IN_TRANSIT, now));
            }
        }
        return extStore.saveShippingBatch(new ShippingBatch(batch.batchId(), batch.tenantId(), batch.carrierId(),
                batch.shipmentIds(), ShippingBatchStatus.IN_TRANSIT, batch.createdAt(), now));
    }

    @Transactional
    public ShippingBatch completeBatch(String tenantId, String batchId) {
        ShippingBatch batch = getBatch(tenantId, batchId);
        if (batch.status() != ShippingBatchStatus.IN_TRANSIT) {
            throw new BizException("BATCH_STATUS_INVALID", "Batch status does not allow completion");
        }
        return extStore.saveShippingBatch(new ShippingBatch(batch.batchId(), batch.tenantId(), batch.carrierId(),
                batch.shipmentIds(), ShippingBatchStatus.COMPLETED, batch.createdAt(), Instant.now()));
    }

    public ShippingBatch getBatch(String tenantId, String batchId) {
        return extStore.findShippingBatch(tenantId, batchId)
                .orElseThrow(() -> new BizException("BATCH_NOT_FOUND", "Shipping batch does not exist"));
    }

    public List<ShippingBatch> listBatches(String tenantId, String carrierId) {
        return extStore.listShippingBatches(tenantId, carrierId);
    }

    private List<String> deduplicateShipmentIds(List<String> shipmentIds) {
        List<String> deduplicated = new LinkedHashSet<>(shipmentIds).stream().toList();
        if (deduplicated.size() != shipmentIds.size()) {
            throw new BizException("BATCH_SHIPMENT_DUPLICATED", "Duplicate shipments are not allowed in one batch");
        }
        return deduplicated;
    }

    private Shipment changeShipmentStatus(Shipment shipment, ShipmentStatus status, Instant updatedAt) {
        return new Shipment(
                shipment.shipmentId(),
                shipment.tenantId(),
                shipment.orderId(),
                shipment.warehouseId(),
                shipment.carrierId(),
                shipment.shippingMethodId(),
                shipment.trackingNo(),
                shipment.destinationCountry(),
                shipment.weight(),
                shipment.length(),
                shipment.width(),
                shipment.height(),
                shipment.estimatedDelivery(),
                shipment.actualDelivery(),
                shipment.estimatedFreight(),
                shipment.estimatedFreightCurrency(),
                shipment.estimatedChargeableWeight(),
                shipment.estimatedAt(),
                status,
                shipment.trackingEvents(),
                shipment.createdAt(),
                updatedAt);
    }

    public record CreateBatchCommand(String carrierId, List<String> shipmentIds) {}
}
