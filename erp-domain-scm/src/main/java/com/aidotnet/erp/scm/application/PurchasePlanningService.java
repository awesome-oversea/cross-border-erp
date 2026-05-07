package com.aidotnet.erp.scm.application;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.scm.client.OmsClient;
import com.aidotnet.erp.scm.client.WmsClient;
import com.aidotnet.erp.scm.domain.PurchaseOrder;
import com.aidotnet.erp.scm.domain.PurchaseOrderLine;
import com.aidotnet.erp.scm.domain.PurchaseOrderStatus;
import com.aidotnet.erp.scm.domain.PurchasePlan;
import com.aidotnet.erp.scm.domain.PurchasePlanLine;
import com.aidotnet.erp.scm.domain.PurchasePlanLineStatus;
import com.aidotnet.erp.scm.domain.PurchasePlanStatus;
import com.aidotnet.erp.scm.domain.ReplenishmentSuggestion;
import com.aidotnet.erp.scm.infrastructure.PurchaseStore;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PurchasePlanningService {

    private final PurchaseStore purchaseStore;
    private final PurchaseService purchaseService;
    private final OmsClient omsClient;
    private final WmsClient wmsClient;

    public PurchasePlanningService(
            PurchaseStore purchaseStore,
            PurchaseService purchaseService,
            OmsClient omsClient,
            WmsClient wmsClient) {
        this.purchaseStore = purchaseStore;
        this.purchaseService = purchaseService;
        this.omsClient = omsClient;
        this.wmsClient = wmsClient;
    }

    public PurchasePlan generatePlan(String tenantId) {
        List<OmsClient.ProcurementDemandResponse> orderDemands = unwrapResult(
                omsClient.listProcurementDemand(),
                "OMS_PROCUREMENT_DEMAND_FAILED",
                "采购计划获取订单需求失败");
        List<ReplenishmentSuggestion> acceptedSuggestions = purchaseStore.findAcceptedSuggestions(tenantId);
        Map<String, DemandAccumulator> demandBySku = new LinkedHashMap<>();
        for (OmsClient.ProcurementDemandResponse demand : orderDemands) {
            if (demand == null || demand.sellerSku() == null || demand.sellerSku().isBlank()) {
                continue;
            }
            demandBySku.computeIfAbsent(demand.sellerSku(), DemandAccumulator::new)
                    .addOrderDemand(demand.orderDemandQuantity(), demand.orderIds());
        }
        for (ReplenishmentSuggestion suggestion : acceptedSuggestions) {
            if (suggestion.sellerSku() == null || suggestion.sellerSku().isBlank()) {
                continue;
            }
            demandBySku.computeIfAbsent(suggestion.sellerSku(), DemandAccumulator::new)
                    .addSuggestionDemand(suggestion.suggestedQuantity(), suggestion.suggestionId());
        }
        if (demandBySku.isEmpty()) {
            throw new BizException("PURCHASE_PLAN_EMPTY", "没有可生成采购计划的需求");
        }

        List<PurchaseOrder> openPurchaseOrders = purchaseStore.listPurchaseOrders(tenantId);
        Instant now = Instant.now();
        List<PurchasePlanLine> lines = demandBySku.values().stream()
                .map(demand -> buildPlanLine(demand, openPurchaseOrders, now))
                .toList();
        PurchasePlan plan = new PurchasePlan(
                UUID.randomUUID().toString(),
                tenantId,
                buildPlanNumber(),
                resolvePlanStatus(lines),
                lines,
                now,
                now);
        return purchaseStore.savePurchasePlan(plan);
    }

    public List<PurchasePlan> listPurchasePlans(String tenantId) {
        return purchaseStore.listPurchasePlans(tenantId);
    }

    public PurchasePlan getPurchasePlan(String tenantId, String planId) {
        return purchaseStore.findPurchasePlan(tenantId, planId)
                .orElseThrow(() -> new BizException("PURCHASE_PLAN_NOT_FOUND", "采购计划不存在"));
    }

    public PurchaseOrderCreationResult createPurchaseOrderFromPlan(
            String tenantId, String planId, CreatePurchaseOrderFromPlanCommand command) {
        PurchasePlan plan = getPurchasePlan(tenantId, planId);
        if (command.lines() == null || command.lines().isEmpty()) {
            throw new BizException("PURCHASE_PLAN_LINE_REQUIRED", "请选择需要转采购单的计划行");
        }

        Map<String, PurchasePlanLine> lineIndex = new LinkedHashMap<>();
        for (PurchasePlanLine line : plan.lines()) {
            lineIndex.put(line.lineId(), line);
        }
        Set<String> selectedLineIds = new HashSet<>();
        List<PurchaseOrderLine> poLines = new ArrayList<>();
        for (SelectedPlanLineCommand selectedLine : command.lines()) {
            if (!selectedLineIds.add(selectedLine.planLineId())) {
                throw new BizException("PURCHASE_PLAN_LINE_DUPLICATED", "同一计划行不能重复转单");
            }
            PurchasePlanLine planLine = lineIndex.get(selectedLine.planLineId());
            if (planLine == null) {
                throw new BizException("PURCHASE_PLAN_LINE_NOT_FOUND", "采购计划行不存在");
            }
            if (planLine.lineStatus() == PurchasePlanLineStatus.ORDERED) {
                throw new BizException("PURCHASE_PLAN_LINE_ALREADY_ORDERED", "采购计划行已转单");
            }
            if (!planLine.canCreatePurchaseOrder()) {
                throw new BizException("PURCHASE_PLAN_LINE_NOT_ACTIONABLE", "采购计划行没有可执行采购量");
            }
            if (selectedLine.unitCost() == null || selectedLine.unitCost().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BizException("PURCHASE_PLAN_LINE_COST_INVALID", "采购单价必须大于0");
            }
            poLines.add(new PurchaseOrderLine(
                    UUID.randomUUID().toString(),
                    null,
                    planLine.sellerSku(),
                    planLine.suggestedPurchaseQuantity(),
                    0,
                    selectedLine.unitCost(),
                    selectedLine.unitCost().multiply(BigDecimal.valueOf(planLine.suggestedPurchaseQuantity())),
                    command.expectedDeliveryDate()));
        }

        PurchaseOrder purchaseOrder = purchaseService.createPurchaseOrder(
                tenantId,
                new PurchaseService.CreatePurchaseOrderCommand(
                        command.supplierId(),
                        command.currency(),
                        command.paymentTerms(),
                        command.shippingTerms(),
                        command.purchaseType(),
                        command.expectedDeliveryDate(),
                        command.notes(),
                        poLines));

        Instant now = Instant.now();
        List<PurchasePlanLine> updatedLines = plan.lines().stream()
                .map(line -> selectedLineIds.contains(line.lineId()) ? new PurchasePlanLine(
                                line.lineId(),
                                line.sellerSku(),
                                line.orderDemandQuantity(),
                                line.replenishmentDemandQuantity(),
                                line.availableInventoryQuantity(),
                                line.inPurchasingQuantity(),
                                line.suggestedPurchaseQuantity(),
                                line.orderSourceRefs(),
                                line.suggestionSourceRefs(),
                                PurchasePlanLineStatus.ORDERED,
                                purchaseOrder.poId(),
                                line.createdAt(),
                                now)
                        : line)
                .toList();
        PurchasePlan updatedPlan = purchaseStore.savePurchasePlan(new PurchasePlan(
                plan.planId(),
                plan.tenantId(),
                plan.planNumber(),
                resolvePlanStatus(updatedLines),
                updatedLines,
                plan.createdAt(),
                now));
        return new PurchaseOrderCreationResult(purchaseOrder, updatedPlan);
    }

    private PurchasePlanLine buildPlanLine(
            DemandAccumulator demand, List<PurchaseOrder> openPurchaseOrders, Instant now) {
        WmsClient.InventoryAvailabilityResponse availability = unwrapResult(
                wmsClient.getAvailability(demand.sellerSku()),
                "WMS_AVAILABILITY_FAILED",
                "采购计划获取可用库存失败");
        int availableInventory = availability == null ? 0 : availability.available();
        int inPurchasingQuantity = calculateInPurchasingQuantity(openPurchaseOrders, demand.sellerSku());
        int suggestedPurchaseQuantity = Math.max(
                demand.orderDemandQuantity() + demand.replenishmentDemandQuantity()
                        - availableInventory - inPurchasingQuantity,
                0);
        PurchasePlanLineStatus lineStatus = suggestedPurchaseQuantity > 0
                ? PurchasePlanLineStatus.GENERATED
                : PurchasePlanLineStatus.NO_ACTION_REQUIRED;
        return new PurchasePlanLine(
                UUID.randomUUID().toString(),
                demand.sellerSku(),
                demand.orderDemandQuantity(),
                demand.replenishmentDemandQuantity(),
                availableInventory,
                inPurchasingQuantity,
                suggestedPurchaseQuantity,
                List.copyOf(demand.orderIds()),
                List.copyOf(demand.suggestionIds()),
                lineStatus,
                null,
                now,
                now);
    }

    private int calculateInPurchasingQuantity(List<PurchaseOrder> openPurchaseOrders, String sellerSku) {
        return openPurchaseOrders.stream()
                .filter(this::isOpenPurchaseOrder)
                .flatMap(order -> order.lines().stream())
                .filter(line -> sellerSku.equals(line.sellerSku()))
                .mapToInt(line -> Math.max(line.quantity() - line.receivedQuantity(), 0))
                .sum();
    }

    private boolean isOpenPurchaseOrder(PurchaseOrder purchaseOrder) {
        return purchaseOrder.status() != PurchaseOrderStatus.CANCELLED
                && purchaseOrder.status() != PurchaseOrderStatus.RECEIVED;
    }

    private PurchasePlanStatus resolvePlanStatus(List<PurchasePlanLine> lines) {
        boolean hasGenerated = lines.stream().anyMatch(line -> line.lineStatus() == PurchasePlanLineStatus.GENERATED);
        boolean hasOrdered = lines.stream().anyMatch(line -> line.lineStatus() == PurchasePlanLineStatus.ORDERED);
        if (hasGenerated && hasOrdered) {
            return PurchasePlanStatus.PARTIALLY_ORDERED;
        }
        if (hasOrdered) {
            return PurchasePlanStatus.ORDERED;
        }
        return PurchasePlanStatus.GENERATED;
    }

    private String buildPlanNumber() {
        return "PPL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private <T> T unwrapResult(Result<T> result, String errorCode, String message) {
        if (result == null || !result.success()) {
            throw new BizException(errorCode, message);
        }
        return result.data();
    }

    public record CreatePurchaseOrderFromPlanCommand(
            String supplierId,
            String currency,
            String paymentTerms,
            String shippingTerms,
            String purchaseType,
            Instant expectedDeliveryDate,
            String notes,
            List<SelectedPlanLineCommand> lines) {}

    public record SelectedPlanLineCommand(String planLineId, BigDecimal unitCost) {}

    public record PurchaseOrderCreationResult(PurchaseOrder purchaseOrder, PurchasePlan purchasePlan) {}

    private static final class DemandAccumulator {
        private final String sellerSku;
        private int orderDemandQuantity;
        private int replenishmentDemandQuantity;
        private final List<String> orderIds = new ArrayList<>();
        private final List<String> suggestionIds = new ArrayList<>();

        private DemandAccumulator(String sellerSku) {
            this.sellerSku = sellerSku;
        }

        private String sellerSku() {
            return sellerSku;
        }

        private int orderDemandQuantity() {
            return orderDemandQuantity;
        }

        private int replenishmentDemandQuantity() {
            return replenishmentDemandQuantity;
        }

        private List<String> orderIds() {
            return orderIds;
        }

        private List<String> suggestionIds() {
            return suggestionIds;
        }

        private void addOrderDemand(int quantity, List<String> refs) {
            orderDemandQuantity += Math.max(quantity, 0);
            if (refs != null) {
                orderIds.addAll(refs.stream().filter(ref -> ref != null && !ref.isBlank()).toList());
            }
        }

        private void addSuggestionDemand(int quantity, String suggestionId) {
            replenishmentDemandQuantity += Math.max(quantity, 0);
            if (suggestionId != null && !suggestionId.isBlank()) {
                suggestionIds.add(suggestionId);
            }
        }
    }
}
