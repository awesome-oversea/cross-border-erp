package com.aidotnet.erp.sys.domain;

public record ApprovalStep(
        int stepOrder,
        String stepName,
        String approverType,
        String approverId,
        boolean autoApprove,
        String condition
) {}
