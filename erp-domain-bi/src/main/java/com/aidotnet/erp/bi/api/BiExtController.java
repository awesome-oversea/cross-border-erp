package com.aidotnet.erp.bi.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.bi.application.BiExtService;
import com.aidotnet.erp.bi.application.BiExtService.CreateAlertRuleCommand;
import com.aidotnet.erp.bi.application.BiExtService.CreateKpiTemplateCommand;
import com.aidotnet.erp.bi.application.BiExtService.CreateReportSnapshotCommand;
import com.aidotnet.erp.bi.application.BiExtService.GenerateRankingCommand;
import com.aidotnet.erp.bi.application.BiExtService.GenerateTrendCommand;
import com.aidotnet.erp.bi.application.BiExtService.UpdateAlertRuleCommand;
import com.aidotnet.erp.bi.domain.AlertCondition;
import com.aidotnet.erp.bi.domain.AlertRule;
import com.aidotnet.erp.bi.domain.AlertSeverity;
import com.aidotnet.erp.bi.domain.CockpitTrend;
import com.aidotnet.erp.bi.domain.KpiTemplate;
import com.aidotnet.erp.bi.domain.RankingData;
import com.aidotnet.erp.bi.domain.RankingData.RankingItem;
import com.aidotnet.erp.bi.domain.ReportSnapshot;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bi/api/in/v1")
public class BiExtController {

    private final BiExtService biExtService;

    public BiExtController(BiExtService biExtService) {
        this.biExtService = biExtService;
    }

    @PostMapping("/alert-rules")
    public Result<AlertRule> createAlertRule(@Valid @RequestBody CreateAlertRuleRequest request) {
        return Result.ok(biExtService.createAlertRule(currentTenant(), new CreateAlertRuleCommand(
                request.ruleName(), request.metricCode(), request.domain(), request.condition(),
                request.threshold(), request.severity(), request.notifyChannel(), request.notifyTargets())));
    }

    @PutMapping("/alert-rules/{ruleId}")
    public Result<AlertRule> updateAlertRule(@PathVariable String ruleId, @Valid @RequestBody UpdateAlertRuleRequest request) {
        return Result.ok(biExtService.updateAlertRule(currentTenant(), ruleId, new UpdateAlertRuleCommand(
                request.ruleName(), request.threshold(), request.severity(), request.notifyChannel(), request.notifyTargets())));
    }

    @PostMapping("/alert-rules/{ruleId}/toggle")
    public Result<AlertRule> toggleAlertRule(@PathVariable String ruleId, @RequestParam boolean enabled) {
        return Result.ok(biExtService.toggleAlertRule(currentTenant(), ruleId, enabled));
    }

    @GetMapping("/alert-rules")
    public Result<List<AlertRule>> listAlertRules(@RequestParam(required = false) String domain) {
        return Result.ok(biExtService.listAlertRules(currentTenant(), domain));
    }

    @GetMapping("/alert-rules/{ruleId}")
    public Result<AlertRule> getAlertRule(@PathVariable String ruleId) {
        return Result.ok(biExtService.getAlertRule(currentTenant(), ruleId));
    }

    @PostMapping("/report-snapshots")
    public Result<ReportSnapshot> createReportSnapshot(@Valid @RequestBody CreateReportSnapshotRequest request) {
        return Result.ok(biExtService.createReportSnapshot(currentTenant(), new CreateReportSnapshotCommand(
                request.reportId(), request.snapshotName(), request.snapshotData(), request.format())));
    }

    @GetMapping("/report-snapshots")
    public Result<List<ReportSnapshot>> listReportSnapshots(@RequestParam String reportId) {
        return Result.ok(biExtService.listReportSnapshots(currentTenant(), reportId));
    }

    @GetMapping("/report-snapshots/{snapshotId}")
    public Result<ReportSnapshot> getReportSnapshot(@PathVariable String snapshotId) {
        return Result.ok(biExtService.getReportSnapshot(currentTenant(), snapshotId));
    }

    @GetMapping("/cockpit")
    public Result<Map<String, Object>> getCockpitData() {
        return Result.ok(biExtService.getCockpitData(currentTenant()));
    }

    @PostMapping("/kpi-templates")
    public Result<KpiTemplate> createKpiTemplate(@Valid @RequestBody CreateKpiTemplateRequest request) {
        return Result.ok(biExtService.createKpiTemplate(currentTenant(), new CreateKpiTemplateCommand(
                request.templateCode(), request.templateName(), request.category(),
                request.defaultUnit(), request.defaultTargetFormula(), request.description())));
    }

    @GetMapping("/kpi-templates")
    public Result<List<KpiTemplate>> listKpiTemplates() {
        return Result.ok(biExtService.listKpiTemplates(currentTenant()));
    }

    @GetMapping("/kpi-templates/{templateId}")
    public Result<KpiTemplate> getKpiTemplate(@PathVariable String templateId) {
        return Result.ok(biExtService.getKpiTemplate(currentTenant(), templateId));
    }

    @PostMapping("/trends")
    public Result<CockpitTrend> generateTrend(@Valid @RequestBody GenerateTrendRequest request) {
        return Result.ok(biExtService.generateTrend(currentTenant(), new GenerateTrendCommand(
                request.metricCode(), request.metricName(), request.period(), request.dataPoints())));
    }

    @GetMapping("/trends")
    public Result<List<CockpitTrend>> listTrends(@RequestParam(required = false) String metricCode) {
        return Result.ok(biExtService.listTrends(currentTenant(), metricCode));
    }

    @PostMapping("/rankings")
    public Result<RankingData> generateRanking(@Valid @RequestBody GenerateRankingRequest request) {
        return Result.ok(biExtService.generateRanking(currentTenant(), new GenerateRankingCommand(
                request.rankingType(), request.dimension(), request.items())));
    }

    @GetMapping("/rankings")
    public Result<List<RankingData>> listRankings(@RequestParam(required = false) String rankingType) {
        return Result.ok(biExtService.listRankings(currentTenant(), rankingType));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record CreateAlertRuleRequest(@NotBlank String ruleName, @NotBlank String metricCode, String domain,
                                         AlertCondition condition, @NotBlank String threshold, AlertSeverity severity,
                                         String notifyChannel, String notifyTargets) {}
    public record UpdateAlertRuleRequest(String ruleName, String threshold, AlertSeverity severity,
                                         String notifyChannel, String notifyTargets) {}
    public record CreateReportSnapshotRequest(@NotBlank String reportId, @NotBlank String snapshotName,
                                              String snapshotData, String format) {}
    public record CreateKpiTemplateRequest(@NotBlank String templateCode, @NotBlank String templateName,
                                           @NotBlank String category, String defaultUnit,
                                           String defaultTargetFormula, String description) {}
    public record GenerateTrendRequest(@NotBlank String metricCode, @NotBlank String metricName,
                                       String period, int dataPoints) {}
    public record GenerateRankingRequest(@NotBlank String rankingType, @NotBlank String dimension,
                                         List<RankingItem> items) {}
}
