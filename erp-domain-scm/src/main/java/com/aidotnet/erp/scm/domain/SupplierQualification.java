package com.aidotnet.erp.scm.domain;

import java.time.Instant;

public record SupplierQualification(
        String qualificationId,
        String tenantId,
        String supplierId,
        String qualificationType,
        String qualificationNo,
        String issuedBy,
        Instant validFrom,
        Instant validUntil,
        String status,
        String remark,
        Instant createdAt,
        Instant updatedAt
) {}
