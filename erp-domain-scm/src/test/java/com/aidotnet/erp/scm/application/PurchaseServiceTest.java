package com.aidotnet.erp.scm.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.scm.client.FmsClient;
import com.aidotnet.erp.scm.client.WmsClient;
import com.aidotnet.erp.scm.domain.PurchaseOrderLine;
import com.aidotnet.erp.scm.domain.Supplier;
import com.aidotnet.erp.scm.domain.SupplierEvaluation;
import com.aidotnet.erp.scm.domain.SupplierProfile;
import com.aidotnet.erp.scm.domain.SupplierScore;
import com.aidotnet.erp.scm.infrastructure.PurchaseStore;
import com.aidotnet.erp.scm.infrastructure.ScmExtStore;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@DisplayName("SCM 采购服务测试")
class PurchaseServiceTest {

    private PurchaseStore purchaseStore;
    private ScmExtStore scmExtStore;
    private FmsClient fmsClient;
    private WmsClient wmsClient;
    private PurchaseTrackingService purchaseTrackingService;
    private PurchaseService purchaseService;

    @BeforeEach
    void setUp() {
        purchaseStore = mock(PurchaseStore.class);
        scmExtStore = mock(ScmExtStore.class);
        fmsClient = mock(FmsClient.class);
        wmsClient = mock(WmsClient.class);
        purchaseTrackingService = mock(PurchaseTrackingService.class);
        purchaseService = new PurchaseService(
                purchaseStore, scmExtStore, fmsClient, wmsClient, purchaseTrackingService);
    }

    @Test
    @DisplayName("创建供应商档案时应返回联系人资质和评分")
    void shouldCreateSupplierProfileWithContactsQualificationsAndScore() {
        when(purchaseStore.saveSupplierProfile(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SupplierProfile result = purchaseService.createSupplier(
                "T1",
                new PurchaseService.CreateSupplierCommand(
                        "华东供应商",
                        "华东贸易有限公司",
                        "王采购",
                        "CN",
                        "A",
                        12,
                        200,
                        List.of(new PurchaseService.SupplierContactCommand(
                                "王采购", "销售经理", "buyer@vendor.com", "13800000000", true)),
                        List.of(new PurchaseService.SupplierQualificationCommand(
                                "BUSINESS_LICENSE",
                                "BL-001",
                                "上海市场监管局",
                                Instant.parse("2026-01-01T00:00:00Z"),
                                Instant.parse("2028-12-31T00:00:00Z"),
                                "VALID",
                                "主营跨境供货")),
                        new PurchaseService.SupplierScoreCommand(
                                BigDecimal.valueOf(95),
                                BigDecimal.valueOf(96),
                                BigDecimal.valueOf(92),
                                BigDecimal.valueOf(94),
                                3)));

        assertNotNull(result.supplier().supplierId());
        assertEquals("华东供应商", result.supplier().name());
        assertEquals(1, result.contacts().size());
        assertEquals("王采购", result.contacts().get(0).name());
        assertEquals(1, result.qualifications().size());
        assertEquals("BUSINESS_LICENSE", result.qualifications().get(0).qualificationType());
        assertNotNull(result.score());
        assertEquals(0, BigDecimal.valueOf(94.50).compareTo(result.score().overallScore()));
        assertEquals(3, result.score().evaluationCount());
    }

    @Test
    @DisplayName("停用供应商不允许新建采购单")
    void shouldRejectPurchaseOrderWhenSupplierInactive() {
        when(purchaseStore.findSupplier("T1", "SUP-2")).thenReturn(Optional.of(new Supplier(
                "SUP-2",
                "T1",
                "停用供应商",
                "停用供应商有限公司",
                "李四",
                "CN",
                "B",
                10,
                100,
                Supplier.SupplierStatus.INACTIVE.name(),
                Instant.now(),
                Instant.now())));

        BizException ex = assertThrows(
                BizException.class,
                () -> purchaseService.createPurchaseOrder(
                        "T1",
                        new PurchaseService.CreatePurchaseOrderCommand(
                                "SUP-2",
                                "CNY",
                                "30D",
                                "FOB",
                                "STANDARD",
                                Instant.parse("2026-05-20T00:00:00Z"),
                                "test",
                                List.of(new PurchaseOrderLine(
                                        "LINE-1",
                                        "PROD-1",
                                        "SKU-1",
                                        10,
                                        0,
                                        BigDecimal.TEN,
                                        BigDecimal.valueOf(100),
                                        Instant.parse("2026-05-20T00:00:00Z"))))));

        assertEquals("SUPPLIER_DISABLED", ex.getCode());
        verifyNoInteractions(fmsClient, wmsClient);
    }

    @Test
    @DisplayName("新增供应商评估后应刷新评分汇总")
    void shouldRefreshSupplierScoreWhenEvaluatingSupplier() {
        Supplier supplier = new Supplier(
                "SUP-1",
                "T1",
                "优质供应商",
                "优质供应商有限公司",
                "张三",
                "CN",
                "A",
                7,
                50,
                Supplier.SupplierStatus.ACTIVE.name(),
                Instant.now(),
                Instant.now());
        when(purchaseStore.findSupplier("T1", "SUP-1")).thenReturn(Optional.of(supplier));
        when(purchaseStore.saveEvaluation(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(purchaseStore.listEvaluations("T1", "SUP-1")).thenReturn(List.of(new SupplierEvaluation(
                "EVAL-1",
                "T1",
                "SUP-1",
                BigDecimal.valueOf(90),
                BigDecimal.valueOf(95),
                BigDecimal.valueOf(85),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(92.50),
                "表现稳定",
                Instant.now())));
        when(purchaseStore.saveSupplierScore(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SupplierEvaluation result = purchaseService.evaluateSupplier(
                "T1",
                new PurchaseService.EvaluateSupplierCommand(
                        "SUP-1",
                        BigDecimal.valueOf(90),
                        BigDecimal.valueOf(95),
                        BigDecimal.valueOf(85),
                        BigDecimal.valueOf(100),
                        "表现稳定"));

        assertEquals(0, BigDecimal.valueOf(92.50).compareTo(result.overallScore()));
        ArgumentCaptor<SupplierScore> captor = ArgumentCaptor.forClass(SupplierScore.class);
        verify(purchaseStore).saveSupplierScore(captor.capture());
        assertEquals(0, BigDecimal.valueOf(92.50).compareTo(captor.getValue().overallScore()));
        assertEquals(1, captor.getValue().evaluationCount());
    }
}
