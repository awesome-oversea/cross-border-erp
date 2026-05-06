package com.aidotnet.erp.sys.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.sys.application.ComplianceService;
import com.aidotnet.erp.sys.application.ComplianceService.CreateAlertCommand;
import com.aidotnet.erp.sys.application.ComplianceService.CreateComplianceRuleCommand;
import com.aidotnet.erp.sys.application.ComplianceService.RecordPolicyChangeCommand;
import com.aidotnet.erp.sys.application.ComplianceService.UpdateComplianceRuleCommand;
import com.aidotnet.erp.sys.domain.ComplianceAlert;
import com.aidotnet.erp.sys.domain.ComplianceRule;
import com.aidotnet.erp.sys.domain.PlatformPolicyChange;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys/api/in/v1/compliance")
public class ComplianceController {

    private final ComplianceService complianceService;

    public ComplianceController(ComplianceService complianceService) {
        this.complianceService = complianceService;
    }

    @PostMapping("/rules")
    public Result<ComplianceRule> createRule(@Valid @RequestBody CreateRuleRequest request) {
        return Result.ok(complianceService.createComplianceRule(currentTenant(), new CreateComplianceRuleCommand(
                request.platform(), request.ruleType(), request.ruleName(), request.description(), request.severity())));
    }

    @PutMapping("/rules/{ruleId}")
    public Result<ComplianceRule> updateRule(@PathVariable String ruleId, @Valid @RequestBody UpdateRuleRequest request) {
        return Result.ok(complianceService.updateComplianceRule(currentTenant(), ruleId, new UpdateComplianceRuleCommand(
                request.ruleName(), request.description(), request.severity(), request.enabled())));
    }

    @GetMapping("/rules")
    public Result<List<ComplianceRule>> listRules(@RequestParam(required = false) String platform,
                                                   @RequestParam(required = false) String ruleType) {
        return Result.ok(complianceService.listComplianceRules(currentTenant(), platform, ruleType));
    }

    @PostMapping("/alerts")
    public Result<ComplianceAlert> createAlert(@Valid @RequestBody CreateAlertRequest request) {
        return Result.ok(complianceService.createAlert(currentTenant(), new CreateAlertCommand(
                request.platform(), request.ruleId(), request.alertType(), request.title(),
                request.description(), request.severity(), request.referenceType(), request.referenceId())));
    }

    @PutMapping("/alerts/{alertId}/resolve")
    public Result<ComplianceAlert> resolveAlert(@PathVariable String alertId) {
        return Result.ok(complianceService.resolveAlert(currentTenant(), alertId, null));
    }

    @GetMapping("/alerts")
    public Result<List<ComplianceAlert>> listAlerts(@RequestParam(required = false) String platform,
                                                     @RequestParam(required = false) String status) {
        return Result.ok(complianceService.listAlerts(currentTenant(), platform, status));
    }

    @PostMapping("/policy-changes")
    public Result<PlatformPolicyChange> recordPolicyChange(@Valid @RequestBody RecordPolicyChangeRequest request) {
        return Result.ok(complianceService.recordPolicyChange(new RecordPolicyChangeCommand(
                request.platform(), request.policyArea(), request.changeTitle(), request.changeSummary(),
                request.impactLevel(), request.sourceUrl(), request.effectiveDate())));
    }

    @GetMapping("/policy-changes")
    public Result<List<PlatformPolicyChange>> listPolicyChanges(@RequestParam(required = false) String platform,
                                                                 @RequestParam(required = false) String policyArea) {
        return Result.ok(complianceService.listPolicyChanges(platform, policyArea));
    }

    @PostMapping("/scan")
    public Result<Void> scanCompliance(@RequestParam String platform) {
        complianceService.scanCompliance(currentTenant(), platform);
        return Result.ok(null);
    }

    private String currentTenant() { return TenantContext.getTenantId(); }

    public record CreateRuleRequest(@NotBlank String platform, @NotBlank String ruleType,
                                    @NotBlank String ruleName, String description, String severity) {}
    public record UpdateRuleRequest(String ruleName, String description, String severity, Boolean enabled) {}
    public record CreateAlertRequest(@NotBlank String platform, String ruleId, @NotBlank String alertType,
                                     @NotBlank String title, String description, String severity,
                                     String referenceType, String referenceId) {}
    public record RecordPolicyChangeRequest(@NotBlank String platform, @NotBlank String policyArea,
                                            @NotBlank String changeTitle, String changeSummary,
                                            String impactLevel, String sourceUrl, Instant effectiveDate) {}
}
