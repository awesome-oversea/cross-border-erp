package com.aidotnet.erp.scm.client;

import com.aidotnet.erp.common.api.Result;
import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "erp-app", contextId = "scm-fms-client", path = "/fms/api/in/v1")
public interface FmsClient {

    @PostMapping("/cost-events")
    Result<CostEventResponse> recordCostEvent(@RequestBody RecordCostEventRequest request);

    @PostMapping("/payment-requests")
    Result<PaymentRequestResponse> createPaymentRequest(@RequestBody CreatePaymentRequestRequest request);

    @GetMapping("/payment-requests")
    Result<List<PaymentRequestResponse>> listPaymentRequests(@RequestParam("poId") String poId);

    record RecordCostEventRequest(String costType, String sourceType, String sourceId, String sellerSku,
                                  String marketplaceId, String currency, BigDecimal amount, Instant occurredAt) {}

    record CostEventResponse(String costEventId, String costType, String sourceType, String sourceId, String sellerSku) {}

    record CreatePaymentRequestRequest(
            String requestId,
            String poId,
            String supplierId,
            BigDecimal amount,
            String currency,
            String requestType,
            String requestedBy,
            JsonNode approvalFlow) {}

    record PaymentRequestResponse(
            String requestId,
            String tenantId,
            String poId,
            String supplierId,
            BigDecimal amount,
            String currency,
            String requestType,
            String status,
            String requestedBy,
            String approvalFlow,
            String paidBy,
            Instant paidAt,
            String writeoffStatus,
            BigDecimal writeoffAmount,
            Instant createdAt,
            Instant updatedAt) {}
}
