package com.aidotnet.erp.common.approval;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.persistence.entity.ApprovalHistoryEntity;
import com.aidotnet.erp.common.persistence.entity.ApprovalInstanceEntity;
import com.aidotnet.erp.common.persistence.mapper.ApprovalHistoryMapper;
import com.aidotnet.erp.common.persistence.mapper.ApprovalInstanceMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PersistentApprovalService {

    private final ApprovalInstanceMapper instanceMapper;
    private final ApprovalHistoryMapper historyMapper;

    public PersistentApprovalService(ApprovalInstanceMapper instanceMapper, ApprovalHistoryMapper historyMapper) {
        this.instanceMapper = instanceMapper;
        this.historyMapper = historyMapper;
    }

    @Transactional
    public ApprovalInstance start(String tenantId, String businessType, String businessId, String applicant) {
        LocalDateTime now = LocalDateTime.now();
        ApprovalInstanceEntity entity = new ApprovalInstanceEntity();
        entity.setApprovalId("appr-" + UUID.randomUUID());
        entity.setTenantId(tenantId);
        entity.setBusinessType(businessType);
        entity.setBusinessId(businessId);
        entity.setApplicant(applicant);
        entity.setStatus(ApprovalStatus.PENDING.name());
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        instanceMapper.insert(entity);
        appendHistory(entity.getApprovalId(), applicant, "START", null, ApprovalStatus.PENDING, now);
        return get(tenantId, entity.getApprovalId());
    }

    @Transactional
    public ApprovalInstance approve(String tenantId, String approvalId, String actor, String comment) {
        return transit(tenantId, approvalId, actor, "APPROVE", comment, ApprovalStatus.APPROVED);
    }

    @Transactional
    public ApprovalInstance reject(String tenantId, String approvalId, String actor, String comment) {
        return transit(tenantId, approvalId, actor, "REJECT", comment, ApprovalStatus.REJECTED);
    }

    public ApprovalInstance get(String tenantId, String approvalId) {
        ApprovalInstanceEntity entity = mustGet(tenantId, approvalId);
        return toRecord(entity, histories(approvalId));
    }

    private ApprovalInstance transit(String tenantId, String approvalId, String actor, String action, String comment,
                                     ApprovalStatus targetStatus) {
        ApprovalInstanceEntity entity = mustGet(tenantId, approvalId);
        if (!ApprovalStatus.PENDING.name().equals(entity.getStatus())) {
            throw new BizException("INVALID_STATUS", "approval is not pending");
        }
        LocalDateTime now = LocalDateTime.now();
        entity.setStatus(targetStatus.name());
        entity.setUpdatedAt(now);
        instanceMapper.updateById(entity);
        appendHistory(approvalId, actor, action, comment, targetStatus, now);
        return get(tenantId, approvalId);
    }

    private ApprovalInstanceEntity mustGet(String tenantId, String approvalId) {
        ApprovalInstanceEntity entity = instanceMapper.selectOne(new LambdaQueryWrapper<ApprovalInstanceEntity>()
                .eq(ApprovalInstanceEntity::getTenantId, tenantId)
                .eq(ApprovalInstanceEntity::getApprovalId, approvalId));
        if (entity == null) {
            throw new BizException("NOT_FOUND", "approval instance not found");
        }
        return entity;
    }

    private void appendHistory(String approvalId, String actor, String action, String comment, ApprovalStatus status,
                               LocalDateTime operatedAt) {
        ApprovalHistoryEntity history = new ApprovalHistoryEntity();
        history.setApprovalId(approvalId);
        history.setActor(actor);
        history.setAction(action);
        history.setComment(comment);
        history.setStatus(status.name());
        history.setOperatedAt(operatedAt);
        historyMapper.insert(history);
    }

    private List<ApprovalHistory> histories(String approvalId) {
        return historyMapper.selectList(new LambdaQueryWrapper<ApprovalHistoryEntity>()
                        .eq(ApprovalHistoryEntity::getApprovalId, approvalId)
                        .orderByAsc(ApprovalHistoryEntity::getId))
                .stream()
                .map(entity -> new ApprovalHistory(entity.getActor(), entity.getAction(), entity.getComment(),
                        ApprovalStatus.valueOf(entity.getStatus()),
                        entity.getOperatedAt().toInstant(ZoneOffset.UTC)))
                .toList();
    }

    private ApprovalInstance toRecord(ApprovalInstanceEntity entity, List<ApprovalHistory> histories) {
        return new ApprovalInstance(entity.getApprovalId(), entity.getTenantId(), entity.getBusinessType(),
                entity.getBusinessId(), entity.getApplicant(), ApprovalStatus.valueOf(entity.getStatus()), histories);
    }
}
