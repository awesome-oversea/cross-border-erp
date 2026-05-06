package com.aidotnet.erp.sys.domain;

import java.time.Instant;
import java.util.Map;

public record WebhookDelivery(
        String deliveryId, String tenantId, String endpointId, String eventType,
        Map<String, Object> payload, int statusCode, String response,
        boolean success, int attemptCount, Instant nextRetryAt, Instant deliveredAt) {}
