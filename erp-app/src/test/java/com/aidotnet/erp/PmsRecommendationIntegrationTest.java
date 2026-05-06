package com.aidotnet.erp;

import com.aidotnet.erp.common.api.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PMS推荐集成测试")
class PmsRecommendationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("提交PMS推荐应返回200")
    void submitRecommendation() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("tenant_id", "T1");
        headers.set("actor_type", "AI");
        headers.set("actor_id", "agent-1");
        headers.set("scope", "oms:write");
        headers.set("purpose", "optimization");
        headers.set("trace_id", "trace-1");
        headers.set("idempotency_key", "idem-int-1");
        headers.set("source_system", "PMS");
        headers.set("signature", "sig");

        String body = """
                {
                  "recommendation_id": "REC-INT-001",
                  "domain": "OMS",
                  "recommendation_type": "PRICE_OPTIMIZATION",
                  "object_type": "RECOMMENDATION",
                  "content": "建议调整价格",
                  "evidence_chain_id": "EV-INT-001",
                  "requested_action": "UPDATE_PRICE"
                }
                """;
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<Result> response = restTemplate.postForEntity(
                "/sys/api/in/v1/pms/recommendations", request, Result.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().success());
    }
}
