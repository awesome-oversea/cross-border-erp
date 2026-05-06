package com.aidotnet.erp.common.integration;

import java.time.Instant;

public record ConnectorRegistration(String tenantId, String connectorCode, String name, boolean enabled,
                                    Instant registeredAt) {

    public ConnectorRegistration withEnabled(boolean enabled) {
        return new ConnectorRegistration(tenantId, connectorCode, name, enabled, registeredAt);
    }
}
