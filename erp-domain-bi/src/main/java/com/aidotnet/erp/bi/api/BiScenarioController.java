package com.aidotnet.erp.bi.api;

import com.aidotnet.erp.bi.application.BiScenarioService;
import com.aidotnet.erp.bi.application.BiScenarioService.CreateCustomReportCommand;
import com.aidotnet.erp.bi.application.BiScenarioService.CreateDeveloperCommissionReportCommand;
import com.aidotnet.erp.bi.domain.CustomReport;
import com.aidotnet.erp.bi.domain.DeveloperCommissionReport;
import com.aidotnet.erp.bi.domain.ReportSnapshot;
import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/bi/api/in/v1", "/bi/api/v1"})
public class BiScenarioController {

    private final BiScenarioService scenarioService;

    public BiScenarioController(BiScenarioService scenarioService) {
        this.scenarioService = scenarioService;
    }

    @PostMapping("/custom-reports")
    public Result<CustomReport> createCustomReport(@Valid @RequestBody CreateCustomReportRequest request) {
        return Result.ok(scenarioService.createCustomReport(currentTenant(), new CreateCustomReportCommand(
                request.reportCode(), request.reportName(), request.subjectArea(), request.dimensions(),
                request.metrics(), request.filters(), request.sorts(), request.visibility(),
                request.permissionCode(), request.dataLevel(), request.description())));
    }

    @GetMapping("/custom-reports")
    public Result<List<CustomReport>> listCustomReports(@RequestParam(required = false) String subjectArea) {
        return Result.ok(scenarioService.listCustomReports(currentTenant(), subjectArea));
    }

    @GetMapping("/custom-reports/{reportId}")
    public Result<CustomReport> getCustomReport(@PathVariable String reportId) {
        return Result.ok(scenarioService.getCustomReport(currentTenant(), reportId));
    }

    @PostMapping("/custom-reports/{reportId}/run")
    public Result<ReportSnapshot> runCustomReport(@PathVariable String reportId) {
        return Result.ok(scenarioService.runCustomReport(currentTenant(), reportId));
    }

    @PostMapping("/custom-reports/{reportId}/exports")
    public Result<com.aidotnet.erp.bi.domain.DataExportTask> exportCustomReport(@PathVariable String reportId,
                                                                                 @RequestBody(required = false) ExportCustomReportRequest request) {
        return Result.ok(scenarioService.exportCustomReport(currentTenant(), reportId,
                request != null ? request.format() : null));
    }

    @PostMapping("/developer-commission")
    public Result<DeveloperCommissionReport> createDeveloperCommission(@Valid @RequestBody CreateDeveloperCommissionRequest request) {
        return Result.ok(scenarioService.createDeveloperCommissionReport(currentTenant(),
                new CreateDeveloperCommissionReportCommand(
                        request.userId(), request.userName(), request.teamCode(), request.period(),
                        request.skuCount(), request.orderCount(), request.salesProfit(), request.kpiScore(),
                        request.baseCommissionRate(), request.currency())));
    }

    @GetMapping("/developer-commission")
    public Result<List<DeveloperCommissionReport>> listDeveloperCommissionReports(
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String userId) {
        return Result.ok(scenarioService.listDeveloperCommissionReports(currentTenant(), period, userId));
    }

    @GetMapping("/developer-commission/{reportId}")
    public Result<DeveloperCommissionReport> getDeveloperCommissionReport(@PathVariable String reportId) {
        return Result.ok(scenarioService.getDeveloperCommissionReport(currentTenant(), reportId));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "Tenant id is required");
        }
        return tenantId;
    }

    public record CreateCustomReportRequest(@NotBlank String reportCode,
                                            @NotBlank String reportName,
                                            @NotBlank String subjectArea,
                                            @NotEmpty List<String> dimensions,
                                            @NotEmpty List<String> metrics,
                                            Map<String, Object> filters,
                                            List<String> sorts,
                                            String visibility,
                                            String permissionCode,
                                            String dataLevel,
                                            String description) {}

    public record ExportCustomReportRequest(String format) {}

    public record CreateDeveloperCommissionRequest(@NotBlank String userId,
                                                   String userName,
                                                   String teamCode,
                                                   @Pattern(regexp = "\\d{4}-\\d{2}") String period,
                                                   @Positive int skuCount,
                                                   @PositiveOrZero int orderCount,
                                                   @Positive BigDecimal salesProfit,
                                                   @Positive BigDecimal kpiScore,
                                                   @Positive BigDecimal baseCommissionRate,
                                                   @NotBlank String currency) {}
}
