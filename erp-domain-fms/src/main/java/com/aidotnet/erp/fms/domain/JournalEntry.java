package com.aidotnet.erp.fms.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record JournalEntry(String entryId, String tenantId, String accountCode, String accountName,
                           JournalEntryType type, BigDecimal amount, String currency, String referenceType,
                           String referenceId, String remark, Instant entryDate, Instant createdAt) {}
