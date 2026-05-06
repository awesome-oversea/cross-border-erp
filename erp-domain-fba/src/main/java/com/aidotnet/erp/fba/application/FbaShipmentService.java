package com.aidotnet.erp.fba.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.fba.domain.CartonLabel;
import com.aidotnet.erp.fba.domain.FbaInboundPlan;
import com.aidotnet.erp.fba.domain.FbaInboundPlanStatus;
import com.aidotnet.erp.fba.domain.FbaInventory;
import com.aidotnet.erp.fba.domain.FbaLocation;
import com.aidotnet.erp.fba.domain.FbaShipment;
import com.aidotnet.erp.fba.domain.FbaShipmentItem;
import com.aidotnet.erp.fba.domain.FbaShipmentStatus;
import com.aidotnet.erp.fba.domain.ReplenishmentPlan;
import com.aidotnet.erp.fba.domain.RestockCartItem;
import com.aidotnet.erp.fba.domain.RestockSuggestion;
import com.aidotnet.erp.fba.domain.ShipmentException;
import com.aidotnet.erp.fba.infrastructure.FbaShipmentRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * FBA发货管理应用服务
 * <p>
 * 描述: FBA/海外仓域核心服务，负责FBA入库计划管理、FBA发货全流程、
 *       箱标管理、FBA库存同步、补货计划等业务逻辑。
 *       是连接仓储域(WMS)和Amazon FBA的桥梁。
 * </p>
 * <p>
 * 核心能力:
 *   1. 入库计划 - 创建/更新/拆分/提交/取消FBA入库计划
 *   2. FBA发货 - 创建/提交/打包/发货/收货FBA货件，跟踪发货状态
 *   3. 箱标管理 - 创建FBA箱标，关联发货货件
 *   4. FBA库存 - 同步Amazon FBA库存数据，维护FNSKU映射
 *   5. FBA库位 - 管理FBA仓库/FC库位信息
 *   6. 补货计划 - 创建/提交FBA补货计划
 * </p>
 * <p>
 * 业务规则:
 *   1. 入库计划状态流转: DRAFT -> SUBMITTED -> SPLIT/COMPLETED/CANCELLED
 *   2. 拆分计划时拆分数量必须小于原始数量
 *   3. FBA发货状态流转: DRAFT -> SUBMITTED -> PACKED -> SHIPPED -> RECEIVED/CLOSED
 *   4. 收货数量不可超过计划数量
 *   5. Amazon Shipment ID唯一性校验
 * </p>
 *
 * @author ERP系统
 * @see FbaShipment
 * @see FbaInboundPlan
 * @see FbaShipmentRepository
 */
@Service
public class FbaShipmentService {

    private final FbaShipmentRepository repository;

    public FbaShipmentService(FbaShipmentRepository repository) {
        this.repository = repository;
    }

    public FbaInboundPlan createInboundPlan(String tenantId, CreateInboundPlanCommand command) {
        Instant now = Instant.now();
        FbaInboundPlan plan = new FbaInboundPlan(
                UUID.randomUUID().toString(),
                tenantId,
                command.warehouseId(),
                command.planName(),
                command.sellerSku(),
                command.plannedQuantity(),
                command.storeId(),
                command.siteCode(),
                null,
                FbaInboundPlanStatus.DRAFT,
                now,
                now);
        return repository.saveInboundPlan(plan);
    }

    public FbaInboundPlan updateInboundPlan(String tenantId, String planId, UpdateInboundPlanCommand command) {
        FbaInboundPlan current = getInboundPlan(tenantId, planId);
        ensurePlanEditable(current);
        FbaInboundPlan updated = new FbaInboundPlan(
                current.planId(),
                current.tenantId(),
                valueOrDefault(command.warehouseId(), current.warehouseId()),
                valueOrDefault(command.planName(), current.planName()),
                valueOrDefault(command.sellerSku(), current.sellerSku()),
                command.plannedQuantity() != null ? command.plannedQuantity() : current.plannedQuantity(),
                valueOrDefault(command.storeId(), current.storeId()),
                valueOrDefault(command.siteCode(), current.siteCode()),
                current.sourcePlanId(),
                current.status(),
                current.createdAt(),
                Instant.now());
        return repository.saveInboundPlan(updated);
    }

    public SplitInboundPlanResult splitInboundPlan(String tenantId, String planId, SplitInboundPlanCommand command) {
        FbaInboundPlan current = getInboundPlan(tenantId, planId);
        ensurePlanEditable(current);
        if (command.splitQuantity() >= current.plannedQuantity()) {
            throw new BizException("FBA_INBOUND_PLAN_SPLIT_INVALID", "Split quantity must be less than original quantity");
        }
        Instant now = Instant.now();
        FbaInboundPlan parent = new FbaInboundPlan(
                current.planId(),
                current.tenantId(),
                current.warehouseId(),
                current.planName(),
                current.sellerSku(),
                current.plannedQuantity() - command.splitQuantity(),
                current.storeId(),
                current.siteCode(),
                current.sourcePlanId(),
                FbaInboundPlanStatus.SPLIT,
                current.createdAt(),
                now);
        FbaInboundPlan child = new FbaInboundPlan(
                UUID.randomUUID().toString(),
                tenantId,
                current.warehouseId(),
                command.newPlanName(),
                current.sellerSku(),
                command.splitQuantity(),
                current.storeId(),
                current.siteCode(),
                current.planId(),
                FbaInboundPlanStatus.DRAFT,
                now,
                now);
        repository.saveInboundPlan(parent);
        repository.saveInboundPlan(child);
        return new SplitInboundPlanResult(parent, child);
    }

    public FbaInboundPlan submitInboundPlan(String tenantId, String planId) {
        FbaInboundPlan current = getInboundPlan(tenantId, planId);
        ensurePlanEditable(current);
        return repository.saveInboundPlan(new FbaInboundPlan(
                current.planId(),
                current.tenantId(),
                current.warehouseId(),
                current.planName(),
                current.sellerSku(),
                current.plannedQuantity(),
                current.storeId(),
                current.siteCode(),
                current.sourcePlanId(),
                FbaInboundPlanStatus.SUBMITTED,
                current.createdAt(),
                Instant.now()));
    }

    public FbaInboundPlan cancelInboundPlan(String tenantId, String planId) {
        FbaInboundPlan current = getInboundPlan(tenantId, planId);
        if (current.status() == FbaInboundPlanStatus.CANCELLED || current.status() == FbaInboundPlanStatus.COMPLETED) {
            throw new BizException("FBA_INBOUND_PLAN_STATUS_INVALID", "Inbound plan cannot be cancelled");
        }
        return repository.saveInboundPlan(new FbaInboundPlan(
                current.planId(),
                current.tenantId(),
                current.warehouseId(),
                current.planName(),
                current.sellerSku(),
                current.plannedQuantity(),
                current.storeId(),
                current.siteCode(),
                current.sourcePlanId(),
                FbaInboundPlanStatus.CANCELLED,
                current.createdAt(),
                Instant.now()));
    }

    public List<FbaInboundPlan> listInboundPlans(String tenantId, String warehouseId, String status) {
        return repository.listInboundPlans(tenantId, warehouseId, status);
    }

    public FbaShipment create(String tenantId, CreateFbaShipmentCommand command) {
        if (command.amazonShipmentId() != null && !command.amazonShipmentId().isBlank()) {
            repository.findByAmazonShipmentId(tenantId, command.amazonShipmentId()).ifPresent(existing -> {
                throw new BizException("FBA_SHIPMENT_DUPLICATED", "FBA shipment already exists");
            });
        }
        FbaInboundPlan plan = null;
        if (command.planId() != null && !command.planId().isBlank()) {
            plan = getInboundPlan(tenantId, command.planId());
            if (plan.status() == FbaInboundPlanStatus.CANCELLED) {
                throw new BizException("FBA_INBOUND_PLAN_CANCELLED", "Inbound plan is cancelled");
            }
        }
        int plannedQuantity = command.plannedQuantity() > 0
                ? command.plannedQuantity()
                : command.items().stream().mapToInt(ShipmentItemCommand::quantity).sum();
        if (plannedQuantity <= 0) {
            throw new BizException("FBA_SHIPMENT_QUANTITY_INVALID", "Planned quantity must be greater than zero");
        }
        Instant now = Instant.now();
        FbaShipment shipment = new FbaShipment(
                UUID.randomUUID().toString(),
                tenantId,
                nonBlankOrDefault(command.amazonShipmentId(), "LOCAL-" + UUID.randomUUID().toString().substring(0, 8)),
                nonBlankOrDefault(command.destinationFc(), plan != null ? plan.warehouseId() : null),
                command.planId(),
                command.carrier(),
                command.trackingNo(),
                plannedQuantity,
                0,
                0,
                BigDecimal.ZERO,
                FbaShipmentStatus.DRAFT,
                null,
                null,
                now,
                now);
        repository.save(shipment);
        for (ShipmentItemCommand item : command.items()) {
            repository.saveShipmentItem(new FbaShipmentItem(
                    UUID.randomUUID().toString(),
                    tenantId,
                    shipment.fbaShipmentId(),
                    item.productId(),
                    item.sellerSku(),
                    item.fnsku(),
                    item.quantity(),
                    item.boxQuantity(),
                    now));
        }
        return shipment;
    }

    public FbaShipment submit(String tenantId, String fbaShipmentId) {
        FbaShipment shipment = getShipment(tenantId, fbaShipmentId);
        if (shipment.status() != FbaShipmentStatus.DRAFT) {
            throw new BizException("FBA_SHIPMENT_STATUS_INVALID", "Only draft shipment can be submitted");
        }
        return repository.save(copyShipment(shipment, shipment.planId(), shipment.carrier(), shipment.trackingNo(),
                shipment.receivedQuantity(), shipment.cartonCount(), shipment.totalWeight(),
                FbaShipmentStatus.SUBMITTED, shipment.packedAt(), shipment.shippedAt()));
    }

    public FbaShipment pack(String tenantId, String fbaShipmentId, PackFbaShipmentCommand command) {
        FbaShipment shipment = getShipment(tenantId, fbaShipmentId);
        if (shipment.status() != FbaShipmentStatus.DRAFT && shipment.status() != FbaShipmentStatus.SUBMITTED) {
            throw new BizException("FBA_SHIPMENT_STATUS_INVALID", "Only draft or submitted shipment can be packed");
        }
        return repository.save(copyShipment(
                shipment,
                shipment.planId(),
                nonBlankOrDefault(command.carrier(), shipment.carrier()),
                shipment.trackingNo(),
                shipment.receivedQuantity(),
                command.cartonCount(),
                command.totalWeight(),
                FbaShipmentStatus.PACKED,
                Instant.now(),
                shipment.shippedAt()));
    }

    public FbaShipment ship(String tenantId, String fbaShipmentId, ShipFbaShipmentCommand command) {
        FbaShipment shipment = getShipment(tenantId, fbaShipmentId);
        if (shipment.status() != FbaShipmentStatus.PACKED && shipment.status() != FbaShipmentStatus.SUBMITTED) {
            throw new BizException("FBA_SHIPMENT_STATUS_INVALID", "Only packed or submitted shipment can be shipped");
        }
        return repository.save(copyShipment(
                shipment,
                shipment.planId(),
                nonBlankOrDefault(command.carrier(), shipment.carrier()),
                command.trackingNo(),
                shipment.receivedQuantity(),
                shipment.cartonCount(),
                shipment.totalWeight(),
                FbaShipmentStatus.SHIPPED,
                shipment.packedAt(),
                Instant.now()));
    }

    public FbaShipment receive(String tenantId, String fbaShipmentId, ReceiveFbaShipmentCommand command) {
        FbaShipment shipment = getShipment(tenantId, fbaShipmentId);
        if (shipment.status() != FbaShipmentStatus.SUBMITTED
                && shipment.status() != FbaShipmentStatus.SHIPPED
                && shipment.status() != FbaShipmentStatus.RECEIVED) {
            throw new BizException("FBA_SHIPMENT_STATUS_INVALID", "Shipment cannot be received in current status");
        }
        int nextReceived = shipment.receivedQuantity() + command.receivedQuantity();
        if (nextReceived > shipment.plannedQuantity()) {
            throw new BizException("FBA_RECEIVE_EXCEEDS_PLANNED", "Received quantity exceeds planned quantity");
        }
        FbaShipmentStatus nextStatus = nextReceived == shipment.plannedQuantity()
                ? FbaShipmentStatus.CLOSED
                : FbaShipmentStatus.RECEIVED;
        return repository.save(copyShipment(
                shipment,
                shipment.planId(),
                shipment.carrier(),
                shipment.trackingNo(),
                nextReceived,
                shipment.cartonCount(),
                shipment.totalWeight(),
                nextStatus,
                shipment.packedAt(),
                shipment.shippedAt()));
    }

    public List<FbaShipment> list(String tenantId, String planId) {
        return repository.list(tenantId, planId);
    }

    public List<FbaShipmentItem> listShipmentItems(String tenantId, String shipmentId) {
        getShipment(tenantId, shipmentId);
        return repository.listShipmentItems(tenantId, shipmentId);
    }

    public CartonLabel createCartonLabel(String tenantId, CreateCartonLabelCommand command) {
        getShipment(tenantId, command.fbaShipmentId());
        return repository.saveCartonLabel(new CartonLabel(
                UUID.randomUUID().toString(),
                tenantId,
                command.fbaShipmentId(),
                command.cartonId(),
                command.sellerSku(),
                command.quantityPerCarton(),
                command.numberOfCartons(),
                command.labelUrl(),
                Instant.now()));
    }

    public List<CartonLabel> listCartonLabels(String tenantId, String fbaShipmentId) {
        getShipment(tenantId, fbaShipmentId);
        return repository.listCartonLabels(tenantId, fbaShipmentId);
    }

    public FbaLocation createLocation(String tenantId, CreateLocationCommand command) {
        Instant now = Instant.now();
        return repository.saveLocation(new FbaLocation(
                UUID.randomUUID().toString(),
                tenantId,
                command.name(),
                command.countryCode(),
                command.address(),
                command.locationType(),
                command.status(),
                now,
                now));
    }

    public List<FbaLocation> listLocations(String tenantId) {
        return repository.listLocations(tenantId);
    }

    public FbaInventory syncInventory(String tenantId, SyncInventoryCommand command) {
        Instant now = Instant.now();
        FbaInventory existing = repository.findInventoryByKey(
                        tenantId, command.warehouseId(), command.sellerSku(), command.fnsku(),
                        command.storeId(), command.siteCode())
                .orElse(null);
        String inventoryId = command.inventoryId();
        if ((inventoryId == null || inventoryId.isBlank()) && existing != null) {
            inventoryId = existing.inventoryId();
        }
        if (inventoryId == null || inventoryId.isBlank()) {
            inventoryId = UUID.randomUUID().toString();
        }
        return repository.saveInventory(new FbaInventory(
                inventoryId,
                tenantId,
                command.warehouseId(),
                command.productId(),
                command.sellerSku(),
                command.fnsku(),
                command.quantity(),
                command.storeId(),
                command.siteCode(),
                command.inventoryAgeDays(),
                now));
    }

    public FbaInventory getInventory(String tenantId, String inventoryId) {
        return repository.findInventory(tenantId, inventoryId)
                .orElseThrow(() -> new BizException("FBA_INVENTORY_NOT_FOUND", "FBA inventory not found"));
    }

    public List<FbaInventory> listInventories(String tenantId, String sellerSku, String storeId, String siteCode,
                                              String warehouseId) {
        return repository.listInventories(tenantId, sellerSku, storeId, siteCode, warehouseId);
    }

    public ReplenishmentPlan createPlan(String tenantId, CreatePlanCommand command) {
        Instant now = Instant.now();
        return repository.savePlan(new ReplenishmentPlan(
                UUID.randomUUID().toString(),
                tenantId,
                command.sellerSku(),
                command.destinationFc(),
                command.suggestedQuantity(),
                command.sourceWarehouseId(),
                ReplenishmentPlan.PlanStatus.DRAFT,
                now,
                now));
    }

    public ReplenishmentPlan submitPlan(String tenantId, String planId) {
        ReplenishmentPlan plan = getPlan(tenantId, planId);
        if (plan.status() != ReplenishmentPlan.PlanStatus.DRAFT) {
            throw new BizException("PLAN_STATUS_INVALID", "Only draft replenishment plan can be submitted");
        }
        return repository.savePlan(new ReplenishmentPlan(
                plan.planId(),
                plan.tenantId(),
                plan.sellerSku(),
                plan.destinationFc(),
                plan.suggestedQuantity(),
                plan.sourceWarehouseId(),
                ReplenishmentPlan.PlanStatus.SUBMITTED,
                plan.createdAt(),
                Instant.now()));
    }

    public List<ReplenishmentPlan> listPlans(String tenantId) {
        return repository.listPlans(tenantId);
    }

    private FbaShipment getShipment(String tenantId, String fbaShipmentId) {
        return repository.find(tenantId, fbaShipmentId)
                .orElseThrow(() -> new BizException("FBA_SHIPMENT_NOT_FOUND", "FBA shipment not found"));
    }

    private ReplenishmentPlan getPlan(String tenantId, String planId) {
        return repository.findPlan(tenantId, planId)
                .orElseThrow(() -> new BizException("PLAN_NOT_FOUND", "Replenishment plan not found"));
    }

    private FbaInboundPlan getInboundPlan(String tenantId, String planId) {
        return repository.findInboundPlan(tenantId, planId)
                .orElseThrow(() -> new BizException("FBA_INBOUND_PLAN_NOT_FOUND", "Inbound plan not found"));
    }

    private void ensurePlanEditable(FbaInboundPlan plan) {
        if (plan.status() == FbaInboundPlanStatus.CANCELLED || plan.status() == FbaInboundPlanStatus.COMPLETED) {
            throw new BizException("FBA_INBOUND_PLAN_STATUS_INVALID", "Inbound plan is not editable");
        }
    }

    private FbaShipment copyShipment(FbaShipment current, String planId, String carrier, String trackingNo,
                                     int receivedQuantity, int cartonCount, BigDecimal totalWeight,
                                     FbaShipmentStatus status, Instant packedAt, Instant shippedAt) {
        return new FbaShipment(
                current.fbaShipmentId(),
                current.tenantId(),
                current.amazonShipmentId(),
                current.destinationFc(),
                planId,
                carrier,
                trackingNo,
                current.plannedQuantity(),
                receivedQuantity,
                cartonCount,
                totalWeight,
                status,
                packedAt,
                shippedAt,
                current.createdAt(),
                Instant.now());
    }

    private String valueOrDefault(String nextValue, String currentValue) {
        return nextValue != null ? nextValue : currentValue;
    }

    private String nonBlankOrDefault(String value, String defaultValue) {
        return value != null && !value.isBlank() ? value : defaultValue;
    }

    public record CreateInboundPlanCommand(String warehouseId, String planName, String sellerSku, int plannedQuantity,
                                           String storeId, String siteCode) {}

    public record UpdateInboundPlanCommand(String warehouseId, String planName, String sellerSku, Integer plannedQuantity,
                                           String storeId, String siteCode) {}

    public record SplitInboundPlanCommand(String newPlanName, int splitQuantity) {}

    public record SplitInboundPlanResult(FbaInboundPlan parentPlan, FbaInboundPlan childPlan) {}

    public record ShipmentItemCommand(String productId, String sellerSku, String fnsku, int quantity, int boxQuantity) {}

    public record CreateFbaShipmentCommand(String amazonShipmentId, String destinationFc, String planId, String carrier,
                                           String trackingNo, int plannedQuantity, List<ShipmentItemCommand> items) {
        public CreateFbaShipmentCommand {
            items = items == null ? List.of() : List.copyOf(items);
        }
    }

    public record PackFbaShipmentCommand(int cartonCount, BigDecimal totalWeight, String carrier) {}

    public record ShipFbaShipmentCommand(String carrier, String trackingNo) {}

    public record ReceiveFbaShipmentCommand(int receivedQuantity) {}

    public record CreateCartonLabelCommand(String fbaShipmentId, String cartonId, String sellerSku,
                                           int quantityPerCarton, int numberOfCartons, String labelUrl) {}

    public record CreateLocationCommand(String name, String countryCode, String address, String locationType, String status) {}

    public record SyncInventoryCommand(String inventoryId, String warehouseId, String productId, String sellerSku,
                                       String fnsku, int quantity, String storeId, String siteCode, int inventoryAgeDays) {}

    public record CreatePlanCommand(String sellerSku, String destinationFc, int suggestedQuantity, String sourceWarehouseId) {}

    public List<RestockSuggestion> calculateRestockSuggestions(String tenantId, CalculateRestockCommand command) {
        List<FbaInventory> inventories = repository.listInventories(tenantId, null, null, null, null);
        List<RestockSuggestion> suggestions = new ArrayList<>();
        Instant now = Instant.now();
        for (FbaInventory inv : inventories) {
            BigDecimal avgDailySales = command.defaultAvgDailySales();
            int leadTimeDays = command.defaultLeadTimeDays();
            int safetyStockDays = command.safetyStockDays();
            int reorderPoint = (int) Math.ceil(avgDailySales.doubleValue() * (leadTimeDays + safetyStockDays));
            if (inv.quantity() < reorderPoint) {
                int suggestedQty = (int) Math.ceil(avgDailySales.doubleValue() * (leadTimeDays + safetyStockDays) * 2) - inv.quantity();
                if (suggestedQty > 0) {
                    Instant suggestedShipDate = now.plus(java.time.Duration.ofDays(Math.max(1, leadTimeDays - command.orderProcessingDays())));
                    suggestions.add(new RestockSuggestion(
                            UUID.randomUUID().toString(), tenantId, inv.productId(), inv.sellerSku(),
                            avgDailySales, leadTimeDays, suggestedQty, suggestedQty, 0,
                            suggestedShipDate, now, now));
                }
            }
        }
        suggestions.forEach(repository::saveRestockSuggestion);
        return suggestions;
    }

    public List<RestockSuggestion> listRestockSuggestions(String tenantId) {
        return repository.listRestockSuggestions(tenantId);
    }

    public RestockCartItem addToRestockCart(String tenantId, AddToCartCommand command) {
        Instant now = Instant.now();
        RestockCartItem item = new RestockCartItem(
                UUID.randomUUID().toString(), tenantId, command.userId(), command.productId(),
                command.sellerSku(), command.qty(), command.warehouseId(), now);
        return repository.saveRestockCartItem(item);
    }

    public List<RestockCartItem> listRestockCart(String tenantId, String userId) {
        return repository.listRestockCartItems(tenantId, userId);
    }

    public FbaInboundPlan confirmRestockCart(String tenantId, ConfirmCartCommand command) {
        List<RestockCartItem> items = repository.listRestockCartItems(tenantId, command.userId());
        if (items.isEmpty()) {
            throw new BizException("CART_EMPTY", "备货购物车为空");
        }
        int totalQty = items.stream().mapToInt(RestockCartItem::qty).sum();
        Instant now = Instant.now();
        FbaInboundPlan plan = new FbaInboundPlan(
                UUID.randomUUID().toString(), tenantId, command.warehouseId(),
                "备货计划-" + now.toString().substring(0, 10),
                items.get(0).sellerSku(), totalQty, null, null, null,
                FbaInboundPlanStatus.DRAFT, now, now);
        repository.saveInboundPlan(plan);
        for (RestockCartItem item : items) {
            repository.removeRestockCartItem(item.cartItemId());
        }
        return plan;
    }

    public void removeFromRestockCart(String tenantId, String cartItemId) {
        repository.removeRestockCartItem(cartItemId);
    }

    public ShipmentException recordShipmentException(String tenantId, RecordExceptionCommand command) {
        getShipment(tenantId, command.shipmentId());
        Instant now = Instant.now();
        ShipmentException exception = new ShipmentException(
                UUID.randomUUID().toString(), tenantId, command.shipmentId(),
                command.type(), command.qty(), ShipmentException.ExceptionStatus.OPEN.name(),
                command.description(), null, null, now, now);
        return repository.saveShipmentException(exception);
    }

    public ShipmentException resolveShipmentException(String tenantId, String exceptionId, ResolveExceptionCommand command) {
        ShipmentException ex = repository.findShipmentException(tenantId, exceptionId)
                .orElseThrow(() -> new BizException("EXCEPTION_NOT_FOUND", "货件异常不存在"));
        if (!ex.status().equals(ShipmentException.ExceptionStatus.OPEN.name())
                && !ex.status().equals(ShipmentException.ExceptionStatus.PROCESSING.name())) {
            throw new BizException("EXCEPTION_STATUS_INVALID", "异常状态不允许处理");
        }
        Instant now = Instant.now();
        ShipmentException resolved = new ShipmentException(
                ex.exceptionId(), ex.tenantId(), ex.shipmentId(), ex.type(), ex.qty(),
                ShipmentException.ExceptionStatus.RESOLVED.name(), ex.description(),
                command.resolvedBy(), now, ex.createdAt(), now);
        return repository.saveShipmentException(resolved);
    }

    public List<ShipmentException> listShipmentExceptions(String tenantId, String shipmentId) {
        return repository.listShipmentExceptions(tenantId, shipmentId);
    }

    public record CalculateRestockCommand(BigDecimal defaultAvgDailySales, int defaultLeadTimeDays,
                                          int safetyStockDays, int orderProcessingDays) {}
    public record AddToCartCommand(String userId, String productId, String sellerSku, int qty, String warehouseId) {}
    public record ConfirmCartCommand(String userId, String warehouseId) {}
    public record RecordExceptionCommand(String shipmentId, ShipmentException.ExceptionType type, int qty, String description) {}
    public record ResolveExceptionCommand(String resolvedBy) {}
}
