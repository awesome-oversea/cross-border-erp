package com.aidotnet.erp.sys.application;

import com.aidotnet.erp.sys.domain.PmsRecommendation;
import com.aidotnet.erp.sys.domain.PmsWriteObjectType;
import com.aidotnet.erp.sys.infrastructure.PmsRecommendationStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PMS推荐服务测试")
class PmsRecommendationServiceTest {

    private PmsRecommendationStore store;
    private PmsRecommendationService service;

    @BeforeEach
    void setUp() {
        store = new PmsRecommendationStore();
        service = new PmsRecommendationService(store);
    }

    @Test
    @DisplayName("提交推荐应生成erpReferenceId且状态为ACCEPTED")
    void submitRecommendation() {
        PmsRecommendationService.PmsSubmitCommand cmd = new PmsRecommendationService.PmsSubmitCommand(
                "REC-001", "OMS", "PRICE_OPTIMIZATION", PmsWriteObjectType.RECOMMENDATION,
                null, null, "建议调整价格", null, null, "EV-001", null, null, null, "UPDATE_PRICE");
        PmsRecommendationService.PmsCallContext ctx = new PmsRecommendationService.PmsCallContext(
                "T1", "agent-1", "AI", null, "oms:write", "optimization",
                "trace-1", "idem-1", "PMS", "sig");
        PmsRecommendation result = service.submit(cmd, ctx);
        assertNotNull(result);
        assertNotNull(result.erpReferenceId());
        assertEquals("ACCEPTED", result.status().name());
    }

    @Test
    @DisplayName("提交审批应流转为PENDING_APPROVAL")
    void submitForApproval() {
        PmsRecommendationService.PmsSubmitCommand cmd = new PmsRecommendationService.PmsSubmitCommand(
                "REC-002", "OMS", "PRICE_OPTIMIZATION", PmsWriteObjectType.RECOMMENDATION,
                null, null, "建议调价", null, null, "EV-002", null, null, null, "UPDATE_PRICE");
        PmsRecommendationService.PmsCallContext ctx = new PmsRecommendationService.PmsCallContext(
                "T1", "agent-1", "AI", null, "oms:write", "optimization",
                "trace-2", "idem-2", "PMS", "sig");
        PmsRecommendation submitted = service.submit(cmd, ctx);
        PmsRecommendation result = service.submitForApproval("T1", submitted.erpReferenceId(), "MANUAL_APPROVAL_REQUIRED");
        assertEquals("PENDING_APPROVAL", result.status().name());
    }

    @Test
    @DisplayName("幂等键重复应返回已有记录")
    void idempotencyKeyDedup() {
        PmsRecommendationService.PmsSubmitCommand cmd = new PmsRecommendationService.PmsSubmitCommand(
                "REC-003", "OMS", "PRICE_OPTIMIZATION", PmsWriteObjectType.RECOMMENDATION,
                null, null, "建议调价", null, null, "EV-003", null, null, null, "UPDATE_PRICE");
        PmsRecommendationService.PmsCallContext ctx = new PmsRecommendationService.PmsCallContext(
                "T1", "agent-1", "AI", null, "oms:write", "optimization",
                "trace-3", "idem-3", "PMS", "sig");
        PmsRecommendation first = service.submit(cmd, ctx);
        PmsRecommendation second = service.submit(cmd, ctx);
        assertEquals(first.erpReferenceId(), second.erpReferenceId());
    }
}
