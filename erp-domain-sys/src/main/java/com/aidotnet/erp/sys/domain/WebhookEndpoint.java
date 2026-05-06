package com.aidotnet.erp.sys.domain;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record WebhookEndpoint(
        String endpointId, String tenantId, String name, String url, String eventType,
        Map<String, String> headers, String secret, boolean active, int retryCount,
        int timeoutSeconds, List<String> subscribedEvents, Instant createdAt, Instant updatedAt) {}
