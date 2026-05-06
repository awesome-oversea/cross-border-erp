package com.aidotnet.erp.sys.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.sys.domain.ApprovalFlowDefinition;
import com.aidotnet.erp.sys.domain.ApprovalStep;
import com.aidotnet.erp.sys.infrastructure.SysExtStore;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 审批流程应用服务
 * <p>
 * 描述: 系统设置域审批流程服务，负责审批流程的发起/审批/拒绝/取消等
 *       业务逻辑。支持多级审批和审批历史记录。
 * </p>
 *
 * @author ERP系统
 */
@Service
public class ApprovalFlowService {
    private final Map<String, ApprovalFlowDefinition> flowStore = new ConcurrentHashMap<>();

    @Transactional
    public ApprovalFlowDefinition createFlow(String tenantId, CreateFlowCommand command) {
        flowStore.values().stream()
                .filter(f -> f.tenantId().equals(tenantId) && f.flowCode().equals(command.flowCode()))
                .findFirst()
                .ifPresent(existing -> { throw new BizException("FLOW_CODE_DUPLICATED", "流程编码已存在"); });
        Instant now = Instant.now();
        ApprovalFlowDefinition flow = new ApprovalFlowDefinition(
                UUID.randomUUID().toString(), tenantId, command.flowCode(), command.flowName(),
                command.businessType(), command.description(), command.steps(), true, now, now);
        flowStore.put(flow.flowId(), flow);
        return flow;
    }

    @Transactional
    public ApprovalFlowDefinition updateFlow(String tenantId, String flowId, UpdateFlowCommand command) {
        ApprovalFlowDefinition existing = getFlow(tenantId, flowId);
        Instant now = Instant.now();
        ApprovalFlowDefinition updated = new ApprovalFlowDefinition(
                existing.flowId(), existing.tenantId(), existing.flowCode(),
                command.flowName() != null ? command.flowName() : existing.flowName(),
                command.businessType() != null ? command.businessType() : existing.businessType(),
                command.description() != null ? command.description() : existing.description(),
                command.steps() != null ? command.steps() : existing.steps(),
                existing.enabled(), existing.createdAt(), now);
        flowStore.put(flowId, updated);
        return updated;
    }

    @Transactional
    public ApprovalFlowDefinition toggleFlow(String tenantId, String flowId, boolean enabled) {
        ApprovalFlowDefinition existing = getFlow(tenantId, flowId);
        Instant now = Instant.now();
        ApprovalFlowDefinition updated = new ApprovalFlowDefinition(
                existing.flowId(), existing.tenantId(), existing.flowCode(), existing.flowName(),
                existing.businessType(), existing.description(), existing.steps(), enabled,
                existing.createdAt(), now);
        flowStore.put(flowId, updated);
        return updated;
    }

    public ApprovalFlowDefinition getFlow(String tenantId, String flowId) {
        ApprovalFlowDefinition flow = flowStore.get(flowId);
        if (flow == null || !flow.tenantId().equals(tenantId)) {
            throw new BizException("FLOW_NOT_FOUND", "审核流程不存在");
        }
        return flow;
    }

    public ApprovalFlowDefinition getFlowByBusinessType(String tenantId, String businessType) {
        return flowStore.values().stream()
                .filter(f -> f.tenantId().equals(tenantId) && f.businessType().equals(businessType) && f.enabled())
                .findFirst()
                .orElseThrow(() -> new BizException("FLOW_NOT_FOUND", "未找到该业务类型的审核流程"));
    }

    public List<ApprovalFlowDefinition> listFlows(String tenantId, String businessType) {
        return flowStore.values().stream()
                .filter(f -> f.tenantId().equals(tenantId))
                .filter(f -> businessType == null || f.businessType().equals(businessType))
                .toList();
    }

    public record CreateFlowCommand(String flowCode, String flowName, String businessType,
                                    String description, List<ApprovalStep> steps) {}
    public record UpdateFlowCommand(String flowName, String businessType,
                                    String description, List<ApprovalStep> steps) {}
}
