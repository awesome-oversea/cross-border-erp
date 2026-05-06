package com.aidotnet.erp.sys.domain;

import java.time.Instant;

public record SystemConfig(String configId, String tenantId, String configKey, String configValue,
                           String description, boolean enabled, Instant createdAt, Instant updatedAt) {}
