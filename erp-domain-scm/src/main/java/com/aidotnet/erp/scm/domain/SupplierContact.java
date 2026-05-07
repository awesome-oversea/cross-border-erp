package com.aidotnet.erp.scm.domain;

import java.time.Instant;

public record SupplierContact(
        String contactId,
        String tenantId,
        String supplierId,
        String name,
        String role,
        String email,
        String phone,
        boolean primaryContact,
        Instant createdAt,
        Instant updatedAt
) {}
