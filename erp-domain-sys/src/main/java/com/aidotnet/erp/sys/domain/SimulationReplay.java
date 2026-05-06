package com.aidotnet.erp.sys.domain;

import java.time.Instant;
import java.util.Map;

public record SimulationReplay(
        String replayId, String tenantId, String ruleId, int ruleVersion,
        String ruleType, Map<String, Object> inputContext, Map<String, Object> outputResult,
        boolean passed, String errorMessage, Instant replayedAt) {}
