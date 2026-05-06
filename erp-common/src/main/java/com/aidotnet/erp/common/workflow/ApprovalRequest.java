package com.aidotnet.erp.common.workflow;

import java.util.Map;
import java.util.Set;

public record ApprovalRequest(
        String requestId,
        String tenantId,
        String domain,
        String aggregateId,
        String action,
        String requestedBy,
        String currentStatus,
        Map<String, Object> payload
) {
    public ApprovalRequest {
        payload = payload != null ? Map.copyOf(payload) : Map.of();
    }
}
