package com.aidotnet.erp.tms.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.tms.domain.Shipment;
import com.aidotnet.erp.tms.domain.ShipmentStatus;
import com.aidotnet.erp.tms.domain.ShippingBatch;
import com.aidotnet.erp.tms.domain.ShippingBatch.ShippingBatchStatus;
import com.aidotnet.erp.tms.infrastructure.ShipmentStore;
import com.aidotnet.erp.tms.infrastructure.TmsExtStore;
import java.time.Instant;
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
                .orElseThrow(() -> new BizException("CARRIER_NOT_FOUND", "物流商不存在"));
        for (String shipmentId : command.shipmentIds()) {
            Shipment shipment = shipmentStore.findShipment(tenantId, shipmentId)
                    .orElseThrow(() -> new BizException("SHIPMENT_NOT_FOUND", "运单不存在: " + shipmentId));
            if (shipment.status() != ShipmentStatus.PENDING) {
                throw new BizException("SHIPMENT_STATUS_INVALID", "运单状态不允许加入批次: " + shipmentId);
            }
        }
        Instant now = Instant.now();
        ShippingBatch batch = new ShippingBatch(UUID.randomUUID().toString(), tenantId, command.carrierId(),
                command.shipmentIds(), ShippingBatchStatus.DRAFT, now, now);
        return extStore.saveShippingBatch(batch);
    }

    @Transactional
    public ShippingBatch submitBatch(String tenantId, String batchId) {
        ShippingBatch batch = getBatch(tenantId, batchId);
        if (batch.status() != ShippingBatchStatus.DRAFT) {
            throw new BizException("BATCH_STATUS_INVALID", "批次状态不允许提交");
        }
        return extStore.saveShippingBatch(new ShippingBatch(batch.batchId(), batch.tenantId(), batch.carrierId(),
                batch.shipmentIds(), ShippingBatchStatus.SUBMITTED, batch.createdAt(), Instant.now()));
    }

    @Transactional
    public ShippingBatch markInTransit(String tenantId, String batchId) {
        ShippingBatch batch = getBatch(tenantId, batchId);
        if (batch.status() != ShippingBatchStatus.SUBMITTED) {
            throw new BizException("BATCH_STATUS_INVALID", "批次状态不允许标记在途");
        }
        return extStore.saveShippingBatch(new ShippingBatch(batch.batchId(), batch.tenantId(), batch.carrierId(),
                batch.shipmentIds(), ShippingBatchStatus.IN_TRANSIT, batch.createdAt(), Instant.now()));
    }

    @Transactional
    public ShippingBatch completeBatch(String tenantId, String batchId) {
        ShippingBatch batch = getBatch(tenantId, batchId);
        if (batch.status() != ShippingBatchStatus.IN_TRANSIT) {
            throw new BizException("BATCH_STATUS_INVALID", "批次状态不允许完成");
        }
        return extStore.saveShippingBatch(new ShippingBatch(batch.batchId(), batch.tenantId(), batch.carrierId(),
                batch.shipmentIds(), ShippingBatchStatus.COMPLETED, batch.createdAt(), Instant.now()));
    }

    public ShippingBatch getBatch(String tenantId, String batchId) {
        return extStore.findShippingBatch(tenantId, batchId)
                .orElseThrow(() -> new BizException("BATCH_NOT_FOUND", "交运批次不存在"));
    }

    public List<ShippingBatch> listBatches(String tenantId, String carrierId) {
        return extStore.listShippingBatches(tenantId, carrierId);
    }

    public record CreateBatchCommand(String carrierId, List<String> shipmentIds) {}
}
