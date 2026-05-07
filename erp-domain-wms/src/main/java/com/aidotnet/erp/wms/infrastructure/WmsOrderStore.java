package com.aidotnet.erp.wms.infrastructure;

import com.aidotnet.erp.wms.domain.DefectiveReturn;
import com.aidotnet.erp.wms.domain.DefectiveReturnStatus;
import com.aidotnet.erp.wms.domain.DefectiveSupplierReply;
import com.aidotnet.erp.wms.domain.InboundOrder;
import com.aidotnet.erp.wms.domain.InboundOrderLine;
import com.aidotnet.erp.wms.domain.InboundOrderStatus;
import com.aidotnet.erp.wms.domain.InventoryMovement;
import com.aidotnet.erp.wms.domain.MovementType;
import com.aidotnet.erp.wms.domain.OutboundOrder;
import com.aidotnet.erp.wms.domain.OutboundOrderLine;
import com.aidotnet.erp.wms.domain.OutboundOrderStatus;
import com.aidotnet.erp.wms.domain.OutboundPackage;
import com.aidotnet.erp.wms.domain.OutboundPackageLine;
import com.aidotnet.erp.wms.domain.OutboundPackageStatus;
import com.aidotnet.erp.wms.domain.ProductRepair;
import com.aidotnet.erp.wms.domain.ProductRepairStatus;
import com.aidotnet.erp.wms.domain.QualityCheck;
import com.aidotnet.erp.wms.domain.QualityCheckResult;
import com.aidotnet.erp.wms.infrastructure.data.DefectiveReturnDO;
import com.aidotnet.erp.wms.infrastructure.data.InboundOrderDO;
import com.aidotnet.erp.wms.infrastructure.data.InboundOrderLineDO;
import com.aidotnet.erp.wms.infrastructure.data.InventoryMovementDO;
import com.aidotnet.erp.wms.infrastructure.data.OutboundOrderDO;
import com.aidotnet.erp.wms.infrastructure.data.OutboundOrderLineDO;
import com.aidotnet.erp.wms.infrastructure.data.OutboundPackageDO;
import com.aidotnet.erp.wms.infrastructure.data.OutboundPackageLineDO;
import com.aidotnet.erp.wms.infrastructure.data.ProductRepairDO;
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

    public OutboundPackage saveOutboundPackage(OutboundPackage outboundPackage) {
        OutboundPackageDO existing = mapper.selectOutboundPackage(outboundPackage.tenantId(), outboundPackage.packageId());
        OutboundPackageDO data = toOutboundPackageData(outboundPackage);
        if (existing == null) {
            mapper.insertOutboundPackage(data);
        } else {
            mapper.updateOutboundPackage(data);
        }
        return outboundPackage;
    }

    public Optional<OutboundPackage> findOutboundPackage(String tenantId, String packageId) {
        return Optional.ofNullable(mapper.selectOutboundPackage(tenantId, packageId)).map(this::toOutboundPackageDomain);
    }

    public List<OutboundPackage> listOutboundPackages(String tenantId, String orderId) {
        return mapper.selectOutboundPackagesByOrder(tenantId, orderId).stream()
                .map(this::toOutboundPackageDomain)
                .collect(Collectors.toList());
    }

    public void saveOutboundPackageLine(OutboundPackageLine packageLine) {
        mapper.insertOutboundPackageLine(toOutboundPackageLineData(packageLine));
    }

    public List<OutboundPackageLine> listOutboundPackageLines(String packageId) {
        return mapper.selectOutboundPackageLines(packageId).stream()
                .map(this::toOutboundPackageLineDomain)
                .collect(Collectors.toList());
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

    public DefectiveReturn saveDefectiveReturn(DefectiveReturn defectiveReturn) {
        DefectiveReturnDO existing = mapper.selectDefectiveReturn(defectiveReturn.tenantId(), defectiveReturn.returnId());
        DefectiveReturnDO data = toDefectiveReturnData(defectiveReturn);
        if (existing == null) {
            mapper.insertDefectiveReturn(data);
        } else {
            mapper.updateDefectiveReturn(data);
        }
        return defectiveReturn;
    }

    public Optional<DefectiveReturn> findDefectiveReturn(String tenantId, String returnId) {
        return Optional.ofNullable(mapper.selectDefectiveReturn(tenantId, returnId)).map(this::toDefectiveReturnDomain);
    }

    public List<DefectiveReturn> listDefectiveReturns(String tenantId, String warehouseId) {
        return mapper.selectDefectiveReturns(tenantId, warehouseId).stream()
                .map(this::toDefectiveReturnDomain)
                .collect(Collectors.toList());
    }

    public int pendingDefectiveReturnQuantity(String tenantId, String warehouseId, String sellerSku) {
        return mapper.selectPendingDefectiveReturnsBySku(tenantId, warehouseId, sellerSku).stream()
                .mapToInt(DefectiveReturnDO::getQuantity)
                .sum();
    }

    public ProductRepair saveProductRepair(ProductRepair productRepair) {
        ProductRepairDO existing = mapper.selectProductRepair(productRepair.tenantId(), productRepair.repairId());
        ProductRepairDO data = toProductRepairData(productRepair);
        if (existing == null) {
            mapper.insertProductRepair(data);
        } else {
            mapper.updateProductRepair(data);
        }
        return productRepair;
    }

    public Optional<ProductRepair> findProductRepair(String tenantId, String repairId) {
        return Optional.ofNullable(mapper.selectProductRepair(tenantId, repairId)).map(this::toProductRepairDomain);
    }

    public List<ProductRepair> listProductRepairs(String tenantId, String warehouseId) {
        return mapper.selectProductRepairs(tenantId, warehouseId).stream()
                .map(this::toProductRepairDomain)
                .collect(Collectors.toList());
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

    private OutboundPackageDO toOutboundPackageData(OutboundPackage p) {
        OutboundPackageDO data = new OutboundPackageDO();
        data.setPackageId(p.packageId());
        data.setTenantId(p.tenantId());
        data.setOrderId(p.orderId());
        data.setWarehouseId(p.warehouseId());
        data.setCarrierCode(p.carrierCode());
        data.setTrackingNo(p.trackingNo());
        data.setWeightKg(p.weightKg());
        data.setStatus(p.status().name());
        data.setRemark(p.remark());
        data.setPackedAt(p.packedAt());
        data.setShippedAt(p.shippedAt());
        data.setCreatedAt(p.createdAt() != null ? p.createdAt() : Instant.now());
        data.setUpdatedAt(p.updatedAt() != null ? p.updatedAt() : Instant.now());
        return data;
    }

    private OutboundPackage toOutboundPackageDomain(OutboundPackageDO d) {
        return new OutboundPackage(
                d.getPackageId(),
                d.getTenantId(),
                d.getOrderId(),
                d.getWarehouseId(),
                d.getCarrierCode(),
                d.getTrackingNo(),
                d.getWeightKg(),
                OutboundPackageStatus.valueOf(d.getStatus()),
                d.getRemark(),
                d.getPackedAt(),
                d.getShippedAt(),
                d.getCreatedAt(),
                d.getUpdatedAt());
    }

    private OutboundPackageLineDO toOutboundPackageLineData(OutboundPackageLine l) {
        OutboundPackageLineDO data = new OutboundPackageLineDO();
        data.setPackageLineId(l.packageLineId());
        data.setPackageId(l.packageId());
        data.setOrderLineId(l.orderLineId());
        data.setSellerSku(l.sellerSku());
        data.setQuantity(l.quantity());
        data.setBatchNo(l.batchNo());
        return data;
    }

    private OutboundPackageLine toOutboundPackageLineDomain(OutboundPackageLineDO d) {
        return new OutboundPackageLine(
                d.getPackageLineId(),
                d.getPackageId(),
                d.getOrderLineId(),
                d.getSellerSku(),
                d.getQuantity(),
                d.getBatchNo());
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

    private DefectiveReturnDO toDefectiveReturnData(DefectiveReturn defectiveReturn) {
        DefectiveReturnDO data = new DefectiveReturnDO();
        data.setReturnId(defectiveReturn.returnId());
        data.setTenantId(defectiveReturn.tenantId());
        data.setWarehouseId(defectiveReturn.warehouseId());
        data.setPoId(defectiveReturn.poId());
        data.setSupplierId(defectiveReturn.supplierId());
        data.setSellerSku(defectiveReturn.sellerSku());
        data.setQuantity(defectiveReturn.quantity());
        data.setReason(defectiveReturn.reason());
        data.setSupplierReply(defectiveReturn.supplierReply() != null ? defectiveReturn.supplierReply().name() : null);
        data.setStatus(defectiveReturn.status().name());
        data.setProcessedBy(defectiveReturn.processedBy());
        data.setRemark(defectiveReturn.remark());
        data.setProcessedAt(defectiveReturn.processedAt());
        data.setCreatedAt(defectiveReturn.createdAt() != null ? defectiveReturn.createdAt() : Instant.now());
        data.setUpdatedAt(defectiveReturn.updatedAt() != null ? defectiveReturn.updatedAt() : Instant.now());
        return data;
    }

    private DefectiveReturn toDefectiveReturnDomain(DefectiveReturnDO data) {
        return new DefectiveReturn(
                data.getReturnId(),
                data.getTenantId(),
                data.getWarehouseId(),
                data.getPoId(),
                data.getSupplierId(),
                data.getSellerSku(),
                data.getQuantity(),
                data.getReason(),
                data.getSupplierReply() != null ? DefectiveSupplierReply.valueOf(data.getSupplierReply()) : null,
                DefectiveReturnStatus.valueOf(data.getStatus()),
                data.getProcessedBy(),
                data.getRemark(),
                data.getProcessedAt(),
                data.getCreatedAt(),
                data.getUpdatedAt());
    }

    private ProductRepairDO toProductRepairData(ProductRepair productRepair) {
        ProductRepairDO data = new ProductRepairDO();
        data.setRepairId(productRepair.repairId());
        data.setTenantId(productRepair.tenantId());
        data.setWarehouseId(productRepair.warehouseId());
        data.setSupplierId(productRepair.supplierId());
        data.setSellerSku(productRepair.sellerSku());
        data.setOutboundQuantity(productRepair.outboundQuantity());
        data.setInboundQuantity(productRepair.inboundQuantity());
        data.setReason(productRepair.reason());
        data.setStatus(productRepair.status().name());
        data.setQcResult(productRepair.qcResult() != null ? productRepair.qcResult().name() : null);
        data.setProcessedBy(productRepair.processedBy());
        data.setRemark(productRepair.remark());
        data.setCompletedAt(productRepair.completedAt());
        data.setCreatedAt(productRepair.createdAt() != null ? productRepair.createdAt() : Instant.now());
        data.setUpdatedAt(productRepair.updatedAt() != null ? productRepair.updatedAt() : Instant.now());
        return data;
    }

    private ProductRepair toProductRepairDomain(ProductRepairDO data) {
        return new ProductRepair(
                data.getRepairId(),
                data.getTenantId(),
                data.getWarehouseId(),
                data.getSupplierId(),
                data.getSellerSku(),
                data.getOutboundQuantity(),
                data.getInboundQuantity(),
                data.getReason(),
                ProductRepairStatus.valueOf(data.getStatus()),
                data.getQcResult() != null ? QualityCheckResult.valueOf(data.getQcResult()) : null,
                data.getProcessedBy(),
                data.getRemark(),
                data.getCompletedAt(),
                data.getCreatedAt(),
                data.getUpdatedAt());
    }
}
