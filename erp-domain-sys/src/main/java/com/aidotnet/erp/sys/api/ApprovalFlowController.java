package com.aidotnet.erp.sys.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.sys.application.ApprovalFlowService;
import com.aidotnet.erp.sys.application.ApprovalFlowService.CreateFlowCommand;
import com.aidotnet.erp.sys.application.ApprovalFlowService.UpdateFlowCommand;
import com.aidotnet.erp.sys.domain.ApprovalFlowDefinition;
import com.aidotnet.erp.sys.domain.ApprovalStep;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
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
@RequestMapping("/sys/api/in/v1/approval-flows")
public class ApprovalFlowController {

    private final ApprovalFlowService approvalFlowService;

    public ApprovalFlowController(ApprovalFlowService approvalFlowService) {
        this.approvalFlowService = approvalFlowService;
    }

    @PostMapping
    public Result<ApprovalFlowDefinition> createFlow(@Valid @RequestBody CreateFlowRequest request) {
        return Result.ok(approvalFlowService.createFlow(currentTenant(), new CreateFlowCommand(
                request.flowCode(), request.flowName(), request.businessType(),
                request.description(), request.steps())));
    }

    @PutMapping("/{flowId}")
    public Result<ApprovalFlowDefinition> updateFlow(@PathVariable String flowId,
                                                     @Valid @RequestBody UpdateFlowRequest request) {
        return Result.ok(approvalFlowService.updateFlow(currentTenant(), flowId, new UpdateFlowCommand(
                request.flowName(), request.businessType(), request.description(), request.steps())));
    }

    @PatchMapping("/{flowId}/toggle")
    public Result<ApprovalFlowDefinition> toggleFlow(@PathVariable String flowId, @RequestParam boolean enabled) {
        return Result.ok(approvalFlowService.toggleFlow(currentTenant(), flowId, enabled));
    }

    @GetMapping
    public Result<List<ApprovalFlowDefinition>> listFlows(@RequestParam(required = false) String businessType) {
        return Result.ok(approvalFlowService.listFlows(currentTenant(), businessType));
    }

    @GetMapping("/{flowId}")
    public Result<ApprovalFlowDefinition> getFlow(@PathVariable String flowId) {
        return Result.ok(approvalFlowService.getFlow(currentTenant(), flowId));
    }

    @GetMapping("/by-business-type/{businessType}")
    public Result<ApprovalFlowDefinition> getFlowByBusinessType(@PathVariable String businessType) {
        return Result.ok(approvalFlowService.getFlowByBusinessType(currentTenant(), businessType));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record CreateFlowRequest(@NotBlank String flowCode, @NotBlank String flowName,
                                    @NotBlank String businessType, String description,
                                    List<ApprovalStep> steps) {}
    public record UpdateFlowRequest(String flowName, String businessType,
                                    String description, List<ApprovalStep> steps) {}
}
