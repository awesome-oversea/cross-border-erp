package com.aidotnet.erp.sys.domain;

import java.time.Instant;

public record ConnectorSecret(
        String secretId,
        String tenantId,
        String configId,
        String keyType,
        String encryptedValue,
        String maskedPreview,
        String lastRotatedBy,
        Instant lastRotatedAt,
        Instant createdAt,
        Instant updatedAt) {}
