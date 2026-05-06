package com.aidotnet.erp.bi.domain;

import java.time.Instant;

public record ReportDefinition(String reportId, String tenantId, String reportCode, String reportName,
                               String reportType, String dataSource, String queryText,
                               String permissionCode, String dataLevel, String description,
                               Instant createdAt, Instant updatedAt) {}
