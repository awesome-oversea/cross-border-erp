package com.aidotnet.erp.common.pms;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.exception.ErrorCode;
import com.aidotnet.erp.common.security.DataScope;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * PMS请求守卫。
 * <p>
 * 描述:
 * 1. 统一校验 PMS 调用 ERP 时必须携带的审计/安全请求头，避免各领域重复定义校验规则。
 * 2. 统一解析 scope 维度和 data_level，供各域只读查询接口执行数据范围过滤和脱敏控制。
 * 3. 保持为 erp-common 共享能力，避免业务域之间产生直接依赖。
 * </p>
 */
@Component
public class PmsRequestGuard {

    private static final Set<String> REQUIRED_HEADERS = Set.of(
            "tenant_id", "actor_id", "actor_type", "scope", "purpose",
            "trace_id", "idempotency_key", "source_system", "signature");

    public PmsDataQueryContext validateReadOnlyRequest(Map<String, String> headers, String requestedDataLevel) {
        for (String requiredHeader : REQUIRED_HEADERS) {
            String value = findHeader(headers, requiredHeader);
            if (value == null || value.isBlank()) {
                throw new BizException(ErrorCode.PMS_HEADER_MISSING, "缺少必需请求头: " + requiredHeader);
            }
        }

        String sourceSystem = findHeader(headers, "source_system");
        if (!"PMS".equalsIgnoreCase(sourceSystem)) {
            throw new BizException(ErrorCode.PMS_SOURCE_INVALID, "source_system必须为PMS");
        }

        String actorType = findHeader(headers, "actor_type");
        String agentId = normalize(findHeader(headers, "agent_id"));
        if ("agent".equalsIgnoreCase(actorType) && agentId == null) {
            throw new BizException(ErrorCode.PMS_AGENT_ID_MISSING, "actor_type为agent时agent_id不能为空");
        }

        String scopeText = findHeader(headers, "scope");
        Map<String, Set<String>> scopeValues = parseScope(scopeText);
        DataScope.DataLevel effectiveDataLevel = resolveEffectiveDataLevel(requestedDataLevel, scopeValues.get("data_level"));
        return new PmsDataQueryContext(
                normalize(findHeader(headers, "tenant_id")),
                normalize(findHeader(headers, "actor_id")),
                normalize(actorType),
                agentId,
                normalize(findHeader(headers, "purpose")),
                normalize(findHeader(headers, "trace_id")),
                normalize(findHeader(headers, "idempotency_key")),
                normalize(scopeText),
                scopeValues,
                effectiveDataLevel);
    }

    private Map<String, Set<String>> parseScope(String scopeText) {
        Map<String, Set<String>> scopeValues = new LinkedHashMap<>();
        for (String token : scopeText.split(",")) {
            String trimmed = token == null ? null : token.trim();
            if (trimmed == null || trimmed.isBlank()) {
                continue;
            }
            int separator = trimmed.indexOf(':');
            if (separator <= 0 || separator >= trimmed.length() - 1) {
                throw new BizException(ErrorCode.PMS_REQUEST_INVALID, "scope格式无效: " + trimmed);
            }
            String dimension = trimmed.substring(0, separator).trim().toLowerCase();
            String value = trimmed.substring(separator + 1).trim();
            if (dimension.isBlank() || value.isBlank()) {
                throw new BizException(ErrorCode.PMS_REQUEST_INVALID, "scope格式无效: " + trimmed);
            }
            scopeValues.computeIfAbsent(dimension, key -> new LinkedHashSet<>()).add(value);
        }
        return scopeValues;
    }

    private DataScope.DataLevel resolveEffectiveDataLevel(String requestedDataLevel, Set<String> scopedDataLevels) {
        DataScope.DataLevel scopeLevel = scopedDataLevels == null || scopedDataLevels.isEmpty()
                ? DataScope.DataLevel.MASKED
                : parseDataLevel(scopedDataLevels.iterator().next(), "scope.data_level");
        DataScope.DataLevel requestedLevel = requestedDataLevel == null || requestedDataLevel.isBlank()
                ? scopeLevel
                : parseDataLevel(requestedDataLevel, "data_level");
        return restrictToLowerPrivilege(requestedLevel, scopeLevel);
    }

    private DataScope.DataLevel restrictToLowerPrivilege(DataScope.DataLevel requestedLevel, DataScope.DataLevel scopeLevel) {
        return levelRank(requestedLevel) <= levelRank(scopeLevel) ? requestedLevel : scopeLevel;
    }

    private int levelRank(DataScope.DataLevel dataLevel) {
        return switch (dataLevel) {
            case MASKED -> 0;
            case SUMMARY -> 1;
            case DETAIL -> 2;
        };
    }

    private DataScope.DataLevel parseDataLevel(String value, String fieldName) {
        try {
            return DataScope.DataLevel.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BizException(ErrorCode.PMS_REQUEST_INVALID,
                    fieldName + "必须是DETAIL、SUMMARY或MASKED");
        }
    }

    private String findHeader(Map<String, String> headers, String name) {
        if (headers == null || headers.isEmpty()) {
            return null;
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public record PmsDataQueryContext(String tenantId,
                                      String actorId,
                                      String actorType,
                                      String agentId,
                                      String purpose,
                                      String traceId,
                                      String idempotencyKey,
                                      String rawScope,
                                      Map<String, Set<String>> scopeValues,
                                      DataScope.DataLevel dataLevel) {

        public Set<String> scopeValues(String dimension) {
            if (scopeValues == null || dimension == null || dimension.isBlank()) {
                return Set.of();
            }
            return scopeValues.getOrDefault(dimension.trim().toLowerCase(), Set.of());
        }
    }
}
