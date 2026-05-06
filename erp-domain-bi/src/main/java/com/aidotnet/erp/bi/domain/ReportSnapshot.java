package com.aidotnet.erp.bi.domain;

import java.time.Instant;

/**
 * 报表运行快照，记录一次报表执行后的输出内容与生成时间。
 */
public record ReportSnapshot(String snapshotId, String tenantId, String reportId, String snapshotName,
                             String snapshotData, String format, Instant snapshotAt, Instant createdAt) {}
