package com.aidotnet.erp.sys.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.sys.application.AuditFlowService;
import com.aidotnet.erp.sys.application.AuditFlowService.CreateFlowCommand;
import com.aidotnet.erp.sys.application.AuditFlowService.FlowStepCommand;
import com.aidotnet.erp.sys.application.AuditFlowService.UpdateFlowCommand;
import com.aidotnet.erp.sys.domain.AuditFlowConfig;
import com.aidotnet.erp.sys.domain.AuditFlowStep;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys/api/in/v1/audit-flows")
public class AuditFlowController {

    private final AuditFlowService service;

    public AuditFlowController(AuditFlowService service) {
        this.service = service;
    }

    @PostMapping
    public Result<AuditFlowConfig> createFlow(@Valid @RequestBody CreateFlowRequest request) {
        CreateFlowCommand command = new CreateFlowCommand(request.flowCode(), request.flowName(),
                request.businessType(), request.requiredApprovals(),
                request.steps().stream().map(s -> new FlowStepCommand(s.stepOrder(), s.stepName(),
                        s.approverRole(), s.autoApprove())).toList());
        return Result.ok(service.createFlow(currentTenant(), command));
    }

    @PatchMapping("/{flowId}")
    public Result<AuditFlowConfig> updateFlow(@PathVariable String flowId, @RequestBody UpdateFlowRequest request) {
        UpdateFlowCommand command = new UpdateFlowCommand(request.flowName(), request.businessType(), request.requiredApprovals());
        return Result.ok(service.updateFlow(currentTenant(), flowId, command));
    }

    @PatchMapping("/{flowId}/toggle")
    public Result<AuditFlowConfig> toggleFlow(@PathVariable String flowId, @RequestParam boolean enabled) {
        return Result.ok(service.toggleFlow(currentTenant(), flowId, enabled));
    }

    @GetMapping
    public Result<List<AuditFlowConfig>> listFlows() {
        return Result.ok(service.listFlows(currentTenant()));
    }

    @GetMapping("/{flowId}")
    public Result<AuditFlowConfig> getFlow(@PathVariable String flowId) {
        return Result.ok(service.getFlow(currentTenant(), flowId));
    }

    @GetMapping("/by-business-type")
    public Result<AuditFlowConfig> getFlowByBusinessType(@RequestParam String businessType) {
        return Result.ok(service.getFlowByBusinessType(currentTenant(), businessType));
    }

    @GetMapping("/{flowId}/steps")
    public Result<List<AuditFlowStep>> getFlowSteps(@PathVariable String flowId) {
        return Result.ok(service.getFlowSteps(currentTenant(), flowId));
    }

    @PostMapping("/{flowId}/steps")
    public Result<AuditFlowStep> addFlowStep(@PathVariable String flowId, @Valid @RequestBody FlowStepRequest request) {
        FlowStepCommand command = new FlowStepCommand(request.stepOrder(), request.stepName(), request.approverRole(), request.autoApprove());
        return Result.ok(service.addFlowStep(currentTenant(), flowId, command));
    }

    @DeleteMapping("/steps/{stepId}")
    public Result<Void> removeFlowStep(@PathVariable String stepId) {
        service.removeFlowStep(currentTenant(), stepId);
        return Result.ok(null);
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record CreateFlowRequest(@NotBlank String flowCode, @NotBlank String flowName,
                                     @NotBlank String businessType, int requiredApprovals,
                                     List<FlowStepRequest> steps) {}
    public record UpdateFlowRequest(String flowName, String businessType, int requiredApprovals) {}
    public record FlowStepRequest(int stepOrder, @NotBlank String stepName, String approverRole, boolean autoApprove) {}
}
