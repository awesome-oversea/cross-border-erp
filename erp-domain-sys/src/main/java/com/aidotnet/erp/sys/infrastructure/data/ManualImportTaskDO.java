package com.aidotnet.erp.sys.infrastructure.data;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.Instant;

/**
 * 手工导入任务数据对象(ManualImportTaskDO)
 * <p>
 * 描述: 手工导入任务数据对象，对应sys_manual_import_task表。
 *       记录Excel/CSV等手工数据导入任务的执行状态和结果。
 * </p>
 *
 * @author ERP系统
 */
@TableName("sys_manual_import_task")
public class ManualImportTaskDO {
    @TableId(type = IdType.ASSIGN_ID)
    private String taskId;
    private String tenantId;
    private String importType;
    private String fileName;
    private Long fileSize;
    private String status;
    private Integer totalRows;
    private Integer successRows;
    private Integer failedRows;
    private String columnMapping;
    private String errorReportUrl;
    private String importedBy;
    private Instant startedAt;
    private Instant completedAt;

    public ManualImportTaskDO() {}

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getImportType() { return importType; }
    public void setImportType(String importType) { this.importType = importType; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getTotalRows() { return totalRows; }
    public void setTotalRows(Integer totalRows) { this.totalRows = totalRows; }
    public Integer getSuccessRows() { return successRows; }
    public void setSuccessRows(Integer successRows) { this.successRows = successRows; }
    public Integer getFailedRows() { return failedRows; }
    public void setFailedRows(Integer failedRows) { this.failedRows = failedRows; }
    public String getColumnMapping() { return columnMapping; }
    public void setColumnMapping(String columnMapping) { this.columnMapping = columnMapping; }
    public String getErrorReportUrl() { return errorReportUrl; }
    public void setErrorReportUrl(String errorReportUrl) { this.errorReportUrl = errorReportUrl; }
    public String getImportedBy() { return importedBy; }
    public void setImportedBy(String importedBy) { this.importedBy = importedBy; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
