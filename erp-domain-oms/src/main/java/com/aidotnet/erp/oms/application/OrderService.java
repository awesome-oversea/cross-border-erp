package com.aidotnet.erp.oms.application;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.context.TraceContext;
import com.aidotnet.erp.common.event.DomainEventPublisher;
import com.aidotnet.erp.common.event.StandardDomainEvent;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.oms.client.FmsClient;
import com.aidotnet.erp.oms.client.TmsClient;
import com.aidotnet.erp.oms.client.WmsClient;
import com.aidotnet.erp.oms.domain.BuyerBlacklistEntry;
import com.aidotnet.erp.oms.domain.FulfillmentPackageStatus;
import com.aidotnet.erp.oms.domain.FulfillmentPlanStatus;
import com.aidotnet.erp.oms.domain.OriginalOrderSnapshot;
import com.aidotnet.erp.oms.domain.OrderFulfillmentPackage;
import com.aidotnet.erp.oms.domain.OrderFulfillmentPackageLine;
import com.aidotnet.erp.oms.domain.OrderFulfillmentPlan;
import com.aidotnet.erp.oms.domain.OrderLine;
import com.aidotnet.erp.oms.domain.OrderRefund;
import com.aidotnet.erp.oms.domain.OrderRiskCheck;
import com.aidotnet.erp.oms.domain.OrderStatus;
import com.aidotnet.erp.oms.domain.OrderStrategy;
import com.aidotnet.erp.oms.domain.OrderSyncLog;
import com.aidotnet.erp.oms.domain.PmsRiskAlert;
import com.aidotnet.erp.oms.domain.PlatformShipmentSyncLog;
import com.aidotnet.erp.oms.domain.PlatformShipmentSyncStatus;
import com.aidotnet.erp.oms.domain.Promotion;
import com.aidotnet.erp.oms.domain.SalesOrder;
import com.aidotnet.erp.oms.infrastructure.FulfillmentPlanStore;
import com.aidotnet.erp.oms.infrastructure.OrderStore;
import com.aidotnet.erp.oms.infrastructure.PlatformShipmentSyncLogStore;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 订单管理应用服务
 * <p>
 * 描述: 订单域核心服务，负责订单全生命周期管理，包括订单导入/审核/支付/发货/签收/退款，
 *       以及履约计划生成、物流商推荐、平台发货同步、买家黑名单等业务逻辑。
 *       是连接销售运营域(SOM)、仓储域(WMS)、物流域(TMS)、财务域(FMS)的核心枢纽。
 * </p>
 * <p>
 * 核心能力:
 *   1. 订单导入 - 从各平台导入订单，自动执行风控审核规则
 *   2. 订单状态流转 - CREATED -> PAID -> SHIPPED -> DELIVERED，支持审核/取消/退款
 *   3. 履约计划 - 根据库存和物流自动生成履约方案，支持拆单/分仓发货
 *   4. 物流商推荐 - 调用TMS获取物流商推荐，自动选择最优物流方案
 *   5. 平台发货同步 - 发货后自动同步物流信息到销售平台(Amazon/Shopify等)
 *   6. 退款管理 - 支持全额/部分退款，退款审批流程
 *   7. 买家黑名单 - 管理高风险买家，自动拦截黑名单买家订单
 *   8. 风控审核 - 多维度风控检查(重复下单/黑名单/库存不足等)
 * </p>
 * <p>
 * 业务规则:
 *   1. 同一平台同一platformOrderNo不可重复导入
 *   2. 风控审核不通过的订单进入REVIEW_REQUIRED状态
 *   3. 只有PAID状态订单可发货，发货需有有效履约计划
 *   4. 退款金额不可超过订单总额
 *   5. 已发货/已签收订单不可取消
 *   6. 平台发货同步失败可重试
 * </p>
 *
 * @author ERP系统
 * @see SalesOrder
 * @see OrderFulfillmentPlan
 * @see OrderStore
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private static final Set<String> SUPPORTED_PLATFORM_SHIPMENT_SYNC = Set.of(
            "AMAZON", "SHOPIFY", "TIKTOK", "WALMART", "EBAY");

    private final OrderStore orderStore;
    private final FulfillmentPlanStore fulfillmentPlanStore;
    private final PlatformShipmentSyncLogStore platformShipmentSyncLogStore;
    private final DomainEventPublisher eventPublisher;
    private final WmsClient wmsClient;
    private final FmsClient fmsClient;
    private final TmsClient tmsClient;

    public OrderService(OrderStore orderStore,
                        FulfillmentPlanStore fulfillmentPlanStore,
                        PlatformShipmentSyncLogStore platformShipmentSyncLogStore,
                        DomainEventPublisher eventPublisher,
                        WmsClient wmsClient,
                        FmsClient fmsClient,
                        TmsClient tmsClient) {
        this.orderStore = orderStore;
        this.fulfillmentPlanStore = fulfillmentPlanStore;
        this.platformShipmentSyncLogStore = platformShipmentSyncLogStore;
        this.eventPublisher = eventPublisher;
        this.wmsClient = wmsClient;
        this.fmsClient = fmsClient;
        this.tmsClient = tmsClient;
    }

    public SalesOrder importOrder(String tenantId, ImportOrderCommand command) {
        OriginalOrderSnapshot snapshot = saveImportSnapshot(tenantId, command, "RECEIVED", null, null);
        if (command.lines() == null || command.lines().isEmpty()) {
            throw new BizException("ORDER_LINE_REQUIRED", "Order lines are required");
        }
        SalesOrder existingOrder = orderStore.findByPlatformOrderNo(tenantId, command.platform(), command.platformOrderNo()).orElse(null);
        if (existingOrder != null) {
            saveImportSnapshot(tenantId, command, "DUPLICATED", null, existingOrder.orderId(), snapshot.snapshotId());
            throw new BizException("ORDER_DUPLICATED", "Platform order already exists");
        }
        List<OrderLine> lines = normalizeLines(command.lines());
        BigDecimal total = lines.stream()
                .map(line -> line.unitPrice().multiply(BigDecimal.valueOf(line.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal marginRate = calculateMarginRate(lines, total);
        Instant now = Instant.now();
        List<SalesOrder> recentOrders = safeOrders(orderStore.listRecentOrdersByBuyer(
                tenantId, command.platform(), command.buyerName(), now.minusSeconds(24 * 60 * 60L)));
        SalesOrder imported = orderStore.save(new SalesOrder(
                UUID.randomUUID().toString(),
                tenantId,
                null,
                command.storeId(),
                command.platform(),
                command.marketplace(),
                command.platformOrderNo(),
                command.buyerName(),
                null,
                command.countryCode(),
                command.shippingAddress(),
                command.currency(),
                total,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                OrderStatus.CREATED,
                "UNPAID",
                "UNFULFILLED",
                null,
                marginRate,
                lines,
                List.of(),
                now,
                now,
                now));
        List<OrderRiskCheck> checks = new ArrayList<>();
        checks.addAll(evaluateAuditRules(tenantId, imported, recentOrders));
        checks.addAll(performInventoryRiskChecks(tenantId, imported));
        if (checks.isEmpty()) {
            checks.add(recordRisk(tenantId, imported.orderId(), OrderRiskCheck.RiskLevel.LOW, "NONE", "Order is normal", "APPROVE"));
        }
        boolean reviewRequired = checks.stream().anyMatch(this::requiresReview);
        SalesOrder finalOrder = reviewRequired ? updateStatus(imported, OrderStatus.REVIEW_REQUIRED) : imported;
        publishEvent("erp.oms.order.created.v1", tenantId, finalOrder.orderId(), Map.of(
                "platform", command.platform(),
                "totalAmount", total.toString(),
                "status", finalOrder.status().name()));
        if (reviewRequired) {
            publishEvent("erp.oms.order.review.required.v1", tenantId, finalOrder.orderId(), Map.of(
                    "riskCount", checks.size(),
                    "buyerName", finalOrder.buyerName()));
        }
        saveImportSnapshot(tenantId, command, "IMPORTED", finalOrder.orderId(), null, snapshot.snapshotId());
        return finalOrder;
    }

    public SalesOrder markPaid(String tenantId, String orderId) {
        SalesOrder order = getOrder(tenantId, orderId);
        if (order.status() != OrderStatus.CREATED) {
            throw new BizException("ORDER_STATUS_INVALID", "Only created orders can be marked as paid");
        }
        SalesOrder paid = updateStatus(order, OrderStatus.PAID);
        createReceivableAfterPaid(tenantId, paid);
        return paid;
    }

    public SalesOrder cancel(String tenantId, String orderId) {
        SalesOrder order = getOrder(tenantId, orderId);
        if (order.status() == OrderStatus.SHIPPED || order.status() == OrderStatus.DELIVERED) {
            throw new BizException("ORDER_STATUS_INVALID", "Shipped or delivered orders cannot be cancelled");
        }
        SalesOrder cancelled = updateStatus(order, OrderStatus.CANCELLED);
        publishEvent("erp.oms.order.cancelled.v1", tenantId, orderId, Map.of("previousStatus", order.status().name()));
        return cancelled;
    }

    public SalesOrder approveReview(String tenantId, String orderId) {
        SalesOrder order = getOrder(tenantId, orderId);
        if (order.status() != OrderStatus.REVIEW_REQUIRED) {
            throw new BizException("ORDER_STATUS_INVALID", "Only review-required orders can be approved");
        }
        SalesOrder approved = updateStatus(order, OrderStatus.CREATED);
        publishEvent("erp.oms.order.review.approved.v1", tenantId, orderId, Map.of("buyerName", approved.buyerName()));
        return approved;
    }

    public SalesOrder rejectReview(String tenantId, String orderId) {
        SalesOrder order = getOrder(tenantId, orderId);
        if (order.status() != OrderStatus.REVIEW_REQUIRED) {
            throw new BizException("ORDER_STATUS_INVALID", "Only review-required orders can be rejected");
        }
        SalesOrder rejected = updateStatus(order, OrderStatus.REVIEW_REJECTED);
        publishEvent("erp.oms.order.review.rejected.v1", tenantId, orderId, Map.of("buyerName", rejected.buyerName()));
        return rejected;
    }

    public OrderFulfillmentPlan generateFulfillmentPlan(String tenantId, String orderId) {
        SalesOrder order = getOrder(tenantId, orderId);
        ensureOrderCanPlanFulfillment(order);
        OrderFulfillmentPlan existingPlan = fulfillmentPlanStore.findByOrderId(tenantId, orderId).orElse(null);
        String planId = existingPlan != null ? existingPlan.planId() : UUID.randomUUID().toString();
        Instant createdAt = existingPlan != null ? existingPlan.createdAt() : Instant.now();
        List<WarehouseStockSnapshot> warehouseSnapshots = loadWarehouseSnapshots(tenantId, order.lines());
        List<PackageDraft> packageDrafts = buildPackageDrafts(order, warehouseSnapshots);

        BigDecimal totalShippingCost = BigDecimal.ZERO;
        boolean hasInventoryWaiting = false;
        boolean hasCarrierWaiting = false;
        int readyPackageCount = 0;
        List<OrderFulfillmentPackage> packages = new ArrayList<>();
        boolean splitShipment = packageDrafts.size() > 1;

        for (PackageDraft draft : packageDrafts) {
            if (draft.inventoryWaiting()) {
                hasInventoryWaiting = true;
                packages.add(draft.toDomain(planId));
                continue;
            }
            reserveInventoryForDraft(order, draft);
            TmsClient.CarrierRecommendationResponse recommendation = recommendCarrier(order, draft, splitShipment);
            if (recommendation == null) {
                releaseInventoryForDraft(order, draft);
                draft.markWaitingCarrier("No carrier recommendation available");
                hasCarrierWaiting = true;
            } else {
                draft.assignCarrier(recommendation);
                totalShippingCost = totalShippingCost.add(recommendation.estimatedCost());
                readyPackageCount++;
            }
            packages.add(draft.toDomain(planId));
        }

        boolean partialShipment = hasInventoryWaiting;
        FulfillmentPlanStatus status;
        if (partialShipment && readyPackageCount == 0) {
            status = FulfillmentPlanStatus.EXCEPTION;
        } else if (partialShipment) {
            status = FulfillmentPlanStatus.PARTIALLY_ALLOCATED;
        } else if (hasCarrierWaiting) {
            status = FulfillmentPlanStatus.EXCEPTION;
        } else {
            status = FulfillmentPlanStatus.PLANNED;
        }

        OrderFulfillmentPlan plan = fulfillmentPlanStore.save(new OrderFulfillmentPlan(
                planId,
                tenantId,
                orderId,
                status,
                splitShipment,
                partialShipment,
                totalShippingCost.setScale(2, RoundingMode.HALF_UP),
                createdAt,
                Instant.now(),
                packages));

        publishEvent("erp.oms.fulfillment.planned.v1", tenantId, orderId, Map.of(
                "planId", plan.planId(),
                "status", plan.status().name(),
                "packageCount", plan.packages().size(),
                "splitShipment", plan.splitShipment(),
                "partialShipment", plan.partialShipment()));
        return plan;
    }

    public OrderFulfillmentPlan getCurrentFulfillmentPlan(String tenantId, String orderId) {
        getOrder(tenantId, orderId);
        return fulfillmentPlanStore.findByOrderId(tenantId, orderId)
                .orElseThrow(() -> new BizException("FULFILLMENT_PLAN_NOT_FOUND", "Fulfillment plan does not exist"));
    }

    public OrderFulfillmentPlan splitOrder(String tenantId, String orderId, SplitOrderCommand command) {
        SalesOrder order = getOrder(tenantId, orderId);
        ensureOrderCanPlanFulfillment(order);
        validateSplitOrderCommand(command);
        OrderFulfillmentPlan existingPlan = fulfillmentPlanStore.findByOrderId(tenantId, orderId).orElse(null);
        ensurePlanCanBeAdjusted(existingPlan);

        String planId = existingPlan != null ? existingPlan.planId() : UUID.randomUUID().toString();
        Instant createdAt = existingPlan != null ? existingPlan.createdAt() : Instant.now();
        Map<String, OrderLine> orderLineIndex = indexOrderLines(order);
        Map<String, Integer> allocatedQuantities = new LinkedHashMap<>();
        order.lines().forEach(line -> allocatedQuantities.put(line.lineId(), 0));

        List<OrderFulfillmentPackage> packages = new ArrayList<>();
        Set<String> usedPackageIds = new java.util.HashSet<>();
        for (SplitPackageCommand packageCommand : command.packages()) {
            packages.add(buildSplitPackage(planId, order, packageCommand, orderLineIndex, allocatedQuantities, usedPackageIds));
        }
        ensureSplitQuantitiesMatchOrder(order.lines(), allocatedQuantities);

        OrderFulfillmentPlan plan = saveManualFulfillmentPlan(
                tenantId,
                orderId,
                planId,
                createdAt,
                packages);
        publishEvent("erp.oms.fulfillment.split.v1", tenantId, orderId, Map.of(
                "planId", plan.planId(),
                "packageCount", plan.packages().size(),
                "lineCount", order.lines().size()));
        return plan;
    }

    public OrderFulfillmentPlan mergePackages(String tenantId, String orderId, MergePackagesCommand command) {
        SalesOrder order = getOrder(tenantId, orderId);
        ensureOrderCanPlanFulfillment(order);
        List<String> targetPackageIds = normalizeMergePackageIds(command);
        OrderFulfillmentPlan currentPlan = getCurrentFulfillmentPlan(tenantId, orderId);
        ensurePlanCanBeAdjusted(currentPlan);

        Map<String, OrderFulfillmentPackage> packageIndex = currentPlan.packages().stream()
                .collect(Collectors.toMap(OrderFulfillmentPackage::packageId, pkg -> pkg, (left, right) -> left, LinkedHashMap::new));
        List<OrderFulfillmentPackage> selectedPackages = targetPackageIds.stream()
                .map(packageId -> packageIndex.get(packageId))
                .peek(pkg -> {
                    if (pkg == null) {
                        throw new BizException("FULFILLMENT_PACKAGE_NOT_FOUND", "Selected package does not exist");
                    }
                    if (pkg.status() == FulfillmentPackageStatus.SHIPPED) {
                        throw new BizException("FULFILLMENT_PACKAGE_STATUS_INVALID", "Shipped packages cannot be merged");
                    }
                })
                .toList();

        OrderFulfillmentPackage mergedPackage = buildMergedPackage(currentPlan.planId(), order, selectedPackages, command.note());
        List<OrderFulfillmentPackage> packages = new ArrayList<>();
        boolean mergedInserted = false;
        for (OrderFulfillmentPackage currentPackage : currentPlan.packages()) {
            if (targetPackageIds.contains(currentPackage.packageId())) {
                if (!mergedInserted) {
                    packages.add(mergedPackage);
                    mergedInserted = true;
                }
                continue;
            }
            packages.add(currentPackage);
        }

        OrderFulfillmentPlan plan = saveManualFulfillmentPlan(
                currentPlan.tenantId(),
                currentPlan.orderId(),
                currentPlan.planId(),
                currentPlan.createdAt(),
                packages);
        publishEvent("erp.oms.fulfillment.packages.merged.v1", tenantId, orderId, Map.of(
                "planId", plan.planId(),
                "mergedPackageCount", targetPackageIds.size(),
                "remainingPackageCount", plan.packages().size()));
        return plan;
    }

    public OrderFulfillmentPlan allocateFulfillmentPlan(String tenantId, String orderId) {
        SalesOrder order = getOrder(tenantId, orderId);
        ensureOrderCanPlanFulfillment(order);
        OrderFulfillmentPlan currentPlan = getCurrentFulfillmentPlan(tenantId, orderId);
        ensurePlanCanBeAdjusted(currentPlan);

        List<WarehouseStockSnapshot> warehouseSnapshots = loadWarehouseSnapshots(tenantId, order.lines());
        boolean splitShipment = currentPlan.packages().size() > 1;
        boolean hasInventoryWaiting = false;
        boolean hasCarrierWaiting = false;
        int readyPackageCount = 0;
        BigDecimal totalShippingCost = BigDecimal.ZERO;
        List<OrderFulfillmentPackage> packages = new ArrayList<>();

        for (OrderFulfillmentPackage currentPackage : currentPlan.packages()) {
            OrderFulfillmentPackage allocatedPackage = needsAllocation(currentPackage)
                    ? allocatePackage(order, currentPackage, warehouseSnapshots, splitShipment)
                    : currentPackage;
            packages.add(allocatedPackage);

            if (allocatedPackage.estimatedShippingCost() != null) {
                totalShippingCost = totalShippingCost.add(allocatedPackage.estimatedShippingCost());
            }
            if (allocatedPackage.status() == FulfillmentPackageStatus.READY) {
                readyPackageCount++;
            } else if (allocatedPackage.status() == FulfillmentPackageStatus.WAITING_INVENTORY) {
                hasInventoryWaiting = true;
            } else if (allocatedPackage.status() == FulfillmentPackageStatus.WAITING_CARRIER) {
                hasCarrierWaiting = true;
            }
        }

        boolean partialShipment = hasInventoryWaiting;
        FulfillmentPlanStatus status;
        if (partialShipment && readyPackageCount == 0) {
            status = FulfillmentPlanStatus.EXCEPTION;
        } else if (partialShipment) {
            status = FulfillmentPlanStatus.PARTIALLY_ALLOCATED;
        } else if (hasCarrierWaiting) {
            status = FulfillmentPlanStatus.EXCEPTION;
        } else {
            status = FulfillmentPlanStatus.PLANNED;
        }

        OrderFulfillmentPlan plan = fulfillmentPlanStore.save(new OrderFulfillmentPlan(
                currentPlan.planId(),
                currentPlan.tenantId(),
                currentPlan.orderId(),
                status,
                currentPlan.packages().size() > 1,
                partialShipment,
                scaleAmount(totalShippingCost),
                currentPlan.createdAt(),
                Instant.now(),
                packages));
        publishEvent("erp.oms.fulfillment.allocated.v1", tenantId, orderId, Map.of(
                "planId", plan.planId(),
                "status", plan.status().name(),
                "packageCount", plan.packages().size(),
                "readyPackageCount", readyPackageCount));
        return plan;
    }

    public SalesOrder ship(String tenantId, String orderId) {
        SalesOrder order = getOrder(tenantId, orderId);
        if (order.status() != OrderStatus.PAID) {
            throw new BizException("ORDER_STATUS_INVALID", "Only paid orders can be shipped");
        }
        performInventoryRiskChecks(tenantId, order);
        OrderFulfillmentPlan currentPlan = getCurrentFulfillmentPlan(tenantId, orderId);
        List<OrderFulfillmentPackage> shippedPackages = executeShipment(order, currentPlan);
        SyncExecutionResult syncResult = syncShippedPackages(order, shippedPackages, false);
        syncResult.logs().forEach(platformShipmentSyncLogStore::save);
        recordFulfillmentCostEvents(order, syncResult.packages());
        OrderFulfillmentPlan savedPlan = fulfillmentPlanStore.save(new OrderFulfillmentPlan(
                currentPlan.planId(),
                currentPlan.tenantId(),
                currentPlan.orderId(),
                resolvePlanStatusAfterShipment(syncResult.packages()),
                currentPlan.splitShipment(),
                currentPlan.partialShipment(),
                currentPlan.estimatedShippingCost(),
                currentPlan.createdAt(),
                Instant.now(),
                syncResult.packages()));
        SalesOrder shipped = updateStatus(order, OrderStatus.SHIPPED);
        publishEvent("erp.oms.order.shipped.v1", tenantId, orderId, Map.of(
                "platform", order.platform(),
                "countryCode", order.countryCode(),
                "lineCount", order.lines().size(),
                "totalAmount", order.totalAmount().toString(),
                "planId", savedPlan.planId(),
                "packageCount", savedPlan.packages().size(),
                "trackingCount", savedPlan.packages().stream().filter(pkg -> pkg.trackingNo() != null).count()));
        return shipped;
    }

    public OrderFulfillmentPlan retryPlatformShipmentSync(String tenantId, String orderId) {
        SalesOrder order = getOrder(tenantId, orderId);
        OrderFulfillmentPlan currentPlan = getCurrentFulfillmentPlan(tenantId, orderId);
        boolean hasRetryTarget = currentPlan.packages().stream()
                .anyMatch(pkg -> pkg.status() == FulfillmentPackageStatus.SHIPPED
                        && pkg.platformSyncStatus() != PlatformShipmentSyncStatus.SUCCESS);
        if (!hasRetryTarget) {
            throw new BizException("PLATFORM_SHIPMENT_SYNC_NOT_REQUIRED", "No platform shipment sync retry is required");
        }
        SyncExecutionResult syncResult = syncShippedPackages(order, currentPlan.packages(), true);
        if (syncResult.logs().isEmpty()) {
            return currentPlan;
        }
        syncResult.logs().forEach(platformShipmentSyncLogStore::save);
        return fulfillmentPlanStore.save(new OrderFulfillmentPlan(
                currentPlan.planId(),
                currentPlan.tenantId(),
                currentPlan.orderId(),
                resolvePlanStatusAfterShipment(syncResult.packages()),
                currentPlan.splitShipment(),
                currentPlan.partialShipment(),
                currentPlan.estimatedShippingCost(),
                currentPlan.createdAt(),
                Instant.now(),
                syncResult.packages()));
    }

    public List<PlatformShipmentSyncLog> listPlatformShipmentSyncLogs(String tenantId, String orderId) {
        getOrder(tenantId, orderId);
        return platformShipmentSyncLogStore.listByOrderId(tenantId, orderId);
    }

    public SalesOrder deliver(String tenantId, String orderId) {
        SalesOrder order = getOrder(tenantId, orderId);
        if (order.status() != OrderStatus.SHIPPED) {
            throw new BizException("ORDER_STATUS_INVALID", "Only shipped orders can be delivered");
        }
        SalesOrder delivered = updateStatus(order, OrderStatus.DELIVERED);
        publishEvent("erp.oms.order.delivered.v1", tenantId, orderId, Map.of());
        return delivered;
    }

    public OrderRefund requestRefund(String tenantId, String orderId, String reason, BigDecimal refundAmount, OrderRefund.RefundType refundType) {
        SalesOrder order = getOrder(tenantId, orderId);
        if (order.status() != OrderStatus.PAID && order.status() != OrderStatus.SHIPPED && order.status() != OrderStatus.DELIVERED) {
            throw new BizException("ORDER_STATUS_INVALID", "Order status does not allow refund requests");
        }
        if (refundType == OrderRefund.RefundType.FULL) {
            refundAmount = order.totalAmount();
        } else {
            if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BizException("REFUND_AMOUNT_INVALID", "Refund amount must be positive");
            }
            if (refundAmount.compareTo(order.totalAmount()) > 0) {
                throw new BizException("REFUND_AMOUNT_EXCEEDS", "Refund amount exceeds order total");
            }
        }
        Instant now = Instant.now();
        OrderRefund refund = orderStore.saveRefund(new OrderRefund(
                UUID.randomUUID().toString(),
                tenantId,
                orderId,
                reason,
                refundAmount,
                refundType,
                OrderRefund.RefundStatus.REQUESTED,
                now,
                now));
        if (order.status() == OrderStatus.DELIVERED) {
            updateStatus(order, OrderStatus.RETURN_REQUESTED);
        } else {
            updateStatus(order, OrderStatus.REFUND_REQUESTED);
        }
        return refund;
    }

    public OrderRefund approveRefund(String tenantId, String refundId) {
        OrderRefund refund = getRefund(tenantId, refundId);
        if (refund.status() != OrderRefund.RefundStatus.REQUESTED) {
            throw new BizException("REFUND_STATUS_INVALID", "Only requested refunds can be approved");
        }
        return updateRefund(refund, OrderRefund.RefundStatus.APPROVED);
    }

    public OrderRefund rejectRefund(String tenantId, String refundId) {
        OrderRefund refund = getRefund(tenantId, refundId);
        if (refund.status() != OrderRefund.RefundStatus.REQUESTED) {
            throw new BizException("REFUND_STATUS_INVALID", "Only requested refunds can be rejected");
        }
        SalesOrder order = getOrder(tenantId, refund.orderId());
        if (order.status() == OrderStatus.REFUND_REQUESTED || order.status() == OrderStatus.RETURN_REQUESTED) {
            updateStatus(order, OrderStatus.PAID);
        }
        return updateRefund(refund, OrderRefund.RefundStatus.REJECTED);
    }

    public OrderRefund completeRefund(String tenantId, String refundId) {
        OrderRefund refund = getRefund(tenantId, refundId);
        if (refund.status() != OrderRefund.RefundStatus.APPROVED) {
            throw new BizException("REFUND_STATUS_INVALID", "Only approved refunds can be completed");
        }
        SalesOrder order = getOrder(tenantId, refund.orderId());
        if (refund.refundType() == OrderRefund.RefundType.FULL) {
            updateStatus(order, OrderStatus.REFUNDED);
        } else {
            updateStatus(order, OrderStatus.PARTIAL_REFUNDED);
        }
        return updateRefund(refund, OrderRefund.RefundStatus.COMPLETED);
    }

    public List<OrderRefund> listRefunds(String tenantId, String orderId) {
        return orderStore.listRefunds(tenantId, orderId);
    }

    public List<OrderRiskCheck> listRiskChecks(String tenantId, String orderId) {
        return orderStore.listRiskChecks(tenantId, orderId);
    }

    public BuyerBlacklistEntry addBuyerBlacklist(String tenantId, AddBuyerBlacklistCommand command) {
        String normalizedBuyerName = normalizeBuyerName(command.buyerName());
        boolean exists = safeBlacklist(orderStore.listBuyerBlacklist(tenantId)).stream()
                .anyMatch(entry -> normalizeBuyerName(entry.buyerName()).equals(normalizedBuyerName));
        if (exists) {
            throw new BizException("BUYER_BLACKLIST_DUPLICATED", "Buyer blacklist entry already exists");
        }
        return orderStore.saveBuyerBlacklist(new BuyerBlacklistEntry(
                UUID.randomUUID().toString(),
                tenantId,
                command.buyerName(),
                command.reason(),
                Instant.now()));
    }

    public List<BuyerBlacklistEntry> listBuyerBlacklist(String tenantId) {
        return safeBlacklist(orderStore.listBuyerBlacklist(tenantId));
    }

    public void removeBuyerBlacklist(String tenantId, String entryId) {
        orderStore.findBuyerBlacklist(tenantId, entryId)
                .orElseThrow(() -> new BizException("BUYER_BLACKLIST_NOT_FOUND", "Buyer blacklist entry does not exist"));
        orderStore.deleteBuyerBlacklist(tenantId, entryId);
    }

    public PmsRiskAlert receivePmsRiskAlert(String tenantId, ReceivePmsRiskAlertCommand command) {
        PmsRiskAlert alert = new PmsRiskAlert(
                UUID.randomUUID().toString(),
                tenantId,
                command.orderId(),
                command.riskType(),
                command.riskScore(),
                command.riskLevel(),
                command.description(),
                command.suggestedAction(),
                command.traceId(),
                command.idempotencyKey(),
                "pending",
                Instant.now());
        PmsRiskAlert saved = orderStore.savePmsRiskAlert(alert);
        if (saved.requiresReview()) {
            SalesOrder order = orderStore.find(tenantId, command.orderId()).orElse(null);
            if (order != null && order.status() != OrderStatus.REVIEW_REQUIRED) {
                updateStatus(order, OrderStatus.REVIEW_REQUIRED);
            }
            publishEvent("erp.oms.pms-risk-alert.received.v1", tenantId, saved.alertId(), Map.of(
                    "orderId", command.orderId(),
                    "riskType", command.riskType(),
                    "riskLevel", command.riskLevel(),
                    "riskScore", command.riskScore().toString()));
        }
        return saved;
    }

    public PmsRiskAlert reviewPmsRiskAlert(String tenantId, String alertId, String action, String reviewerNote) {
        PmsRiskAlert alert = orderStore.findPmsRiskAlert(tenantId, alertId)
                .orElseThrow(() -> new BizException("PMS_RISK_ALERT_NOT_FOUND", "PMS risk alert does not exist"));
        if (!alert.isPending()) {
            throw new BizException("PMS_RISK_ALERT_NOT_PENDING", "Only pending alerts can be reviewed");
        }
        String newStatus = "approved".equals(action) ? "approved" : "rejected";
        orderStore.updatePmsRiskAlertStatus(alertId, newStatus);
        if ("approved".equals(action) && alert.orderId() != null) {
            SalesOrder order = orderStore.find(tenantId, alert.orderId()).orElse(null);
            if (order != null) {
                SalesOrder updated = orderStore.save(new SalesOrder(
                        order.orderId(), order.tenantId(), order.listingId(), order.storeId(),
                        order.platform(), order.marketplace(), order.platformOrderNo(), order.buyerName(),
                        order.customerId(), order.countryCode(), order.shippingAddress(),
                        order.currency(), order.totalAmount(), order.taxAmount(),
                        order.shippingAmount(), order.discountAmount(), order.status(),
                        order.paymentStatus(), order.fulfillmentStatus(), alert.riskLevel().toLowerCase(),
                        order.profitMargin(), order.lines(), order.promotions(),
                        order.orderDate(), order.createdAt(), Instant.now()));
                publishEvent("erp.oms.order.risk-level-updated.v1", tenantId, order.orderId(), Map.of(
                        "riskLevel", alert.riskLevel(),
                        "alertId", alertId));
            }
        }
        publishEvent("erp.oms.pms-risk-alert.reviewed.v1", tenantId, alertId, Map.of(
                "action", action,
                "riskLevel", alert.riskLevel()));
        return orderStore.findPmsRiskAlert(tenantId, alertId)
                .orElseThrow(() -> new BizException("PMS_RISK_ALERT_NOT_FOUND", "PMS risk alert not found after update"));
    }

    public List<PmsRiskAlert> listPmsRiskAlerts(String tenantId, String orderId) {
        if (orderId != null && !orderId.isBlank()) {
            return orderStore.listPmsRiskAlertsByOrder(tenantId, orderId);
        }
        return orderStore.listPendingPmsRiskAlerts(tenantId);
    }

    public OrderSyncLog syncOrders(String tenantId, SyncOrdersCommand command) {
        Instant startedAt = Instant.now();
        OrderSyncLog syncLog = new OrderSyncLog(
                UUID.randomUUID().toString(),
                tenantId,
                command.platform(),
                command.syncType(),
                OrderSyncLog.SyncStatus.RUNNING.name(),
                0,
                0,
                null,
                startedAt,
                null);
        orderStore.saveOrderSyncLog(syncLog);
        publishEvent("erp.oms.order-sync.started.v1", tenantId, syncLog.syncId(), Map.of(
                "platform", command.platform(),
                "syncType", command.syncType()));
        int syncedCount = 0;
        int failedCount = 0;
        String errorMessage = null;
        try {
            List<SalesOrder> existingOrders = orderStore.list(tenantId);
            syncedCount = existingOrders.size();
        } catch (Exception ex) {
            failedCount = 1;
            errorMessage = ex.getMessage();
            log.warn("Order sync failed. tenantId={}, platform={}", tenantId, command.platform(), ex);
        }
        Instant completedAt = Instant.now();
        String finalStatus = failedCount > 0 && syncedCount == 0
                ? OrderSyncLog.SyncStatus.FAILED.name()
                : OrderSyncLog.SyncStatus.COMPLETED.name();
        OrderSyncLog completed = new OrderSyncLog(
                syncLog.syncId(),
                tenantId,
                command.platform(),
                command.syncType(),
                finalStatus,
                syncedCount,
                failedCount,
                errorMessage,
                startedAt,
                completedAt);
        orderStore.saveOrderSyncLog(completed);
        publishEvent("erp.oms.order-sync.completed.v1", tenantId, syncLog.syncId(), Map.of(
                "platform", command.platform(),
                "status", finalStatus,
                "syncedCount", syncedCount,
                "failedCount", failedCount));
        return completed;
    }

    public List<OrderSyncLog> listSyncLogs(String tenantId, String platform) {
        return orderStore.listOrderSyncLogs(tenantId, platform);
    }

    public Promotion applyPromotion(String tenantId, String orderId, ApplyPromotionCommand command) {
        SalesOrder order = getOrder(tenantId, orderId);
        if (order.status() == OrderStatus.CANCELLED || order.status() == OrderStatus.REFUNDED) {
            throw new BizException("ORDER_STATUS_INVALID", "Cannot apply promotion to cancelled or refunded orders");
        }
        Promotion promotion = new Promotion(
                UUID.randomUUID().toString(),
                tenantId,
                orderId,
                command.promoType(),
                command.promoCode(),
                command.discount(),
                command.description(),
                Instant.now());
        Promotion saved = orderStore.savePromotion(promotion);
        BigDecimal newDiscount = order.discountAmount().add(command.discount());
        orderStore.save(new SalesOrder(
                order.orderId(), order.tenantId(), order.listingId(), order.storeId(),
                order.platform(), order.marketplace(), order.platformOrderNo(), order.buyerName(),
                order.customerId(), order.countryCode(), order.shippingAddress(),
                order.currency(), order.totalAmount(), order.taxAmount(),
                order.shippingAmount(), newDiscount, order.status(),
                order.paymentStatus(), order.fulfillmentStatus(), order.riskLevel(),
                order.profitMargin(), order.lines(),
                orderStore.listPromotions(tenantId, orderId),
                order.orderDate(), order.createdAt(), Instant.now()));
        publishEvent("erp.oms.promotion.applied.v1", tenantId, orderId, Map.of(
                "promoId", saved.promoId(),
                "promoType", command.promoType(),
                "discount", command.discount().toString()));
        return saved;
    }

    public List<Promotion> listPromotions(String tenantId, String orderId) {
        return orderStore.listPromotions(tenantId, orderId);
    }

    public OrderStrategy createOrderStrategy(String tenantId, CreateOrderStrategyCommand command) {
        OrderStrategy strategy = new OrderStrategy(
                UUID.randomUUID().toString(),
                tenantId,
                command.strategyType(),
                command.name(),
                command.description(),
                command.rules(),
                command.enabled(),
                command.priority(),
                Instant.now(),
                Instant.now());
        OrderStrategy saved = orderStore.saveOrderStrategy(strategy);
        publishEvent("erp.oms.order-strategy.created.v1", tenantId, saved.strategyId(), Map.of(
                "strategyType", command.strategyType(),
                "name", command.name()));
        return saved;
    }

    public OrderStrategy updateOrderStrategy(String tenantId, String strategyId, UpdateOrderStrategyCommand command) {
        OrderStrategy existing = orderStore.findOrderStrategy(tenantId, strategyId)
                .orElseThrow(() -> new BizException("ORDER_STRATEGY_NOT_FOUND", "Order strategy does not exist"));
        OrderStrategy updated = new OrderStrategy(
                existing.strategyId(),
                existing.tenantId(),
                command.strategyType() != null ? command.strategyType() : existing.strategyType(),
                command.name() != null ? command.name() : existing.name(),
                command.description() != null ? command.description() : existing.description(),
                command.rules() != null ? command.rules() : existing.rules(),
                command.enabled() != null ? command.enabled() : existing.enabled(),
                command.priority() != null ? command.priority() : existing.priority(),
                existing.createdAt(),
                Instant.now());
        OrderStrategy saved = orderStore.saveOrderStrategy(updated);
        publishEvent("erp.oms.order-strategy.updated.v1", tenantId, saved.strategyId(), Map.of(
                "strategyType", saved.strategyType(),
                "name", saved.name()));
        return saved;
    }

    public OrderStrategy getOrderStrategy(String tenantId, String strategyId) {
        return orderStore.findOrderStrategy(tenantId, strategyId)
                .orElseThrow(() -> new BizException("ORDER_STRATEGY_NOT_FOUND", "Order strategy does not exist"));
    }

    public List<OrderStrategy> listOrderStrategies(String tenantId, String strategyType) {
        return orderStore.listOrderStrategies(tenantId, strategyType);
    }

    public List<SalesOrder> list(String tenantId) {
        return orderStore.list(tenantId);
    }

    public SalesOrder getOrder(String tenantId, String orderId) {
        return orderStore.find(tenantId, orderId)
                .orElseThrow(() -> new BizException("ORDER_NOT_FOUND", "Order does not exist"));
    }

    private void ensureOrderCanPlanFulfillment(SalesOrder order) {
        if (order.status() == OrderStatus.REVIEW_REQUIRED || order.status() == OrderStatus.REVIEW_REJECTED) {
            throw new BizException("ORDER_STATUS_INVALID", "Order review must be completed before fulfillment planning");
        }
        if (order.status() == OrderStatus.CANCELLED || order.status() == OrderStatus.REFUNDED) {
            throw new BizException("ORDER_STATUS_INVALID", "Cancelled or refunded orders cannot be planned");
        }
        if (order.status() == OrderStatus.SHIPPED || order.status() == OrderStatus.DELIVERED) {
            throw new BizException("ORDER_STATUS_INVALID", "Shipped orders do not need a new fulfillment plan");
        }
    }

    private void ensurePlanCanBeAdjusted(OrderFulfillmentPlan plan) {
        if (plan == null) {
            return;
        }
        boolean hasShippedPackage = plan.packages().stream()
                .anyMatch(pkg -> pkg.status() == FulfillmentPackageStatus.SHIPPED);
        if (hasShippedPackage) {
            throw new BizException("FULFILLMENT_PLAN_STATUS_INVALID", "Fulfillment packages already shipped cannot be manually adjusted");
        }
    }

    private void validateSplitOrderCommand(SplitOrderCommand command) {
        if (command == null || command.packages() == null || command.packages().isEmpty()) {
            throw new BizException("ORDER_SPLIT_PACKAGE_REQUIRED", "Split packages are required");
        }
    }

    private List<String> normalizeMergePackageIds(MergePackagesCommand command) {
        if (command == null || command.packageIds() == null || command.packageIds().isEmpty()) {
            throw new BizException("FULFILLMENT_PACKAGE_REQUIRED", "Packages to merge are required");
        }
        List<String> packageIds = command.packageIds().stream()
                .filter(packageId -> packageId != null && !packageId.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
        if (packageIds.size() < 2) {
            throw new BizException("FULFILLMENT_PACKAGE_MERGE_INVALID", "At least two distinct packages are required");
        }
        return packageIds;
    }

    private Map<String, OrderLine> indexOrderLines(SalesOrder order) {
        return order.lines().stream()
                .collect(Collectors.toMap(OrderLine::lineId, line -> line, (left, right) -> left, LinkedHashMap::new));
    }

    private OrderFulfillmentPackage buildSplitPackage(String planId,
                                                      SalesOrder order,
                                                      SplitPackageCommand packageCommand,
                                                      Map<String, OrderLine> orderLineIndex,
                                                      Map<String, Integer> allocatedQuantities,
                                                      Set<String> usedPackageIds) {
        if (packageCommand == null || packageCommand.allocations() == null || packageCommand.allocations().isEmpty()) {
            throw new BizException("ORDER_SPLIT_PACKAGE_REQUIRED", "Split package allocations are required");
        }

        Map<String, Integer> packageQuantities = new LinkedHashMap<>();
        for (SplitLineAllocationCommand allocation : packageCommand.allocations()) {
            if (allocation == null || allocation.lineId() == null || allocation.lineId().isBlank()) {
                throw new BizException("ORDER_SPLIT_LINE_REQUIRED", "Split line id is required");
            }
            if (allocation.quantity() <= 0) {
                throw new BizException("ORDER_SPLIT_QUANTITY_INVALID", "Split quantity must be positive");
            }
            OrderLine orderLine = orderLineIndex.get(allocation.lineId());
            if (orderLine == null) {
                throw new BizException("ORDER_SPLIT_LINE_NOT_FOUND", "Split line does not exist");
            }
            int nextAllocated = allocatedQuantities.getOrDefault(orderLine.lineId(), 0) + allocation.quantity();
            if (nextAllocated > orderLine.quantity()) {
                throw new BizException("ORDER_SPLIT_QUANTITY_MISMATCH",
                        "Split quantity exceeds original order line quantity for line " + orderLine.lineId());
            }
            packageQuantities.merge(orderLine.lineId(), allocation.quantity(), Integer::sum);
            allocatedQuantities.put(orderLine.lineId(), nextAllocated);
        }

        List<OrderFulfillmentPackageLine> lines = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalQuantity = 0;
        for (Map.Entry<String, Integer> entry : packageQuantities.entrySet()) {
            OrderLine orderLine = orderLineIndex.get(entry.getKey());
            int quantity = entry.getValue();
            BigDecimal lineAmount = calculatePackageLineAmount(orderLine.unitPrice(), quantity);
            lines.add(new OrderFulfillmentPackageLine(
                    UUID.randomUUID().toString(),
                    orderLine.lineId(),
                    orderLine.sellerSku(),
                    orderLine.title(),
                    quantity,
                    orderLine.unitPrice(),
                    lineAmount));
            totalQuantity += quantity;
            totalAmount = totalAmount.add(lineAmount);
        }

        return new OrderFulfillmentPackage(
                resolvePackageId(packageCommand.packageId(), usedPackageIds),
                planId,
                null,
                null,
                null,
                null,
                null,
                order.countryCode(),
                null,
                FulfillmentPackageStatus.WAITING_INVENTORY,
                totalQuantity,
                scaleAmount(totalAmount),
                null,
                null,
                packageCommand.note(),
                null,
                null,
                null,
                PlatformShipmentSyncStatus.NOT_SYNCED,
                0,
                null,
                null,
                List.copyOf(lines));
    }

    private OrderFulfillmentPackage buildMergedPackage(String planId,
                                                       SalesOrder order,
                                                       List<OrderFulfillmentPackage> selectedPackages,
                                                       String note) {
        Map<String, OrderLine> orderLineIndex = indexOrderLines(order);
        Map<String, Integer> mergedQuantities = new LinkedHashMap<>();

        for (OrderFulfillmentPackage fulfillmentPackage : selectedPackages) {
            for (OrderFulfillmentPackageLine line : fulfillmentPackage.lines()) {
                OrderLine orderLine = orderLineIndex.get(line.orderLineId());
                if (orderLine == null) {
                    throw new BizException("ORDER_LINE_NOT_FOUND", "Order line referenced by fulfillment package does not exist");
                }
                int mergedQuantity = mergedQuantities.getOrDefault(orderLine.lineId(), 0) + line.quantity();
                if (mergedQuantity > orderLine.quantity()) {
                    throw new BizException("FULFILLMENT_PACKAGE_MERGE_INVALID",
                            "Merged quantity exceeds original order line quantity for line " + orderLine.lineId());
                }
                mergedQuantities.put(orderLine.lineId(), mergedQuantity);
            }
        }

        List<OrderFulfillmentPackageLine> mergedLines = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalQuantity = 0;
        for (Map.Entry<String, Integer> entry : mergedQuantities.entrySet()) {
            OrderLine orderLine = orderLineIndex.get(entry.getKey());
            int quantity = entry.getValue();
            BigDecimal lineAmount = calculatePackageLineAmount(orderLine.unitPrice(), quantity);
            mergedLines.add(new OrderFulfillmentPackageLine(
                    UUID.randomUUID().toString(),
                    orderLine.lineId(),
                    orderLine.sellerSku(),
                    orderLine.title(),
                    quantity,
                    orderLine.unitPrice(),
                    lineAmount));
            totalQuantity += quantity;
            totalAmount = totalAmount.add(lineAmount);
        }

        return new OrderFulfillmentPackage(
                UUID.randomUUID().toString(),
                planId,
                null,
                null,
                null,
                null,
                null,
                order.countryCode(),
                null,
                FulfillmentPackageStatus.WAITING_INVENTORY,
                totalQuantity,
                scaleAmount(totalAmount),
                null,
                null,
                buildMergedPackageNote(note, selectedPackages),
                null,
                null,
                null,
                PlatformShipmentSyncStatus.NOT_SYNCED,
                0,
                null,
                null,
                List.copyOf(mergedLines));
    }

    private void ensureSplitQuantitiesMatchOrder(List<OrderLine> orderLines, Map<String, Integer> allocatedQuantities) {
        for (OrderLine orderLine : orderLines) {
            int allocated = allocatedQuantities.getOrDefault(orderLine.lineId(), 0);
            if (allocated != orderLine.quantity()) {
                throw new BizException("ORDER_SPLIT_QUANTITY_MISMATCH",
                        "Split quantity does not match original order line quantity for line " + orderLine.lineId());
            }
        }
    }

    private OrderFulfillmentPlan saveManualFulfillmentPlan(String tenantId,
                                                           String orderId,
                                                           String planId,
                                                           Instant createdAt,
                                                           List<OrderFulfillmentPackage> packages) {
        BigDecimal estimatedShippingCost = packages.stream()
                .map(pkg -> pkg.estimatedShippingCost() == null ? BigDecimal.ZERO : pkg.estimatedShippingCost())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return fulfillmentPlanStore.save(new OrderFulfillmentPlan(
                planId,
                tenantId,
                orderId,
                FulfillmentPlanStatus.PLANNED,
                packages.size() > 1,
                false,
                scaleAmount(estimatedShippingCost),
                createdAt,
                Instant.now(),
                List.copyOf(packages)));
    }

    private boolean needsAllocation(OrderFulfillmentPackage fulfillmentPackage) {
        return fulfillmentPackage.status() != FulfillmentPackageStatus.READY
                || fulfillmentPackage.warehouseId() == null
                || fulfillmentPackage.warehouseId().isBlank()
                || fulfillmentPackage.carrierId() == null
                || fulfillmentPackage.carrierId().isBlank();
    }

    private OrderFulfillmentPackage allocatePackage(SalesOrder order,
                                                    OrderFulfillmentPackage currentPackage,
                                                    List<WarehouseStockSnapshot> warehouseSnapshots,
                                                    boolean splitShipment) {
        WarehouseStockSnapshot warehouse = findWarehouseForPackage(currentPackage, warehouseSnapshots);
        if (warehouse == null) {
            return rebuildPackage(
                    currentPackage,
                    null,
                    null,
                    FulfillmentPackageStatus.WAITING_INVENTORY,
                    composePackageNote(currentPackage.note(), "No warehouse has enough inventory for the package"));
        }

        reserveInventory(warehouse.warehouseId(), currentPackage.lines(),
                "OMS_FULFILLMENT_PACKAGE", currentPackage.packageId(), "OMS fulfillment package reserve");
        TmsClient.CarrierRecommendationResponse recommendation = recommendCarrier(
                currentPackage.destinationCountry() == null || currentPackage.destinationCountry().isBlank()
                        ? order.countryCode()
                        : currentPackage.destinationCountry(),
                warehouse.countryCode(),
                currentPackage.totalQuantity(),
                currentPackage.totalAmount(),
                splitShipment);
        if (recommendation == null) {
            releaseInventory(warehouse.warehouseId(), currentPackage.lines(),
                    "OMS_FULFILLMENT_PACKAGE", currentPackage.packageId(), "OMS fulfillment package release");
            return rebuildPackage(
                    currentPackage,
                    warehouse,
                    null,
                    FulfillmentPackageStatus.WAITING_CARRIER,
                    composePackageNote(currentPackage.note(), "No carrier recommendation available"));
        }

        consumeWarehouseInventory(warehouse, currentPackage.lines());
        return rebuildPackage(
                currentPackage,
                warehouse,
                recommendation,
                FulfillmentPackageStatus.READY,
                currentPackage.note());
    }

    private WarehouseStockSnapshot findWarehouseForPackage(OrderFulfillmentPackage fulfillmentPackage,
                                                           List<WarehouseStockSnapshot> warehouseSnapshots) {
        String destinationCountry = fulfillmentPackage.destinationCountry() == null || fulfillmentPackage.destinationCountry().isBlank()
                ? ""
                : fulfillmentPackage.destinationCountry();
        return warehouseSnapshots.stream()
                .filter(snapshot -> fulfillmentPackage.lines().stream()
                        .allMatch(line -> snapshot.available(line.sellerSku()) >= line.quantity()))
                .sorted(compareWarehouseForPackage(destinationCountry, fulfillmentPackage.lines()))
                .findFirst()
                .orElse(null);
    }

    private Comparator<WarehouseStockSnapshot> compareWarehouseForPackage(String destinationCountry,
                                                                          List<OrderFulfillmentPackageLine> lines) {
        return Comparator.<WarehouseStockSnapshot>comparingInt(snapshot -> destinationCountry.equalsIgnoreCase(snapshot.countryCode()) ? 1 : 0)
                .reversed()
                .thenComparing(Comparator.comparingInt((WarehouseStockSnapshot snapshot) -> totalAvailableForPackage(snapshot, lines)).reversed())
                .thenComparing(WarehouseStockSnapshot::warehouseCode);
    }

    private int totalAvailableForPackage(WarehouseStockSnapshot snapshot, List<OrderFulfillmentPackageLine> lines) {
        return lines.stream().mapToInt(line -> snapshot.available(line.sellerSku())).sum();
    }

    private void consumeWarehouseInventory(WarehouseStockSnapshot warehouse,
                                           List<OrderFulfillmentPackageLine> lines) {
        for (OrderFulfillmentPackageLine line : lines) {
            warehouse.consume(line.sellerSku(), line.quantity());
        }
    }

    private OrderFulfillmentPackage rebuildPackage(OrderFulfillmentPackage source,
                                                   WarehouseStockSnapshot warehouse,
                                                   TmsClient.CarrierRecommendationResponse recommendation,
                                                   FulfillmentPackageStatus status,
                                                   String note) {
        return new OrderFulfillmentPackage(
                source.packageId(),
                source.planId(),
                warehouse == null ? null : warehouse.warehouseId(),
                warehouse == null ? null : warehouse.warehouseCode(),
                recommendation == null ? null : recommendation.carrierId(),
                recommendation == null ? null : recommendation.carrierCode(),
                recommendation == null ? null : recommendation.carrierName(),
                source.destinationCountry(),
                recommendation == null ? null : recommendation.serviceLevel(),
                status,
                source.totalQuantity(),
                source.totalAmount(),
                recommendation == null || recommendation.estimatedCost() == null ? null : scaleAmount(recommendation.estimatedCost()),
                recommendation == null ? null : recommendation.estimatedDeliveryDays(),
                note,
                null,
                null,
                null,
                PlatformShipmentSyncStatus.NOT_SYNCED,
                0,
                null,
                null,
                source.lines());
    }

    private String composePackageNote(String existingNote, String newNote) {
        if (existingNote == null || existingNote.isBlank()) {
            return newNote;
        }
        return existingNote + "; " + newNote;
    }

    private String resolvePackageId(String preferredPackageId, Set<String> usedPackageIds) {
        String packageId = preferredPackageId == null || preferredPackageId.isBlank()
                ? UUID.randomUUID().toString()
                : preferredPackageId.trim();
        if (!usedPackageIds.add(packageId)) {
            throw new BizException("ORDER_SPLIT_PACKAGE_DUPLICATED", "Split package id duplicated");
        }
        return packageId;
    }

    private String buildMergedPackageNote(String note, List<OrderFulfillmentPackage> selectedPackages) {
        String sourcePackages = selectedPackages.stream()
                .map(OrderFulfillmentPackage::packageId)
                .collect(Collectors.joining(","));
        if (note == null || note.isBlank()) {
            return "manual merge from " + sourcePackages;
        }
        return "manual merge from " + sourcePackages + " - " + note.trim();
    }

    private BigDecimal calculatePackageLineAmount(BigDecimal unitPrice, int quantity) {
        return scaleAmount(unitPrice.multiply(BigDecimal.valueOf(quantity)));
    }

    private BigDecimal scaleAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private List<PackageDraft> buildPackageDrafts(SalesOrder order, List<WarehouseStockSnapshot> warehouseSnapshots) {
        Map<String, PackageDraft> realPackages = new LinkedHashMap<>();
        PackageDraft shortagePackage = null;
        String singleWarehouseId = findSingleWarehouse(order, warehouseSnapshots);
        if (singleWarehouseId != null) {
            WarehouseStockSnapshot snapshot = warehouseSnapshots.stream()
                    .filter(candidate -> candidate.warehouseId().equals(singleWarehouseId))
                    .findFirst()
                    .orElseThrow();
            PackageDraft draft = new PackageDraft(snapshot, order.countryCode());
            for (OrderLine line : order.lines()) {
                draft.addLine(line, line.quantity());
            }
            return List.of(draft);
        }

        for (OrderLine line : order.lines()) {
            int remaining = line.quantity();
            List<WarehouseStockSnapshot> candidates = warehouseSnapshots.stream()
                    .filter(snapshot -> snapshot.available(line.sellerSku()) > 0)
                    .sorted(compareWarehouse(order.countryCode(), line.sellerSku()))
                    .toList();
            for (WarehouseStockSnapshot candidate : candidates) {
                if (remaining <= 0) {
                    break;
                }
                int allocated = Math.min(remaining, candidate.available(line.sellerSku()));
                if (allocated <= 0) {
                    continue;
                }
                candidate.consume(line.sellerSku(), allocated);
                PackageDraft draft = realPackages.computeIfAbsent(candidate.warehouseId(),
                        key -> new PackageDraft(candidate, order.countryCode()));
                draft.addLine(line, allocated);
                remaining -= allocated;
            }
            if (remaining > 0) {
                if (shortagePackage == null) {
                    shortagePackage = PackageDraft.inventoryWaiting(order.countryCode(), "Inventory shortage, backorder package generated");
                }
                shortagePackage.addLine(line, remaining);
            }
        }

        List<PackageDraft> result = new ArrayList<>(realPackages.values());
        if (shortagePackage != null) {
            result.add(shortagePackage);
        }
        return result;
    }

    private Comparator<WarehouseStockSnapshot> compareWarehouse(String destinationCountry, String sellerSku) {
        return Comparator.<WarehouseStockSnapshot>comparingInt(snapshot -> destinationCountry.equalsIgnoreCase(snapshot.countryCode()) ? 1 : 0)
                .reversed()
                .thenComparing(Comparator.comparingInt((WarehouseStockSnapshot snapshot) -> snapshot.available(sellerSku)).reversed())
                .thenComparing(WarehouseStockSnapshot::warehouseCode);
    }

    private String findSingleWarehouse(SalesOrder order, List<WarehouseStockSnapshot> warehouseSnapshots) {
        return warehouseSnapshots.stream()
                .filter(snapshot -> order.lines().stream().allMatch(line -> snapshot.available(line.sellerSku()) >= line.quantity()))
                .sorted(Comparator.<WarehouseStockSnapshot>comparingInt(snapshot -> order.countryCode().equalsIgnoreCase(snapshot.countryCode()) ? 1 : 0)
                        .reversed()
                        .thenComparing(WarehouseStockSnapshot::warehouseCode))
                .map(WarehouseStockSnapshot::warehouseId)
                .findFirst()
                .orElse(null);
    }

    private List<WarehouseStockSnapshot> loadWarehouseSnapshots(String tenantId, List<OrderLine> lines) {
        Set<String> targetSkus = lines.stream().map(OrderLine::sellerSku).collect(Collectors.toSet());
        List<WmsClient.WarehouseResponse> warehouses = safeList(wmsClient.listWarehouses());
        List<WarehouseStockSnapshot> snapshots = new ArrayList<>();
        for (WmsClient.WarehouseResponse warehouse : warehouses) {
            Map<String, Integer> availability = new LinkedHashMap<>();
            for (WmsClient.InventoryBalanceResponse balance : safeList(wmsClient.listBalances(warehouse.warehouseId()))) {
                if (targetSkus.contains(balance.sellerSku())) {
                    availability.put(balance.sellerSku(), Math.max(balance.available(), 0));
                }
            }
            snapshots.add(new WarehouseStockSnapshot(
                    warehouse.warehouseId(),
                    warehouse.code(),
                    warehouse.countryCode(),
                    availability));
        }
        return snapshots;
    }

    private TmsClient.CarrierRecommendationResponse recommendCarrier(SalesOrder order, PackageDraft draft, boolean splitShipment) {
        if (draft.inventoryWaiting()) {
            return null;
        }
        return recommendCarrier(order.countryCode(), draft.warehouseCountry(), draft.totalQuantity(), draft.totalAmount(), splitShipment);
    }

    private TmsClient.CarrierRecommendationResponse recommendCarrier(String destinationCountry,
                                                                     String warehouseCountry,
                                                                     int packageQuantity,
                                                                     BigDecimal packageAmount,
                                                                     boolean splitShipment) {
        try {
            Result<List<TmsClient.CarrierRecommendationResponse>> result = tmsClient.recommendCarriers(
                    new TmsClient.RecommendCarrierRequest(
                            destinationCountry,
                            warehouseCountry,
                            packageQuantity,
                            packageAmount,
                            splitShipment));
            List<TmsClient.CarrierRecommendationResponse> recommendations = result != null && result.data() != null
                    ? result.data()
                    : List.of();
            return recommendations.stream()
                    .sorted(Comparator.comparingInt(TmsClient.CarrierRecommendationResponse::recommendationScore)
                            .reversed()
                            .thenComparing(TmsClient.CarrierRecommendationResponse::estimatedCost, Comparator.nullsLast(BigDecimal::compareTo))
                            .thenComparingInt(TmsClient.CarrierRecommendationResponse::estimatedDeliveryDays))
                    .findFirst()
                    .orElse(null);
        } catch (Exception ex) {
            return null;
        }
    }

    private <T> List<T> safeList(Result<List<T>> result) {
        return result == null || result.data() == null ? List.of() : result.data();
    }

    private void reserveInventoryForDraft(SalesOrder order, PackageDraft draft) {
        reserveInventory(draft.warehouseId(), draft.lines(), "OMS_FULFILLMENT_PLAN", order.orderId(), "OMS fulfillment plan reserve");
    }

    private void releaseInventoryForDraft(SalesOrder order, PackageDraft draft) {
        releaseInventory(draft.warehouseId(), draft.lines(), "OMS_FULFILLMENT_PLAN", order.orderId(), "OMS fulfillment plan release");
    }

    private void reserveInventory(String warehouseId,
                                  List<OrderFulfillmentPackageLine> lines,
                                  String referenceType,
                                  String referenceId,
                                  String remark) {
        for (OrderFulfillmentPackageLine line : lines) {
            wmsClient.reserve(new WmsClient.StockRequest(
                    warehouseId,
                    line.sellerSku(),
                    line.quantity(),
                    referenceType,
                    referenceId,
                    remark));
        }
    }

    private void releaseInventory(String warehouseId,
                                  List<OrderFulfillmentPackageLine> lines,
                                  String referenceType,
                                  String referenceId,
                                  String remark) {
        for (OrderFulfillmentPackageLine line : lines) {
            wmsClient.release(new WmsClient.StockRequest(
                    warehouseId,
                    line.sellerSku(),
                    line.quantity(),
                    referenceType,
                    referenceId,
                    remark));
        }
    }

    private List<OrderFulfillmentPackage> executeShipment(SalesOrder order, OrderFulfillmentPlan plan) {
        List<OrderFulfillmentPackage> shippedPackages = new ArrayList<>();
        boolean shippedAnyPackage = false;
        for (OrderFulfillmentPackage fulfillmentPackage : plan.packages()) {
            if (fulfillmentPackage.status() != FulfillmentPackageStatus.READY) {
                shippedPackages.add(fulfillmentPackage);
                continue;
            }
            shippedAnyPackage = true;
            shippedPackages.add(shipPackage(order, fulfillmentPackage));
        }
        if (!shippedAnyPackage) {
            throw new BizException("FULFILLMENT_PACKAGE_NOT_READY", "No fulfillment package is ready to ship");
        }
        return shippedPackages;
    }

    private OrderFulfillmentPackage shipPackage(SalesOrder order, OrderFulfillmentPackage fulfillmentPackage) {
        if (fulfillmentPackage.warehouseId() == null || fulfillmentPackage.warehouseId().isBlank()) {
            throw new BizException("FULFILLMENT_WAREHOUSE_REQUIRED", "Fulfillment package warehouse is required");
        }
        if (fulfillmentPackage.carrierId() == null || fulfillmentPackage.carrierId().isBlank()) {
            throw new BizException("FULFILLMENT_CARRIER_REQUIRED", "Fulfillment package carrier is required");
        }
        for (OrderFulfillmentPackageLine line : fulfillmentPackage.lines()) {
            WmsClient.StockRequest request = new WmsClient.StockRequest(
                    fulfillmentPackage.warehouseId(),
                    line.sellerSku(),
                    line.quantity(),
                    "OMS_ORDER",
                    order.orderId(),
                    "OMS shipment execution");
            wmsClient.deduct(request);
        }
        String trackingNo = generateTrackingNo(order, fulfillmentPackage);
        TmsClient.ShipmentResponse shipment = requireData(
                tmsClient.createShipment(new TmsClient.CreateShipmentRequest(
                        order.orderId(),
                        fulfillmentPackage.carrierId(),
                        trackingNo,
                        order.countryCode())),
                "TMS_SHIPMENT_CREATE_FAILED",
                "TMS shipment create failed");
        try {
            tmsClient.addTracking(
                    shipment.shipmentId(),
                    new TmsClient.AddTrackingRequest(
                            "SHIPPED",
                            fulfillmentPackage.warehouseCode() != null ? fulfillmentPackage.warehouseCode() : fulfillmentPackage.warehouseId(),
                            "Shipment confirmed by OMS"));
        } catch (Exception ex) {
            log.warn("Failed to add TMS tracking event for shipment {}", shipment.shipmentId(), ex);
        }
        Instant shippedAt = Instant.now();
        return copyPackage(
                fulfillmentPackage,
                FulfillmentPackageStatus.SHIPPED,
                shipment.shipmentId(),
                trackingNo,
                shippedAt,
                PlatformShipmentSyncStatus.NOT_SYNCED,
                fulfillmentPackage.platformSyncAttempts(),
                null,
                null);
    }

    private SyncExecutionResult syncShippedPackages(SalesOrder order,
                                                    List<OrderFulfillmentPackage> packages,
                                                    boolean retryOnlyNonSuccess) {
        List<OrderFulfillmentPackage> updatedPackages = new ArrayList<>();
        List<PlatformShipmentSyncLog> logs = new ArrayList<>();
        for (OrderFulfillmentPackage fulfillmentPackage : packages) {
            if (fulfillmentPackage.status() != FulfillmentPackageStatus.SHIPPED) {
                updatedPackages.add(fulfillmentPackage);
                continue;
            }
            boolean shouldSync = retryOnlyNonSuccess
                    ? fulfillmentPackage.platformSyncStatus() != PlatformShipmentSyncStatus.SUCCESS
                    : fulfillmentPackage.platformSyncStatus() == PlatformShipmentSyncStatus.NOT_SYNCED;
            if (!shouldSync) {
                updatedPackages.add(fulfillmentPackage);
                continue;
            }
            PlatformSyncAttemptResult attemptResult = attemptPlatformShipmentSync(order, fulfillmentPackage);
            updatedPackages.add(attemptResult.updatedPackage());
            logs.add(attemptResult.log());
        }
        return new SyncExecutionResult(List.copyOf(updatedPackages), List.copyOf(logs));
    }

    private PlatformSyncAttemptResult attemptPlatformShipmentSync(SalesOrder order, OrderFulfillmentPackage fulfillmentPackage) {
        int attemptNo = fulfillmentPackage.platformSyncAttempts() + 1;
        Instant syncedAt = Instant.now();
        String platform = normalizePlatform(order.platform());
        String errorMessage = null;
        PlatformShipmentSyncStatus status = PlatformShipmentSyncStatus.SUCCESS;
        if (order.platformOrderNo() == null || order.platformOrderNo().isBlank()) {
            status = PlatformShipmentSyncStatus.FAILED;
            errorMessage = "Platform order number is missing";
        } else if (fulfillmentPackage.trackingNo() == null || fulfillmentPackage.trackingNo().isBlank()) {
            status = PlatformShipmentSyncStatus.FAILED;
            errorMessage = "Tracking number is missing";
        } else if (!SUPPORTED_PLATFORM_SHIPMENT_SYNC.contains(platform)) {
            status = PlatformShipmentSyncStatus.FAILED;
            errorMessage = "Platform shipment sync is not supported yet for " + order.platform();
        }
        PlatformShipmentSyncLog log = new PlatformShipmentSyncLog(
                UUID.randomUUID().toString(),
                order.tenantId(),
                order.orderId(),
                fulfillmentPackage.packageId(),
                order.platform(),
                order.platformOrderNo(),
                fulfillmentPackage.trackingNo(),
                status,
                attemptNo,
                errorMessage,
                syncedAt);
        OrderFulfillmentPackage updatedPackage = copyPackage(
                fulfillmentPackage,
                fulfillmentPackage.status(),
                fulfillmentPackage.shipmentId(),
                fulfillmentPackage.trackingNo(),
                fulfillmentPackage.shippedAt(),
                status,
                attemptNo,
                errorMessage,
                status == PlatformShipmentSyncStatus.SUCCESS ? syncedAt : null);
        publishEvent(
                status == PlatformShipmentSyncStatus.SUCCESS
                        ? "erp.oms.order.platform-shipment-synced.v1"
                        : "erp.oms.order.platform-shipment-sync-failed.v1",
                order.tenantId(),
                order.orderId(),
                Map.of(
                        "packageId", fulfillmentPackage.packageId(),
                        "platform", order.platform(),
                        "trackingNo", valueOrEmpty(fulfillmentPackage.trackingNo()),
                        "attemptNo", attemptNo,
                        "status", status.name(),
                        "errorMessage", valueOrEmpty(errorMessage)));
        return new PlatformSyncAttemptResult(updatedPackage, log);
    }

    private OrderFulfillmentPackage copyPackage(OrderFulfillmentPackage source,
                                                FulfillmentPackageStatus status,
                                                String shipmentId,
                                                String trackingNo,
                                                Instant shippedAt,
                                                PlatformShipmentSyncStatus platformSyncStatus,
                                                int platformSyncAttempts,
                                                String platformSyncError,
                                                Instant platformSyncedAt) {
        return new OrderFulfillmentPackage(
                source.packageId(),
                source.planId(),
                source.warehouseId(),
                source.warehouseCode(),
                source.carrierId(),
                source.carrierCode(),
                source.carrierName(),
                source.destinationCountry(),
                source.serviceLevel(),
                status,
                source.totalQuantity(),
                source.totalAmount(),
                source.estimatedShippingCost(),
                source.estimatedDeliveryDays(),
                source.note(),
                shipmentId,
                trackingNo,
                shippedAt,
                platformSyncStatus,
                platformSyncAttempts,
                platformSyncError,
                platformSyncedAt,
                source.lines());
    }

    private FulfillmentPlanStatus resolvePlanStatusAfterShipment(List<OrderFulfillmentPackage> packages) {
        boolean allShipped = packages.stream().allMatch(pkg -> pkg.status() == FulfillmentPackageStatus.SHIPPED);
        if (allShipped) {
            return FulfillmentPlanStatus.SHIPPED;
        }
        boolean hasShipped = packages.stream().anyMatch(pkg -> pkg.status() == FulfillmentPackageStatus.SHIPPED);
        if (hasShipped) {
            return FulfillmentPlanStatus.PARTIALLY_SHIPPED;
        }
        return FulfillmentPlanStatus.PLANNED;
    }

    private String generateTrackingNo(SalesOrder order, OrderFulfillmentPackage fulfillmentPackage) {
        return "TRK-"
                + normalizePlatform(order.platform())
                + "-"
                + fulfillmentPackage.packageId().substring(0, Math.min(8, fulfillmentPackage.packageId().length())).toUpperCase(Locale.ROOT)
                + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private String normalizePlatform(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private void recordFulfillmentCostEvents(SalesOrder order, List<OrderFulfillmentPackage> packages) {
        Map<String, OrderLine> orderLines = order.lines().stream()
                .collect(Collectors.toMap(OrderLine::lineId, line -> line, (left, right) -> left, LinkedHashMap::new));
        for (OrderFulfillmentPackage fulfillmentPackage : packages) {
            if (fulfillmentPackage.status() != FulfillmentPackageStatus.SHIPPED) {
                continue;
            }
            Instant occurredAt = fulfillmentPackage.shippedAt() != null ? fulfillmentPackage.shippedAt() : Instant.now();
            for (OrderFulfillmentPackageLine line : fulfillmentPackage.lines()) {
                OrderLine sourceLine = orderLines.get(line.orderLineId());
                if (sourceLine != null
                        && sourceLine.estimatedUnitCost() != null
                        && sourceLine.estimatedUnitCost().compareTo(BigDecimal.ZERO) > 0) {
                    tryRecordCostEvent(order.tenantId(), order.orderId(), "PRODUCT_COST", fulfillmentPackage.packageId(),
                            line.sellerSku(), order.platform(), order.currency(),
                            sourceLine.estimatedUnitCost().multiply(BigDecimal.valueOf(line.quantity())).setScale(2, RoundingMode.HALF_UP),
                            occurredAt);
                }
            }
            distributeShippingCost(order, fulfillmentPackage, occurredAt);
        }
    }

    private void distributeShippingCost(SalesOrder order, OrderFulfillmentPackage fulfillmentPackage, Instant occurredAt) {
        if (fulfillmentPackage.estimatedShippingCost() == null
                || fulfillmentPackage.estimatedShippingCost().compareTo(BigDecimal.ZERO) <= 0
                || fulfillmentPackage.lines().isEmpty()) {
            return;
        }
        BigDecimal totalShippingCost = fulfillmentPackage.estimatedShippingCost().setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalLineAmount = fulfillmentPackage.lines().stream()
                .map(OrderFulfillmentPackageLine::lineAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal allocated = BigDecimal.ZERO;
        for (int index = 0; index < fulfillmentPackage.lines().size(); index++) {
            OrderFulfillmentPackageLine line = fulfillmentPackage.lines().get(index);
            BigDecimal lineShippingCost;
            if (index == fulfillmentPackage.lines().size() - 1 || totalLineAmount.compareTo(BigDecimal.ZERO) <= 0) {
                lineShippingCost = totalShippingCost.subtract(allocated);
            } else {
                lineShippingCost = totalShippingCost.multiply(line.lineAmount())
                        .divide(totalLineAmount, 2, RoundingMode.HALF_UP);
                allocated = allocated.add(lineShippingCost);
            }
            if (lineShippingCost.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            tryRecordCostEvent(order.tenantId(), order.orderId(), "SHIPPING_COST", fulfillmentPackage.packageId(),
                    line.sellerSku(), order.platform(), order.currency(), lineShippingCost, occurredAt);
        }
    }

    private void tryRecordCostEvent(String tenantId,
                                    String orderId,
                                    String costType,
                                    String sourceId,
                                    String sellerSku,
                                    String marketplaceId,
                                    String currency,
                                    BigDecimal amount,
                                    Instant occurredAt) {
        try {
            fmsClient.recordCostEvent(new FmsClient.RecordCostEventRequest(
                    costType,
                    "OMS_FULFILLMENT_PACKAGE",
                    sourceId,
                    sellerSku,
                    marketplaceId,
                    currency,
                    amount,
                    occurredAt));
        } catch (Exception ex) {
            log.warn("Record cost event failed. tenantId={}, orderId={}, costType={}, sourceId={}, sellerSku={}",
                    tenantId, orderId, costType, sourceId, sellerSku, ex);
            recordRisk(tenantId, orderId, OrderRiskCheck.RiskLevel.MEDIUM, "COST_EVENT_RECORD_FAILED",
                    "Cost event record failed for " + costType + " / " + sellerSku, "CHECK_FINANCE");
        }
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private <T> T requireData(Result<T> result, String errorCode, String defaultMessage) {
        if (result == null || result.data() == null) {
            throw new BizException(errorCode, defaultMessage);
        }
        return result.data();
    }

    private OrderRefund getRefund(String tenantId, String refundId) {
        return orderStore.findRefund(tenantId, refundId)
                .orElseThrow(() -> new BizException("REFUND_NOT_FOUND", "Refund does not exist"));
    }

    private SalesOrder updateStatus(SalesOrder order, OrderStatus status) {
        return orderStore.save(new SalesOrder(
                order.orderId(),
                order.tenantId(),
                order.listingId(),
                order.storeId(),
                order.platform(),
                order.marketplace(),
                order.platformOrderNo(),
                order.buyerName(),
                order.customerId(),
                order.countryCode(),
                order.shippingAddress(),
                order.currency(),
                order.totalAmount(),
                order.taxAmount(),
                order.shippingAmount(),
                order.discountAmount(),
                status,
                order.paymentStatus(),
                order.fulfillmentStatus(),
                order.riskLevel(),
                order.profitMargin(),
                order.lines(),
                order.promotions(),
                order.orderDate(),
                order.createdAt(),
                Instant.now()));
    }

    private OrderRefund updateRefund(OrderRefund refund, OrderRefund.RefundStatus status) {
        return orderStore.saveRefund(new OrderRefund(
                refund.refundId(),
                refund.tenantId(),
                refund.orderId(),
                refund.reason(),
                refund.refundAmount(),
                refund.refundType(),
                status,
                refund.createdAt(),
                Instant.now()));
    }

    private void createReceivableAfterPaid(String tenantId, SalesOrder order) {
        try {
            fmsClient.createReceivable(new FmsClient.CreateReceivableRequest(
                    "OMS_ORDER", order.orderId(), order.buyerName(), order.currency(), order.totalAmount()));
        } catch (Exception ex) {
            recordRisk(tenantId, order.orderId(), OrderRiskCheck.RiskLevel.MEDIUM, "RECEIVABLE_CREATE_FAILED",
                    "Receivable creation failed after payment", "VERIFY_RECEIVABLE");
        }
    }

    private List<OrderRiskCheck> evaluateAuditRules(String tenantId, SalesOrder order, List<SalesOrder> recentOrders) {
        List<OrderRiskCheck> checks = new ArrayList<>();
        if (order.totalAmount().compareTo(new BigDecimal("10000")) > 0) {
            checks.add(recordRisk(tenantId, order.orderId(), OrderRiskCheck.RiskLevel.HIGH, "HIGH_AMOUNT",
                    "Order amount exceeds 10000", "MANUAL_REVIEW"));
        } else if (order.totalAmount().compareTo(new BigDecimal("5000")) > 0) {
            checks.add(recordRisk(tenantId, order.orderId(), OrderRiskCheck.RiskLevel.MEDIUM, "AMOUNT_WARNING",
                    "Order amount is high", "VERIFY_MARGIN"));
        }

        boolean blacklisted = safeBlacklist(orderStore.listBuyerBlacklist(tenantId)).stream()
                .anyMatch(entry -> normalizeBuyerName(entry.buyerName()).equals(normalizeBuyerName(order.buyerName())));
        if (blacklisted) {
            checks.add(recordRisk(tenantId, order.orderId(), OrderRiskCheck.RiskLevel.CRITICAL, "BLACKLISTED_BUYER",
                    "Buyer matches blacklist", "REJECT_OR_VERIFY"));
        }

        boolean duplicateOrder = recentOrders.stream().anyMatch(existing ->
                existing.totalAmount().compareTo(order.totalAmount()) == 0
                        || normalizeAddress(existing.shippingAddress()).equals(normalizeAddress(order.shippingAddress())));
        if (duplicateOrder) {
            checks.add(recordRisk(tenantId, order.orderId(), OrderRiskCheck.RiskLevel.HIGH, "DUPLICATE_ORDER",
                    "Potential duplicate order within 24 hours", "VERIFY_DUPLICATE"));
        }

        if (isAbnormalAddress(order.shippingAddress())) {
            checks.add(recordRisk(tenantId, order.orderId(), OrderRiskCheck.RiskLevel.MEDIUM, "ABNORMAL_ADDRESS",
                    "Shipping address is abnormal", "VERIFY_ADDRESS"));
        }

        BigDecimal marginRate = calculateMarginRate(order.lines(), order.totalAmount());
        if (marginRate != null && marginRate.compareTo(new BigDecimal("0.05")) < 0) {
            checks.add(recordRisk(tenantId, order.orderId(), OrderRiskCheck.RiskLevel.HIGH, "LOW_PROFIT",
                    "Estimated margin rate below 5%", "VERIFY_MARGIN"));
        } else if (marginRate != null && marginRate.compareTo(new BigDecimal("0.15")) < 0) {
            checks.add(recordRisk(tenantId, order.orderId(), OrderRiskCheck.RiskLevel.MEDIUM, "LOW_PROFIT_WARNING",
                    "Estimated margin rate below 15%", "VERIFY_MARGIN"));
        }

        return checks;
    }

    private List<OrderRiskCheck> performInventoryRiskChecks(String tenantId, SalesOrder order) {
        List<OrderRiskCheck> checks = new ArrayList<>();
        for (OrderLine line : order.lines()) {
            try {
                WmsClient.InventoryAvailabilityResponse availability = wmsClient.checkAvailability(line.sellerSku()).data();
                int available = availability == null ? 0 : availability.available();
                if (available < line.quantity()) {
                    checks.add(recordRisk(tenantId, order.orderId(), OrderRiskCheck.RiskLevel.HIGH, "INVENTORY_SHORTAGE",
                            "Inventory is not enough for SKU " + line.sellerSku(), "CHECK_INVENTORY"));
                }
            } catch (Exception ex) {
                checks.add(recordRisk(tenantId, order.orderId(), OrderRiskCheck.RiskLevel.MEDIUM, "INVENTORY_CHECK_FAILED",
                        "Inventory availability check failed", "CHECK_INVENTORY"));
            }
        }
        return checks;
    }

    private OrderRiskCheck recordRisk(String tenantId, String orderId, OrderRiskCheck.RiskLevel level, String riskType,
                                      String description, String suggestedAction) {
        OrderRiskCheck check = new OrderRiskCheck(
                UUID.randomUUID().toString(),
                tenantId,
                orderId,
                level,
                riskType,
                description,
                suggestedAction,
                Instant.now());
        orderStore.saveRiskCheck(check);
        return check;
    }

    private boolean requiresReview(OrderRiskCheck check) {
        return check.riskLevel() == OrderRiskCheck.RiskLevel.MEDIUM
                || check.riskLevel() == OrderRiskCheck.RiskLevel.HIGH
                || check.riskLevel() == OrderRiskCheck.RiskLevel.CRITICAL;
    }

    private List<OrderLine> normalizeLines(List<OrderLine> lines) {
        return lines.stream()
                .map(line -> new OrderLine(
                        line.lineId() == null || line.lineId().isBlank() ? UUID.randomUUID().toString() : line.lineId(),
                        line.productId(),
                        line.sellerSku(),
                        line.title(),
                        line.quantity(),
                        line.unitPrice(),
                        line.totalPrice(),
                        line.taxAmount(),
                        line.estimatedUnitCost()))
                .toList();
    }

    private BigDecimal calculateMarginRate(List<OrderLine> lines, BigDecimal totalRevenue) {
        boolean hasCompleteCost = lines.stream().allMatch(line -> line.estimatedUnitCost() != null);
        if (!hasCompleteCost || totalRevenue.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        BigDecimal estimatedCost = lines.stream()
                .map(line -> line.estimatedUnitCost().multiply(BigDecimal.valueOf(line.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return totalRevenue.subtract(estimatedCost).divide(totalRevenue, 4, RoundingMode.HALF_UP);
    }

    private boolean isAbnormalAddress(String shippingAddress) {
        String normalized = normalizeAddress(shippingAddress);
        return normalized.length() < 8
                || normalized.contains("PO BOX")
                || normalized.contains("P.O. BOX")
                || normalized.contains("UNKNOWN")
                || normalized.contains("TEST");
    }

    private String normalizeAddress(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private String normalizeBuyerName(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private List<SalesOrder> safeOrders(List<SalesOrder> orders) {
        return orders == null ? List.of() : orders;
    }

    private List<BuyerBlacklistEntry> safeBlacklist(List<BuyerBlacklistEntry> entries) {
        return entries == null ? List.of() : entries;
    }

    private OriginalOrderSnapshot saveImportSnapshot(String tenantId,
                                                     ImportOrderCommand command,
                                                     String processingStatus,
                                                     String standardOrderId,
                                                     String duplicateOrderId) {
        return saveImportSnapshot(tenantId, command, processingStatus, standardOrderId, duplicateOrderId, UUID.randomUUID().toString());
    }

    private OriginalOrderSnapshot saveImportSnapshot(String tenantId,
                                                     ImportOrderCommand command,
                                                     String processingStatus,
                                                     String standardOrderId,
                                                     String duplicateOrderId,
                                                     String snapshotId) {
        Instant now = Instant.now();
        OriginalOrderSnapshot snapshot = new OriginalOrderSnapshot(
                snapshotId,
                tenantId,
                command.platform(),
                command.marketplace(),
                command.storeId(),
                command.platformOrderNo(),
                command.buyerName(),
                command.countryCode(),
                command.shippingAddress(),
                command.currency(),
                command.importMode(),
                command.rawPayload() == null || command.rawPayload().isBlank() ? buildDefaultRawPayload(command) : command.rawPayload(),
                processingStatus,
                standardOrderId,
                duplicateOrderId,
                now,
                now);
        orderStore.saveOriginalOrderSnapshot(snapshot);
        return snapshot;
    }

    private String buildDefaultRawPayload(ImportOrderCommand command) {
        return """
                {"platform":"%s","marketplace":"%s","storeId":"%s","platformOrderNo":"%s","buyerName":"%s","countryCode":"%s","currency":"%s","lineCount":%d}
                """.formatted(
                safeJson(command.platform()),
                safeJson(command.marketplace()),
                safeJson(command.storeId()),
                safeJson(command.platformOrderNo()),
                safeJson(command.buyerName()),
                safeJson(command.countryCode()),
                safeJson(command.currency()),
                command.lines() == null ? 0 : command.lines().size()).replaceAll("\\s+", "");
    }

    private String safeJson(String value) {
        return value == null ? "" : value.replace("\"", "'");
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
            throw new BizException("EVENT_PUBLISH_FAILED", "Event publish failed: " + e.getMessage());
        }
    }

    public record ImportOrderCommand(String platform, String platformOrderNo, String buyerName, String countryCode,
                                     String shippingAddress, String currency, List<OrderLine> lines,
                                     String importMode, String storeId, String marketplace, String rawPayload) {
        public ImportOrderCommand(String platform, String platformOrderNo, String buyerName, String countryCode,
                                  String shippingAddress, String currency, List<OrderLine> lines) {
            this(platform, platformOrderNo, buyerName, countryCode, shippingAddress, currency, lines,
                    "API", null, null, null);
        }
    }

    public record SplitOrderCommand(List<SplitPackageCommand> packages) {}

    public record SplitPackageCommand(String packageId, String note, List<SplitLineAllocationCommand> allocations) {}

    public record SplitLineAllocationCommand(String lineId, int quantity) {}

    public record MergePackagesCommand(List<String> packageIds, String note) {}

    public record AddBuyerBlacklistCommand(String buyerName, String reason) {}

    public record ReceivePmsRiskAlertCommand(String orderId, String riskType, BigDecimal riskScore,
                                             String riskLevel, String description, String suggestedAction,
                                             String traceId, String idempotencyKey) {}

    public record SyncOrdersCommand(String platform, String syncType) {}

    public record ApplyPromotionCommand(String promoType, String promoCode, BigDecimal discount,
                                        String description) {}

    public record CreateOrderStrategyCommand(String strategyType, String name, String description,
                                             String rules, boolean enabled, int priority) {}

    public record UpdateOrderStrategyCommand(String strategyType, String name, String description,
                                             String rules, Boolean enabled, Integer priority) {}

    private record SyncExecutionResult(List<OrderFulfillmentPackage> packages, List<PlatformShipmentSyncLog> logs) {}

    private record PlatformSyncAttemptResult(OrderFulfillmentPackage updatedPackage, PlatformShipmentSyncLog log) {}

    private static final class WarehouseStockSnapshot {
        private final String warehouseId;
        private final String warehouseCode;
        private final String countryCode;
        private final Map<String, Integer> skuAvailability;

        private WarehouseStockSnapshot(String warehouseId, String warehouseCode, String countryCode, Map<String, Integer> skuAvailability) {
            this.warehouseId = warehouseId;
            this.warehouseCode = warehouseCode;
            this.countryCode = countryCode;
            this.skuAvailability = new LinkedHashMap<>(skuAvailability);
        }

        private String warehouseId() {
            return warehouseId;
        }

        private String warehouseCode() {
            return warehouseCode == null ? warehouseId : warehouseCode;
        }

        private String countryCode() {
            return countryCode == null ? "" : countryCode;
        }

        private int available(String sellerSku) {
            return skuAvailability.getOrDefault(sellerSku, 0);
        }

        private void consume(String sellerSku, int quantity) {
            skuAvailability.put(sellerSku, Math.max(available(sellerSku) - quantity, 0));
        }
    }

    private static final class PackageDraft {
        private final String packageId = UUID.randomUUID().toString();
        private final String warehouseId;
        private final String warehouseCode;
        private final String warehouseCountry;
        private final String destinationCountry;
        private FulfillmentPackageStatus status;
        private String carrierId;
        private String carrierCode;
        private String carrierName;
        private String serviceLevel;
        private BigDecimal estimatedShippingCost;
        private Integer estimatedDeliveryDays;
        private String note;
        private final List<OrderFulfillmentPackageLine> lines = new ArrayList<>();
        private int totalQuantity;
        private BigDecimal totalAmount = BigDecimal.ZERO;

        private PackageDraft(WarehouseStockSnapshot warehouse, String destinationCountry) {
            this.warehouseId = warehouse.warehouseId();
            this.warehouseCode = warehouse.warehouseCode();
            this.warehouseCountry = warehouse.countryCode();
            this.destinationCountry = destinationCountry;
            this.status = FulfillmentPackageStatus.READY;
        }

        private PackageDraft(String destinationCountry, String note) {
            this.warehouseId = null;
            this.warehouseCode = null;
            this.warehouseCountry = "";
            this.destinationCountry = destinationCountry;
            this.status = FulfillmentPackageStatus.WAITING_INVENTORY;
            this.note = note;
        }

        private static PackageDraft inventoryWaiting(String destinationCountry, String note) {
            return new PackageDraft(destinationCountry, note);
        }

        private void addLine(OrderLine line, int quantity) {
            BigDecimal lineAmount = line.unitPrice().multiply(BigDecimal.valueOf(quantity));
            lines.add(new OrderFulfillmentPackageLine(
                    UUID.randomUUID().toString(),
                    line.lineId(),
                    line.sellerSku(),
                    line.title(),
                    quantity,
                    line.unitPrice(),
                    lineAmount));
            totalQuantity += quantity;
            totalAmount = totalAmount.add(lineAmount);
        }

        private boolean inventoryWaiting() {
            return status == FulfillmentPackageStatus.WAITING_INVENTORY;
        }

        private String warehouseCountry() {
            return warehouseCountry;
        }

        private String warehouseId() {
            return warehouseId;
        }

        private List<OrderFulfillmentPackageLine> lines() {
            return List.copyOf(lines);
        }

        private int totalQuantity() {
            return totalQuantity;
        }

        private BigDecimal totalAmount() {
            return totalAmount;
        }

        private void assignCarrier(TmsClient.CarrierRecommendationResponse recommendation) {
            this.carrierId = recommendation.carrierId();
            this.carrierCode = recommendation.carrierCode();
            this.carrierName = recommendation.carrierName();
            this.serviceLevel = recommendation.serviceLevel();
            this.estimatedShippingCost = recommendation.estimatedCost();
            this.estimatedDeliveryDays = recommendation.estimatedDeliveryDays();
            this.status = FulfillmentPackageStatus.READY;
        }

        private void markWaitingCarrier(String note) {
            this.status = FulfillmentPackageStatus.WAITING_CARRIER;
            this.note = note;
        }

        private OrderFulfillmentPackage toDomain(String planId) {
            return new OrderFulfillmentPackage(
                    packageId,
                    planId,
                    warehouseId,
                    warehouseCode,
                    carrierId,
                    carrierCode,
                    carrierName,
                    destinationCountry,
                    serviceLevel,
                    status,
                    totalQuantity,
                    totalAmount.setScale(2, RoundingMode.HALF_UP),
                    estimatedShippingCost == null ? null : estimatedShippingCost.setScale(2, RoundingMode.HALF_UP),
                    estimatedDeliveryDays,
                    note,
                    null,
                    null,
                    null,
                    PlatformShipmentSyncStatus.NOT_SYNCED,
                    0,
                    null,
                    null,
                    List.copyOf(lines));
        }
    }
}
