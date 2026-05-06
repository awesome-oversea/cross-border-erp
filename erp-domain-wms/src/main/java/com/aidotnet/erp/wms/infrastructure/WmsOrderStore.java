package com.aidotnet.erp.wms.infrastructure;

import com.aidotnet.erp.wms.domain.InboundOrder;
import com.aidotnet.erp.wms.domain.InboundOrderLine;
import com.aidotnet.erp.wms.domain.InboundOrderStatus;
import com.aidotnet.erp.wms.domain.InventoryMovement;
import com.aidotnet.erp.wms.domain.MovementType;
import com.aidotnet.erp.wms.domain.OutboundOrder;
import com.aidotnet.erp.wms.domain.OutboundOrderLine;
import com.aidotnet.erp.wms.domain.OutboundOrderStatus;
import com.aidotnet.erp.wms.domain.QualityCheck;
import com.aidotnet.erp.wms.domain.QualityCheckResult;
import com.aidotnet.erp.wms.infrastructure.data.InboundOrderDO;
import com.aidotnet.erp.wms.infrastructure.data.InboundOrderLineDO;
import com.aidotnet.erp.wms.infrastructure.data.InventoryMovementDO;
import com.aidotnet.erp.wms.infrastructure.data.OutboundOrderDO;
import com.aidotnet.erp.wms.infrastructure.data.OutboundOrderLineDO;
import com.aidotnet.erp.wms.infrastructure.data.QualityCheckDO;
import com.aidotnet.erp.wms.infrastructure.mapper.WmsOrderMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class WmsOrderStore {

    private final WmsOrderMapper mapper;

    public WmsOrderStore(WmsOrderMapper mapper) {
        this.mapper = mapper;
    }

    public InboundOrder saveInboundOrder(InboundOrder order) {
        InboundOrderDO existing = mapper.selectInboundOrder(order.tenantId(), order.orderId());
        InboundOrderDO data = toInboundOrderData(order);
        if (existing == null) {
            mapper.insertInboundOrder(data);
        } else {
            mapper.updateInboundOrder(data);
        }
        return order;
    }

    public Optional<InboundOrder> findInboundOrder(String tenantId, String orderId) {
        return Optional.ofNullable(mapper.selectInboundOrder(tenantId, orderId)).map(this::toInboundOrderDomain);
    }

    public List<InboundOrder> listInboundOrders(String tenantId, String warehouseId) {
        return mapper.selectInboundOrders(tenantId, warehouseId).stream().map(this::toInboundOrderDomain).collect(Collectors.toList());
    }

    public void saveInboundOrderLine(InboundOrderLine line) {
        mapper.insertInboundOrderLine(toInboundOrderLineData(line));
    }

    public void updateInboundOrderLine(InboundOrderLine line) {
        mapper.updateInboundOrderLine(toInboundOrderLineData(line));
    }

    public List<InboundOrderLine> listInboundOrderLines(String orderId) {
        return mapper.selectInboundOrderLines(orderId).stream().map(this::toInboundOrderLineDomain).collect(Collectors.toList());
    }

    public OutboundOrder saveOutboundOrder(OutboundOrder order) {
        OutboundOrderDO existing = mapper.selectOutboundOrder(order.tenantId(), order.orderId());
        OutboundOrderDO data = toOutboundOrderData(order);
        if (existing == null) {
            mapper.insertOutboundOrder(data);
        } else {
            mapper.updateOutboundOrder(data);
        }
        return order;
    }

    public Optional<OutboundOrder> findOutboundOrder(String tenantId, String orderId) {
        return Optional.ofNullable(mapper.selectOutboundOrder(tenantId, orderId)).map(this::toOutboundOrderDomain);
    }

    public List<OutboundOrder> listOutboundOrders(String tenantId, String warehouseId) {
        return mapper.selectOutboundOrders(tenantId, warehouseId).stream().map(this::toOutboundOrderDomain).collect(Collectors.toList());
    }

    public void saveOutboundOrderLine(OutboundOrderLine line) {
        mapper.insertOutboundOrderLine(toOutboundOrderLineData(line));
    }

    public void updateOutboundOrderLine(OutboundOrderLine line) {
        mapper.updateOutboundOrderLine(toOutboundOrderLineData(line));
    }

    public List<OutboundOrderLine> listOutboundOrderLines(String orderId) {
        return mapper.selectOutboundOrderLines(orderId).stream().map(this::toOutboundOrderLineDomain).collect(Collectors.toList());
    }

    public InventoryMovement saveMovement(InventoryMovement movement) {
        mapper.insertMovement(toMovementData(movement));
        return movement;
    }

    public List<InventoryMovement> listMovements(String tenantId, String warehouseId) {
        return mapper.selectMovements(tenantId, warehouseId).stream().map(this::toMovementDomain).collect(Collectors.toList());
    }

    public QualityCheck saveQualityCheck(QualityCheck check) {
        mapper.insertQualityCheck(toQualityCheckData(check));
        return check;
    }

    public Optional<QualityCheck> findQualityCheck(String tenantId, String checkId) {
        return Optional.ofNullable(mapper.selectQualityCheck(tenantId, checkId)).map(this::toQualityCheckDomain);
    }

    public List<QualityCheck> listQualityChecks(String tenantId, String warehouseId) {
        return mapper.selectQualityChecks(tenantId, warehouseId).stream().map(this::toQualityCheckDomain).collect(Collectors.toList());
    }

    public List<QualityCheck> listQualityChecksByInboundOrder(String tenantId, String inboundOrderId) {
        return mapper.selectQualityChecksByInboundOrder(tenantId, inboundOrderId).stream().map(this::toQualityCheckDomain).collect(Collectors.toList());
    }

    private InboundOrderDO toInboundOrderData(InboundOrder o) {
        InboundOrderDO data = new InboundOrderDO();
        data.setOrderId(o.orderId());
        data.setTenantId(o.tenantId());
        data.setWarehouseId(o.warehouseId());
        data.setReferenceType(o.referenceType());
        data.setReferenceId(o.referenceId());
        data.setStatus(o.status().name());
        data.setRemark(o.remark());
        data.setCreatedAt(o.createdAt() != null ? o.createdAt() : Instant.now());
        data.setUpdatedAt(o.updatedAt() != null ? o.updatedAt() : Instant.now());
        return data;
    }

    private InboundOrder toInboundOrderDomain(InboundOrderDO d) {
        return new InboundOrder(d.getOrderId(), d.getTenantId(), d.getWarehouseId(), d.getReferenceType(),
                d.getReferenceId(), InboundOrderStatus.valueOf(d.getStatus()), d.getRemark(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private InboundOrderLineDO toInboundOrderLineData(InboundOrderLine l) {
        InboundOrderLineDO data = new InboundOrderLineDO();
        data.setLineId(l.lineId());
        data.setOrderId(l.orderId());
        data.setSellerSku(l.sellerSku());
        data.setLocationId(l.locationId());
        data.setExpectedQuantity(l.expectedQuantity());
        data.setReceivedQuantity(l.receivedQuantity());
        data.setUnitCost(l.unitCost());
        data.setBatchNo(l.batchNo());
        return data;
    }

    private InboundOrderLine toInboundOrderLineDomain(InboundOrderLineDO d) {
        return new InboundOrderLine(d.getLineId(), d.getOrderId(), d.getSellerSku(), d.getLocationId(),
                d.getExpectedQuantity(), d.getReceivedQuantity(), d.getUnitCost(), d.getBatchNo());
    }

    private OutboundOrderDO toOutboundOrderData(OutboundOrder o) {
        OutboundOrderDO data = new OutboundOrderDO();
        data.setOrderId(o.orderId());
        data.setTenantId(o.tenantId());
        data.setWarehouseId(o.warehouseId());
        data.setReferenceType(o.referenceType());
        data.setReferenceId(o.referenceId());
        data.setStatus(o.status().name());
        data.setRemark(o.remark());
        data.setCreatedAt(o.createdAt() != null ? o.createdAt() : Instant.now());
        data.setUpdatedAt(o.updatedAt() != null ? o.updatedAt() : Instant.now());
        return data;
    }

    private OutboundOrder toOutboundOrderDomain(OutboundOrderDO d) {
        return new OutboundOrder(d.getOrderId(), d.getTenantId(), d.getWarehouseId(), d.getReferenceType(),
                d.getReferenceId(), OutboundOrderStatus.valueOf(d.getStatus()), d.getRemark(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private OutboundOrderLineDO toOutboundOrderLineData(OutboundOrderLine l) {
        OutboundOrderLineDO data = new OutboundOrderLineDO();
        data.setLineId(l.lineId());
        data.setOrderId(l.orderId());
        data.setSellerSku(l.sellerSku());
        data.setLocationId(l.locationId());
        data.setRequiredQuantity(l.requiredQuantity());
        data.setPickedQuantity(l.pickedQuantity());
        data.setBatchNo(l.batchNo());
        return data;
    }

    private OutboundOrderLine toOutboundOrderLineDomain(OutboundOrderLineDO d) {
        return new OutboundOrderLine(d.getLineId(), d.getOrderId(), d.getSellerSku(), d.getLocationId(),
                d.getRequiredQuantity(), d.getPickedQuantity(), d.getBatchNo());
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

    private InventoryMovement toMovementDomain(InventoryMovementDO d) {
        return new InventoryMovement(d.getMovementId(), d.getTenantId(), d.getWarehouseId(), d.getSellerSku(),
                d.getFromLocationId(), d.getToLocationId(), d.getQuantity(), MovementType.valueOf(d.getType()),
                d.getReferenceType(), d.getReferenceId(), d.getMovedAt());
    }

    private QualityCheckDO toQualityCheckData(QualityCheck c) {
        QualityCheckDO data = new QualityCheckDO();
        data.setCheckId(c.checkId());
        data.setTenantId(c.tenantId());
        data.setWarehouseId(c.warehouseId());
        data.setInboundOrderId(c.inboundOrderId());
        data.setSellerSku(c.sellerSku());
        data.setSampleQuantity(c.sampleQuantity());
        data.setPassQuantity(c.passQuantity());
        data.setFailQuantity(c.failQuantity());
        data.setResult(c.result().name());
        data.setInspector(c.inspector());
        data.setRemark(c.remark());
        data.setCheckedAt(c.checkedAt() != null ? c.checkedAt() : Instant.now());
        data.setCreatedAt(c.createdAt() != null ? c.createdAt() : Instant.now());
        return data;
    }

    private QualityCheck toQualityCheckDomain(QualityCheckDO d) {
        return new QualityCheck(d.getCheckId(), d.getTenantId(), d.getWarehouseId(), d.getInboundOrderId(),
                d.getSellerSku(), d.getSampleQuantity(), d.getPassQuantity(), d.getFailQuantity(),
                QualityCheckResult.valueOf(d.getResult()), d.getInspector(), d.getRemark(), d.getCheckedAt(), d.getCreatedAt());
    }
}
