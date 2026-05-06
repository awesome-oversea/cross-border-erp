package com.aidotnet.erp.sys.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.sys.domain.ManualImportTask;
import com.aidotnet.erp.sys.infrastructure.SysExtStore;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 手工导入应用服务
 * <p>
 * 描述: 系统设置域手工导入服务，负责模板管理、数据导入、
 *       导入结果记录等业务逻辑。支持CSV/Excel格式的批量数据导入。
 * </p>
 * <p>
 * 核心能力:
 *   1. 模板管理 - 创建/更新/删除导入模板，定义字段映射和校验规则
 *   2. 数据导入 - 按模板解析导入文件，校验数据格式，批量写入
 *   3. 导入记录 - 记录每次导入的结果(成功/失败/跳过行数)
 * </p>
 *
 * @author ERP系统
 */
@Service
public class ManualImportService {
    private static final Logger log = LoggerFactory.getLogger(ManualImportService.class);
    private static final List<String> SUPPORTED_TYPES = List.of(
            "PRODUCT", "ORDER", "INVENTORY", "SUPPLIER", "CUSTOMER", "COST", "EXCHANGE_RATE");
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;

    private final SysExtStore extStore;

    public ManualImportService(SysExtStore extStore) {
        this.extStore = extStore;
    }

    @Transactional
    public ManualImportTask createImportTask(String tenantId, CreateImportCommand command) {
        validateImportType(command.importType());
        validateFileSize(command.fileSize());
        Instant now = Instant.now();
        ManualImportTask task = new ManualImportTask(
                UUID.randomUUID().toString(), tenantId, command.importType(), command.fileName(),
                command.fileSize(), "PENDING", 0, 0, 0,
                command.columnMapping(), null, command.importedBy(), now, null);
        return extStore.saveManualImportTask(task);
    }

    @Transactional
    public ManualImportTask startImport(String tenantId, String taskId) {
        ManualImportTask task = getTask(tenantId, taskId);
        if (!"PENDING".equals(task.status())) {
            throw new BizException("IMPORT_STATUS_INVALID", "只有PENDING状态的任务可以开始");
        }
        Instant now = Instant.now();
        ManualImportTask started = new ManualImportTask(
                task.taskId(), task.tenantId(), task.importType(), task.fileName(),
                task.fileSize(), "PROCESSING", task.totalRows(), task.successRows(),
                task.failedRows(), task.columnMapping(), task.errorReportUrl(),
                task.importedBy(), task.startedAt(), now);
        return extStore.saveManualImportTask(started);
    }

    @Transactional
    public ManualImportTask completeImport(String tenantId, String taskId, ImportResult result) {
        ManualImportTask task = getTask(tenantId, taskId);
        if (!"PROCESSING".equals(task.status())) {
            throw new BizException("IMPORT_STATUS_INVALID", "只有PROCESSING状态的任务可以完成");
        }
        Instant now = Instant.now();
        ManualImportTask completed = new ManualImportTask(
                task.taskId(), task.tenantId(), task.importType(), task.fileName(),
                task.fileSize(), "COMPLETED", result.totalRows(), result.successRows(),
                result.failedRows(), task.columnMapping(), result.errorReportUrl(),
                task.importedBy(), task.startedAt(), now);
        return extStore.saveManualImportTask(completed);
    }

    @Transactional
    public ManualImportTask failImport(String tenantId, String taskId, String errorMessage) {
        ManualImportTask task = getTask(tenantId, taskId);
        if (!"PROCESSING".equals(task.status()) && !"PENDING".equals(task.status())) {
            throw new BizException("IMPORT_STATUS_INVALID", "任务状态不允许标记为失败");
        }
        Instant now = Instant.now();
        ManualImportTask failed = new ManualImportTask(
                task.taskId(), task.tenantId(), task.importType(), task.fileName(),
                task.fileSize(), "FAILED", task.totalRows(), task.successRows(),
                task.failedRows(), task.columnMapping(), errorMessage,
                task.importedBy(), task.startedAt(), now);
        return extStore.saveManualImportTask(failed);
    }

    public ManualImportTask getTask(String tenantId, String taskId) {
        return extStore.findManualImportTask(tenantId, taskId)
                .orElseThrow(() -> new BizException("IMPORT_TASK_NOT_FOUND", "导入任务不存在"));
    }

    public List<ManualImportTask> listTasks(String tenantId, String importType) {
        return extStore.listManualImportTasks(tenantId, importType);
    }

    private void validateImportType(String importType) {
        if (importType == null || !SUPPORTED_TYPES.contains(importType.toUpperCase())) {
            throw new BizException("IMPORT_TYPE_INVALID", "不支持的导入类型: " + importType);
        }
    }

    private void validateFileSize(long fileSize) {
        if (fileSize <= 0) {
            throw new BizException("IMPORT_FILE_INVALID", "文件大小无效");
        }
        if (fileSize > MAX_FILE_SIZE) {
            throw new BizException("IMPORT_FILE_TOO_LARGE", "文件大小超过50MB限制");
        }
    }

    public record CreateImportCommand(
            String importType, String fileName, long fileSize,
            Map<String, Object> columnMapping, String importedBy) {}

    public record ImportResult(
            int totalRows, int successRows, int failedRows, String errorReportUrl) {}
}
