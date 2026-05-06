package com.aidotnet.erp.common.approval;

import com.aidotnet.erp.common.exception.BizException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ApprovalService {

    private final Map<String, ApprovalInstance> instances = new ConcurrentHashMap<>();

    public ApprovalInstance start(String tenantId, String businessType, String businessId, String applicant) {
        ApprovalInstance instance = new ApprovalInstance(
                "appr-" + UUID.randomUUID(), tenantId, businessType, businessId, applicant, ApprovalStatus.PENDING,
                new ArrayList<>());
        instance.history().add(new ApprovalHistory(applicant, "START", null, ApprovalStatus.PENDING, Instant.now()));
        instances.put(instance.approvalId(), instance);
        return instance;
    }

    public ApprovalInstance approve(String tenantId, String approvalId, String actor, String comment) {
        ApprovalInstance instance = mustGet(tenantId, approvalId);
        if (instance.status() != ApprovalStatus.PENDING) {
            throw new BizException("INVALID_STATUS", "approval is not pending");
        }
        ApprovalInstance approved = instance.withStatus(ApprovalStatus.APPROVED);
        approved.history().add(new ApprovalHistory(actor, "APPROVE", comment, ApprovalStatus.APPROVED, Instant.now()));
        instances.put(approvalId, approved);
        return approved;
    }

    public ApprovalInstance reject(String tenantId, String approvalId, String actor, String comment) {
        ApprovalInstance instance = mustGet(tenantId, approvalId);
        if (instance.status() != ApprovalStatus.PENDING) {
            throw new BizException("INVALID_STATUS", "approval is not pending");
        }
        ApprovalInstance rejected = instance.withStatus(ApprovalStatus.REJECTED);
        rejected.history().add(new ApprovalHistory(actor, "REJECT", comment, ApprovalStatus.REJECTED, Instant.now()));
        instances.put(approvalId, rejected);
        return rejected;
    }

    public ApprovalInstance get(String tenantId, String approvalId) {
        return mustGet(tenantId, approvalId);
    }

    private ApprovalInstance mustGet(String tenantId, String approvalId) {
        ApprovalInstance instance = instances.get(approvalId);
        if (instance == null || !instance.tenantId().equals(tenantId)) {
            throw new BizException("NOT_FOUND", "approval instance not found");
        }
        return instance;
    }
}
