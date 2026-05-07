package com.aidotnet.erp.bi.application;

import com.aidotnet.erp.bi.domain.BiDashboard;
import com.aidotnet.erp.bi.domain.DashboardWidget;
import com.aidotnet.erp.bi.domain.DataVisualization;
import com.aidotnet.erp.bi.domain.Dimension;
import com.aidotnet.erp.bi.domain.KpiAlert;
import com.aidotnet.erp.bi.domain.KpiMetric;
import com.aidotnet.erp.bi.domain.KpiStatus;
import com.aidotnet.erp.bi.domain.MetricDefinition;
import com.aidotnet.erp.bi.domain.ReportDefinition;
import com.aidotnet.erp.bi.domain.ReportSnapshot;
import com.aidotnet.erp.bi.infrastructure.ReportRepository;
import com.aidotnet.erp.common.exception.BizException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * 报表与商业智能应用服务
 * <p>
 * 描述: 商业智能域核心服务，负责报表定义/执行、KPI指标管理、
 *       指标口径定义、仪表盘/组件/可视化/维度管理等业务逻辑。
 *       支持AI驱动的KPI异常检测和经营驾驶舱能力。
 * </p>
 * <p>
 * 核心能力:
 *   1. 报表管理 - 创建/更新/执行报表定义，生成报表快照
 *   2. KPI管理 - 记录KPI指标值，计算达成率，自动检测异常
 *   3. 指标口径 - 定义/管理指标口径，统一指标计算标准
 *   4. 仪表盘 - 创建/更新BI仪表盘，管理组件和可视化配置
 *   5. 维度管理 - 创建/管理分析维度，支持多维度交叉分析
 *   6. 经营驾驶舱 - 聚合各域KPI数据，生成经营概览
 * </p>
 * <p>
 * 业务规则:
 *   1. 报表编码在同一租户下唯一
 *   2. KPI状态自动计算: ON_TRACK(≥90%) / AT_RISK(≥70%) / OFF_TRACK(<70%)
 *   3. KPI异常自动生成告警，包含偏离率和严重级别
 *   4. 指标口径编码唯一，确保指标定义一致性
 * </p>
 *
 * @author ERP系统
 * @see KpiMetric
 * @see ReportDefinition
 * @see ReportRepository
 */
@Service
public class ReportService {

    private static final List<String> COCKPIT_CATEGORIES = List.of("sales", "profit", "inventory", "ads", "crm");

    private final ReportRepository repository;

    public ReportService(ReportRepository repository) {
        this.repository = repository;
    }

    public ReportDefinition create(String tenantId, SaveReportCommand command) {
        repository.findByCode(tenantId, command.reportCode()).ifPresent(existing -> {
            throw new BizException("REPORT_DUPLICATED", "Report code already exists");
        });
        Instant now = Instant.now();
        return repository.save(new ReportDefinition(
                UUID.randomUUID().toString(),
                tenantId,
                command.reportCode(),
                command.reportName(),
                command.reportType(),
                command.dataSource(),
                command.queryText(),
                command.permissionCode(),
                command.dataLevel(),
                command.description(),
                now,
                now));
    }

    public ReportDefinition update(String tenantId, String reportId, UpdateReportCommand command) {
        ReportDefinition existing = repository.find(tenantId, reportId)
                .orElseThrow(() -> new BizException("REPORT_NOT_FOUND", "Report not found"));
        return repository.save(new ReportDefinition(
                existing.reportId(),
                existing.tenantId(),
                command.reportCode() != null ? command.reportCode() : existing.reportCode(),
                command.reportName() != null ? command.reportName() : existing.reportName(),
                command.reportType() != null ? command.reportType() : existing.reportType(),
                command.dataSource() != null ? command.dataSource() : existing.dataSource(),
                command.queryText() != null ? command.queryText() : existing.queryText(),
                command.permissionCode() != null ? command.permissionCode() : existing.permissionCode(),
                command.dataLevel() != null ? command.dataLevel() : existing.dataLevel(),
                command.description() != null ? command.description() : existing.description(),
                existing.createdAt(),
                Instant.now()));
    }

    public ReportSnapshot run(String tenantId, String reportCode) {
        ReportDefinition report = repository.findByCode(tenantId, reportCode)
                .orElseThrow(() -> new BizException("REPORT_NOT_FOUND", "Report not found"));
        String snapshotData = "{\"dataSource\":\"" + report.dataSource() + "\",\"rows\":0}";
        Instant now = Instant.now();
        return new ReportSnapshot(UUID.randomUUID().toString(), report.tenantId(), report.reportId(),
                report.reportName(), snapshotData, "json", now, now);
    }

    public ReportDefinition getReportByCode(String tenantId, String reportCode) {
        return repository.findByCode(tenantId, reportCode)
                .orElseThrow(() -> new BizException("REPORT_NOT_FOUND", "Report not found"));
    }

    public List<ReportDefinition> list(String tenantId) {
        return repository.list(tenantId);
    }

    public List<ReportDefinition> listByType(String tenantId, String reportType) {
        return repository.listByType(tenantId, reportType);
    }

    public KpiMetric recordKpi(String tenantId, RecordKpiCommand command) {
        KpiStatus status = computeKpiStatus(command.value(), command.targetValue());
        return repository.saveKpi(new KpiMetric(
                UUID.randomUUID().toString(),
                tenantId,
                command.kpiCode(),
                command.kpiName(),
                command.category(),
                command.value(),
                command.targetValue(),
                command.unit(),
                status,
                Instant.now()));
    }

    public List<KpiMetric> listKpis(String tenantId) {
        return repository.listKpis(tenantId);
    }

    public List<KpiMetric> listKpisByCategory(String tenantId, String category) {
        return repository.listKpisByCategory(tenantId, category);
    }

    public KpiMetric getKpi(String tenantId, String kpiId) {
        return repository.findKpi(tenantId, kpiId)
                .orElseThrow(() -> new BizException("KPI_NOT_FOUND", "KPI not found"));
    }

    public KpiMetric getLatestKpiByCode(String tenantId, String kpiCode) {
        return repository.findLatestKpiByCode(tenantId, kpiCode)
                .orElseThrow(() -> new BizException("KPI_NOT_FOUND", "KPI not found"));
    }

    public KpiMetric updateKpiTarget(String tenantId, String kpiId, UpdateKpiTargetCommand command) {
        KpiMetric current = getKpi(tenantId, kpiId);
        KpiStatus status = computeKpiStatus(current.value(), command.targetValue());
        return repository.updateKpiTarget(new KpiMetric(
                current.kpiId(),
                current.tenantId(),
                current.kpiCode(),
                current.kpiName(),
                current.category(),
                current.value(),
                command.targetValue(),
                current.unit(),
                status,
                current.measuredAt()));
    }

    public KpiAchievement getKpiAchievement(String tenantId, String kpiCode) {
        KpiMetric kpi = getLatestKpiByCode(tenantId, kpiCode);
        BigDecimal achievementRate = BigDecimal.ZERO;
        if (kpi.targetValue() != null && kpi.targetValue().compareTo(BigDecimal.ZERO) > 0) {
            achievementRate = kpi.value().divide(kpi.targetValue(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        boolean achieved = kpi.targetValue() != null && kpi.value().compareTo(kpi.targetValue()) >= 0;
        return new KpiAchievement(kpi, achievementRate, achieved);
    }

    public List<KpiAlert> detectKpiAnomalies(String tenantId) {
        List<KpiMetric> allKpis = repository.listKpis(tenantId);
        List<KpiAlert> alerts = new ArrayList<>();
        for (KpiMetric kpi : allKpis) {
            if (kpi.status() == KpiStatus.AT_RISK || kpi.status() == KpiStatus.OFF_TRACK) {
                BigDecimal deviationRate = BigDecimal.ZERO;
                if (kpi.targetValue() != null && kpi.targetValue().compareTo(BigDecimal.ZERO) > 0) {
                    deviationRate = kpi.value().subtract(kpi.targetValue())
                            .divide(kpi.targetValue(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));
                }
                String severity = kpi.status() == KpiStatus.OFF_TRACK ? "CRITICAL" : "WARNING";
                String message = String.format("KPI %s (%s) 偏离目标 %.2f%%, 当前值: %s, 目标值: %s",
                        kpi.kpiName(), kpi.kpiCode(), deviationRate, kpi.value(), kpi.targetValue());
                alerts.add(new KpiAlert(
                        UUID.randomUUID().toString(),
                        tenantId,
                        kpi.kpiCode(),
                        kpi.kpiName(),
                        kpi.value(),
                        kpi.targetValue(),
                        deviationRate,
                        kpi.status(),
                        severity,
                        message,
                        Instant.now()));
            }
        }
        return alerts;
    }

    public MetricDefinition createMetric(String tenantId, SaveMetricCommand command) {
        repository.findMetricByCode(tenantId, command.metricCode()).ifPresent(existing -> {
            throw new BizException("METRIC_DEFINITION_DUPLICATED", "Metric definition code already exists");
        });
        Instant now = Instant.now();
        return repository.saveMetric(new MetricDefinition(
                UUID.randomUUID().toString(),
                tenantId,
                command.metricCode(),
                command.metricName(),
                command.category(),
                command.formula(),
                command.unit(),
                command.permissionCode(),
                command.dataLevel(),
                command.description(),
                command.enabled(),
                now,
                now));
    }

    public MetricDefinition updateMetric(String tenantId, String metricId, UpdateMetricCommand command) {
        MetricDefinition current = getMetric(tenantId, metricId);
        if (command.metricCode() != null && !command.metricCode().equals(current.metricCode())) {
            repository.findMetricByCode(tenantId, command.metricCode()).ifPresent(existing -> {
                throw new BizException("METRIC_DEFINITION_DUPLICATED", "Metric definition code already exists");
            });
        }
        return repository.saveMetric(new MetricDefinition(
                current.metricId(),
                current.tenantId(),
                valueOrDefault(command.metricCode(), current.metricCode()),
                valueOrDefault(command.metricName(), current.metricName()),
                valueOrDefault(command.category(), current.category()),
                valueOrDefault(command.formula(), current.formula()),
                valueOrDefault(command.unit(), current.unit()),
                valueOrDefault(command.permissionCode(), current.permissionCode()),
                valueOrDefault(command.dataLevel(), current.dataLevel()),
                valueOrDefault(command.description(), current.description()),
                command.enabled() != null ? command.enabled() : current.enabled(),
                current.createdAt(),
                Instant.now()));
    }

    public MetricDefinition getMetric(String tenantId, String metricId) {
        return repository.findMetric(tenantId, metricId)
                .orElseThrow(() -> new BizException("METRIC_DEFINITION_NOT_FOUND", "Metric definition not found"));
    }

    public MetricDefinition getMetricByCode(String tenantId, String metricCode) {
        return repository.findMetricByCode(tenantId, metricCode)
                .orElseThrow(() -> new BizException("METRIC_DEFINITION_NOT_FOUND", "Metric definition not found"));
    }

    public List<MetricDefinition> listMetrics(String tenantId, String category, Boolean enabled) {
        return repository.listMetrics(tenantId, category, enabled);
    }

    public DashboardWidget createWidget(String tenantId, CreateWidgetCommand command) {
        Instant now = Instant.now();
        return repository.saveWidget(new DashboardWidget(
                UUID.randomUUID().toString(),
                tenantId,
                command.widgetCode(),
                command.widgetName(),
                command.widgetType(),
                command.config(),
                command.sortOrder(),
                true,
                now,
                now));
    }

    public List<DashboardWidget> listWidgets(String tenantId) {
        return repository.listWidgets(tenantId);
    }

    public DashboardWidget updateWidgetConfig(String tenantId, String widgetId, UpdateWidgetConfigCommand command) {
        DashboardWidget widget = repository.findWidget(tenantId, widgetId)
                .orElseThrow(() -> new BizException("WIDGET_NOT_FOUND", "Dashboard widget not found"));
        return repository.saveWidget(new DashboardWidget(
                widget.widgetId(),
                widget.tenantId(),
                widget.widgetCode(),
                widget.widgetName(),
                widget.widgetType(),
                command.config(),
                widget.sortOrder(),
                widget.enabled(),
                widget.createdAt(),
                Instant.now()));
    }

    public DashboardWidget toggleWidget(String tenantId, String widgetId, boolean enabled) {
        DashboardWidget widget = repository.findWidget(tenantId, widgetId)
                .orElseThrow(() -> new BizException("WIDGET_NOT_FOUND", "Dashboard widget not found"));
        return repository.saveWidget(new DashboardWidget(
                widget.widgetId(),
                widget.tenantId(),
                widget.widgetCode(),
                widget.widgetName(),
                widget.widgetType(),
                widget.config(),
                widget.sortOrder(),
                enabled,
                widget.createdAt(),
                Instant.now()));
    }

    public DataVisualization createVisualization(String tenantId, CreateVisualizationCommand command) {
        Instant now = Instant.now();
        return repository.saveVisualization(new DataVisualization(
                UUID.randomUUID().toString(),
                tenantId,
                command.reportId(),
                command.vizName(),
                command.vizType(),
                command.config(),
                command.sortOrder(),
                true,
                now,
                now));
    }

    public List<DataVisualization> listVisualizations(String tenantId, String reportId) {
        return repository.listVisualizations(tenantId, reportId);
    }

    public Dimension createDimension(String tenantId, SaveDimensionCommand command) {
        repository.findDimensionByCode(tenantId, command.dimensionCode()).ifPresent(existing -> {
            throw new BizException("DIMENSION_DUPLICATED", "Dimension code already exists");
        });
        Instant now = Instant.now();
        return repository.saveDimension(new Dimension(
                UUID.randomUUID().toString(),
                tenantId,
                command.dimensionCode(),
                command.dimensionName(),
                command.dimensionType(),
                command.sourceField(),
                command.description(),
                true,
                now,
                now));
    }

    public List<Dimension> listDimensions(String tenantId, Boolean enabled) {
        return repository.listDimensions(tenantId, enabled);
    }

    public BiDashboard createDashboard(String tenantId, CreateDashboardCommand command) {
        Instant now = Instant.now();
        return repository.saveDashboard(new BiDashboard(
                UUID.randomUUID().toString(),
                tenantId,
                command.dashboardName(),
                command.dashboardType(),
                command.config(),
                command.owner(),
                true,
                now,
                now));
    }

    public List<BiDashboard> listDashboards(String tenantId) {
        return repository.listDashboards(tenantId);
    }

    public BiDashboard getDashboard(String tenantId, String dashboardId) {
        return repository.findDashboard(tenantId, dashboardId)
                .orElseThrow(() -> new BizException("DASHBOARD_NOT_FOUND", "Dashboard not found"));
    }

    public BiDashboard updateDashboard(String tenantId, String dashboardId, UpdateDashboardCommand command) {
        BiDashboard existing = getDashboard(tenantId, dashboardId);
        return repository.saveDashboard(new BiDashboard(
                existing.dashboardId(),
                existing.tenantId(),
                command.dashboardName() != null ? command.dashboardName() : existing.dashboardName(),
                command.dashboardType() != null ? command.dashboardType() : existing.dashboardType(),
                command.config() != null ? command.config() : existing.config(),
                command.owner() != null ? command.owner() : existing.owner(),
                existing.enabled(),
                existing.createdAt(),
                Instant.now()));
    }

    public CockpitSummary getCockpitSummary(String tenantId) {
        Map<String, CockpitCard> cards = new LinkedHashMap<>();
        for (String category : COCKPIT_CATEGORIES) {
            List<KpiMetric> metrics = repository.listKpisByCategory(tenantId, category);
            if (metrics.isEmpty()) {
                cards.put(category, new CockpitCard(category, null, null, BigDecimal.ZERO, false));
                continue;
            }
            KpiMetric latest = metrics.get(0);
            KpiAchievement achievement = getKpiAchievement(tenantId, latest.kpiCode());
            cards.put(category, new CockpitCard(
                    category,
                    latest.kpiCode(),
                    latest.kpiName(),
                    achievement.achievementRate(),
                    achievement.achieved()));
        }
        return new CockpitSummary(
                repository.listMetrics(tenantId, null, true).size(),
                repository.list(tenantId).size(),
                repository.listWidgets(tenantId).size(),
                cards,
                Instant.now());
    }

    private KpiStatus computeKpiStatus(BigDecimal value, BigDecimal targetValue) {
        if (targetValue == null || targetValue.compareTo(BigDecimal.ZERO) == 0) {
            return KpiStatus.NOT_STARTED;
        }
        BigDecimal ratio = value.divide(targetValue, 4, RoundingMode.HALF_UP);
        if (ratio.compareTo(BigDecimal.valueOf(0.9)) >= 0) {
            return KpiStatus.ON_TRACK;
        } else if (ratio.compareTo(BigDecimal.valueOf(0.7)) >= 0) {
            return KpiStatus.AT_RISK;
        } else {
            return KpiStatus.OFF_TRACK;
        }
    }

    private String valueOrDefault(String nextValue, String currentValue) {
        return nextValue != null ? nextValue : currentValue;
    }

    public record SaveReportCommand(String reportCode, String reportName, String reportType, String dataSource,
                                    String queryText, String permissionCode, String dataLevel, String description) {}

    public record UpdateReportCommand(String reportCode, String reportName, String reportType, String dataSource,
                                      String queryText, String permissionCode, String dataLevel, String description) {}

    public record RecordKpiCommand(String kpiCode, String kpiName, String category,
                                   BigDecimal value, BigDecimal targetValue, String unit) {}

    public record UpdateKpiTargetCommand(BigDecimal targetValue) {}

    public record KpiAchievement(KpiMetric kpi, BigDecimal achievementRate, boolean achieved) {}

    public record SaveMetricCommand(String metricCode, String metricName, String category, String formula, String unit,
                                    String permissionCode, String dataLevel, String description, boolean enabled) {}

    public record UpdateMetricCommand(String metricCode, String metricName, String category, String formula, String unit,
                                      String permissionCode, String dataLevel, String description, Boolean enabled) {}

    public record CreateWidgetCommand(String widgetCode, String widgetName, String widgetType,
                                      Map<String, Object> config, int sortOrder) {}

    public record UpdateWidgetConfigCommand(Map<String, Object> config) {}

    public record CreateVisualizationCommand(String reportId, String vizName, String vizType,
                                             Map<String, Object> config, int sortOrder) {}

    public record SaveDimensionCommand(String dimensionCode, String dimensionName, String dimensionType,
                                       String sourceField, String description) {}

    public record CreateDashboardCommand(String dashboardName, String dashboardType,
                                         Map<String, Object> config, String owner) {}

    public record UpdateDashboardCommand(String dashboardName, String dashboardType,
                                         Map<String, Object> config, String owner) {}

    public record CockpitCard(String category, String kpiCode, String kpiName,
                              BigDecimal achievementRate, boolean achieved) {}

    public record CockpitSummary(int metricCount, int reportCount, int widgetCount,
                                 Map<String, CockpitCard> cockpitCards, Instant generatedAt) {}
}
