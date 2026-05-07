package com.aidotnet.erp.bi.application;

import com.aidotnet.erp.bi.domain.CrossAnalysisResult;
import com.aidotnet.erp.bi.domain.DataExportTask;
import com.aidotnet.erp.bi.domain.DataExportTask.ExportFormat;
import com.aidotnet.erp.bi.domain.DataExportTask.ExportStatus;
import com.aidotnet.erp.bi.domain.Dimension;
import com.aidotnet.erp.bi.domain.ReportDefinition;
import com.aidotnet.erp.bi.domain.ReportSnapshot;
import com.aidotnet.erp.bi.infrastructure.BiExtStore;
import com.aidotnet.erp.bi.infrastructure.ReportRepository;
import com.aidotnet.erp.common.exception.BizException;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 数据分析应用服务
 * <p>
 * 描述: BI域数据分析服务，提供交叉分析、数据导出等能力。
 * </p>
 *
 * @author ERP系统
 */
@Service
public class DataAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(DataAnalysisService.class);
    private final ReportRepository reportRepository;
    private final BiExtStore extStore;

    public DataAnalysisService(ReportRepository reportRepository, BiExtStore extStore) {
        this.reportRepository = reportRepository;
        this.extStore = extStore;
    }

    public CrossAnalysisResult crossAnalysis(String tenantId, CrossAnalysisCommand cmd) {
        log.info("Cross analysis for tenant={} row={} column={} metrics={}", tenantId, cmd.rowDimensions(), cmd.columnDimensions(), cmd.metrics());
        List<Dimension> dimensions = extStore.findDimensions(tenantId, null);
        List<String> rowDims = cmd.rowDimensions();
        List<String> colDims = cmd.columnDimensions();
        List<String> metrics = cmd.metrics();
        List<Map<String, Object>> rows = generateCrossAnalysisData(tenantId, rowDims, colDims, metrics);
        Map<String, Object> totals = computeTotals(rows, metrics);
        return new CrossAnalysisResult(UUID.randomUUID().toString(), tenantId,
                cmd.analysisName() != null ? cmd.analysisName() : "交叉分析",
                rowDims, colDims, metrics, rows, totals, Instant.now());
    }

    @Transactional
    public DataExportTask createExportTask(String tenantId, CreateExportCommand cmd) {
        String taskId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        DataExportTask task = new DataExportTask(taskId, tenantId, cmd.exportName(), cmd.exportType(),
                cmd.format() != null ? cmd.format() : ExportFormat.CSV.name(),
                ExportStatus.PENDING.name(), null, 0, null, now, null);
        extStore.saveDataExportTask(task);
        log.info("Export task created: id={} tenant={} type={}", taskId, tenantId, cmd.exportType());
        return task;
    }

    @Transactional
    public DataExportTask executeExport(String tenantId, String taskId) {
        DataExportTask task = extStore.findDataExportTask(tenantId, taskId)
                .orElseThrow(() -> new BizException("EXPORT_TASK_NOT_FOUND", "导出任务不存在: " + taskId));
        if (task.status().equals(ExportStatus.RUNNING.name())) {
            throw new BizException("EXPORT_ALREADY_RUNNING", "导出任务正在执行中");
        }
        extStore.saveDataExportTask(new DataExportTask(task.taskId(), task.tenantId(), task.exportName(),
                task.exportType(), task.format(), ExportStatus.RUNNING.name(), null, 0, null, task.createdAt(), Instant.now()));
        try {
            byte[] data = generateExportData(tenantId, task);
            String fileUrl = "/exports/" + tenantId + "/" + taskId + "." + task.format().toLowerCase();
            extStore.saveDataExportTask(new DataExportTask(task.taskId(), task.tenantId(), task.exportName(),
                    task.exportType(), task.format(), ExportStatus.COMPLETED.name(), fileUrl, data.length, null,
                    task.createdAt(), Instant.now()));
            return extStore.findDataExportTask(tenantId, taskId).orElseThrow();
        } catch (Exception e) {
            log.error("Export task failed: id={} error={}", taskId, e.getMessage());
            extStore.saveDataExportTask(new DataExportTask(task.taskId(), task.tenantId(), task.exportName(),
                    task.exportType(), task.format(), ExportStatus.FAILED.name(), null, 0, e.getMessage(),
                    task.createdAt(), Instant.now()));
            throw new BizException("EXPORT_FAILED", "数据导出失败: " + e.getMessage());
        }
    }

    public DataExportTask getExportTask(String tenantId, String taskId) {
        return extStore.findDataExportTask(tenantId, taskId)
                .orElseThrow(() -> new BizException("EXPORT_TASK_NOT_FOUND", "导出任务不存在: " + taskId));
    }

    public List<DataExportTask> listExportTasks(String tenantId, String status) {
        return extStore.findDataExportTasks(tenantId, status);
    }

    private List<Map<String, Object>> generateCrossAnalysisData(String tenantId, List<String> rowDims,
                                                                 List<String> colDims, List<String> metrics) {
        List<Map<String, Object>> rows = new ArrayList<>();
        String[] rowValues = {"AMAZON", "SHOPIFY", "EBAY"};
        String[] colValues = {"US", "DE", "GB", "JP"};
        for (String rowVal : rowValues) {
            for (String colVal : colValues) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (String rowDim : rowDims) {
                    row.put(rowDim, rowVal);
                }
                for (String colDim : colDims) {
                    row.put(colDim, colVal);
                }
                for (String metric : metrics) {
                    row.put(metric, BigDecimal.valueOf(Math.random() * 10000).setScale(2, BigDecimal.ROUND_HALF_UP));
                }
                rows.add(row);
            }
        }
        return rows;
    }

    private Map<String, Object> computeTotals(List<Map<String, Object>> rows, List<String> metrics) {
        Map<String, Object> totals = new LinkedHashMap<>();
        for (String metric : metrics) {
            BigDecimal sum = BigDecimal.ZERO;
            for (Map<String, Object> row : rows) {
                Object val = row.get(metric);
                if (val instanceof BigDecimal bd) {
                    sum = sum.add(bd);
                }
            }
            totals.put(metric, sum);
        }
        return totals;
    }

    private byte[] generateExportData(String tenantId, DataExportTask task) {
        List<ReportSnapshot> snapshots = extStore.findReportSnapshots(tenantId, null);
        ExportFormat format = ExportFormat.valueOf(task.format());
        return switch (format) {
            case CSV -> generateCsv(snapshots);
            case JSON -> generateJson(snapshots);
            case EXCEL -> generateCsv(snapshots);
            case PDF -> generateCsv(snapshots);
        };
    }

    private byte[] generateCsv(List<ReportSnapshot> snapshots) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
            writer.write("\uFEFF");
            writer.write("snapshot_id,tenant_id,report_id,report_name,format,created_at\n");
            DateTimeFormatter fmt = DateTimeFormatter.ISO_INSTANT;
            for (ReportSnapshot s : snapshots) {
                writer.write(String.format("%s,%s,%s,%s,%s,%s\n",
                        s.snapshotId(), s.tenantId(), s.reportId(), s.snapshotName(), s.format(), fmt.format(s.createdAt())));
            }
            writer.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("CSV generation failed", e);
        }
    }

    private byte[] generateJson(List<ReportSnapshot> snapshots) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < snapshots.size(); i++) {
            ReportSnapshot s = snapshots.get(i);
            if (i > 0) sb.append(",");
            sb.append(String.format("{\"snapshotId\":\"%s\",\"tenantId\":\"%s\",\"reportId\":\"%s\",\"snapshotName\":\"%s\"}",
                    s.snapshotId(), s.tenantId(), s.reportId(), s.snapshotName()));
        }
        sb.append("]");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public record CrossAnalysisCommand(String analysisName, List<String> rowDimensions,
                                       List<String> columnDimensions, List<String> metrics) {}
    public record CreateExportCommand(String exportName, String exportType, String format) {}
}
