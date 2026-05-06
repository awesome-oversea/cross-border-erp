package com.aidotnet.erp.common.approval;

import java.time.Instant;

public record ApprovalHistory(String actor, String action, String comment, ApprovalStatus status, Instant occurredAt) {
}
