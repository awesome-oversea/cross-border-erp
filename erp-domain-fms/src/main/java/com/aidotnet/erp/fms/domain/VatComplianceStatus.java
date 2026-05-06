package com.aidotnet.erp.fms.domain;

import java.time.Instant;
import java.util.Map;

public record VatComplianceStatus(
        String statusId,
        String tenantId,
        String countryCode,
        String vatNumber,
        String registrationStatus,
        String filingStatus,
        Instant nextFilingDate,
        Instant registrationDate,
        Instant expiryDate,
        Map<String, String> details,
        Instant updatedAt
) {
    public enum RegistrationStatus {
        REGISTERED,
        PENDING,
        NOT_REGISTERED,
        EXPIRED,
        CANCELLED
    }

    public enum FilingStatus {
        UP_TO_DATE,
        DUE_SOON,
        OVERDUE,
        FILED,
        EXEMPT
    }
}
