package com.aidotnet.erp.bi.application;

import com.aidotnet.erp.bi.domain.BiDashboard;
import com.aidotnet.erp.bi.domain.KpiAlert;
import com.aidotnet.erp.bi.domain.KpiMetric;
import com.aidotnet.erp.bi.domain.MetricDefinition;
import com.aidotnet.erp.bi.domain.ReportDefinition;
import com.aidotnet.erp.bi.domain.ReportSnapshot;
import com.aidotnet.erp.common.pms.PmsRequestGuard.PmsDataQueryContext;
import com.aidotnet.erp.common.security.DataScope;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * BI出站应用服务
 * <p>
 * 描述: 统一承接 BI 域对外导出能力，避免控制器直接拼装静态返回。
 * 1. 报表导出复用报表定义 + 运行快照，保证导出口径和运行口径一致。
 * 2. KPI 导出默认按 KPI 编码去重，只暴露每个 KPI 的最新观测值。
 * 3. 告警导出直接复用 KPI 异常检测结果，供工作台/外域订阅告警信息。
 * </p>
 */
@Service
public class BiOutboundService {

    private static final String MASKED_VALUE = "******";

    private final ReportService reportService;

    public BiOutboundService(ReportService reportService) {
        this.reportService = reportService;
    }

    public ReportDataExportResult exportReportData(String tenantId, String reportCode, String period) {
        ReportDefinition report = reportService.getReportByCode(tenantId, reportCode);
        ReportSnapshot snapshot = reportService.run(tenantId, reportCode);
        return new ReportDataExportResult(
                report.reportCode(),
                report.reportName(),
                normalize(period),
                report.reportType(),
                report.dataSource(),
                snapshot.snapshotId(),
                snapshot.snapshotData(),
                snapshot.format(),
                snapshot.snapshotAt());
    }

    public List<KpiExportResult> exportKpis(String tenantId, String category) {
        List<KpiMetric> rawKpis = hasText(category)
                ? reportService.listKpisByCategory(tenantId, category)
                : reportService.listKpis(tenantId);

        Map<String, KpiMetric> latestByCode = new LinkedHashMap<>();
        for (KpiMetric kpi : rawKpis) {
            latestByCode.putIfAbsent(kpi.kpiCode(), kpi);
        }

        return latestByCode.values().stream()
                .map(this::toKpiExportResult)
                .toList();
    }

    public List<MetricDefinition> exportMetrics(String tenantId, String category) {
        return reportService.listMetrics(tenantId, normalize(category), true);
    }

    public List<PmsKpiExportResult> exportPmsKpis(PmsDataQueryContext context, String category) {
        String normalizedCategory = normalize(category);
        return exportKpis(context.tenantId(), normalizedCategory).stream()
                .filter(kpi -> matchesCategoryScope(kpi.category(), normalizedCategory, context.scopeValues("category")))
                .map(kpi -> toPmsKpiExportResult(kpi, context.dataLevel()))
                .toList();
    }

    public List<PmsMetricExportResult> exportPmsMetrics(PmsDataQueryContext context, String category) {
        String normalizedCategory = normalize(category);
        return exportMetrics(context.tenantId(), normalizedCategory).stream()
                .filter(metric -> matchesCategoryScope(metric.category(), normalizedCategory, context.scopeValues("category")))
                .map(metric -> toPmsMetricExportResult(metric, context.dataLevel()))
                .toList();
    }

    public DashboardExportResult exportDashboard(String tenantId, String dashboardId) {
        BiDashboard dashboard = reportService.getDashboard(tenantId, dashboardId);
        return new DashboardExportResult(
                dashboard.dashboardId(),
                dashboard.dashboardName(),
                dashboard.dashboardType(),
                dashboard.config(),
                dashboard.owner(),
                dashboard.enabled(),
                dashboard.updatedAt(),
                reportService.getCockpitSummary(tenantId),
                Instant.now());
    }

    public List<KpiAlert> exportAlerts(String tenantId, String alertType) {
        String filter = normalize(alertType);
        return reportService.detectKpiAnomalies(tenantId).stream()
                .filter(alert -> matchesAlertType(alert, filter))
                .sorted((left, right) -> right.detectedAt().compareTo(left.detectedAt()))
                .collect(Collectors.toList());
    }

    private PmsKpiExportResult toPmsKpiExportResult(KpiExportResult kpi, DataScope.DataLevel dataLevel) {
        if (dataLevel == DataScope.DataLevel.MASKED) {
            return new PmsKpiExportResult(
                    kpi.kpiId(),
                    kpi.kpiCode(),
                    kpi.kpiName(),
                    kpi.category(),
                    null,
                    null,
                    kpi.unit(),
                    kpi.status(),
                    null,
                    null,
                    kpi.measuredAt(),
                    DataScope.DataLevel.MASKED.name(),
                    true,
                    MASKED_VALUE,
                    MASKED_VALUE);
        }
        return new PmsKpiExportResult(
                kpi.kpiId(),
                kpi.kpiCode(),
                kpi.kpiName(),
                kpi.category(),
                kpi.value(),
                kpi.targetValue(),
                kpi.unit(),
                kpi.status(),
                kpi.achievementRate(),
                kpi.achieved(),
                kpi.measuredAt(),
                dataLevel.name(),
                false,
                null,
                null);
    }

    private PmsMetricExportResult toPmsMetricExportResult(MetricDefinition metric, DataScope.DataLevel dataLevel) {
        if (dataLevel == DataScope.DataLevel.MASKED) {
            return new PmsMetricExportResult(
                    metric.metricId(),
                    metric.metricCode(),
                    metric.metricName(),
                    metric.category(),
                    MASKED_VALUE,
                    metric.unit(),
                    MASKED_VALUE,
                    DataScope.DataLevel.MASKED.name(),
                    MASKED_VALUE,
                    metric.enabled(),
                    metric.updatedAt(),
                    true);
        }
        return new PmsMetricExportResult(
                metric.metricId(),
                metric.metricCode(),
                metric.metricName(),
                metric.category(),
                metric.formula(),
                metric.unit(),
                metric.permissionCode(),
                dataLevel.name(),
                metric.description(),
                metric.enabled(),
                metric.updatedAt(),
                false);
    }

    private boolean matchesCategoryScope(String candidateCategory, String requestedCategory, java.util.Set<String> scopedCategories) {
        if (requestedCategory != null && !requestedCategory.equalsIgnoreCase(candidateCategory)) {
            return false;
        }
        if (scopedCategories == null || scopedCategories.isEmpty()) {
            return true;
        }
        return scopedCategories.stream().anyMatch(scopeValue -> scopeValue.equalsIgnoreCase(candidateCategory));
    }

    private KpiExportResult toKpiExportResult(KpiMetric kpi) {
        BigDecimal achievementRate = BigDecimal.ZERO;
        if (kpi.targetValue() != null && kpi.targetValue().compareTo(BigDecimal.ZERO) > 0) {
            achievementRate = kpi.value()
                    .divide(kpi.targetValue(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        boolean achieved = kpi.targetValue() != null && kpi.value().compareTo(kpi.targetValue()) >= 0;
        return new KpiExportResult(
                kpi.kpiId(),
                kpi.kpiCode(),
                kpi.kpiName(),
                kpi.category(),
                kpi.value(),
                kpi.targetValue(),
                kpi.unit(),
                kpi.status().name(),
                achievementRate,
                achieved,
                kpi.measuredAt());
    }

    private boolean matchesAlertType(KpiAlert alert, String filter) {
        return filter == null
                || alert.severity().equalsIgnoreCase(filter)
                || alert.status().name().equalsIgnoreCase(filter)
                || alert.kpiCode().equalsIgnoreCase(filter);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalize(String value) {
        return hasText(value) ? value.trim() : null;
    }

    public record ReportDataExportResult(String reportCode,
                                         String reportName,
                                         String period,
                                         String reportType,
                                         String dataSource,
                                         String snapshotId,
                                         String snapshotData,
                                         String format,
                                         Instant exportedAt) {}

    public record KpiExportResult(String kpiId,
                                  String kpiCode,
                                  String kpiName,
                                  String category,
                                  BigDecimal value,
                                  BigDecimal targetValue,
                                  String unit,
                                  String status,
                                  BigDecimal achievementRate,
                                  boolean achieved,
                                  Instant measuredAt) {}

    public record DashboardExportResult(String dashboardId,
                                        String dashboardName,
                                        String dashboardType,
                                        Map<String, Object> config,
                                        String owner,
                                        boolean enabled,
                                        Instant updatedAt,
                                        ReportService.CockpitSummary cockpitSummary,
                                        Instant exportedAt) {}

    public record PmsKpiExportResult(String kpiId,
                                     String kpiCode,
                                     String kpiName,
                                     String category,
                                     BigDecimal value,
                                     BigDecimal targetValue,
                                     String unit,
                                     String status,
                                     BigDecimal achievementRate,
                                     Boolean achieved,
                                     Instant measuredAt,
                                     String dataLevel,
                                     boolean masked,
                                     String valueDisplay,
                                     String targetValueDisplay) {}

    public record PmsMetricExportResult(String metricId,
                                        String metricCode,
                                        String metricName,
                                        String category,
                                        String formula,
                                        String unit,
                                        String permissionCode,
                                        String dataLevel,
                                        String description,
                                        boolean enabled,
                                        Instant updatedAt,
                                        boolean masked) {}
}
