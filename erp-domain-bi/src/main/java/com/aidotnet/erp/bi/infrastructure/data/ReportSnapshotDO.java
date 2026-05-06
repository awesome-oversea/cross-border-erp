package com.aidotnet.erp.bi.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 报表快照数据对象
 * <p>
 * 描述: 对应bi_report_snapshot表，用于存储报表执行后的快照数据。
 *       每次执行报表都会生成一条快照记录，包含执行时间、输出数据和格式，
 *       支持报表数据版本管理和历史数据追溯。
 * </p>
 * <p>
 * 业务规则:
 *   1. 快照数据(snapshotData)以JSON格式存储，支持大数据量报表输出
 *   2. 快照按时间倒序排列，最新快照排在前面
 *   3. 快照可通过报表ID关联查询，支持同一报表多次执行的历史记录
 * </p>
 *
 * @author ERP系统
 * @see com.aidotnet.erp.bi.domain.ReportSnapshot
 */
@TableName("bi_report_snapshot")
public class ReportSnapshotDO {

    @TableId(type = IdType.ASSIGN_ID)
    private String snapshotId;
    private String tenantId;
    private String reportId;
    private String snapshotName;
    private String snapshotData;
    private String format;
    private Instant snapshotAt;
    private Instant createdAt;

    public ReportSnapshotDO() {}

    public String getSnapshotId() { return snapshotId; }
    public void setSnapshotId(String snapshotId) { this.snapshotId = snapshotId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getReportId() { return reportId; }
    public void setReportId(String reportId) { this.reportId = reportId; }
    public String getSnapshotName() { return snapshotName; }
    public void setSnapshotName(String snapshotName) { this.snapshotName = snapshotName; }
    public String getSnapshotData() { return snapshotData; }
    public void setSnapshotData(String snapshotData) { this.snapshotData = snapshotData; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public Instant getSnapshotAt() { return snapshotAt; }
    public void setSnapshotAt(Instant snapshotAt) { this.snapshotAt = snapshotAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
