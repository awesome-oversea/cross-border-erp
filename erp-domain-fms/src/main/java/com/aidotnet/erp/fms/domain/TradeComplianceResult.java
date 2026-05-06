package com.aidotnet.erp.fms.domain;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record TradeComplianceResult(
        String resultId, String tenantId, String orderId, String sellerSku,
        String hsCode, String originCountry, String destinationCountry,
        String status, List<String> violations, List<String> warnings,
        Map<String, Object> details, Instant checkedAt) {}
