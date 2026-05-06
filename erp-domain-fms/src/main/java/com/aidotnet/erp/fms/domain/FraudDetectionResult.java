package com.aidotnet.erp.fms.domain;

import java.time.Instant;
import java.util.Map;

public record FraudDetectionResult(
        String resultId,
        String tenantId,
        String orderId,
        String buyerId,
        String detectionType,
        String severity,
        String description,
        Map<String, Object> indicators,
        String status,
        Instant detectedAt
) {
    public enum DetectionType {
        STOLEN_CARD,
        RETURN_ABUSE,
        FAKE_SHIPPING,
        SUSPICIOUS_IP,
        VELOCITY_ABUSE,
        ADDRESS_MISMATCH,
        OTHER
    }
}
