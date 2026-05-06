package com.aidotnet.erp.sys.domain;

import java.time.Instant;

public record AIFeatureToggle(String toggleId, String tenantId, String featureCode, String featureName,
                              String domain, boolean enabled, String description, String configJson,
                              Instant createdAt, Instant updatedAt) {}
