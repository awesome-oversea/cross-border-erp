package com.aidotnet.erp.sys.domain;

import java.time.Instant;
import java.util.Map;

public record ManualImportTask(
        String taskId, String tenantId, String importType, String fileName,
        long fileSize, String status, int totalRows, int successRows,
        int failedRows, Map<String, Object> columnMapping, String errorReportUrl,
        String importedBy, Instant startedAt, Instant completedAt) {}
