package com.aidotnet.erp.common.integration;

public record ApiClient(String tenantId, String clientCode, String secretHash, boolean enabled, String plainSecretOnce) {
}
