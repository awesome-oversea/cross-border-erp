package com.aidotnet.erp.bi.domain;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record CrossAnalysisResult(String analysisId, String tenantId, String analysisName,
                                  List<String> rowDimensions, List<String> columnDimensions,
                                  List<String> metrics, List<Map<String, Object>> rows,
                                  Map<String, Object> totals, Instant analyzedAt) {}
