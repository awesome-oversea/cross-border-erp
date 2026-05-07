package com.aidotnet.erp.wms.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.wms.domain.DefectiveReturn;
import com.aidotnet.erp.wms.domain.DefectiveReturnStatus;
import com.aidotnet.erp.wms.domain.DefectiveSupplierReply;
import com.aidotnet.erp.wms.domain.InboundOrder;
import com.aidotnet.erp.wms.domain.InboundOrderLine;
import com.aidotnet.erp.wms.domain.InboundOrderStatus;
import com.aidotnet.erp.wms.domain.InventoryMovement;
import com.aidotnet.erp.wms.domain.InventoryTransactionType;
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
import com.aidotnet.erp.wms.infrastructure.InventoryStore;
import com.aidotnet.erp.wms.infrastructure.WmsOrderStore;
import java.math.BigDecimal;
import java.util.HashMap;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Warehouse operations service.
 * Receipt is first booked into on-hand inventory and then frozen for inspection.
 * After quality check, passed quantity is released to available stock and failed quantity remains frozen.
 */
@Service
public class WarehouseOperationService {

    private final WmsOrderStore orderStore;
    private final InventoryStore inventoryStore;

    public WarehouseOperationService(WmsOrderStore orderStore, InventoryStore inventoryStore) {
        this.orderStore = orderStore;
        this.inventoryStore = inventoryStore;
    }

    @Transactional
    public InboundOrder createInboundOrder(String tenantId, CreateInboundOrderCommand command) {
        ensureWarehouse(tenantId, command.warehouseId());
        Instant now = Instant.now();
        InboundOrder order = new InboundOrder(
                UUID.randomUUID().toString(),
                tenantId,
                command.warehouseId(),
                command.referenceType(),
                command.referenceId(),
                InboundOrderStatus.PENDING,
                command.remark(),
                now,
                now);
        orderStore.saveInboundOrder(order);
        for (InboundOrderLineCommand lineCmd : command.lines()) {
            InboundOrderLine line = new InboundOrderLine(
                    UUID.randomUUID().toString(),
                    order.orderId(),
                    lineCmd.sellerSku(),
                    lineCmd.locationId(),
                    lineCmd.expectedQuantity(),
                    0,
                    lineCmd.unitCost(),
                    lineCmd.batchNo());
            orderStore.saveInboundOrderLine(line);
        }
        return order;
    }

    @Transactional
    public InboundOrder receiveInboundOrder(String tenantId, String orderId, ReceiveInboundCommand command) {
        InboundOrder order = getInboundOrder(tenantId, orderId);
        if (order.status() != InboundOrderStatus.PENDING && order.status() != InboundOrderStatus.RECEIVING) {
            throw new BizException("INBOUND_STATUS_INVALID", "inbound order status does not allow receipt");
        }
        List<InboundOrderLine> lines = orderStore.listInboundOrderLines(orderId);
        for (ReceiveLineCommand receipt : command.receipts()) {
            InboundOrderLine line = lines.stream()
                    .filter(candidate -> candidate.lineId().equals(receipt.lineId()))
                    .findFirst()
                    .orElseThrow(() -> new BizException("INBOUND_LINE_NOT_FOUND", "inbound order line not found"));
            int totalReceived = line.receivedQuantity() + receipt.receivedQuantity();
            if (totalReceived > line.expectedQuantity()) {
                throw new BizException("RECEIVE_EXCEEDS_EXPECTED", "received quantity exceeds expected quantity");
            }
            orderStore.updateInboundOrderLine(new InboundOrderLine(
                    line.lineId(),
                    line.orderId(),
                    line.sellerSku(),
                    line.locationId(),
                    line.expectedQuantity(),
                    totalReceived,
                    line.unitCost(),
                    line.batchNo()));
            // Receipt first increases on-hand stock, then freezes it for inbound inspection.
            inventoryStore.addInventory(
                    tenantId,
                    line.sellerSku(),
                    order.warehouseId(),
                    BigDecimal.valueOf(receipt.receivedQuantity()),
                    "INBOUND_RECEIVE",
                    orderId,
                    "Receive inbound stock into on-hand inventory",
                    InventoryTransactionType.RECEIVE);
            inventoryStore.freezeInventory(
                    tenantId,
                    line.sellerSku(),
                    order.warehouseId(),
                    receipt.receivedQuantity(),
                    "INBOUND_INSPECTION_HOLD",
                    orderId,
                    "Hold received stock for inbound quality inspection");
            orderStore.saveMovement(new InventoryMovement(
                    UUID.randomUUID().toString(),
                    tenantId,
                    order.warehouseId(),
                    line.sellerSku(),
                    null,
                    line.locationId(),
                    receipt.receivedQuantity(),
                    MovementType.RECEIVING,
                    "INBOUND_ORDER",
                    orderId,
                    Instant.now()));
        }
        List<InboundOrderLine> updatedLines = orderStore.listInboundOrderLines(orderId);
        boolean allReceived = updatedLines.stream().allMatch(line -> line.receivedQuantity() >= line.expectedQuantity());
        InboundOrderStatus newStatus = allReceived ? InboundOrderStatus.COMPLETED : InboundOrderStatus.RECEIVING;
        return orderStore.saveInboundOrder(new InboundOrder(
                order.orderId(),
                order.tenantId(),
                order.warehouseId(),
                order.referenceType(),
                order.referenceId(),
                newStatus,
                order.remark(),
                order.createdAt(),
                Instant.now()));
    }

    public List<InboundOrder> listInboundOrders(String tenantId, String warehouseId) {
        return orderStore.listInboundOrders(tenantId, warehouseId);
    }

    public InboundOrder getInboundOrder(String tenantId, String orderId) {
        return orderStore.findInboundOrder(tenantId, orderId)
                .orElseThrow(() -> new BizException("INBOUND_ORDER_NOT_FOUND", "inbound order not found"));
    }

    public List<InboundOrderLine> listInboundOrderLines(String orderId) {
        return orderStore.listInboundOrderLines(orderId);
    }

    @Transactional
    public OutboundOrder createOutboundOrder(String tenantId, CreateOutboundOrderCommand command) {
        ensureWarehouse(tenantId, command.warehouseId());
        Instant now = Instant.now();
        OutboundOrder order = new OutboundOrder(
                UUID.randomUUID().toString(),
                tenantId,
                command.warehouseId(),
                command.referenceType(),
                command.referenceId(),
                OutboundOrderStatus.PENDING,
                command.remark(),
                now,
                now);
        orderStore.saveOutboundOrder(order);
        for (OutboundOrderLineCommand lineCmd : command.lines()) {
            OutboundOrderLine line = new OutboundOrderLine(
                    UUID.randomUUID().toString(),
                    order.orderId(),
                    lineCmd.sellerSku(),
                    lineCmd.locationId(),
                    lineCmd.requiredQuantity(),
                    0,
                    lineCmd.batchNo());
            orderStore.saveOutboundOrderLine(line);
        }
        return order;
    }

    @Transactional
    public OutboundOrder pickOutboundOrder(String tenantId, String orderId, PickOutboundCommand command) {
        OutboundOrder order = getOutboundOrder(tenantId, orderId);
        if (order.status() != OutboundOrderStatus.PENDING && order.status() != OutboundOrderStatus.PICKING) {
            throw new BizException("OUTBOUND_STATUS_INVALID", "outbound order status does not allow picking");
        }
        List<OutboundOrderLine> lines = orderStore.listOutboundOrderLines(orderId);
        for (PickLineCommand pick : command.picks()) {
            OutboundOrderLine line = lines.stream()
                    .filter(candidate -> candidate.lineId().equals(pick.lineId()))
                    .findFirst()
                    .orElseThrow(() -> new BizException("OUTBOUND_LINE_NOT_FOUND", "outbound order line not found"));
            int totalPicked = line.pickedQuantity() + pick.pickedQuantity();
            if (totalPicked > line.requiredQuantity()) {
                throw new BizException("PICK_EXCEEDS_REQUIRED", "picked quantity exceeds required quantity");
            }
            orderStore.updateOutboundOrderLine(new OutboundOrderLine(
                    line.lineId(),
                    line.orderId(),
                    line.sellerSku(),
                    line.locationId(),
                    line.requiredQuantity(),
                    totalPicked,
                    line.batchNo()));
            orderStore.saveMovement(new InventoryMovement(
                    UUID.randomUUID().toString(),
                    tenantId,
                    order.warehouseId(),
                    line.sellerSku(),
                    line.locationId(),
                    null,
                    pick.pickedQuantity(),
                    MovementType.PICKING,
                    "OUTBOUND_ORDER",
                    orderId,
                    Instant.now()));
        }
        List<OutboundOrderLine> updatedLines = orderStore.listOutboundOrderLines(orderId);
        ensurePickedQuantitiesWithinReservedStock(tenantId, order.warehouseId(), updatedLines);
        // 拣货完成后仅进入待打包/待发货阶段，真正发货扣减发生在包裹出运时。
        OutboundOrderStatus newStatus = OutboundOrderStatus.PICKING;
        return orderStore.saveOutboundOrder(new OutboundOrder(
                order.orderId(),
                order.tenantId(),
                order.warehouseId(),
                order.referenceType(),
                order.referenceId(),
                newStatus,
                order.remark(),
                order.createdAt(),
                Instant.now()));
    }

    public List<OutboundOrder> listOutboundOrders(String tenantId, String warehouseId) {
        return orderStore.listOutboundOrders(tenantId, warehouseId);
    }

    public OutboundOrder getOutboundOrder(String tenantId, String orderId) {
        return orderStore.findOutboundOrder(tenantId, orderId)
                .orElseThrow(() -> new BizException("OUTBOUND_ORDER_NOT_FOUND", "outbound order not found"));
    }

    public List<OutboundOrderLine> listOutboundOrderLines(String orderId) {
        return orderStore.listOutboundOrderLines(orderId);
    }

    /**
     * 创建出库包裹
     *
     * 业务语义:
     * 1. 包裹只能装入已拣货数量
     * 2. 同一出库行允许拆分多个包裹，但累计装箱数量不能超过已拣货数量
     * 3. 打包不扣减库存，库存扣减在发货确认时执行
     */
    @Transactional
    public OutboundPackage createOutboundPackage(String tenantId, String orderId, CreateOutboundPackageCommand command) {
        OutboundOrder order = getOutboundOrder(tenantId, orderId);
        if (order.status() == OutboundOrderStatus.PENDING || order.status() == OutboundOrderStatus.CANCELLED) {
            throw new BizException("OUTBOUND_PACKAGE_STATUS_INVALID", "outbound order status does not allow packing");
        }
        if (command.lines() == null || command.lines().isEmpty()) {
            throw new BizException("OUTBOUND_PACKAGE_LINES_REQUIRED", "package lines are required");
        }
        List<OutboundOrderLine> outboundLines = orderStore.listOutboundOrderLines(orderId);
        Map<String, Integer> packagedQuantities = calculatePackagedQuantities(tenantId, orderId, false);
        for (OutboundPackageLineCommand lineCommand : command.lines()) {
            OutboundOrderLine orderLine = outboundLines.stream()
                    .filter(candidate -> candidate.lineId().equals(lineCommand.lineId()))
                    .findFirst()
                    .orElseThrow(() -> new BizException("OUTBOUND_LINE_NOT_FOUND", "outbound order line not found"));
            if (orderLine.pickedQuantity() <= 0) {
                throw new BizException("OUTBOUND_LINE_NOT_PICKED", "outbound order line has not been picked");
            }
            int totalPacked = packagedQuantities.getOrDefault(orderLine.lineId(), 0) + lineCommand.quantity();
            if (totalPacked > orderLine.pickedQuantity()) {
                throw new BizException("PACKAGE_EXCEEDS_PICKED", "package quantity exceeds picked quantity");
            }
        }

        Instant now = Instant.now();
        OutboundPackage outboundPackage = orderStore.saveOutboundPackage(new OutboundPackage(
                UUID.randomUUID().toString(),
                tenantId,
                orderId,
                order.warehouseId(),
                null,
                null,
                null,
                OutboundPackageStatus.PACKED,
                command.remark(),
                now,
                null,
                now,
                now));
        for (OutboundPackageLineCommand lineCommand : command.lines()) {
            OutboundOrderLine orderLine = outboundLines.stream()
                    .filter(candidate -> candidate.lineId().equals(lineCommand.lineId()))
                    .findFirst()
                    .orElseThrow(() -> new BizException("OUTBOUND_LINE_NOT_FOUND", "outbound order line not found"));
            orderStore.saveOutboundPackageLine(new OutboundPackageLine(
                    UUID.randomUUID().toString(),
                    outboundPackage.packageId(),
                    orderLine.lineId(),
                    orderLine.sellerSku(),
                    lineCommand.quantity(),
                    orderLine.batchNo()));
        }
        return outboundPackage;
    }

    public List<OutboundPackage> listOutboundPackages(String tenantId, String orderId) {
        getOutboundOrder(tenantId, orderId);
        return orderStore.listOutboundPackages(tenantId, orderId);
    }

    @Transactional
    public OutboundPackage weighOutboundPackage(String tenantId, String packageId, WeighOutboundPackageCommand command) {
        OutboundPackage outboundPackage = getOutboundPackage(tenantId, packageId);
        if (outboundPackage.status() == OutboundPackageStatus.SHIPPED) {
            throw new BizException("OUTBOUND_PACKAGE_STATUS_INVALID", "shipped package cannot be weighed again");
        }
        return orderStore.saveOutboundPackage(new OutboundPackage(
                outboundPackage.packageId(),
                outboundPackage.tenantId(),
                outboundPackage.orderId(),
                outboundPackage.warehouseId(),
                outboundPackage.carrierCode(),
                outboundPackage.trackingNo(),
                command.weightKg(),
                OutboundPackageStatus.WEIGHED,
                outboundPackage.remark(),
                outboundPackage.packedAt(),
                outboundPackage.shippedAt(),
                outboundPackage.createdAt(),
                Instant.now()));
    }

    /**
     * 包裹发货确认
     *
     * 业务语义:
     * 1. 仅允许已称重包裹执行发货
     * 2. 发货时从预占库存中扣减在手库存
     * 3. 当出库单全部应发数量完成发运后，出库单状态推进为 SHIPPED
     */
    @Transactional
    public OutboundPackage shipOutboundPackage(String tenantId, String packageId, ShipOutboundPackageCommand command) {
        OutboundPackage outboundPackage = getOutboundPackage(tenantId, packageId);
        if (outboundPackage.status() != OutboundPackageStatus.WEIGHED) {
            throw new BizException("OUTBOUND_PACKAGE_STATUS_INVALID", "package must be weighed before shipping");
        }
        if (outboundPackage.weightKg() == null) {
            throw new BizException("OUTBOUND_PACKAGE_WEIGHT_REQUIRED", "package weight is required before shipping");
        }
        List<OutboundPackageLine> packageLines = orderStore.listOutboundPackageLines(packageId);
        if (packageLines.isEmpty()) {
            throw new BizException("OUTBOUND_PACKAGE_LINES_REQUIRED", "package lines are required before shipping");
        }

        Instant now = Instant.now();
        for (OutboundPackageLine packageLine : packageLines) {
            inventoryStore.deductReservedInventory(
                    tenantId,
                    packageLine.sellerSku(),
                    outboundPackage.warehouseId(),
                    packageLine.quantity(),
                    "OUTBOUND_SHIP",
                    packageId,
                    "出库包裹发货扣减");
        }

        OutboundPackage shippedPackage = orderStore.saveOutboundPackage(new OutboundPackage(
                outboundPackage.packageId(),
                outboundPackage.tenantId(),
                outboundPackage.orderId(),
                outboundPackage.warehouseId(),
                command.carrierCode(),
                command.trackingNo(),
                outboundPackage.weightKg(),
                OutboundPackageStatus.SHIPPED,
                outboundPackage.remark(),
                outboundPackage.packedAt(),
                now,
                outboundPackage.createdAt(),
                now));

        List<OutboundOrderLine> outboundLines = orderStore.listOutboundOrderLines(outboundPackage.orderId());
        if (isAllOutboundQuantityShipped(tenantId, outboundPackage.orderId(), outboundLines)) {
            OutboundOrder order = getOutboundOrder(tenantId, outboundPackage.orderId());
            orderStore.saveOutboundOrder(new OutboundOrder(
                    order.orderId(),
                    order.tenantId(),
                    order.warehouseId(),
                    order.referenceType(),
                    order.referenceId(),
                    OutboundOrderStatus.SHIPPED,
                    order.remark(),
                    order.createdAt(),
                    now));
        }
        return shippedPackage;
    }

    public OutboundPackage getOutboundPackage(String tenantId, String packageId) {
        return orderStore.findOutboundPackage(tenantId, packageId)
                .orElseThrow(() -> new BizException("OUTBOUND_PACKAGE_NOT_FOUND", "outbound package not found"));
    }

    public InventoryMovement moveInventory(String tenantId, MoveInventoryCommand command) {
        ensureWarehouse(tenantId, command.warehouseId());
        return orderStore.saveMovement(new InventoryMovement(
                UUID.randomUUID().toString(),
                tenantId,
                command.warehouseId(),
                command.sellerSku(),
                command.fromLocationId(),
                command.toLocationId(),
                command.quantity(),
                MovementType.TRANSFER,
                null,
                null,
                Instant.now()));
    }

    public List<InventoryMovement> listMovements(String tenantId, String warehouseId) {
        return orderStore.listMovements(tenantId, warehouseId);
    }

    public QualityCheck performQualityCheck(String tenantId, PerformQualityCheckCommand command) {
        ensureWarehouse(tenantId, command.warehouseId());
        if (command.sampleQuantity() != command.passQuantity() + command.failQuantity()) {
            throw new BizException("QUALITY_SAMPLE_MISMATCH", "sample quantity must equal pass quantity plus fail quantity");
        }

        String locationId = null;
        if (command.inboundOrderId() != null && !command.inboundOrderId().isBlank()) {
            getInboundOrder(tenantId, command.inboundOrderId());
            List<InboundOrderLine> inboundLines = orderStore.listInboundOrderLines(command.inboundOrderId()).stream()
                    .filter(line -> line.sellerSku().equals(command.sellerSku()))
                    .toList();
            if (inboundLines.isEmpty()) {
                throw new BizException("INBOUND_SKU_NOT_FOUND", "seller sku not found in inbound order");
            }
            int totalReceived = inboundLines.stream().mapToInt(InboundOrderLine::receivedQuantity).sum();
            int checkedQuantity = orderStore.listQualityChecksByInboundOrder(tenantId, command.inboundOrderId()).stream()
                    .filter(check -> check.sellerSku().equals(command.sellerSku()))
                    .mapToInt(QualityCheck::sampleQuantity)
                    .sum();
            if (checkedQuantity + command.sampleQuantity() > totalReceived) {
                throw new BizException("QUALITY_CHECK_EXCEEDS_RECEIVED", "quality check quantity exceeds received quantity");
            }
            locationId = inboundLines.stream()
                    .map(InboundOrderLine::locationId)
                    .filter(value -> value != null && !value.isBlank())
                    .findFirst()
                    .orElse(null);
        }

        QualityCheckResult result;
        if (command.failQuantity() == 0) {
            result = QualityCheckResult.PASS;
        } else if (command.passQuantity() == 0) {
            result = QualityCheckResult.FAIL;
        } else {
            result = QualityCheckResult.PARTIAL_PASS;
        }

        Instant now = Instant.now();
        QualityCheck check = orderStore.saveQualityCheck(new QualityCheck(
                UUID.randomUUID().toString(),
                tenantId,
                command.warehouseId(),
                command.inboundOrderId(),
                command.sellerSku(),
                command.sampleQuantity(),
                command.passQuantity(),
                command.failQuantity(),
                result,
                command.inspector(),
                command.remark(),
                now,
                now));

        if (command.inboundOrderId() != null && !command.inboundOrderId().isBlank()) {
            if (command.passQuantity() > 0) {
                inventoryStore.unfreezeInventory(
                        tenantId,
                        command.sellerSku(),
                        command.warehouseId(),
                        command.passQuantity(),
                        "QUALITY_CHECK_RELEASE",
                        check.checkId(),
                        "Release passed quantity to available stock");
                orderStore.saveMovement(new InventoryMovement(
                        UUID.randomUUID().toString(),
                        tenantId,
                        command.warehouseId(),
                        command.sellerSku(),
                        locationId,
                        locationId,
                        command.passQuantity(),
                        MovementType.ADJUSTMENT,
                        "QUALITY_CHECK_RELEASE",
                        check.checkId(),
                        Instant.now()));
            }
            if (command.failQuantity() > 0) {
                orderStore.saveMovement(new InventoryMovement(
                        UUID.randomUUID().toString(),
                        tenantId,
                        command.warehouseId(),
                        command.sellerSku(),
                        locationId,
                        locationId,
                        command.failQuantity(),
                        MovementType.ADJUSTMENT,
                        "QUALITY_CHECK_DEFECTIVE",
                        check.checkId(),
                        Instant.now()));
            }
        }
        return check;
    }

    public List<QualityCheck> listQualityChecks(String tenantId, String warehouseId) {
        return orderStore.listQualityChecks(tenantId, warehouseId);
    }

    public List<QualityCheck> listQualityChecksByInboundOrder(String tenantId, String inboundOrderId) {
        return orderStore.listQualityChecksByInboundOrder(tenantId, inboundOrderId);
    }

    @Transactional
    public DefectiveReturn createDefectiveReturn(String tenantId, CreateDefectiveReturnCommand command) {
        ensureWarehouse(tenantId, command.warehouseId());
        ensureDefectiveInventoryAvailable(tenantId, command.warehouseId(), command.sellerSku(), command.quantity());
        Instant now = Instant.now();
        return orderStore.saveDefectiveReturn(new DefectiveReturn(
                UUID.randomUUID().toString(),
                tenantId,
                command.warehouseId(),
                command.poId(),
                command.supplierId(),
                command.sellerSku(),
                command.quantity(),
                command.reason(),
                null,
                DefectiveReturnStatus.PENDING,
                null,
                command.remark(),
                null,
                now,
                now));
    }

    public DefectiveReturn getDefectiveReturn(String tenantId, String returnId) {
        return orderStore.findDefectiveReturn(tenantId, returnId)
                .orElseThrow(() -> new BizException("DEFECTIVE_RETURN_NOT_FOUND", "defective return not found"));
    }

    public List<DefectiveReturn> listDefectiveReturns(String tenantId, String warehouseId) {
        ensureWarehouse(tenantId, warehouseId);
        return orderStore.listDefectiveReturns(tenantId, warehouseId);
    }

    @Transactional
    public DefectiveReturn processDefectiveReturn(String tenantId, String returnId, ProcessDefectiveReturnCommand command) {
        DefectiveReturn defectiveReturn = getDefectiveReturn(tenantId, returnId);
        if (defectiveReturn.status() != DefectiveReturnStatus.PENDING) {
            throw new BizException("DEFECTIVE_RETURN_STATUS_INVALID", "defective return status does not allow processing");
        }

        Instant now = Instant.now();
        switch (command.supplierReply()) {
            case INBOUND -> {
                inventoryStore.unfreezeInventory(
                        tenantId,
                        defectiveReturn.sellerSku(),
                        defectiveReturn.warehouseId(),
                        defectiveReturn.quantity(),
                        "DEFECTIVE_RETURN_RELEASE",
                        defectiveReturn.returnId(),
                        "Release defective stock back to available inventory");
                orderStore.saveMovement(new InventoryMovement(
                        UUID.randomUUID().toString(),
                        tenantId,
                        defectiveReturn.warehouseId(),
                        defectiveReturn.sellerSku(),
                        null,
                        null,
                        defectiveReturn.quantity(),
                        MovementType.ADJUSTMENT,
                        "DEFECTIVE_RETURN_RELEASE",
                        defectiveReturn.returnId(),
                        now));
            }
            case VOID -> {
                inventoryStore.consumeFrozenInventory(
                        tenantId,
                        defectiveReturn.sellerSku(),
                        defectiveReturn.warehouseId(),
                        defectiveReturn.quantity(),
                        "DEFECTIVE_RETURN_VOID",
                        defectiveReturn.returnId(),
                        "Write off defective stock");
                orderStore.saveMovement(new InventoryMovement(
                        UUID.randomUUID().toString(),
                        tenantId,
                        defectiveReturn.warehouseId(),
                        defectiveReturn.sellerSku(),
                        null,
                        null,
                        defectiveReturn.quantity(),
                        MovementType.ADJUSTMENT,
                        "DEFECTIVE_RETURN_VOID",
                        defectiveReturn.returnId(),
                        now));
            }
            case RETURN -> {
                inventoryStore.consumeFrozenInventory(
                        tenantId,
                        defectiveReturn.sellerSku(),
                        defectiveReturn.warehouseId(),
                        defectiveReturn.quantity(),
                        "DEFECTIVE_RETURN_OUTBOUND",
                        defectiveReturn.returnId(),
                        "Return defective stock to supplier");
                orderStore.saveMovement(new InventoryMovement(
                        UUID.randomUUID().toString(),
                        tenantId,
                        defectiveReturn.warehouseId(),
                        defectiveReturn.sellerSku(),
                        null,
                        null,
                        defectiveReturn.quantity(),
                        MovementType.RETURN,
                        "DEFECTIVE_RETURN_OUTBOUND",
                        defectiveReturn.returnId(),
                        now));
            }
        }

        return orderStore.saveDefectiveReturn(new DefectiveReturn(
                defectiveReturn.returnId(),
                defectiveReturn.tenantId(),
                defectiveReturn.warehouseId(),
                defectiveReturn.poId(),
                defectiveReturn.supplierId(),
                defectiveReturn.sellerSku(),
                defectiveReturn.quantity(),
                defectiveReturn.reason(),
                command.supplierReply(),
                DefectiveReturnStatus.COMPLETED,
                command.processedBy(),
                command.remark(),
                now,
                defectiveReturn.createdAt(),
                now));
    }

    @Transactional
    public ProductRepair createProductRepair(String tenantId, CreateProductRepairCommand command) {
        ensureWarehouse(tenantId, command.warehouseId());
        ensureRepairableDefectiveInventoryAvailable(tenantId, command.warehouseId(), command.sellerSku(), command.quantity());
        Instant now = Instant.now();
        String repairId = UUID.randomUUID().toString();
        inventoryStore.consumeFrozenInventory(
                tenantId,
                command.sellerSku(),
                command.warehouseId(),
                command.quantity(),
                "PRODUCT_REPAIR_OUTBOUND",
                repairId,
                "Send defective stock out for repair");
        orderStore.saveMovement(new InventoryMovement(
                UUID.randomUUID().toString(),
                tenantId,
                command.warehouseId(),
                command.sellerSku(),
                null,
                null,
                command.quantity(),
                MovementType.RETURN,
                "PRODUCT_REPAIR_OUTBOUND",
                repairId,
                now));
        return orderStore.saveProductRepair(new ProductRepair(
                repairId,
                tenantId,
                command.warehouseId(),
                command.supplierId(),
                command.sellerSku(),
                command.quantity(),
                0,
                command.reason(),
                ProductRepairStatus.IN_REPAIR,
                null,
                null,
                command.remark(),
                null,
                now,
                now));
    }

    public ProductRepair getProductRepair(String tenantId, String repairId) {
        return orderStore.findProductRepair(tenantId, repairId)
                .orElseThrow(() -> new BizException("PRODUCT_REPAIR_NOT_FOUND", "product repair not found"));
    }

    public List<ProductRepair> listProductRepairs(String tenantId, String warehouseId) {
        ensureWarehouse(tenantId, warehouseId);
        return orderStore.listProductRepairs(tenantId, warehouseId);
    }

    @Transactional
    public ProductRepair completeProductRepair(String tenantId, String repairId, CompleteProductRepairCommand command) {
        ProductRepair productRepair = getProductRepair(tenantId, repairId);
        if (productRepair.status() != ProductRepairStatus.IN_REPAIR) {
            throw new BizException("PRODUCT_REPAIR_STATUS_INVALID", "product repair status does not allow completion");
        }
        if (command.inboundQuantity() > productRepair.outboundQuantity()) {
            throw new BizException("PRODUCT_REPAIR_INBOUND_EXCEEDS_OUTBOUND", "inbound quantity exceeds outbound quantity");
        }

        Instant now = Instant.now();
        if (command.inboundQuantity() > 0) {
            inventoryStore.addInventory(
                    tenantId,
                    productRepair.sellerSku(),
                    productRepair.warehouseId(),
                    BigDecimal.valueOf(command.inboundQuantity()),
                    "PRODUCT_REPAIR_COMPLETE",
                    productRepair.repairId(),
                    "Receive repaired stock back into on-hand inventory",
                    InventoryTransactionType.RECEIVE);
            orderStore.saveMovement(new InventoryMovement(
                    UUID.randomUUID().toString(),
                    tenantId,
                    productRepair.warehouseId(),
                    productRepair.sellerSku(),
                    null,
                    null,
                    command.inboundQuantity(),
                    MovementType.RECEIVING,
                    "PRODUCT_REPAIR_COMPLETE",
                    productRepair.repairId(),
                    now));
        }

        return orderStore.saveProductRepair(new ProductRepair(
                productRepair.repairId(),
                productRepair.tenantId(),
                productRepair.warehouseId(),
                productRepair.supplierId(),
                productRepair.sellerSku(),
                productRepair.outboundQuantity(),
                command.inboundQuantity(),
                productRepair.reason(),
                ProductRepairStatus.COMPLETED,
                command.qcResult(),
                command.processedBy(),
                command.remark(),
                now,
                productRepair.createdAt(),
                now));
    }

    private void ensurePickedQuantitiesWithinReservedStock(String tenantId, String warehouseId, List<OutboundOrderLine> lines) {
        Map<String, Integer> pickedBySku = new HashMap<>();
        for (OutboundOrderLine line : lines) {
            pickedBySku.merge(line.sellerSku(), line.pickedQuantity(), Integer::sum);
        }
        for (Map.Entry<String, Integer> entry : pickedBySku.entrySet()) {
            if (entry.getValue() <= 0) {
                continue;
            }
            int reserved = inventoryStore.findBalance(tenantId, warehouseId, entry.getKey())
                    .orElseThrow(() -> new BizException("INVENTORY_NOT_FOUND", "inventory not found"))
                    .reserved();
            if (reserved < entry.getValue()) {
                throw new BizException("PICK_EXCEEDS_RESERVED", "picked quantity exceeds reserved inventory");
            }
        }
    }

    private Map<String, Integer> calculatePackagedQuantities(String tenantId, String orderId, boolean shippedOnly) {
        Map<String, Integer> packagedQuantities = new HashMap<>();
        for (OutboundPackage outboundPackage : orderStore.listOutboundPackages(tenantId, orderId)) {
            if (shippedOnly && outboundPackage.status() != OutboundPackageStatus.SHIPPED) {
                continue;
            }
            for (OutboundPackageLine packageLine : orderStore.listOutboundPackageLines(outboundPackage.packageId())) {
                packagedQuantities.merge(packageLine.orderLineId(), packageLine.quantity(), Integer::sum);
            }
        }
        return packagedQuantities;
    }

    private boolean isAllOutboundQuantityShipped(String tenantId, String orderId, List<OutboundOrderLine> lines) {
        Map<String, Integer> shippedQuantities = calculatePackagedQuantities(tenantId, orderId, true);
        return lines.stream().allMatch(line -> shippedQuantities.getOrDefault(line.lineId(), 0) >= line.requiredQuantity());
    }

    private void ensureDefectiveInventoryAvailable(String tenantId, String warehouseId, String sellerSku, int quantity) {
        int frozen = inventoryStore.findBalance(tenantId, warehouseId, sellerSku)
                .orElseThrow(() -> new BizException("INVENTORY_NOT_FOUND", "inventory not found"))
                .frozen();
        int pendingReturnQuantity = orderStore.pendingDefectiveReturnQuantity(tenantId, warehouseId, sellerSku);
        if (frozen - pendingReturnQuantity < quantity) {
            throw new BizException("DEFECTIVE_INVENTORY_NOT_ENOUGH", "defective inventory is insufficient for return");
        }
    }

    private void ensureRepairableDefectiveInventoryAvailable(String tenantId, String warehouseId, String sellerSku, int quantity) {
        int frozen = inventoryStore.findBalance(tenantId, warehouseId, sellerSku)
                .orElseThrow(() -> new BizException("INVENTORY_NOT_FOUND", "inventory not found"))
                .frozen();
        int pendingReturnQuantity = orderStore.pendingDefectiveReturnQuantity(tenantId, warehouseId, sellerSku);
        if (frozen - pendingReturnQuantity < quantity) {
            throw new BizException("DEFECTIVE_INVENTORY_NOT_ENOUGH", "defective inventory is insufficient for repair");
        }
    }

    private void ensureWarehouse(String tenantId, String warehouseId) {
        inventoryStore.findWarehouse(tenantId, warehouseId)
                .orElseThrow(() -> new BizException("WAREHOUSE_NOT_FOUND", "warehouse not found"));
    }

    public record CreateInboundOrderCommand(
            String warehouseId, String referenceType, String referenceId, String remark, List<InboundOrderLineCommand> lines) {}

    public record InboundOrderLineCommand(
            String sellerSku, String locationId, int expectedQuantity, BigDecimal unitCost, String batchNo) {}

    public record ReceiveInboundCommand(List<ReceiveLineCommand> receipts) {}

    public record ReceiveLineCommand(String lineId, int receivedQuantity) {}

    public record CreateOutboundOrderCommand(
            String warehouseId, String referenceType, String referenceId, String remark, List<OutboundOrderLineCommand> lines) {}

    public record OutboundOrderLineCommand(String sellerSku, String locationId, int requiredQuantity, String batchNo) {}

    public record PickOutboundCommand(List<PickLineCommand> picks) {}

    public record PickLineCommand(String lineId, int pickedQuantity) {}

    public record CreateOutboundPackageCommand(String remark, List<OutboundPackageLineCommand> lines) {}

    public record OutboundPackageLineCommand(String lineId, int quantity) {}

    public record WeighOutboundPackageCommand(BigDecimal weightKg) {}

    public record ShipOutboundPackageCommand(String carrierCode, String trackingNo) {}

    public record MoveInventoryCommand(String warehouseId, String sellerSku, String fromLocationId, String toLocationId, int quantity) {}

    public record PerformQualityCheckCommand(
            String warehouseId,
            String inboundOrderId,
            String sellerSku,
            int sampleQuantity,
            int passQuantity,
            int failQuantity,
            String inspector,
            String remark) {}

    public record CreateDefectiveReturnCommand(
            String warehouseId,
            String poId,
            String supplierId,
            String sellerSku,
            int quantity,
            String reason,
            String remark) {}

    public record ProcessDefectiveReturnCommand(
            DefectiveSupplierReply supplierReply,
            String processedBy,
            String remark) {}

    public record CreateProductRepairCommand(
            String warehouseId,
            String supplierId,
            String sellerSku,
            int quantity,
            String reason,
            String remark) {}

    public record CompleteProductRepairCommand(
            QualityCheckResult qcResult,
            int inboundQuantity,
            String processedBy,
            String remark) {}
}
