package com.aidotnet.erp.tms.client;

import com.aidotnet.erp.common.api.Result;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "erp-app", contextId = "tms-fms-client", path = "/fms/api/in/v1")
public interface FmsClient {

    @PostMapping("/cost-events")
    Result<CostEventResponse> recordCostEvent(@RequestBody RecordCostEventRequest request);

    record RecordCostEventRequest(String costType, String sourceType, String sourceId, String sellerSku,
                                  String marketplaceId, String currency, BigDecimal amount, Instant occurredAt) {}

    record CostEventResponse(String costEventId, String costType, String sourceType, String sourceId, String sellerSku) {}
}
