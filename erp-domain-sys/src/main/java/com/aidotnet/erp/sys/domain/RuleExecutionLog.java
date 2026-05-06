package com.aidotnet.erp.sys.domain;

import java.time.Instant;
import java.util.Map;

public record RuleExecutionLog(
        String logId, String tenantId, String ruleId, int ruleVersion,
        String ruleType, String businessType, String referenceId,
        Map<String, Object> inputContext, Map<String, Object> outputResult,
        boolean success, String errorMessage, long executionTimeMs, Instant executedAt) {}
