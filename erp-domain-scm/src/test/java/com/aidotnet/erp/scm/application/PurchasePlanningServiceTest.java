package com.aidotnet.erp.scm.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("SCM 采购计划服务测试")
class PurchasePlanningServiceTest {

    private PurchaseStore purchaseStore;
    private PurchaseService purchaseService;
    private OmsClient omsClient;
    private WmsClient wmsClient;
    private PurchasePlanningService purchasePlanningService;

    @BeforeEach
    void setUp() {
        purchaseStore = mock(PurchaseStore.class);
        purchaseService = mock(PurchaseService.class);
        omsClient = mock(OmsClient.class);
        wmsClient = mock(WmsClient.class);
        purchasePlanningService = new PurchasePlanningService(purchaseStore, purchaseService, omsClient, wmsClient);
    }

    @Test
    @DisplayName("应按订单需求补货建议可用库存和采购中数量生成采购计划")
    void shouldGeneratePurchasePlanFromDemandInventoryAndOpenPurchaseOrders() {
        when(omsClient.listProcurementDemand()).thenReturn(Result.ok(List.of(
                new OmsClient.ProcurementDemandResponse("SKU-1", 12, List.of("ORD-1", "ORD-2")))));
        when(purchaseStore.findAcceptedSuggestions("T1")).thenReturn(List.of(new ReplenishmentSuggestion(
                "SUG-1",
                "T1",
                "SKU-1",
                "W1",
                5,
                2,
                4,
                4,
                8,
                ReplenishmentSuggestion.SuggestionPriority.HIGH,
                ReplenishmentSuggestion.SuggestionStatus.ACCEPTED,
                Instant.now(),
                Instant.now())));
        when(wmsClient.getAvailability("SKU-1"))
                .thenReturn(Result.ok(new WmsClient.InventoryAvailabilityResponse("SKU-1", 7, 2, 5)));
        when(purchaseStore.listPurchaseOrders("T1")).thenReturn(List.of(new PurchaseOrder(
                "PO-OPEN-1",
                "T1",
                "SUP-1",
                "PO-OPEN-1",
                "USD",
                BigDecimal.valueOf(72),
                PurchaseOrderStatus.APPROVED,
                null,
                null,
                "STANDARD",
                Instant.parse("2026-05-20T00:00:00Z"),
                null,
                null,
                null,
                null,
                List.of(new PurchaseOrderLine(
                        "POL-1",
                        null,
                        "SKU-1",
                        9,
                        3,
                        BigDecimal.valueOf(8),
                        BigDecimal.valueOf(72),
                        Instant.parse("2026-05-20T00:00:00Z"))),
                Instant.now(),
                Instant.now())));
        when(purchaseStore.savePurchasePlan(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PurchasePlan plan = purchasePlanningService.generatePlan("T1");

        assertEquals(PurchasePlanStatus.GENERATED, plan.status());
        assertEquals(1, plan.lines().size());
        PurchasePlanLine line = plan.lines().get(0);
        assertEquals("SKU-1", line.sellerSku());
        assertEquals(12, line.orderDemandQuantity());
        assertEquals(8, line.replenishmentDemandQuantity());
        assertEquals(5, line.availableInventoryQuantity());
        assertEquals(6, line.inPurchasingQuantity());
        assertEquals(9, line.suggestedPurchaseQuantity());
        assertEquals(List.of("ORD-1", "ORD-2"), line.orderSourceRefs());
        assertEquals(List.of("SUG-1"), line.suggestionSourceRefs());
    }

    @Test
    @DisplayName("应按计划行生成采购单并回写计划状态")
    void shouldCreatePurchaseOrderFromPlanAndUpdatePlanStatus() {
        Instant now = Instant.now();
        PurchasePlan existingPlan = new PurchasePlan(
                "PLAN-1",
                "T1",
                "PPL-001",
                PurchasePlanStatus.GENERATED,
                List.of(
                        new PurchasePlanLine(
                                "PL-1",
                                "SKU-1",
                                12,
                                8,
                                5,
                                6,
                                9,
                                List.of("ORD-1", "ORD-2"),
                                List.of("SUG-1"),
                                PurchasePlanLineStatus.GENERATED,
                                null,
                                now,
                                now),
                        new PurchasePlanLine(
                                "PL-2",
                                "SKU-2",
                                6,
                                0,
                                0,
                                0,
                                6,
                                List.of("ORD-3"),
                                List.of(),
                                PurchasePlanLineStatus.GENERATED,
                                null,
                                now,
                                now)),
                now,
                now);
        when(purchaseStore.findPurchasePlan("T1", "PLAN-1")).thenReturn(Optional.of(existingPlan));
        when(purchaseService.createPurchaseOrder(any(), any())).thenReturn(new PurchaseOrder(
                "PO-NEW-1",
                "T1",
                "SUP-1",
                "PO-NEW-1",
                "USD",
                BigDecimal.valueOf(85.50),
                PurchaseOrderStatus.DRAFT,
                "30D",
                "FOB",
                "STANDARD",
                Instant.parse("2026-05-21T00:00:00Z"),
                null,
                "from plan",
                null,
                null,
                List.of(new PurchaseOrderLine(
                        "POL-NEW-1",
                        null,
                        "SKU-1",
                        9,
                        0,
                        new BigDecimal("9.50"),
                        new BigDecimal("85.50"),
                        Instant.parse("2026-05-21T00:00:00Z"))),
                now,
                now));
        when(purchaseStore.savePurchasePlan(any())).thenAnswer(invocation -> invocation.getArgument(0));

        PurchasePlanningService.PurchaseOrderCreationResult result = purchasePlanningService.createPurchaseOrderFromPlan(
                "T1",
                "PLAN-1",
                new PurchasePlanningService.CreatePurchaseOrderFromPlanCommand(
                        "SUP-1",
                        "USD",
                        "30D",
                        "FOB",
                        "STANDARD",
                        Instant.parse("2026-05-21T00:00:00Z"),
                        "from plan",
                        List.of(new PurchasePlanningService.SelectedPlanLineCommand(
                                "PL-1",
                                new BigDecimal("9.50")))));

        PurchasePlan updatedPlan = result.purchasePlan();
        assertEquals(PurchasePlanStatus.PARTIALLY_ORDERED, updatedPlan.status());
        assertEquals(PurchasePlanLineStatus.ORDERED, updatedPlan.lines().get(0).lineStatus());
        assertEquals("PO-NEW-1", updatedPlan.lines().get(0).linkedPoId());
        assertEquals(PurchasePlanLineStatus.GENERATED, updatedPlan.lines().get(1).lineStatus());
    }

    @Test
    @DisplayName("已转单计划行不允许重复转采购单")
    void shouldRejectAlreadyOrderedPlanLine() {
        Instant now = Instant.now();
        PurchasePlan existingPlan = new PurchasePlan(
                "PLAN-2",
                "T1",
                "PPL-002",
                PurchasePlanStatus.PARTIALLY_ORDERED,
                List.of(new PurchasePlanLine(
                        "PL-ORDERED",
                        "SKU-1",
                        12,
                        8,
                        5,
                        6,
                        9,
                        List.of("ORD-1"),
                        List.of("SUG-1"),
                        PurchasePlanLineStatus.ORDERED,
                        "PO-OLD-1",
                        now,
                        now)),
                now,
                now);
        when(purchaseStore.findPurchasePlan("T1", "PLAN-2")).thenReturn(Optional.of(existingPlan));

        BizException ex = assertThrows(
                BizException.class,
                () -> purchasePlanningService.createPurchaseOrderFromPlan(
                        "T1",
                        "PLAN-2",
                        new PurchasePlanningService.CreatePurchaseOrderFromPlanCommand(
                                "SUP-1",
                                "USD",
                                null,
                                null,
                                "STANDARD",
                                Instant.parse("2026-05-21T00:00:00Z"),
                                null,
                                List.of(new PurchasePlanningService.SelectedPlanLineCommand(
                                        "PL-ORDERED",
                                        new BigDecimal("9.50"))))));

        assertEquals("PURCHASE_PLAN_LINE_ALREADY_ORDERED", ex.getCode());
    }
}
