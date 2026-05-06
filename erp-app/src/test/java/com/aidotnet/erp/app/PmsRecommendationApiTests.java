package com.aidotnet.erp.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@ErpAppTest
@AutoConfigureMockMvc
class PmsRecommendationApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void submitRecommendationIsIdempotentAndTenantScoped() throws Exception {
        String body = recommendationBody("pms-rec-001", "PDM", "RECOMMENDATION");
        String response = mockMvc.perform(pmsHeaders(post("/api/pms/v1/recommendations"), "tenant-pms", "idem-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.data.approvalPolicy").value("MANUAL_APPROVAL_REQUIRED"))
                .andExpect(jsonPath("$.data.auditId").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        String erpReferenceId = objectMapper.readTree(response).at("/data/erpReferenceId").asText();

        mockMvc.perform(pmsHeaders(post("/api/pms/v1/recommendations"), "tenant-pms", "idem-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.erpReferenceId").value(erpReferenceId));

        mockMvc.perform(pmsHeaders(get("/api/pms/v1/recommendations/" + erpReferenceId), "tenant-other", "read-001"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PMS_RECOMMENDATION_NOT_FOUND"));
    }

    @Test
    void recommendationRequiresPmsSecurityHeadersAndSourceSystem() throws Exception {
        mockMvc.perform(post("/api/pms/v1/recommendations")
                        .header("tenant_id", "tenant-pms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recommendationBody("pms-rec-002", "SCM", "DRAFT")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PMS_REQUEST_INVALID"));

        mockMvc.perform(pmsHeaders(post("/api/pms/v1/recommendations"), "tenant-pms", "idem-002")
                        .header("source_system", "OTHER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recommendationBody("pms-rec-002", "SCM", "DRAFT")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PMS_SOURCE_INVALID"));
    }

    @Test
    void approvalAndExecutionStateMachinePreventsBypass() throws Exception {
        String response = mockMvc.perform(pmsHeaders(post("/api/pms/v1/recommendations"), "tenant-flow", "idem-flow-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(recommendationBody("pms-rec-flow", "SOM", "PENDING_ACTION")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String erpReferenceId = objectMapper.readTree(response).at("/data/erpReferenceId").asText();

        mockMvc.perform(pmsHeaders(patch("/api/pms/v1/recommendations/" + erpReferenceId + "/start-execution"), "tenant-flow",
                        "idem-flow-start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PMS_STATUS_TRANSITION_INVALID"));

        mockMvc.perform(pmsHeaders(patch("/api/pms/v1/recommendations/" + erpReferenceId + "/submit-approval"), "tenant-flow",
                        "idem-flow-submit-approval")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"approval_policy\":\"OPS_MANAGER_REQUIRED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING_APPROVAL"));

        mockMvc.perform(pmsHeaders(patch("/api/pms/v1/recommendations/" + erpReferenceId + "/approve"), "tenant-flow",
                        "idem-flow-approve"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("APPROVED"));

        mockMvc.perform(pmsHeaders(patch("/api/pms/v1/recommendations/" + erpReferenceId + "/start-execution"), "tenant-flow",
                        "idem-flow-exec"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("EXECUTING"));

        mockMvc.perform(pmsHeaders(patch("/api/pms/v1/recommendations/" + erpReferenceId + "/complete-execution"), "tenant-flow",
                        "idem-flow-complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"execution_result\":\"created listing draft only, no formal publish\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("EXECUTED"))
                .andExpect(jsonPath("$.data.executionResult").value("created listing draft only, no formal publish"));

        mockMvc.perform(pmsHeaders(patch("/api/pms/v1/recommendations/" + erpReferenceId + "/measure"), "tenant-flow",
                        "idem-flow-measure")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"measured_result\":\"BI KPI pending observation\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("MEASURED"));
    }

    private static MockHttpServletRequestBuilder pmsHeaders(MockHttpServletRequestBuilder builder, String tenantId, String idempotencyKey) {
        return builder.header("tenant_id", tenantId)
                .header("actor_id", "agent-product-001")
                .header("actor_type", "agent")
                .header("agent_id", "pms-agent-001")
                .header("scope", "store:amazon-us,category:home")
                .header("purpose", "ai_recommendation_submit")
                .header("trace_id", "trace-" + idempotencyKey)
                .header("idempotency_key", idempotencyKey)
                .header("source_system", "PMS")
                .header("signature", "mock-signature");
    }

    private static String recommendationBody(String recommendationId, String domain, String objectType) {
        return "{"
                + "\"recommendation_id\":\"" + recommendationId + "\","
                + "\"domain\":\"" + domain + "\","
                + "\"recommendation_type\":\"AI_SUGGESTION\","
                + "\"object_type\":\"" + objectType + "\","
                + "\"target_object_type\":\"SKU\","
                + "\"target_object_id\":\"SKU-001\","
                + "\"content\":\"PMS suggests creating an ERP-controlled draft and submitting approval.\","
                + "\"score\":87.5,"
                + "\"confidence\":0.82,"
                + "\"evidence_chain_id\":\"ev-chain-001\","
                + "\"data_sources\":[\"ERP_ORDER_SUMMARY\",\"MARKET_SIGNAL\"],"
                + "\"risk_flags\":[\"NEEDS_HUMAN_APPROVAL\"],"
                + "\"explainability\":\"based on ERP snapshot and PMS feature scoring\","
                + "\"requested_action\":\"CREATE_DRAFT_FOR_APPROVAL\""
                + "}";
    }
}
