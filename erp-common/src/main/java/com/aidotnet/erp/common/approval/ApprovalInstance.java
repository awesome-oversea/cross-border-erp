package com.aidotnet.erp.common.approval;

import java.util.List;

public record ApprovalInstance(String approvalId, String tenantId, String businessType, String businessId, String applicant,
                               ApprovalStatus status, List<ApprovalHistory> history) {

    public ApprovalInstance withStatus(ApprovalStatus newStatus) {
        return new ApprovalInstance(approvalId, tenantId, businessType, businessId, applicant, newStatus, history);
    }
}
