package com.aidotnet.erp.wms.infrastructure;

import com.aidotnet.erp.wms.domain.InventoryMovement;
import com.aidotnet.erp.wms.domain.StockCheckOrder;
import com.aidotnet.erp.wms.domain.StockCheckOrderLine;
import com.aidotnet.erp.wms.domain.TransferOrder;
import com.aidotnet.erp.wms.domain.TransferOrderLine;
import com.aidotnet.erp.wms.infrastructure.data.InventoryMovementDO;
import com.aidotnet.erp.wms.infrastructure.data.StockCheckOrderDO;
import com.aidotnet.erp.wms.infrastructure.data.StockCheckOrderLineDO;
import com.aidotnet.erp.wms.infrastructure.data.TransferOrderDO;
import com.aidotnet.erp.wms.infrastructure.data.TransferOrderLineDO;
import com.aidotnet.erp.wms.infrastructure.mapper.WmsExtMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class WmsExtStore {

    private final WmsExtMapper mapper;

    public WmsExtStore(WmsExtMapper mapper) {
        this.mapper = mapper;
    }

    public TransferOrder saveTransferOrder(TransferOrder order) {
        TransferOrderDO existing = mapper.selectTransferOrder(order.tenantId(), order.transferId());
        TransferOrderDO data = toTransferOrderData(order);
        if (existing == null) {
            mapper.insertTransferOrder(data);
        } else {
            mapper.updateTransferOrder(data);
        }
        return order;
    }

    public Optional<TransferOrder> findTransferOrder(String tenantId, String transferId) {
        return Optional.ofNullable(mapper.selectTransferOrder(tenantId, transferId)).map(this::toTransferOrderDomain);
    }

    public List<TransferOrder> listTransferOrders(String tenantId, String warehouseId) {
        return mapper.selectTransferOrders(tenantId, warehouseId).stream().map(this::toTransferOrderDomain).collect(Collectors.toList());
    }

    public TransferOrderLine saveTransferOrderLine(String tenantId, TransferOrderLine line) {
        TransferOrderLineDO existing = mapper.selectTransferOrderLine(tenantId, line.lineId());
        TransferOrderLineDO data = toTransferOrderLineData(tenantId, line);
        if (existing == null) {
            mapper.insertTransferOrderLine(data);
        } else {
            mapper.updateTransferOrderLine(data);
        }
        return line;
    }

    public List<TransferOrderLine> listTransferOrderLines(String tenantId, String transferId) {
        return mapper.selectTransferOrderLines(tenantId, transferId).stream().map(this::toTransferOrderLineDomain).collect(Collectors.toList());
    }

    public StockCheckOrder saveStockCheckOrder(StockCheckOrder order) {
        StockCheckOrderDO existing = mapper.selectStockCheckOrder(order.tenantId(), order.checkOrderId());
        StockCheckOrderDO data = toStockCheckOrderData(order);
        if (existing == null) {
            mapper.insertStockCheckOrder(data);
        } else {
            mapper.updateStockCheckOrder(data);
        }
        return order;
    }

    public Optional<StockCheckOrder> findStockCheckOrder(String tenantId, String checkOrderId) {
        return Optional.ofNullable(mapper.selectStockCheckOrder(tenantId, checkOrderId)).map(this::toStockCheckOrderDomain);
    }

    public List<StockCheckOrder> listStockCheckOrders(String tenantId, String warehouseId) {
        return mapper.selectStockCheckOrders(tenantId, warehouseId).stream().map(this::toStockCheckOrderDomain).collect(Collectors.toList());
    }

    public StockCheckOrderLine saveStockCheckOrderLine(String tenantId, StockCheckOrderLine line) {
        StockCheckOrderLineDO existing = mapper.selectStockCheckOrderLine(tenantId, line.checkOrderId(), line.lineId());
        StockCheckOrderLineDO data = toStockCheckOrderLineData(tenantId, line);
        if (existing == null) {
            mapper.insertStockCheckOrderLine(data);
        } else {
            mapper.updateStockCheckOrderLine(data);
        }
        return line;
    }

    public Optional<StockCheckOrderLine> findStockCheckOrderLine(String tenantId, String checkOrderId, String lineId) {
        return Optional.ofNullable(mapper.selectStockCheckOrderLine(tenantId, checkOrderId, lineId))
                .map(this::toStockCheckOrderLineDomain);
    }

    public List<StockCheckOrderLine> listStockCheckOrderLines(String tenantId, String checkOrderId) {
        return mapper.selectStockCheckOrderLines(tenantId, checkOrderId).stream().map(this::toStockCheckOrderLineDomain).collect(Collectors.toList());
    }

    private TransferOrderDO toTransferOrderData(TransferOrder o) {
        TransferOrderDO data = new TransferOrderDO();
        data.setTransferId(o.transferId());
        data.setTenantId(o.tenantId());
        data.setFromWarehouseId(o.fromWarehouseId());
        data.setToWarehouseId(o.toWarehouseId());
        data.setStatus(o.status().name());
        data.setRemark(o.remark());
        data.setCreatedAt(o.createdAt() != null ? o.createdAt() : Instant.now());
        data.setUpdatedAt(o.updatedAt() != null ? o.updatedAt() : Instant.now());
        return data;
    }

    private TransferOrder toTransferOrderDomain(TransferOrderDO d) {
        return new TransferOrder(d.getTransferId(), d.getTenantId(), d.getFromWarehouseId(), d.getToWarehouseId(),
                TransferOrder.TransferStatus.valueOf(d.getStatus()), d.getRemark(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private TransferOrderLineDO toTransferOrderLineData(String tenantId, TransferOrderLine l) {
        TransferOrderLineDO data = new TransferOrderLineDO();
        data.setLineId(l.lineId());
        data.setTenantId(tenantId);
        data.setTransferId(l.transferId());
        data.setSellerSku(l.sellerSku());
        data.setTransferQuantity(l.transferQuantity());
        data.setReceivedQuantity(l.receivedQuantity());
        data.setUnitCost(l.unitCost());
        data.setBatchNo(l.batchNo());
        return data;
    }

    private TransferOrderLine toTransferOrderLineDomain(TransferOrderLineDO d) {
        return new TransferOrderLine(d.getLineId(), d.getTransferId(), d.getSellerSku(),
                d.getTransferQuantity(), d.getReceivedQuantity(), d.getUnitCost(), d.getBatchNo());
    }

    private StockCheckOrderDO toStockCheckOrderData(StockCheckOrder o) {
        StockCheckOrderDO data = new StockCheckOrderDO();
        data.setCheckOrderId(o.checkOrderId());
        data.setTenantId(o.tenantId());
        data.setWarehouseId(o.warehouseId());
        data.setCheckType(o.checkType().name());
        data.setStatus(o.status().name());
        data.setCheckedBy(o.checkedBy());
        data.setRemark(o.remark());
        data.setCreatedAt(o.createdAt() != null ? o.createdAt() : Instant.now());
        data.setCompletedAt(o.completedAt());
        return data;
    }

    private StockCheckOrder toStockCheckOrderDomain(StockCheckOrderDO d) {
        return new StockCheckOrder(d.getCheckOrderId(), d.getTenantId(), d.getWarehouseId(),
                StockCheckOrder.CheckType.valueOf(d.getCheckType()), StockCheckOrder.CheckOrderStatus.valueOf(d.getStatus()),
                d.getCheckedBy(), d.getRemark(), d.getCreatedAt(), d.getCompletedAt());
    }

    private StockCheckOrderLineDO toStockCheckOrderLineData(String tenantId, StockCheckOrderLine l) {
        StockCheckOrderLineDO data = new StockCheckOrderLineDO();
        data.setLineId(l.lineId());
        data.setTenantId(tenantId);
        data.setCheckOrderId(l.checkOrderId());
        data.setSellerSku(l.sellerSku());
        data.setLocationId(l.locationId());
        data.setSystemQuantity(l.systemQuantity());
        data.setActualQuantity(l.actualQuantity());
        data.setDifference(l.difference());
        data.setStatus(l.status().name());
        return data;
    }

    private StockCheckOrderLine toStockCheckOrderLineDomain(StockCheckOrderLineDO d) {
        return new StockCheckOrderLine(d.getLineId(), d.getCheckOrderId(), d.getSellerSku(), d.getLocationId(),
                d.getSystemQuantity(), d.getActualQuantity(), d.getDifference(), StockCheckOrderLine.LineStatus.valueOf(d.getStatus()));
    }

    public InventoryMovement saveMovement(InventoryMovement movement) {
        InventoryMovementDO data = toMovementData(movement);
        mapper.insertInventoryMovement(data);
        return movement;
    }

    private InventoryMovementDO toMovementData(InventoryMovement m) {
        InventoryMovementDO data = new InventoryMovementDO();
        data.setMovementId(m.movementId());
        data.setTenantId(m.tenantId());
        data.setWarehouseId(m.warehouseId());
        data.setSellerSku(m.sellerSku());
        data.setFromLocationId(m.fromLocationId());
        data.setToLocationId(m.toLocationId());
        data.setQuantity(m.quantity());
        data.setType(m.type().name());
        data.setReferenceType(m.referenceType());
        data.setReferenceId(m.referenceId());
        data.setMovedAt(m.movedAt() != null ? m.movedAt() : Instant.now());
        return data;
    }
}
