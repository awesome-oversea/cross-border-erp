package com.aidotnet.erp.wms.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.wms.domain.InventoryBalance;
import com.aidotnet.erp.wms.domain.InventoryMovement;
import com.aidotnet.erp.wms.domain.InventoryTransaction;
import com.aidotnet.erp.wms.domain.MovementType;
import com.aidotnet.erp.wms.domain.StockCheckOrder;
import com.aidotnet.erp.wms.domain.StockCheckOrder.CheckType;
import com.aidotnet.erp.wms.domain.StockCheckOrder.CheckOrderStatus;
import com.aidotnet.erp.wms.domain.StockCheckOrderLine;
import com.aidotnet.erp.wms.domain.StockCheckOrderLine.LineStatus;
import com.aidotnet.erp.wms.domain.TransferOrder;
import com.aidotnet.erp.wms.domain.TransferOrder.TransferStatus;
import com.aidotnet.erp.wms.domain.TransferOrderLine;
import com.aidotnet.erp.wms.infrastructure.InventoryStore;
import com.aidotnet.erp.wms.infrastructure.WmsExtStore;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferAndCheckService {

    private final WmsExtStore extStore;
    private final InventoryStore inventoryStore;

    public TransferAndCheckService(WmsExtStore extStore, InventoryStore inventoryStore) {
        this.extStore = extStore;
        this.inventoryStore = inventoryStore;
    }

    @Transactional
    public TransferOrder createTransferOrder(String tenantId, CreateTransferCommand command) {
        inventoryStore.findWarehouse(tenantId, command.fromWarehouseId())
                .orElseThrow(() -> new BizException("FROM_WAREHOUSE_NOT_FOUND", "调出仓库不存在"));
        inventoryStore.findWarehouse(tenantId, command.toWarehouseId())
                .orElseThrow(() -> new BizException("TO_WAREHOUSE_NOT_FOUND", "调入仓库不存在"));
        Instant now = Instant.now();
        TransferOrder order = new TransferOrder(UUID.randomUUID().toString(), tenantId, command.fromWarehouseId(),
                command.toWarehouseId(), TransferStatus.PENDING, command.remark(), now, now);
        extStore.saveTransferOrder(order);
        for (TransferLineCommand lineCmd : command.lines()) {
            InventoryBalance balance = inventoryStore.findBalance(tenantId, command.fromWarehouseId(), lineCmd.sellerSku())
                    .orElseThrow(() -> new BizException("INSUFFICIENT_INVENTORY", "调出仓库库存不足: " + lineCmd.sellerSku()));
            if (balance.getAvailable() < lineCmd.transferQuantity()) {
                throw new BizException("INSUFFICIENT_INVENTORY", "库存不足: " + lineCmd.sellerSku());
            }
            TransferOrderLine line = new TransferOrderLine(UUID.randomUUID().toString(), order.transferId(),
                    lineCmd.sellerSku(), lineCmd.transferQuantity(), 0, lineCmd.unitCost(), lineCmd.batchNo());
            extStore.saveTransferOrderLine(tenantId, line);
        }
        return order;
    }

    @Transactional
    public TransferOrder shipTransfer(String tenantId, String transferId) {
        TransferOrder order = getTransferOrder(tenantId, transferId);
        if (order.status() != TransferStatus.PENDING) {
            throw new BizException("TRANSFER_STATUS_INVALID", "调拨单状态不允许发货");
        }
        List<TransferOrderLine> lines = extStore.listTransferOrderLines(tenantId, transferId);
        for (TransferOrderLine line : lines) {
            inventoryStore.deductInventory(line.sellerSku(), order.fromWarehouseId(),
                    BigDecimal.valueOf(line.transferQuantity()), transferId,
                    com.aidotnet.erp.wms.domain.InventoryTransactionType.TRANSFER_OUT);
            inventoryStore.addInTransitInventory(tenantId, line.sellerSku(), order.toWarehouseId(), line.transferQuantity(),
                    "TRANSFER_IN_TRANSIT", transferId, "调拨发运在途");
            extStore.saveMovement(new InventoryMovement(UUID.randomUUID().toString(), tenantId, order.fromWarehouseId(),
                    line.sellerSku(), null, null, line.transferQuantity(), MovementType.TRANSFER,
                    "TRANSFER_ORDER", transferId, Instant.now()));
        }
        return extStore.saveTransferOrder(new TransferOrder(order.transferId(), order.tenantId(),
                order.fromWarehouseId(), order.toWarehouseId(), TransferStatus.IN_TRANSIT,
                order.remark(), order.createdAt(), Instant.now()));
    }

    @Transactional
    public TransferOrder receiveTransfer(String tenantId, String transferId, List<ReceiveTransferLineCommand> receipts) {
        TransferOrder order = getTransferOrder(tenantId, transferId);
        if (order.status() != TransferStatus.IN_TRANSIT) {
            throw new BizException("TRANSFER_STATUS_INVALID", "调拨单状态不允许收货");
        }
        List<TransferOrderLine> lines = extStore.listTransferOrderLines(tenantId, transferId);
        for (ReceiveTransferLineCommand receipt : receipts) {
            TransferOrderLine line = lines.stream()
                    .filter(l -> l.lineId().equals(receipt.lineId()))
                    .findFirst()
                    .orElseThrow(() -> new BizException("TRANSFER_LINE_NOT_FOUND", "调拨明细不存在"));
            int totalReceived = line.receivedQuantity() + receipt.receivedQuantity();
            if (totalReceived > line.transferQuantity()) {
                throw new BizException("RECEIVE_EXCEEDS_TRANSFER", "收货数量超过调拨数量");
            }
            extStore.saveTransferOrderLine(tenantId, new TransferOrderLine(line.lineId(), line.transferId(),
                    line.sellerSku(), line.transferQuantity(), totalReceived, line.unitCost(), line.batchNo()));
            inventoryStore.receiveInTransitInventory(tenantId, line.sellerSku(), order.toWarehouseId(),
                    receipt.receivedQuantity(), "TRANSFER_IN_RECEIVE", transferId, "调拨收货入库");
        }
        List<TransferOrderLine> updatedLines = extStore.listTransferOrderLines(tenantId, transferId);
        boolean allReceived = updatedLines.stream().allMatch(l -> l.receivedQuantity() >= l.transferQuantity());
        TransferStatus newStatus = allReceived ? TransferStatus.RECEIVED : TransferStatus.IN_TRANSIT;
        return extStore.saveTransferOrder(new TransferOrder(order.transferId(), order.tenantId(),
                order.fromWarehouseId(), order.toWarehouseId(), newStatus,
                order.remark(), order.createdAt(), Instant.now()));
    }

    public TransferOrder getTransferOrder(String tenantId, String transferId) {
        return extStore.findTransferOrder(tenantId, transferId)
                .orElseThrow(() -> new BizException("TRANSFER_ORDER_NOT_FOUND", "调拨单不存在"));
    }

    public List<TransferOrder> listTransferOrders(String tenantId, String warehouseId) {
        return extStore.listTransferOrders(tenantId, warehouseId);
    }

    public List<TransferOrderLine> listTransferOrderLines(String tenantId, String transferId) {
        return extStore.listTransferOrderLines(tenantId, transferId);
    }

    @Transactional
    public StockCheckOrder createStockCheckOrder(String tenantId, CreateStockCheckCommand command) {
        inventoryStore.findWarehouse(tenantId, command.warehouseId())
                .orElseThrow(() -> new BizException("WAREHOUSE_NOT_FOUND", "仓库不存在"));
        Instant now = Instant.now();
        StockCheckOrder order = new StockCheckOrder(UUID.randomUUID().toString(), tenantId, command.warehouseId(),
                command.checkType(), CheckOrderStatus.PENDING, command.checkedBy(), command.remark(), now, null);
        extStore.saveStockCheckOrder(order);
        for (StockCheckLineCommand lineCmd : command.lines()) {
            InventoryBalance balance = inventoryStore.findBalance(tenantId, command.warehouseId(), lineCmd.sellerSku())
                    .orElse(new InventoryBalance(tenantId, command.warehouseId(), lineCmd.sellerSku(), 0, 0, 0, 0, now));
            StockCheckOrderLine line = new StockCheckOrderLine(UUID.randomUUID().toString(), order.checkOrderId(),
                    lineCmd.sellerSku(), lineCmd.locationId(), balance.onHand(), 0, 0, LineStatus.PENDING);
            extStore.saveStockCheckOrderLine(tenantId, line);
        }
        return order;
    }

    @Transactional
    public StockCheckOrderLine countStockCheckLine(String tenantId, String checkOrderId, String lineId, int actualQuantity) {
        StockCheckOrder order = extStore.findStockCheckOrder(tenantId, checkOrderId)
                .orElseThrow(() -> new BizException("CHECK_ORDER_NOT_FOUND", "盘点单不存在"));
        if (order.status() != CheckOrderStatus.PENDING && order.status() != CheckOrderStatus.IN_PROGRESS) {
            throw new BizException("CHECK_STATUS_INVALID", "盘点单状态不允许继续盘点");
        }
        StockCheckOrderLine line = extStore.findStockCheckOrderLine(tenantId, checkOrderId, lineId)
                .orElseThrow(() -> new BizException("CHECK_LINE_NOT_FOUND", "盘点明细不存在"));
        int difference = actualQuantity - line.systemQuantity();
        StockCheckOrderLine updated = new StockCheckOrderLine(line.lineId(), line.checkOrderId(),
                line.sellerSku(), line.locationId(), line.systemQuantity(), actualQuantity, difference, LineStatus.COUNTED);
        extStore.saveStockCheckOrderLine(tenantId, updated);
        if (order.status() == CheckOrderStatus.PENDING) {
            extStore.saveStockCheckOrder(new StockCheckOrder(order.checkOrderId(), order.tenantId(),
                    order.warehouseId(), order.checkType(), CheckOrderStatus.IN_PROGRESS,
                    order.checkedBy(), order.remark(), order.createdAt(), order.completedAt()));
        }
        return updated;
    }

    @Transactional
    public StockCheckOrder completeStockCheck(String tenantId, String checkOrderId) {
        StockCheckOrder order = extStore.findStockCheckOrder(tenantId, checkOrderId)
                .orElseThrow(() -> new BizException("CHECK_ORDER_NOT_FOUND", "盘点单不存在"));
        List<StockCheckOrderLine> lines = extStore.listStockCheckOrderLines(tenantId, checkOrderId);
        boolean allCounted = lines.stream().allMatch(l -> l.status() == LineStatus.COUNTED);
        if (!allCounted) {
            throw new BizException("NOT_ALL_COUNTED", "还有明细未完成盘点");
        }
        return extStore.saveStockCheckOrder(new StockCheckOrder(order.checkOrderId(), order.tenantId(),
                order.warehouseId(), order.checkType(), CheckOrderStatus.COMPLETED,
                order.checkedBy(), order.remark(), order.createdAt(), Instant.now()));
    }

    @Transactional
    public StockCheckOrder adjustStockCheck(String tenantId, String checkOrderId) {
        StockCheckOrder order = extStore.findStockCheckOrder(tenantId, checkOrderId)
                .orElseThrow(() -> new BizException("CHECK_ORDER_NOT_FOUND", "盘点单不存在"));
        if (order.status() != CheckOrderStatus.COMPLETED) {
            throw new BizException("CHECK_STATUS_INVALID", "盘点单未完成，无法调整");
        }
        List<StockCheckOrderLine> lines = extStore.listStockCheckOrderLines(tenantId, checkOrderId);
        for (StockCheckOrderLine line : lines) {
            if (line.difference() != 0) {
                if (line.difference() > 0) {
                    inventoryStore.addInventory(line.sellerSku(), order.warehouseId(),
                            BigDecimal.valueOf(line.difference()), checkOrderId,
                            com.aidotnet.erp.wms.domain.InventoryTransactionType.STOCK_CHECK_GAIN);
                } else {
                    inventoryStore.deductInventory(line.sellerSku(), order.warehouseId(),
                            BigDecimal.valueOf(Math.abs(line.difference())), checkOrderId,
                            com.aidotnet.erp.wms.domain.InventoryTransactionType.STOCK_CHECK_LOSS);
                }
            }
            extStore.saveStockCheckOrderLine(tenantId, new StockCheckOrderLine(line.lineId(), line.checkOrderId(),
                    line.sellerSku(), line.locationId(), line.systemQuantity(), line.actualQuantity(),
                    line.difference(), LineStatus.ADJUSTED));
        }
        return extStore.saveStockCheckOrder(new StockCheckOrder(order.checkOrderId(), order.tenantId(),
                order.warehouseId(), order.checkType(), CheckOrderStatus.ADJUSTED,
                order.checkedBy(), order.remark(), order.createdAt(), Instant.now()));
    }

    public StockCheckOrder getStockCheckOrder(String tenantId, String checkOrderId) {
        return extStore.findStockCheckOrder(tenantId, checkOrderId)
                .orElseThrow(() -> new BizException("CHECK_ORDER_NOT_FOUND", "盘点单不存在"));
    }

    public List<StockCheckOrder> listStockCheckOrders(String tenantId, String warehouseId) {
        return extStore.listStockCheckOrders(tenantId, warehouseId);
    }

    public List<StockCheckOrderLine> listStockCheckOrderLines(String tenantId, String checkOrderId) {
        return extStore.listStockCheckOrderLines(tenantId, checkOrderId);
    }

    public record CreateTransferCommand(String fromWarehouseId, String toWarehouseId, String remark,
                                         List<TransferLineCommand> lines) {}
    public record TransferLineCommand(String sellerSku, int transferQuantity, BigDecimal unitCost, String batchNo) {}
    public record ReceiveTransferLineCommand(String lineId, int receivedQuantity) {}
    public record CreateStockCheckCommand(String warehouseId, CheckType checkType, String checkedBy,
                                           String remark, List<StockCheckLineCommand> lines) {}
    public record StockCheckLineCommand(String sellerSku, String locationId) {}
}
