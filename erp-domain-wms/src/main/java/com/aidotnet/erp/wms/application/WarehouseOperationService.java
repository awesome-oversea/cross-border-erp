package com.aidotnet.erp.wms.application;

import com.aidotnet.erp.common.exception.BizException;
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
import com.aidotnet.erp.wms.infrastructure.InventoryStore;
import com.aidotnet.erp.wms.infrastructure.WmsOrderStore;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        InboundOrder order = new InboundOrder(UUID.randomUUID().toString(), tenantId, command.warehouseId(),
                command.referenceType(), command.referenceId(), InboundOrderStatus.PENDING, command.remark(), now, now);
        orderStore.saveInboundOrder(order);
        for (InboundOrderLineCommand lineCmd : command.lines()) {
            InboundOrderLine line = new InboundOrderLine(UUID.randomUUID().toString(), order.orderId(),
                    lineCmd.sellerSku(), lineCmd.locationId(), lineCmd.expectedQuantity(), 0,
                    lineCmd.unitCost(), lineCmd.batchNo());
            orderStore.saveInboundOrderLine(line);
        }
        return order;
    }

    @Transactional
    public InboundOrder receiveInboundOrder(String tenantId, String orderId, ReceiveInboundCommand command) {
        InboundOrder order = getInboundOrder(tenantId, orderId);
        if (order.status() != InboundOrderStatus.PENDING && order.status() != InboundOrderStatus.RECEIVING) {
            throw new BizException("INBOUND_STATUS_INVALID", "入库单状态不允许收货");
        }
        List<InboundOrderLine> lines = orderStore.listInboundOrderLines(orderId);
        for (ReceiveLineCommand receipt : command.receipts()) {
            InboundOrderLine line = lines.stream()
                    .filter(l -> l.lineId().equals(receipt.lineId()))
                    .findFirst()
                    .orElseThrow(() -> new BizException("INBOUND_LINE_NOT_FOUND", "入库明细不存在"));
            int totalReceived = line.receivedQuantity() + receipt.receivedQuantity();
            if (totalReceived > line.expectedQuantity()) {
                throw new BizException("RECEIVE_EXCEEDS_EXPECTED", "收货数量超过预期数量");
            }
            orderStore.updateInboundOrderLine(new InboundOrderLine(line.lineId(), line.orderId(), line.sellerSku(),
                    line.locationId(), line.expectedQuantity(), totalReceived, line.unitCost(), line.batchNo()));
            inventoryStore.saveBalance(inventoryStore.findBalance(tenantId, order.warehouseId(), line.sellerSku())
                    .map(existing -> new com.aidotnet.erp.wms.domain.InventoryBalance(tenantId, order.warehouseId(),
                            line.sellerSku(), existing.onHand() + receipt.receivedQuantity(), existing.reserved(),
                            existing.inTransit(), existing.frozen(), Instant.now()))
                    .orElse(new com.aidotnet.erp.wms.domain.InventoryBalance(tenantId, order.warehouseId(),
                            line.sellerSku(), receipt.receivedQuantity(), 0, 0, 0, Instant.now())));
            orderStore.saveMovement(new InventoryMovement(UUID.randomUUID().toString(), tenantId, order.warehouseId(),
                    line.sellerSku(), null, line.locationId(), receipt.receivedQuantity(), MovementType.RECEIVING,
                    "INBOUND_ORDER", orderId, Instant.now()));
        }
        List<InboundOrderLine> updatedLines = orderStore.listInboundOrderLines(orderId);
        boolean allReceived = updatedLines.stream().allMatch(l -> l.receivedQuantity() >= l.expectedQuantity());
        InboundOrderStatus newStatus = allReceived ? InboundOrderStatus.COMPLETED : InboundOrderStatus.RECEIVING;
        return orderStore.saveInboundOrder(new InboundOrder(order.orderId(), order.tenantId(), order.warehouseId(),
                order.referenceType(), order.referenceId(), newStatus, order.remark(), order.createdAt(), Instant.now()));
    }

    public List<InboundOrder> listInboundOrders(String tenantId, String warehouseId) {
        return orderStore.listInboundOrders(tenantId, warehouseId);
    }

    public InboundOrder getInboundOrder(String tenantId, String orderId) {
        return orderStore.findInboundOrder(tenantId, orderId)
                .orElseThrow(() -> new BizException("INBOUND_ORDER_NOT_FOUND", "入库单不存在"));
    }

    public List<InboundOrderLine> listInboundOrderLines(String orderId) {
        return orderStore.listInboundOrderLines(orderId);
    }

    @Transactional
    public OutboundOrder createOutboundOrder(String tenantId, CreateOutboundOrderCommand command) {
        ensureWarehouse(tenantId, command.warehouseId());
        Instant now = Instant.now();
        OutboundOrder order = new OutboundOrder(UUID.randomUUID().toString(), tenantId, command.warehouseId(),
                command.referenceType(), command.referenceId(), OutboundOrderStatus.PENDING, command.remark(), now, now);
        orderStore.saveOutboundOrder(order);
        for (OutboundOrderLineCommand lineCmd : command.lines()) {
            OutboundOrderLine line = new OutboundOrderLine(UUID.randomUUID().toString(), order.orderId(),
                    lineCmd.sellerSku(), lineCmd.locationId(), lineCmd.requiredQuantity(), 0, lineCmd.batchNo());
            orderStore.saveOutboundOrderLine(line);
        }
        return order;
    }

    @Transactional
    public OutboundOrder pickOutboundOrder(String tenantId, String orderId, PickOutboundCommand command) {
        OutboundOrder order = getOutboundOrder(tenantId, orderId);
        if (order.status() != OutboundOrderStatus.PENDING && order.status() != OutboundOrderStatus.PICKING) {
            throw new BizException("OUTBOUND_STATUS_INVALID", "出库单状态不允许拣货");
        }
        List<OutboundOrderLine> lines = orderStore.listOutboundOrderLines(orderId);
        for (PickLineCommand pick : command.picks()) {
            OutboundOrderLine line = lines.stream()
                    .filter(l -> l.lineId().equals(pick.lineId()))
                    .findFirst()
                    .orElseThrow(() -> new BizException("OUTBOUND_LINE_NOT_FOUND", "出库明细不存在"));
            int totalPicked = line.pickedQuantity() + pick.pickedQuantity();
            if (totalPicked > line.requiredQuantity()) {
                throw new BizException("PICK_EXCEEDS_REQUIRED", "拣货数量超过需求数量");
            }
            orderStore.updateOutboundOrderLine(new OutboundOrderLine(line.lineId(), line.orderId(), line.sellerSku(),
                    line.locationId(), line.requiredQuantity(), totalPicked, line.batchNo()));
            orderStore.saveMovement(new InventoryMovement(UUID.randomUUID().toString(), tenantId, order.warehouseId(),
                    line.sellerSku(), line.locationId(), null, pick.pickedQuantity(), MovementType.PICKING,
                    "OUTBOUND_ORDER", orderId, Instant.now()));
        }
        List<OutboundOrderLine> updatedLines = orderStore.listOutboundOrderLines(orderId);
        boolean allPicked = updatedLines.stream().allMatch(l -> l.pickedQuantity() >= l.requiredQuantity());
        OutboundOrderStatus newStatus = allPicked ? OutboundOrderStatus.SHIPPED : OutboundOrderStatus.PICKING;
        return orderStore.saveOutboundOrder(new OutboundOrder(order.orderId(), order.tenantId(), order.warehouseId(),
                order.referenceType(), order.referenceId(), newStatus, order.remark(), order.createdAt(), Instant.now()));
    }

    public List<OutboundOrder> listOutboundOrders(String tenantId, String warehouseId) {
        return orderStore.listOutboundOrders(tenantId, warehouseId);
    }

    public OutboundOrder getOutboundOrder(String tenantId, String orderId) {
        return orderStore.findOutboundOrder(tenantId, orderId)
                .orElseThrow(() -> new BizException("OUTBOUND_ORDER_NOT_FOUND", "出库单不存在"));
    }

    public List<OutboundOrderLine> listOutboundOrderLines(String orderId) {
        return orderStore.listOutboundOrderLines(orderId);
    }

    public InventoryMovement moveInventory(String tenantId, MoveInventoryCommand command) {
        ensureWarehouse(tenantId, command.warehouseId());
        return orderStore.saveMovement(new InventoryMovement(UUID.randomUUID().toString(), tenantId, command.warehouseId(),
                command.sellerSku(), command.fromLocationId(), command.toLocationId(), command.quantity(),
                MovementType.TRANSFER, null, null, Instant.now()));
    }

    public List<InventoryMovement> listMovements(String tenantId, String warehouseId) {
        return orderStore.listMovements(tenantId, warehouseId);
    }

    public QualityCheck performQualityCheck(String tenantId, PerformQualityCheckCommand command) {
        ensureWarehouse(tenantId, command.warehouseId());
        QualityCheckResult result;
        if (command.failQuantity() == 0) {
            result = QualityCheckResult.PASS;
        } else if (command.passQuantity() == 0) {
            result = QualityCheckResult.FAIL;
        } else {
            result = QualityCheckResult.PARTIAL_PASS;
        }
        Instant now = Instant.now();
        return orderStore.saveQualityCheck(new QualityCheck(UUID.randomUUID().toString(), tenantId, command.warehouseId(),
                command.inboundOrderId(), command.sellerSku(), command.sampleQuantity(), command.passQuantity(),
                command.failQuantity(), result, command.inspector(), command.remark(), now, now));
    }

    public List<QualityCheck> listQualityChecks(String tenantId, String warehouseId) {
        return orderStore.listQualityChecks(tenantId, warehouseId);
    }

    public List<QualityCheck> listQualityChecksByInboundOrder(String tenantId, String inboundOrderId) {
        return orderStore.listQualityChecksByInboundOrder(tenantId, inboundOrderId);
    }

    private void ensureWarehouse(String tenantId, String warehouseId) {
        inventoryStore.findWarehouse(tenantId, warehouseId)
                .orElseThrow(() -> new BizException("WAREHOUSE_NOT_FOUND", "仓库不存在"));
    }

    public record CreateInboundOrderCommand(String warehouseId, String referenceType, String referenceId,
                                            String remark, List<InboundOrderLineCommand> lines) {}

    public record InboundOrderLineCommand(String sellerSku, String locationId, int expectedQuantity,
                                          BigDecimal unitCost, String batchNo) {}

    public record ReceiveInboundCommand(List<ReceiveLineCommand> receipts) {}

    public record ReceiveLineCommand(String lineId, int receivedQuantity) {}

    public record CreateOutboundOrderCommand(String warehouseId, String referenceType, String referenceId,
                                             String remark, List<OutboundOrderLineCommand> lines) {}

    public record OutboundOrderLineCommand(String sellerSku, String locationId, int requiredQuantity, String batchNo) {}

    public record PickOutboundCommand(List<PickLineCommand> picks) {}

    public record PickLineCommand(String lineId, int pickedQuantity) {}

    public record MoveInventoryCommand(String warehouseId, String sellerSku, String fromLocationId,
                                       String toLocationId, int quantity) {}

    public record PerformQualityCheckCommand(String warehouseId, String inboundOrderId, String sellerSku,
                                             int sampleQuantity, int passQuantity, int failQuantity,
                                             String inspector, String remark) {}
}
