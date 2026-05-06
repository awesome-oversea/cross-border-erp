package com.aidotnet.erp.bi.domain;

import java.time.Instant;
import java.util.List;

public record MetricCaliber(
        String caliberId,
        String tenantId,
        String metricCode,
        String metricName,
        String category,
        String caliberType,
        String formula,
        String formulaDescription,
        String numeratorMetric,
        String denominatorMetric,
        String unit,
        String dataSource,
        String calculationScope,
        List<String> dimensions,
        List<String> excludeConditions,
        String permissionCode,
        String dataLevel,
        boolean enabled,
        String version,
        String description,
        Instant createdAt,
        Instant updatedAt) {}
