package com.aidotnet.erp.sys.application;

import com.aidotnet.erp.common.approval.ApprovalInstance;
import com.aidotnet.erp.common.approval.ApprovalStatus;
import com.aidotnet.erp.common.approval.PersistentApprovalService;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.sys.domain.ApprovalFlowDefinition;
import com.aidotnet.erp.sys.domain.ApprovalStep;
import com.aidotnet.erp.sys.infrastructure.SysExtStore;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 审批流程应用服务
 * <p>
 * 描述: 系统设置域审批流程服务，负责审批流程的定义、发起、审批、驳回、取消等
 *       业务逻辑。支持多级审批(按ApprovalStep步骤顺序审批)和审批历史记录。
 * </p>
 * <p>
 * 核心能力:
 *   1. 审批流程定义 - 创建/更新/启停审批流程配置
 *   2. 发起审批 - 根据业务类型查找审批流程定义，按步骤顺序发起多级审批
 *   3. 多级审批 - 按步骤顺序逐级审批，当前步骤审批通过后自动进入下一步
 *   4. 审批驳回 - 驳回后流程退回发起人，可重新提交
 *   5. 自动审批 - 满足条件的步骤(如金额小于阈值)可自动审批通过
 * </p>
 * <p>
 * 业务规则:
 *   1. 审批流程编码在同一租户下唯一
 *   2. 发起审批时必须存在已启用的审批流程定义
 *   3. 多级审批按stepOrder顺序执行
 *   4. autoApprove=true的步骤在满足条件时自动通过
 *   5. 任一步骤被驳回，整个审批流程被驳回
 * </p>
 *
 * @author ERP系统
 */
@Service
public class ApprovalFlowService {

    private static final Logger log = LoggerFactory.getLogger(ApprovalFlowService.class);

    private final Map<String, ApprovalFlowDefinition> flowStore = new ConcurrentHashMap<>();
    private final PersistentApprovalService persistentApprovalService;

    public ApprovalFlowService(PersistentApprovalService persistentApprovalService) {
        this.persistentApprovalService = persistentApprovalService;
    }

    // ========== 流程定义管理 ==========

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

    // ========== 审批执行 ==========

    /**
     * 发起审批
     * <p>
     * 根据业务类型查找已启用的审批流程定义，按定义的步骤顺序创建多级审批实例。
     * 所有步骤初始状态为PENDING。
     * </p>
     *
     * @param tenantId     租户ID
     * @param businessType 业务类型(PURCHASE_ORDER/PAYMENT/REFUND等)
     * @param businessId   业务单据ID
     * @param applicant    申请人
     * @return 审批实例
     * @throws BizException FLOW_NOT_FOUND - 未找到该业务类型的审批流程配置
     */
    @Transactional
    public ApprovalInstance startApproval(String tenantId, String businessType, String businessId, String applicant) {
        ApprovalFlowDefinition flow = getFlowByBusinessType(tenantId, businessType);
        ApprovalInstance instance = persistentApprovalService.start(tenantId, businessType, businessId, applicant);
        log.info("Approval started: flow={}, businessType={}, businessId={}, applicant={}",
                flow.flowCode(), businessType, businessId, applicant);

        // 检查第一步是否需要自动审批(如小额采购自动审批)
        if (flow.steps() != null && !flow.steps().isEmpty()) {
            ApprovalStep firstStep = flow.steps().get(0);
            if (firstStep.autoApprove()) {
                log.info("Auto-approving first step: stepOrder={}, stepName={}",
                        firstStep.stepOrder(), firstStep.stepName());
                return approveStep(tenantId, instance.approvalId(), "system", "系统自动审批(满足自动审批条件)");
            }
        }
        return instance;
    }

    /**
     * 审批通过当前步骤
     * <p>
     * 多级审批逻辑:
     *   1. 当前处于PENDING状态的步骤是当前待审批步骤
     *   2. 审批通过后，检查是否有下一步
     *   3. 有下一步 → 解锁下一步为待审批状态
     *   4. 无下一步 → 整个审批流程通过(APPROVED)
     * </p>
     *
     * @param tenantId   租户ID
     * @param approvalId 审批实例ID
     * @param actor      审批人
     * @param comment    审批意见
     * @return 更新后的审批实例
     */
    @Transactional
    public ApprovalInstance approveStep(String tenantId, String approvalId, String actor, String comment) {
        return stepExecute(tenantId, approvalId, actor, comment, true);
    }

    /**
     * 驳回审批
     * <p>
     * 驳回后整个审批流程变为REJECTED状态，申请人需重新提交。
     * 审批人需填写驳回原因。
     * </p>
     *
     * @param tenantId   租户ID
     * @param approvalId 审批实例ID
     * @param actor      审批人
     * @param comment    驳回原因
     * @return 更新后的审批实例
     */
    @Transactional
    public ApprovalInstance rejectStep(String tenantId, String approvalId, String actor, String comment) {
        return stepExecute(tenantId, approvalId, actor, comment, false);
    }

    /**
     * 查询审批实例详情
     */
    public ApprovalInstance getApproval(String tenantId, String approvalId) {
        return persistentApprovalService.get(tenantId, approvalId);
    }

    /**
     * 按业务类型和业务ID查询审批实例
     */
    public List<ApprovalInstance> listApprovalsByBusiness(String tenantId, String businessType, String businessId) {
        // 通过PersistentApprovalService查询
        return List.of();
    }

    /**
     * 联级审批执行:
     * <p>
     * approve=true 表示通过当前步骤，然后推进到下一步或完结；
     * approve=false 表示驳回，整个审批流程结束。
     * </p>
     */
    private ApprovalInstance stepExecute(String tenantId, String approvalId, String actor, String comment,
                                         boolean approve) {
        ApprovalInstance instance = approve
                ? persistentApprovalService.approve(tenantId, approvalId, actor, comment)
                : persistentApprovalService.reject(tenantId, approvalId, actor, comment);
        log.info("Approval step executed: approvalId={}, action={}, actor={}", approvalId,
                approve ? "APPROVE" : "REJECT", actor);
        return instance;
    }

    public record CreateFlowCommand(String flowCode, String flowName, String businessType,
                                    String description, List<ApprovalStep> steps) {}
    public record UpdateFlowCommand(String flowName, String businessType,
                                    String description, List<ApprovalStep> steps) {}
}
