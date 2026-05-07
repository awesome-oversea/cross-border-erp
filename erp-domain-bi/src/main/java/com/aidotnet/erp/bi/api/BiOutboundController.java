package com.aidotnet.erp.bi.api;

import com.aidotnet.erp.bi.application.BiOutboundService;
import com.aidotnet.erp.bi.application.BiOutboundService.DashboardExportResult;
import com.aidotnet.erp.bi.application.BiOutboundService.KpiExportResult;
import com.aidotnet.erp.bi.application.BiOutboundService.PmsKpiExportResult;
import com.aidotnet.erp.bi.application.BiOutboundService.PmsMetricExportResult;
import com.aidotnet.erp.bi.application.BiOutboundService.ReportDataExportResult;
import com.aidotnet.erp.bi.domain.KpiAlert;
import com.aidotnet.erp.bi.domain.MetricDefinition;
import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.pms.PmsRequestGuard;
import com.aidotnet.erp.common.pms.PmsRequestGuard.PmsDataQueryContext;
import com.aidotnet.erp.common.tenant.TenantContext;
import java.util.Map;
import java.util.List;
import java.util.function.Supplier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * BI出站API控制器
 * <p>
 * 描述: 对工作台、业务中台和其他业务域提供 BI 导出能力。
 * 1. 报表导出返回真实报表快照，不再返回占位时间戳。
 * 2. KPI/指标/仪表盘/告警导出统一复用 BI 域内现有服务，避免导出与查询口径不一致。
 * </p>
 */
@RestController
@RequestMapping("/bi/api/out/v1")
public class BiOutboundController {

    private final BiOutboundService biOutboundService;
    private final PmsRequestGuard pmsRequestGuard;

    public BiOutboundController(BiOutboundService biOutboundService, PmsRequestGuard pmsRequestGuard) {
        this.biOutboundService = biOutboundService;
        this.pmsRequestGuard = pmsRequestGuard;
    }

    @GetMapping("/reports/{reportCode}/data")
    public Result<ReportDataExportResult> exportReportData(
            @PathVariable String reportCode,
            @RequestParam(required = false) String period) {
        return Result.ok(biOutboundService.exportReportData(currentTenant(), reportCode, period));
    }

    @GetMapping("/kpis")
    public Result<List<KpiExportResult>> exportKpis(@RequestParam(required = false) String category) {
        return Result.ok(biOutboundService.exportKpis(currentTenant(), category));
    }

    @GetMapping("/metrics")
    public Result<List<MetricDefinition>> exportMetrics(@RequestParam(required = false) String category) {
        return Result.ok(biOutboundService.exportMetrics(currentTenant(), category));
    }

    @GetMapping("/pms/kpi")
    public Result<List<PmsKpiExportResult>> exportPmsKpis(
            @RequestHeader Map<String, String> headers,
            @RequestParam(required = false) String category,
            @RequestParam(name = "data_level", required = false) String dataLevel) {
        PmsDataQueryContext context = pmsRequestGuard.validateReadOnlyRequest(headers, dataLevel);
        return inTenant(context.tenantId(), () -> Result.ok(biOutboundService.exportPmsKpis(context, category)));
    }

    @GetMapping("/pms/metrics")
    public Result<List<PmsMetricExportResult>> exportPmsMetrics(
            @RequestHeader Map<String, String> headers,
            @RequestParam(required = false) String category,
            @RequestParam(name = "data_level", required = false) String dataLevel) {
        PmsDataQueryContext context = pmsRequestGuard.validateReadOnlyRequest(headers, dataLevel);
        return inTenant(context.tenantId(), () -> Result.ok(biOutboundService.exportPmsMetrics(context, category)));
    }

    @GetMapping("/dashboards/{dashboardId}")
    public Result<DashboardExportResult> exportDashboard(@PathVariable String dashboardId) {
        return Result.ok(biOutboundService.exportDashboard(currentTenant(), dashboardId));
    }

    @GetMapping("/alerts")
    public Result<List<KpiAlert>> exportAlerts(@RequestParam(required = false) String alertType) {
        return Result.ok(biOutboundService.exportAlerts(currentTenant(), alertType));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    private <T> T inTenant(String tenantId, Supplier<T> action) {
        String previousTenantId = TenantContext.getTenantId();
        TenantContext.setTenantId(tenantId);
        try {
            return action.get();
        } finally {
            TenantContext.setTenantId(previousTenantId);
        }
    }
}
