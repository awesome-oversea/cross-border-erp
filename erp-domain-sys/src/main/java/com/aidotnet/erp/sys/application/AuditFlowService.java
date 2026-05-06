package com.aidotnet.erp.sys.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.sys.domain.AuditFlowConfig;
import com.aidotnet.erp.sys.domain.AuditFlowStep;
import com.aidotnet.erp.sys.infrastructure.AuditFlowStore;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditFlowService {

    private final AuditFlowStore auditFlowStore;

    public AuditFlowService(AuditFlowStore auditFlowStore) {
        this.auditFlowStore = auditFlowStore;
    }

    @Transactional
    public AuditFlowConfig createFlow(String tenantId, CreateFlowCommand command) {
        Instant now = Instant.now();
        AuditFlowConfig flow = new AuditFlowConfig(UUID.randomUUID().toString(), tenantId, command.flowCode(),
                command.flowName(), command.businessType(), command.requiredApprovals(), true, now, now);
        auditFlowStore.saveFlowConfig(flow);
        for (FlowStepCommand stepCmd : command.steps()) {
            AuditFlowStep step = new AuditFlowStep(UUID.randomUUID().toString(), flow.flowId(), tenantId,
                    stepCmd.stepOrder(), stepCmd.stepName(), stepCmd.approverRole(), stepCmd.autoApprove(), now);
            auditFlowStore.saveFlowStep(step);
        }
        return flow;
    }

    @Transactional
    public AuditFlowConfig updateFlow(String tenantId, String flowId, UpdateFlowCommand command) {
        AuditFlowConfig existing = getFlow(tenantId, flowId);
        Instant now = Instant.now();
        AuditFlowConfig updated = new AuditFlowConfig(existing.flowId(), existing.tenantId(), existing.flowCode(),
                command.flowName() != null ? command.flowName() : existing.flowName(),
                command.businessType() != null ? command.businessType() : existing.businessType(),
                command.requiredApprovals() > 0 ? command.requiredApprovals() : existing.requiredApprovals(),
                existing.enabled(), existing.createdAt(), now);
        return auditFlowStore.saveFlowConfig(updated);
    }

    @Transactional
    public AuditFlowConfig toggleFlow(String tenantId, String flowId, boolean enabled) {
        AuditFlowConfig existing = getFlow(tenantId, flowId);
        Instant now = Instant.now();
        AuditFlowConfig updated = new AuditFlowConfig(existing.flowId(), existing.tenantId(), existing.flowCode(),
                existing.flowName(), existing.businessType(), existing.requiredApprovals(), enabled, existing.createdAt(), now);
        return auditFlowStore.saveFlowConfig(updated);
    }

    public AuditFlowConfig getFlow(String tenantId, String flowId) {
        return auditFlowStore.findFlowConfig(tenantId, flowId)
                .orElseThrow(() -> new BizException("FLOW_NOT_FOUND", "审核流程不存在"));
    }

    public AuditFlowConfig getFlowByBusinessType(String tenantId, String businessType) {
        return auditFlowStore.findFlowConfigByBusinessType(tenantId, businessType)
                .orElseThrow(() -> new BizException("FLOW_NOT_FOUND", "审核流程不存在: " + businessType));
    }

    public List<AuditFlowConfig> listFlows(String tenantId) {
        return auditFlowStore.listFlowConfigs(tenantId);
    }

    public List<AuditFlowStep> getFlowSteps(String tenantId, String flowId) {
        return auditFlowStore.listFlowSteps(tenantId, flowId);
    }

    @Transactional
    public AuditFlowStep addFlowStep(String tenantId, String flowId, FlowStepCommand command) {
        Instant now = Instant.now();
        AuditFlowStep step = new AuditFlowStep(UUID.randomUUID().toString(), flowId, tenantId,
                command.stepOrder(), command.stepName(), command.approverRole(), command.autoApprove(), now);
        return auditFlowStore.saveFlowStep(step);
    }

    @Transactional
    public void removeFlowStep(String tenantId, String stepId) {
        auditFlowStore.deleteFlowStep(tenantId, stepId);
    }

    public record CreateFlowCommand(String flowCode, String flowName, String businessType, int requiredApprovals,
                                     List<FlowStepCommand> steps) {}
    public record UpdateFlowCommand(String flowName, String businessType, int requiredApprovals) {}
    public record FlowStepCommand(int stepOrder, String stepName, String approverRole, boolean autoApprove) {}
}
