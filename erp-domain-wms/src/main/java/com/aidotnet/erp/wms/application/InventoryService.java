package com.aidotnet.erp.wms.application;

import com.aidotnet.erp.common.context.TraceContext;
import com.aidotnet.erp.common.event.DomainEventPublisher;
import com.aidotnet.erp.common.event.StandardDomainEvent;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.wms.domain.InventoryBalance;
import com.aidotnet.erp.wms.domain.InventoryPrediction;
import com.aidotnet.erp.wms.domain.InventoryTransaction;
import com.aidotnet.erp.wms.domain.StockCheck;
import com.aidotnet.erp.wms.domain.Warehouse;
import com.aidotnet.erp.wms.domain.WarehouseLocation;
import com.aidotnet.erp.wms.infrastructure.InventoryStore;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * 库存管理应用服务
 * <p>
 * 描述: 仓储域核心服务，负责仓库/库位管理、库存收发存操作、
 *       库存盘点/调整、AI库存预测等业务逻辑。是连接订单域(OMS)、
 *       供应链域(SCM)、财务域(FMS)的库存数据中枢。
 * </p>
 * <p>
 * 核心能力:
 *   1. 仓库管理 - 创建/查询仓库和库位，支持多仓多国布局
 *   2. 库存操作 - 入库(RECEIVE)/预占(RESERVE)/释放(RELEASE)/出库扣减(DEDUCT)
 *   3. 库存盘点 - 创建盘点记录，盘点差异自动调整库存
 *   4. AI库存预测 - 基于日均销量预测7天/30天需求，计算可售天数
 *   5. 库存事务 - 所有库存变动自动记录事务日志，确保可追溯
 * </p>
 * <p>
 * 业务规则:
 *   1. 预占库存不可超过可用库存(onHand - reserved)
 *   2. 释放数量不可超过已预占数量
 *   3. 出库扣减数量不可超过已预占数量
 *   4. 盘点调整后库存以实际数量为准
 *   5. 库存预测级别: OUT_OF_STOCK(≤3天) / CRITICAL(≤7天) / WARNING(≤14天) / SUFFICIENT(>14天)
 *   6. 低库存事件自动发布，供SCM域订阅触发补货
 * </p>
 *
 * @author ERP系统
 * @see InventoryBalance
 * @see InventoryPrediction
 * @see InventoryStore
 */
@Service
public class InventoryService {

    private final InventoryStore inventoryStore;
    private final DomainEventPublisher eventPublisher;

    public InventoryService(InventoryStore inventoryStore, DomainEventPublisher eventPublisher) {
        this.inventoryStore = inventoryStore;
        this.eventPublisher = eventPublisher;
    }

    public Warehouse createWarehouse(String tenantId, CreateWarehouseCommand command) {
        Instant now = Instant.now();
        return inventoryStore.saveWarehouse(new Warehouse(
                UUID.randomUUID().toString(),
                tenantId,
                command.code(),
                command.name(),
                command.type(),
                command.countryCode(),
                command.address(),
                Warehouse.WarehouseStatus.ACTIVE.name(),
                command.contactPerson(),
                command.phone(),
                now,
                now));
    }

    public List<Warehouse> listWarehouses(String tenantId) {
        return inventoryStore.listWarehouses(tenantId);
    }

    public Warehouse getWarehouse(String tenantId, String warehouseId) {
        return inventoryStore.findWarehouse(tenantId, warehouseId)
                .orElseThrow(() -> new BizException("WAREHOUSE_NOT_FOUND", "仓库不存在"));
    }

    public WarehouseLocation createLocation(String tenantId, CreateLocationCommand command) {
        ensureWarehouse(tenantId, command.warehouseId());
        Instant now = Instant.now();
        return inventoryStore.saveLocation(new WarehouseLocation(
                UUID.randomUUID().toString(),
                tenantId,
                command.warehouseId(),
                command.locationCode(),
                command.zone(),
                command.aisle(),
                command.shelf(),
                command.bin(),
                true,
                now,
                now));
    }

    public List<WarehouseLocation> listLocations(String tenantId, String warehouseId) {
        ensureWarehouse(tenantId, warehouseId);
        return inventoryStore.listLocations(tenantId, warehouseId);
    }

    public InventoryBalance receive(String tenantId, StockCommand command) {
        ensureWarehouse(tenantId, command.warehouseId());
        InventoryBalance current = inventoryStore.findBalance(tenantId, command.warehouseId(), command.sellerSku())
                .orElse(new InventoryBalance(tenantId, command.warehouseId(), command.sellerSku(), 0, 0, 0, 0, Instant.now()));
        InventoryBalance updated = inventoryStore.saveBalance(new InventoryBalance(
                tenantId,
                command.warehouseId(),
                command.sellerSku(),
                current.onHand() + command.quantity(),
                current.reserved(),
                current.inTransit(),
                current.frozen(),
                Instant.now()));
        recordTransaction(command, InventoryTransaction.TransactionType.RECEIVE, current, updated, "MANUAL_RECEIVE", "库存入库");
        publishEvent("erp.wms.inventory.updated.v1", tenantId, command.sellerSku(), Map.of(
                "warehouseId", command.warehouseId(),
                "quantity", command.quantity(),
                "operation", "RECEIVE"));
        return updated;
    }

    public InventoryBalance reserve(String tenantId, StockCommand command) {
        InventoryBalance current = getBalance(tenantId, command.warehouseId(), command.sellerSku());
        if (current.getAvailable() < command.quantity()) {
            throw new BizException("INVENTORY_NOT_ENOUGH", "可用库存不足");
        }
        InventoryBalance updated = inventoryStore.saveBalance(new InventoryBalance(
                tenantId,
                command.warehouseId(),
                command.sellerSku(),
                current.onHand(),
                current.reserved() + command.quantity(),
                current.inTransit(),
                current.frozen(),
                Instant.now()));
        recordTransaction(command, InventoryTransaction.TransactionType.RESERVE, current, updated, "MANUAL_RESERVE", "库存预占");
        return updated;
    }

    public InventoryBalance release(String tenantId, StockCommand command) {
        InventoryBalance current = getBalance(tenantId, command.warehouseId(), command.sellerSku());
        if (current.reserved() < command.quantity()) {
            throw new BizException("INVENTORY_RELEASE_EXCEEDS_RESERVED", "释放数量超过预占库存");
        }
        InventoryBalance updated = inventoryStore.saveBalance(new InventoryBalance(
                tenantId,
                command.warehouseId(),
                command.sellerSku(),
                current.onHand(),
                current.reserved() - command.quantity(),
                current.inTransit(),
                current.frozen(),
                Instant.now()));
        recordTransaction(command, InventoryTransaction.TransactionType.RELEASE, current, updated, "MANUAL_RELEASE", "库存释放");
        return updated;
    }

    public InventoryBalance deduct(String tenantId, StockCommand command) {
        InventoryBalance current = getBalance(tenantId, command.warehouseId(), command.sellerSku());
        if (current.reserved() < command.quantity()) {
            throw new BizException("INVENTORY_DEDUCT_EXCEEDS_RESERVED", "扣减数量超过预占库存");
        }
        InventoryBalance updated = inventoryStore.saveBalance(new InventoryBalance(
                tenantId,
                command.warehouseId(),
                command.sellerSku(),
                current.onHand() - command.quantity(),
                current.reserved() - command.quantity(),
                current.inTransit(),
                current.frozen(),
                Instant.now()));
        recordTransaction(command, InventoryTransaction.TransactionType.DEDUCT, current, updated, "MANUAL_DEDUCT", "库存出库扣减");
        return updated;
    }

    public List<InventoryBalance> listBalances(String tenantId, String warehouseId) {
        ensureWarehouse(tenantId, warehouseId);
        return inventoryStore.listBalances(tenantId, warehouseId);
    }

    public InventoryAvailability getAvailability(String tenantId, String sellerSku) {
        List<InventoryBalance> balances = inventoryStore.listBalancesBySku(tenantId, sellerSku);
        int onHand = balances.stream().mapToInt(InventoryBalance::onHand).sum();
        int reserved = balances.stream().mapToInt(InventoryBalance::reserved).sum();
        int available = balances.stream().mapToInt(InventoryBalance::getAvailable).sum();
        return new InventoryAvailability(sellerSku, onHand, reserved, available, balances);
    }

    public StockCheck createStockCheck(String tenantId, CreateStockCheckCommand command) {
        ensureWarehouse(tenantId, command.warehouseId());
        InventoryBalance balance = inventoryStore.findBalance(tenantId, command.warehouseId(), command.sellerSku())
                .orElse(new InventoryBalance(tenantId, command.warehouseId(), command.sellerSku(), 0, 0, 0, 0, Instant.now()));
        int diff = command.actualQuantity() - balance.onHand();
        return inventoryStore.saveStockCheck(new StockCheck(
                UUID.randomUUID().toString(),
                tenantId,
                command.warehouseId(),
                command.sellerSku(),
                balance.onHand(),
                command.actualQuantity(),
                diff,
                StockCheck.CheckStatus.COMPLETED,
                command.checkedBy(),
                Instant.now(),
                Instant.now()));
    }

    public StockCheck adjustStock(String tenantId, String checkId) {
        StockCheck check = inventoryStore.findStockCheck(tenantId, checkId)
                .orElseThrow(() -> new BizException("STOCK_CHECK_NOT_FOUND", "盘点记录不存在"));
        if (check.status() == StockCheck.CheckStatus.ADJUSTED) {
            throw new BizException("STOCK_CHECK_ALREADY_ADJUSTED", "盘点已调整");
        }
        if (check.sellerSku() == null || check.sellerSku().isBlank()) {
            throw new BizException("STOCK_CHECK_DATA_INVALID", "盘点明细不存在");
        }
        InventoryBalance balance = inventoryStore.findBalance(tenantId, check.warehouseId(), check.sellerSku())
                .orElse(new InventoryBalance(tenantId, check.warehouseId(), check.sellerSku(), 0, 0, 0, 0, Instant.now()));
        InventoryBalance updated = inventoryStore.saveBalance(new InventoryBalance(
                tenantId,
                check.warehouseId(),
                check.sellerSku(),
                check.actualQuantity(),
                balance.reserved(),
                balance.inTransit(),
                balance.frozen(),
                Instant.now()));
        if (check.difference() != 0) {
            recordTransaction(
                    new StockCommand(check.warehouseId(), check.sellerSku(), Math.abs(check.difference()),
                            "STOCK_CHECK", check.checkId(), "盘点调整"),
                    InventoryTransaction.TransactionType.STOCK_ADJUST,
                    balance,
                    updated,
                    "STOCK_CHECK",
                    "盘点调整");
        }
        return inventoryStore.saveStockCheck(new StockCheck(
                check.checkId(),
                check.tenantId(),
                check.warehouseId(),
                check.sellerSku(),
                check.systemQuantity(),
                check.actualQuantity(),
                check.difference(),
                StockCheck.CheckStatus.ADJUSTED,
                check.checkedBy(),
                check.checkedAt() != null ? check.checkedAt() : Instant.now(),
                check.createdAt()));
    }

    public List<StockCheck> listStockChecks(String tenantId, String warehouseId) {
        return inventoryStore.listStockChecks(tenantId, warehouseId);
    }

    public InventoryPrediction predict(String tenantId, PredictCommand command) {
        ensureWarehouse(tenantId, command.warehouseId());
        InventoryBalance balance = inventoryStore.findBalance(tenantId, command.warehouseId(), command.sellerSku())
                .orElse(new InventoryBalance(tenantId, command.warehouseId(), command.sellerSku(), 0, 0, 0, 0, Instant.now()));
        int currentStock = balance.onHand();
        int predicted7d = (int) (command.avgDailySales() * 7);
        int predicted30d = (int) (command.avgDailySales() * 30);
        int daysOfStock = command.avgDailySales() > 0 ? (int) (currentStock / command.avgDailySales()) : 999;
        InventoryPrediction.PredictionLevel level;
        if (daysOfStock <= 3) {
            level = InventoryPrediction.PredictionLevel.OUT_OF_STOCK;
        } else if (daysOfStock <= 7) {
            level = InventoryPrediction.PredictionLevel.CRITICAL;
        } else if (daysOfStock <= 14) {
            level = InventoryPrediction.PredictionLevel.WARNING;
        } else {
            level = InventoryPrediction.PredictionLevel.SUFFICIENT;
        }
        InventoryPrediction prediction = inventoryStore.savePrediction(new InventoryPrediction(
                UUID.randomUUID().toString(),
                tenantId,
                command.warehouseId(),
                command.sellerSku(),
                currentStock,
                predicted7d,
                predicted30d,
                daysOfStock,
                level,
                Instant.now()));
        if (level == InventoryPrediction.PredictionLevel.OUT_OF_STOCK
                || level == InventoryPrediction.PredictionLevel.CRITICAL) {
            publishEvent("erp.wms.inventory.low-stock.v1", tenantId, command.sellerSku(), Map.of(
                    "warehouseId", command.warehouseId(),
                    "daysOfStock", daysOfStock,
                    "level", level.name()));
        }
        return prediction;
    }

    public List<InventoryPrediction> listPredictions(String tenantId, String warehouseId) {
        return inventoryStore.listPredictions(tenantId, warehouseId);
    }

    public List<InventoryTransaction> listTransactions(String tenantId, String warehouseId, String sellerSku) {
        ensureWarehouse(tenantId, warehouseId);
        return inventoryStore.listTransactions(tenantId, warehouseId, sellerSku);
    }

    private void ensureWarehouse(String tenantId, String warehouseId) {
        inventoryStore.findWarehouse(tenantId, warehouseId)
                .orElseThrow(() -> new BizException("WAREHOUSE_NOT_FOUND", "仓库不存在"));
    }

    private InventoryBalance getBalance(String tenantId, String warehouseId, String sellerSku) {
        ensureWarehouse(tenantId, warehouseId);
        return inventoryStore.findBalance(tenantId, warehouseId, sellerSku)
                .orElseThrow(() -> new BizException("INVENTORY_NOT_FOUND", "库存不存在"));
    }

    private void recordTransaction(StockCommand command,
                                   InventoryTransaction.TransactionType transactionType,
                                   InventoryBalance before,
                                   InventoryBalance after,
                                   String defaultReferenceType,
                                   String defaultRemark) {
        inventoryStore.saveTransaction(new InventoryTransaction(
                UUID.randomUUID().toString(),
                before.tenantId(),
                before.warehouseId(),
                before.sellerSku(),
                transactionType,
                command.quantity(),
                before.onHand(),
                before.reserved(),
                after.onHand(),
                after.reserved(),
                valueOrDefault(command.referenceType(), defaultReferenceType),
                command.referenceId(),
                valueOrDefault(command.remark(), defaultRemark),
                Instant.now()));
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private void publishEvent(String eventType, String tenantId, String aggregateId, Map<String, Object> payload) {
        try {
            eventPublisher.publish(new StandardDomainEvent(
                    UUID.randomUUID().toString(),
                    tenantId,
                    TraceContext.getTraceId(),
                    eventType,
                    aggregateId,
                    Instant.now(),
                    payload));
        } catch (Exception e) {
            throw new BizException("EVENT_PUBLISH_FAILED", "事件发布失败: " + e.getMessage());
        }
    }

    public record CreateWarehouseCommand(String code, String name, String type, String countryCode,
                                         String address, String contactPerson, String phone) {}

    public record CreateLocationCommand(String warehouseId, String locationCode, String zone, String aisle, String shelf, String bin) {}

    public record StockCommand(String warehouseId, String sellerSku, int quantity,
                               String referenceType, String referenceId, String remark) {
        public StockCommand(String warehouseId, String sellerSku, int quantity) {
            this(warehouseId, sellerSku, quantity, null, null, null);
        }
    }

    public record InventoryAvailability(String sellerSku, int onHand, int reserved, int available,
                                        List<InventoryBalance> warehouseBalances) {}

    public record CreateStockCheckCommand(String warehouseId, String sellerSku, int actualQuantity, String checkedBy) {}

    public record PredictCommand(String warehouseId, String sellerSku, double avgDailySales) {}
}
