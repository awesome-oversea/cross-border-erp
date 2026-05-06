package com.aidotnet.erp.common.integration;

import com.aidotnet.erp.common.exception.BizException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class IntegrationRegistry {

    private final Map<String, ConnectorRegistration> connectors = new ConcurrentHashMap<>();
    private final Map<String, ApiClient> clients = new ConcurrentHashMap<>();

    public ConnectorRegistration registerConnector(String tenantId, String connectorCode, String name) {
        ConnectorRegistration registration = new ConnectorRegistration(tenantId, connectorCode, name, true, Instant.now());
        connectors.put(tenantId + ":" + connectorCode, registration);
        return registration;
    }

    public ConnectorRegistration disableConnector(String tenantId, String connectorCode) {
        ConnectorRegistration connector = mustConnector(tenantId, connectorCode).withEnabled(false);
        connectors.put(tenantId + ":" + connectorCode, connector);
        return connector;
    }

    public ApiClient createApiClient(String tenantId, String clientCode) {
        String secret = "sk-" + UUID.randomUUID();
        ApiClient client = new ApiClient(tenantId, clientCode, hash(secret), true, secret);
        clients.put(tenantId + ":" + clientCode, client);
        return client;
    }

    public void authenticate(String tenantId, String clientCode, String secret) {
        ApiClient client = clients.get(tenantId + ":" + clientCode);
        if (client == null || !client.enabled() || !client.secretHash().equals(hash(secret))) {
            throw new BizException("AUTH_FAILED", "api client authentication failed");
        }
    }

    private ConnectorRegistration mustConnector(String tenantId, String connectorCode) {
        ConnectorRegistration connector = connectors.get(tenantId + ":" + connectorCode);
        if (connector == null) {
            throw new BizException("NOT_FOUND", "connector not found");
        }
        return connector;
    }

    private String hash(String secret) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(secret.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
