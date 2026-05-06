package com.aidotnet.erp.bi.api;

import com.aidotnet.erp.bi.application.ReportService;
import com.aidotnet.erp.bi.application.ReportService.CockpitSummary;
import com.aidotnet.erp.bi.application.ReportService.CreateDashboardCommand;
import com.aidotnet.erp.bi.application.ReportService.CreateVisualizationCommand;
import com.aidotnet.erp.bi.application.ReportService.CreateWidgetCommand;
import com.aidotnet.erp.bi.application.ReportService.KpiAchievement;
import com.aidotnet.erp.bi.application.ReportService.RecordKpiCommand;
import com.aidotnet.erp.bi.application.ReportService.SaveDimensionCommand;
import com.aidotnet.erp.bi.application.ReportService.SaveMetricCommand;
import com.aidotnet.erp.bi.application.ReportService.SaveReportCommand;
import com.aidotnet.erp.bi.application.ReportService.UpdateDashboardCommand;
import com.aidotnet.erp.bi.application.ReportService.UpdateKpiTargetCommand;
import com.aidotnet.erp.bi.application.ReportService.UpdateMetricCommand;
import com.aidotnet.erp.bi.application.ReportService.UpdateReportCommand;
import com.aidotnet.erp.bi.application.ReportService.UpdateWidgetConfigCommand;
import com.aidotnet.erp.bi.domain.BiDashboard;
import com.aidotnet.erp.bi.domain.DashboardWidget;
import com.aidotnet.erp.bi.domain.DataVisualization;
import com.aidotnet.erp.bi.domain.Dimension;
import com.aidotnet.erp.bi.domain.KpiAlert;
import com.aidotnet.erp.bi.domain.KpiMetric;
import com.aidotnet.erp.bi.domain.MetricDefinition;
import com.aidotnet.erp.bi.domain.ReportDefinition;
import com.aidotnet.erp.bi.domain.ReportSnapshot;
import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/bi/api/in/v1", "/api/bi"})
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/reports")
    public Result<ReportDefinition> create(@Valid @RequestBody SaveReportRequest request) {
        return Result.ok(reportService.create(currentTenant(), new SaveReportCommand(
                request.reportCode(),
                request.reportName(),
                request.reportType(),
                request.dataSource(),
                request.queryText(),
                request.permissionCode(),
                request.dataLevel(),
                request.description())));
    }

    @PutMapping("/reports/{reportId}")
    public Result<ReportDefinition> update(@PathVariable String reportId, @Valid @RequestBody UpdateReportRequest request) {
        return Result.ok(reportService.update(currentTenant(), reportId, new UpdateReportCommand(
                request.reportCode(),
                request.reportName(),
                request.reportType(),
                request.dataSource(),
                request.queryText(),
                request.permissionCode(),
                request.dataLevel(),
                request.description())));
    }

    @PostMapping("/reports/{reportCode}/run")
    public Result<ReportSnapshot> run(@PathVariable String reportCode) {
        return Result.ok(reportService.run(currentTenant(), reportCode));
    }

    @GetMapping("/reports")
    public Result<List<ReportDefinition>> list(@RequestParam(required = false) String reportType) {
        if (reportType != null && !reportType.isBlank()) {
            return Result.ok(reportService.listByType(currentTenant(), reportType));
        }
        return Result.ok(reportService.list(currentTenant()));
    }

    @PostMapping("/kpis")
    public Result<KpiMetric> recordKpi(@Valid @RequestBody RecordKpiRequest request) {
        return Result.ok(reportService.recordKpi(currentTenant(), new RecordKpiCommand(
                request.kpiCode(),
                request.kpiName(),
                request.category(),
                request.value(),
                request.targetValue(),
                request.unit())));
    }

    @GetMapping("/kpis")
    public Result<List<KpiMetric>> listKpis() {
        return Result.ok(reportService.listKpis(currentTenant()));
    }

    @GetMapping("/kpis/by-category")
    public Result<List<KpiMetric>> listKpisByCategory(@NotBlank @RequestParam String category) {
        return Result.ok(reportService.listKpisByCategory(currentTenant(), category));
    }

    @GetMapping("/kpis/{kpiId}")
    public Result<KpiMetric> getKpi(@PathVariable String kpiId) {
        return Result.ok(reportService.getKpi(currentTenant(), kpiId));
    }

    @PatchMapping("/kpis/{kpiId}/target")
    public Result<KpiMetric> updateKpiTarget(@PathVariable String kpiId,
                                             @Valid @RequestBody UpdateKpiTargetRequest request) {
        return Result.ok(reportService.updateKpiTarget(currentTenant(), kpiId,
                new UpdateKpiTargetCommand(request.targetValue())));
    }

    @GetMapping("/kpis/by-code/{kpiCode}")
    public Result<KpiMetric> getLatestKpiByCode(@PathVariable String kpiCode) {
        return Result.ok(reportService.getLatestKpiByCode(currentTenant(), kpiCode));
    }

    @GetMapping("/kpis/by-code/{kpiCode}/achievement")
    public Result<KpiAchievement> getKpiAchievement(@PathVariable String kpiCode) {
        return Result.ok(reportService.getKpiAchievement(currentTenant(), kpiCode));
    }

    @GetMapping("/kpis/anomalies")
    public Result<List<KpiAlert>> detectKpiAnomalies() {
        return Result.ok(reportService.detectKpiAnomalies(currentTenant()));
    }

    @PostMapping("/metrics")
    public Result<MetricDefinition> createMetric(@Valid @RequestBody SaveMetricRequest request) {
        return Result.ok(reportService.createMetric(currentTenant(), new SaveMetricCommand(
                request.metricCode(),
                request.metricName(),
                request.category(),
                request.formula(),
                request.unit(),
                request.permissionCode(),
                request.dataLevel(),
                request.description(),
                request.enabled() == null || request.enabled())));
    }

    @PatchMapping("/metrics/{metricId}")
    public Result<MetricDefinition> updateMetric(@PathVariable String metricId,
                                                 @Valid @RequestBody UpdateMetricRequest request) {
        return Result.ok(reportService.updateMetric(currentTenant(), metricId, new UpdateMetricCommand(
                request.metricCode(),
                request.metricName(),
                request.category(),
                request.formula(),
                request.unit(),
                request.permissionCode(),
                request.dataLevel(),
                request.description(),
                request.enabled())));
    }

    @GetMapping("/metrics")
    public Result<List<MetricDefinition>> listMetrics(@RequestParam(required = false) String category,
                                                      @RequestParam(required = false) Boolean enabled) {
        return Result.ok(reportService.listMetrics(currentTenant(), category, enabled));
    }

    @GetMapping("/metrics/{metricId}")
    public Result<MetricDefinition> getMetric(@PathVariable String metricId) {
        return Result.ok(reportService.getMetric(currentTenant(), metricId));
    }

    @GetMapping("/metrics/by-code/{metricCode}")
    public Result<MetricDefinition> getMetricByCode(@PathVariable String metricCode) {
        return Result.ok(reportService.getMetricByCode(currentTenant(), metricCode));
    }

    @GetMapping("/dashboard/summary")
    public Result<CockpitSummary> getDashboardSummary() {
        return Result.ok(reportService.getCockpitSummary(currentTenant()));
    }

    @PostMapping("/widgets")
    public Result<DashboardWidget> createWidget(@Valid @RequestBody CreateWidgetRequest request) {
        return Result.ok(reportService.createWidget(currentTenant(), new CreateWidgetCommand(
                request.widgetCode(),
                request.widgetName(),
                request.widgetType(),
                request.config(),
                request.sortOrder())));
    }

    @GetMapping("/widgets")
    public Result<List<DashboardWidget>> listWidgets() {
        return Result.ok(reportService.listWidgets(currentTenant()));
    }

    @PatchMapping("/widgets/{widgetId}/config")
    public Result<DashboardWidget> updateWidgetConfig(@PathVariable String widgetId,
                                                      @Valid @RequestBody UpdateWidgetConfigRequest request) {
        return Result.ok(reportService.updateWidgetConfig(currentTenant(), widgetId,
                new UpdateWidgetConfigCommand(request.config())));
    }

    @PatchMapping("/widgets/{widgetId}/toggle")
    public Result<DashboardWidget> toggleWidget(@PathVariable String widgetId, @RequestParam boolean enabled) {
        return Result.ok(reportService.toggleWidget(currentTenant(), widgetId, enabled));
    }

    @PostMapping("/visualizations")
    public Result<DataVisualization> createVisualization(@Valid @RequestBody CreateVisualizationRequest request) {
        return Result.ok(reportService.createVisualization(currentTenant(), new CreateVisualizationCommand(
                request.reportId(),
                request.vizName(),
                request.vizType(),
                request.config(),
                request.sortOrder())));
    }

    @GetMapping("/visualizations")
    public Result<List<DataVisualization>> listVisualizations(@RequestParam(required = false) String reportId) {
        return Result.ok(reportService.listVisualizations(currentTenant(), reportId));
    }

    @PostMapping("/dimensions")
    public Result<Dimension> createDimension(@Valid @RequestBody SaveDimensionRequest request) {
        return Result.ok(reportService.createDimension(currentTenant(), new SaveDimensionCommand(
                request.dimensionCode(),
                request.dimensionName(),
                request.dimensionType(),
                request.sourceField(),
                request.description())));
    }

    @GetMapping("/dimensions")
    public Result<List<Dimension>> listDimensions(@RequestParam(required = false) Boolean enabled) {
        return Result.ok(reportService.listDimensions(currentTenant(), enabled));
    }

    @PostMapping("/dashboards")
    public Result<BiDashboard> createDashboard(@Valid @RequestBody CreateDashboardRequest request) {
        return Result.ok(reportService.createDashboard(currentTenant(), new CreateDashboardCommand(
                request.dashboardName(),
                request.dashboardType(),
                request.config(),
                request.owner())));
    }

    @GetMapping("/dashboards")
    public Result<List<BiDashboard>> listDashboards() {
        return Result.ok(reportService.listDashboards(currentTenant()));
    }

    @GetMapping("/dashboards/{dashboardId}")
    public Result<BiDashboard> getDashboard(@PathVariable String dashboardId) {
        return Result.ok(reportService.getDashboard(currentTenant(), dashboardId));
    }

    @PutMapping("/dashboards/{dashboardId}")
    public Result<BiDashboard> updateDashboard(@PathVariable String dashboardId,
                                               @Valid @RequestBody UpdateDashboardRequest request) {
        return Result.ok(reportService.updateDashboard(currentTenant(), dashboardId, new UpdateDashboardCommand(
                request.dashboardName(),
                request.dashboardType(),
                request.config(),
                request.owner())));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "Tenant id is required");
        }
        return tenantId;
    }

    public record SaveReportRequest(@NotBlank String reportCode, @NotBlank String reportName,
                                    String reportType, @NotBlank String dataSource, String queryText,
                                    String permissionCode, String dataLevel, String description) {}

    public record UpdateReportRequest(String reportCode, String reportName, String reportType,
                                      String dataSource, String queryText, String permissionCode,
                                      String dataLevel, String description) {}

    public record RecordKpiRequest(@NotBlank String kpiCode, @NotBlank String kpiName,
                                   @NotBlank String category, @NotNull BigDecimal value,
                                   BigDecimal targetValue, String unit) {}

    public record UpdateKpiTargetRequest(@NotNull BigDecimal targetValue) {}

    public record SaveMetricRequest(@NotBlank String metricCode, @NotBlank String metricName,
                                    @NotBlank String category, String formula, String unit,
                                    String permissionCode, String dataLevel, String description,
                                    Boolean enabled) {}

    public record UpdateMetricRequest(String metricCode, String metricName, String category,
                                      String formula, String unit, String permissionCode,
                                      String dataLevel, String description, Boolean enabled) {}

    public record CreateWidgetRequest(@NotBlank String widgetCode, @NotBlank String widgetName,
                                      @NotBlank String widgetType, Map<String, Object> config,
                                      int sortOrder) {}

    public record UpdateWidgetConfigRequest(Map<String, Object> config) {}

    public record CreateVisualizationRequest(@NotBlank String reportId, @NotBlank String vizName,
                                             @NotBlank String vizType, Map<String, Object> config,
                                             int sortOrder) {}

    public record SaveDimensionRequest(@NotBlank String dimensionCode, @NotBlank String dimensionName,
                                       @NotBlank String dimensionType, String sourceField,
                                       String description) {}

    public record CreateDashboardRequest(@NotBlank String dashboardName, String dashboardType,
                                         Map<String, Object> config, String owner) {}

    public record UpdateDashboardRequest(String dashboardName, String dashboardType,
                                         Map<String, Object> config, String owner) {}
}
