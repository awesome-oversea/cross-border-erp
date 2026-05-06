package com.aidotnet.erp.pdm.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.aidotnet.erp.common.event.DomainEventPublisher;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.pdm.domain.Brand;
import com.aidotnet.erp.pdm.domain.Category;
import com.aidotnet.erp.pdm.domain.DevStage;
import com.aidotnet.erp.pdm.domain.ProductDevelopment;
import com.aidotnet.erp.pdm.domain.ProductStatus;
import com.aidotnet.erp.pdm.domain.ProposalStatus;
import com.aidotnet.erp.pdm.domain.SelectionProposal;
import com.aidotnet.erp.pdm.domain.Spu;
import com.aidotnet.erp.pdm.infrastructure.ProductStore;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PDM产品开发流程测试")
class ProductDevelopmentFlowServiceTest {

    private ProductStore productStore;
    private ProductService productService;

    @BeforeEach
    void setUp() {
        productStore = mock(ProductStore.class);
        productService = new ProductService(productStore, mock(DomainEventPublisher.class));
    }

    @Test
    @DisplayName("未审批提报不可创建开发流程")
    void shouldRejectDevelopmentCreationWhenProposalNotApproved() {
        when(productStore.findSpu("T1", "SPU-1")).thenReturn(Optional.of(spu("SPU-1")));
        when(productStore.findProposal("T1", "P-1")).thenReturn(Optional.of(proposal("P-1", ProposalStatus.SUBMITTED)));

        BizException ex = assertThrows(BizException.class, () -> productService.createDevelopment(
                "T1",
                new ProductService.CreateDevCommand("SPU-1", "P-1", "alice", "bob", "carol", 8)));

        assertEquals("PROPOSAL_STATUS_INVALID", ex.getCode());
    }

    @Test
    @DisplayName("已审批提报创建开发流程后应转为已转化")
    void shouldConvertApprovedProposalWhenCreatingDevelopment() {
        SelectionProposal approved = proposal("P-2", ProposalStatus.APPROVED);
        when(productStore.findSpu("T1", "SPU-1")).thenReturn(Optional.of(spu("SPU-1")));
        when(productStore.findProposal("T1", "P-2")).thenReturn(Optional.of(approved));
        when(productStore.saveProposal(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(productStore.saveDevelopment(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ProductDevelopment result = productService.createDevelopment(
                "T1",
                new ProductService.CreateDevCommand("SPU-1", "P-2", "alice", "bob", "carol", 8));

        assertEquals(DevStage.RESEARCH, result.stage());
        assertEquals("active", result.status());
        verify(productStore).saveProposal(any(SelectionProposal.class));
    }

    @Test
    @DisplayName("开发阶段不可跳级")
    void shouldRejectSkippingDevelopmentStage() {
        when(productStore.findDevelopment("T1", "DEV-1"))
                .thenReturn(Optional.of(development("DEV-1", DevStage.RESEARCH, "active", "alice", 5)));

        BizException ex = assertThrows(BizException.class, () -> productService.updateDevStage(
                "T1", "DEV-1", DevStage.TESTING, "skip"));

        assertEquals("DEV_STAGE_INVALID", ex.getCode());
    }

    @Test
    @DisplayName("开发阶段进入LISTED后应自动完成")
    void shouldCompleteDevelopmentWhenMovedToListed() {
        when(productStore.findDevelopment("T1", "DEV-2"))
                .thenReturn(Optional.of(development("DEV-2", DevStage.MASS_PRODUCTION, "active", "alice", 5)));
        when(productStore.saveDevelopment(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ProductDevelopment result = productService.updateDevStage("T1", "DEV-2", DevStage.LISTED, "ready");

        assertEquals(DevStage.LISTED, result.stage());
        assertEquals("completed", result.status());
    }

    @Test
    @DisplayName("批量分配应更新全部目标开发流程")
    void shouldBatchAssignDevelopmentTeam() {
        when(productStore.findDevelopment("T1", "DEV-10"))
                .thenReturn(Optional.of(development("DEV-10", DevStage.RESEARCH, "active", "old-a", 2)));
        when(productStore.findDevelopment("T1", "DEV-11"))
                .thenReturn(Optional.of(development("DEV-11", DevStage.SAMPLING, "active", "old-b", 3)));
        when(productStore.saveDevelopment(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<ProductDevelopment> result = productService.batchAssignDevTeam(
                "T1",
                List.of("DEV-10", "DEV-11"),
                new ProductService.AssignDevTeamCommand("alice", "bob", "carol", 9));

        assertEquals(2, result.size());
        assertEquals("alice", result.get(0).developer());
        assertEquals("alice", result.get(1).developer());
        assertEquals(9, result.get(0).priority());
        assertEquals(9, result.get(1).priority());
    }

    @Test
    @DisplayName("开发统计应返回阶段数量负责人负载和平均进度")
    void shouldAggregateDevelopmentStats() {
        when(productStore.listDevelopments("T1")).thenReturn(List.of(
                development("DEV-20", DevStage.RESEARCH, "active", "alice", 5),
                development("DEV-21", DevStage.MASS_PRODUCTION, "active", "alice", 7),
                development("DEV-22", DevStage.ARCHIVED, "completed", "bob", 4)));

        ProductService.DevelopmentStats result = productService.getDevelopmentStats("T1");

        assertEquals(3, result.totalCount());
        assertEquals(2, result.activeCount());
        assertEquals(1, result.completedCount());
        assertEquals(1L, result.stageCounts().get(DevStage.RESEARCH));
        assertEquals(1L, result.stageCounts().get(DevStage.MASS_PRODUCTION));
        assertEquals(2L, result.developerWorkload().get("alice"));
        assertEquals(57, result.averageProgressPercent());
    }

    private Spu spu(String spuId) {
        return new Spu(
                spuId,
                "T1",
                "SPU-CODE-" + spuId,
                "Test SPU",
                "desc",
                "CAT-1",
                "BR-1",
                ProductStatus.DRAFT,
                "none",
                Instant.now(),
                Instant.now());
    }

    private SelectionProposal proposal(String proposalId, ProposalStatus status) {
        return new SelectionProposal(
                proposalId,
                "T1",
                "Test Product",
                "Test Title",
                "CAT-1",
                "MANUAL",
                "SRC-1",
                BigDecimal.TEN,
                "{}",
                "{}",
                "{}",
                false,
                status,
                "submitter",
                null,
                null,
                Instant.now(),
                Instant.now());
    }

    private ProductDevelopment development(String devId, DevStage stage, String status, String developer, int priority) {
        return new ProductDevelopment(
                devId,
                "T1",
                "SPU-1",
                "P-1",
                stage,
                "note",
                developer,
                "editor",
                "designer",
                priority,
                status,
                Instant.now(),
                Instant.now());
    }
}
