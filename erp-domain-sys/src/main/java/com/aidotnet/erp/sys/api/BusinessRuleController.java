package com.aidotnet.erp.sys.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.sys.application.BusinessRuleService;
import com.aidotnet.erp.sys.application.BusinessRuleService.CreateRuleVersionCommand;
import com.aidotnet.erp.sys.application.BusinessRuleService.ExecutionStatistics;
import com.aidotnet.erp.sys.application.BusinessRuleService.LogExecutionCommand;
import com.aidotnet.erp.sys.application.BusinessRuleService.SimulateRuleCommand;
import com.aidotnet.erp.sys.domain.BusinessRuleVersion;
import com.aidotnet.erp.sys.domain.RuleExecutionLog;
import com.aidotnet.erp.sys.domain.SimulationReplay;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
@RequestMapping("/sys/api/in/v1/business-rules")
public class BusinessRuleController {

    private final BusinessRuleService businessRuleService;

    public BusinessRuleController(BusinessRuleService businessRuleService) {
        this.businessRuleService = businessRuleService;
    }

    @PostMapping("/versions")
    public Result<BusinessRuleVersion> createVersion(@Valid @RequestBody CreateVersionRequest request) {
        return Result.ok(businessRuleService.createVersion(currentTenant(), new CreateRuleVersionCommand(
                request.ruleId(), request.ruleType(), request.ruleName(), request.contentJson(),
                request.changeDescription(), request.changedBy())));
    }

    @PostMapping("/{ruleId}/rollback/{version}")
    public Result<BusinessRuleVersion> rollbackVersion(@PathVariable String ruleId, @PathVariable int version,
                                                        @RequestBody RollbackRequest request) {
        return Result.ok(businessRuleService.rollbackToVersion(currentTenant(), ruleId, version, request.changedBy()));
    }

    @GetMapping("/{ruleId}/versions")
    public Result<List<BusinessRuleVersion>> listVersions(@PathVariable String ruleId) {
        return Result.ok(businessRuleService.listVersions(currentTenant(), ruleId));
    }

    @GetMapping("/{ruleId}/versions/latest")
    public Result<BusinessRuleVersion> getLatestVersion(@PathVariable String ruleId) {
        return Result.ok(businessRuleService.getLatestVersion(currentTenant(), ruleId));
    }

    @GetMapping("/{ruleId}/versions/{version}")
    public Result<BusinessRuleVersion> getVersion(@PathVariable String ruleId, @PathVariable int version) {
        return Result.ok(businessRuleService.getVersion(currentTenant(), ruleId, version));
    }

    @GetMapping("/versions/by-type")
    public Result<List<BusinessRuleVersion>> listVersionsByType(@RequestParam String ruleType) {
        return Result.ok(businessRuleService.listVersionsByType(currentTenant(), ruleType));
    }

    @PostMapping("/simulate")
    public Result<SimulationReplay> simulate(@Valid @RequestBody SimulateRequest request) {
        return Result.ok(businessRuleService.simulate(currentTenant(), new SimulateRuleCommand(
                request.ruleId(), request.ruleVersion(), request.inputContext())));
    }

    @PostMapping("/simulate/batch")
    public Result<List<SimulationReplay>> batchSimulate(@RequestBody List<SimulateRequest> requests) {
        List<SimulateRuleCommand> commands = requests.stream()
                .map(r -> new SimulateRuleCommand(r.ruleId(), r.ruleVersion(), r.inputContext()))
                .toList();
        return Result.ok(businessRuleService.batchSimulate(currentTenant(), commands));
    }

    @GetMapping("/{ruleId}/simulations")
    public Result<List<SimulationReplay>> listSimulations(@PathVariable String ruleId) {
        return Result.ok(businessRuleService.listSimulationReplays(currentTenant(), ruleId));
    }

    @PostMapping("/execution-logs")
    public Result<RuleExecutionLog> logExecution(@Valid @RequestBody LogExecutionRequest request) {
        return Result.ok(businessRuleService.logExecution(currentTenant(), new LogExecutionCommand(
                request.ruleId(), request.ruleVersion(), request.ruleType(), request.businessType(),
                request.referenceId(), request.inputContext(), request.outputResult(),
                request.success(), request.errorMessage(), request.executionTimeMs())));
    }

    @GetMapping("/{ruleId}/execution-logs")
    public Result<List<RuleExecutionLog>> listExecutionLogs(@PathVariable String ruleId,
                                                             @RequestParam(required = false) String businessType) {
        return Result.ok(businessRuleService.listExecutionLogs(currentTenant(), ruleId, businessType));
    }

    @GetMapping("/execution-logs/by-reference/{referenceId}")
    public Result<List<RuleExecutionLog>> listExecutionLogsByReference(@PathVariable String referenceId) {
        return Result.ok(businessRuleService.listExecutionLogsByReference(currentTenant(), referenceId));
    }

    @GetMapping("/{ruleId}/statistics")
    public Result<ExecutionStatistics> getStatistics(@PathVariable String ruleId) {
        return Result.ok(businessRuleService.getExecutionStatistics(currentTenant(), ruleId));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record CreateVersionRequest(@NotBlank String ruleId, @NotBlank String ruleType,
                                       @NotBlank String ruleName, @NotBlank String contentJson,
                                       String changeDescription, String changedBy) {}
    public record RollbackRequest(@NotBlank String changedBy) {}
    public record SimulateRequest(@NotBlank String ruleId, int ruleVersion,
                                  Map<String, Object> inputContext) {}
    public record LogExecutionRequest(@NotBlank String ruleId, int ruleVersion, @NotBlank String ruleType,
                                      String businessType, String referenceId,
                                      Map<String, Object> inputContext, Map<String, Object> outputResult,
                                      boolean success, String errorMessage, long executionTimeMs) {}
}
