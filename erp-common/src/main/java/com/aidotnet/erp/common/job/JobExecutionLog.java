package com.aidotnet.erp.common.job;

import java.time.Instant;

public record JobExecutionLog(String jobCode, boolean success, String errorMessage, Instant executedAt) {
}
