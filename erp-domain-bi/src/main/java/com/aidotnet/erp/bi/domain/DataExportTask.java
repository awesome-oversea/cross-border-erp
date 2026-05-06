package com.aidotnet.erp.bi.domain;

import java.time.Instant;

public record DataExportTask(String taskId, String tenantId, String exportName, String exportType,
                             String format, String status, String fileUrl, long recordCount,
                             String error, Instant createdAt, Instant completedAt) {

    public enum ExportFormat {
        CSV, EXCEL, PDF, JSON
    }

    public enum ExportStatus {
        PENDING, RUNNING, COMPLETED, FAILED
    }
}
