package com.aidotnet.erp.bi.application;

import com.aidotnet.erp.bi.domain.CustomReport;
import com.aidotnet.erp.bi.domain.DeveloperCommissionReport;
import com.aidotnet.erp.bi.domain.ReportSnapshot;
import com.aidotnet.erp.bi.infrastructure.BiExtStore;
import com.aidotnet.erp.bi.infrastructure.ReportRepository;
import com.aidotnet.erp.common.exception.BizException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * BI 经营分析场景服务。
 * <p>
 * 职责:
 * 1. 管理自定义报表配置，确保维度/指标口径在 BI 域内可追溯。
 * 2. 生成自定义报表快照与导出任务，支撑报表执行和审计闭环。
 * 3. 统一计算开发人员提成报表，固化开发产出、出单表现与 KPI 口径。
 * </p>
 */
@Service
public class BiScenarioService {

    private static final String DEFAULT_VISIBILITY = "PRIVATE";
    private static final String DEFAULT_EXPORT_FORMAT = "CSV";
    private static final String DEFAULT_EXPORT_TYPE = "CUSTOM_REPORT";

    private final ReportRepository reportRepository;
    private final BiExtStore extStore;
    private final ObjectMapper objectMapper;

    public BiScenarioService(ReportRepository reportRepository, BiExtStore extStore, ObjectMapper objectMapper) {
        this.reportRepository = reportRepository;
        this.extStore = extStore;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public CustomReport createCustomReport(String tenantId, CreateCustomReportCommand command) {
        extStore.findCustomReportByCode(tenantId, command.reportCode()).ifPresent(existing -> {
            throw new BizException("CUSTOM_REPORT_DUPLICATED", "Custom report code already exists");
        });
        validateDimensions(tenantId, command.dimensions());
        validateMetrics(tenantId, command.metrics());
        Instant now = Instant.now();
        return extStore.saveCustomReport(new CustomReport(
                UUID.randomUUID().toString(),
                tenantId,
                command.reportCode(),
                command.reportName(),
                command.subjectArea(),
                command.dimensions(),
                command.metrics(),
                command.filters(),
                command.sorts(),
                command.visibility() != null && !command.visibility().isBlank() ? command.visibility() : DEFAULT_VISIBILITY,
                command.permissionCode(),
                command.dataLevel(),
                command.description(),
                true,
                null,
                null,
                now,
                now));
    }

    public List<CustomReport> listCustomReports(String tenantId, String subjectArea) {
        return extStore.listCustomReports(tenantId, subjectArea);
    }

    public CustomReport getCustomReport(String tenantId, String reportId) {
        return extStore.findCustomReport(tenantId, reportId)
                .orElseThrow(() -> new BizException("CUSTOM_REPORT_NOT_FOUND", "Custom report not found"));
    }

    @Transactional
    public ReportSnapshot runCustomReport(String tenantId, String reportId) {
        CustomReport report = getCustomReport(tenantId, reportId);
        Instant now = Instant.now();
        String snapshotId = UUID.randomUUID().toString();
        String snapshotData = buildCustomReportSnapshotData(report);
        ReportSnapshot snapshot = extStore.saveReportSnapshot(new ReportSnapshot(
                snapshotId,
                tenantId,
                report.reportId(),
                report.reportName(),
                snapshotData,
                "json",
                now,
                now));
        extStore.saveCustomReport(new CustomReport(
                report.reportId(),
                report.tenantId(),
                report.reportCode(),
                report.reportName(),
                report.subjectArea(),
                report.dimensions(),
                report.metrics(),
                report.filters(),
                report.sorts(),
                report.visibility(),
                report.permissionCode(),
                report.dataLevel(),
                report.description(),
                report.enabled(),
                snapshot.snapshotId(),
                now,
                report.createdAt(),
                now));
        return snapshot;
    }

    @Transactional
    public com.aidotnet.erp.bi.domain.DataExportTask exportCustomReport(String tenantId, String reportId, String format) {
        CustomReport report = getCustomReport(tenantId, reportId);
        Instant now = Instant.now();
        com.aidotnet.erp.bi.domain.DataExportTask task = new com.aidotnet.erp.bi.domain.DataExportTask(
                UUID.randomUUID().toString(),
                tenantId,
                report.reportName() + " Export",
                DEFAULT_EXPORT_TYPE,
                format != null && !format.isBlank() ? format.toUpperCase() : DEFAULT_EXPORT_FORMAT,
                com.aidotnet.erp.bi.domain.DataExportTask.ExportStatus.PENDING.name(),
                null,
                0,
                null,
                now,
                null);
        extStore.saveDataExportTask(task);
        return task;
    }

    @Transactional
    public DeveloperCommissionReport createDeveloperCommissionReport(String tenantId,
                                                                     CreateDeveloperCommissionReportCommand command) {
        BigDecimal orderRate = BigDecimal.valueOf(command.orderCount())
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(command.skuCount()), 4, RoundingMode.HALF_UP);
        // 统一提成口径: 出单率表现 * KPI 绩效得分，结果再乘以基础利润提成率。
        BigDecimal commissionCoefficient = orderRate
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
                .multiply(command.kpiScore().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP))
                .setScale(4, RoundingMode.HALF_UP);
        BigDecimal commissionAmount = command.salesProfit()
                .multiply(command.baseCommissionRate().divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP))
                .multiply(commissionCoefficient)
                .setScale(4, RoundingMode.HALF_UP);
        DeveloperCommissionReport report = new DeveloperCommissionReport(
                UUID.randomUUID().toString(),
                tenantId,
                command.userId(),
                command.userName(),
                command.teamCode(),
                command.period(),
                command.skuCount(),
                command.orderCount(),
                orderRate,
                command.salesProfit(),
                command.kpiScore(),
                command.baseCommissionRate(),
                commissionCoefficient,
                commissionAmount,
                command.currency(),
                Instant.now());
        return extStore.saveDeveloperCommissionReport(report);
    }

    public List<DeveloperCommissionReport> listDeveloperCommissionReports(String tenantId, String period, String userId) {
        return extStore.listDeveloperCommissionReports(tenantId, period, userId);
    }

    public DeveloperCommissionReport getDeveloperCommissionReport(String tenantId, String reportId) {
        return extStore.findDeveloperCommissionReport(tenantId, reportId)
                .orElseThrow(() -> new BizException("DEVELOPER_COMMISSION_REPORT_NOT_FOUND",
                        "Developer commission report not found"));
    }

    private void validateDimensions(String tenantId, List<String> dimensions) {
        for (String dimensionCode : dimensions) {
            reportRepository.findDimensionByCode(tenantId, dimensionCode)
                    .orElseThrow(() -> new BizException("CUSTOM_REPORT_DIMENSION_NOT_FOUND",
                            "Dimension not found: " + dimensionCode));
        }
    }

    private void validateMetrics(String tenantId, List<String> metrics) {
        for (String metricCode : metrics) {
            reportRepository.findMetricByCode(tenantId, metricCode)
                    .orElseThrow(() -> new BizException("CUSTOM_REPORT_METRIC_NOT_FOUND",
                            "Metric not found: " + metricCode));
        }
    }

    private String buildCustomReportSnapshotData(CustomReport report) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("reportCode", report.reportCode());
        payload.put("subjectArea", report.subjectArea());
        payload.put("dimensions", report.dimensions());
        payload.put("metrics", report.metrics());
        payload.put("filters", report.filters());
        payload.put("sorts", report.sorts());
        payload.put("rows", 0);
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new BizException("CUSTOM_REPORT_SNAPSHOT_SERIALIZE_FAILED",
                    "Custom report snapshot serialize failed");
        }
    }

    public record CreateCustomReportCommand(String reportCode, String reportName, String subjectArea,
                                            List<String> dimensions, List<String> metrics,
                                            Map<String, Object> filters, List<String> sorts,
                                            String visibility, String permissionCode,
                                            String dataLevel, String description) {}

    public record CreateDeveloperCommissionReportCommand(String userId, String userName, String teamCode,
                                                         String period, int skuCount, int orderCount,
                                                         BigDecimal salesProfit, BigDecimal kpiScore,
                                                         BigDecimal baseCommissionRate, String currency) {}
}
