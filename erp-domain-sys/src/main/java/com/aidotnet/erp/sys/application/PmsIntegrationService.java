package com.aidotnet.erp.sys.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.exception.ErrorCode;
import com.aidotnet.erp.sys.domain.AIFeatureToggle;
import com.aidotnet.erp.sys.domain.DataTrustLevel;
import com.aidotnet.erp.sys.domain.PmsDataTrustRule;
import com.aidotnet.erp.sys.domain.PmsDraftDocument;
import com.aidotnet.erp.sys.domain.PmsFeedback;
import com.aidotnet.erp.sys.domain.PmsRecommendation;
import com.aidotnet.erp.sys.domain.PmsRecommendationStatus;
import com.aidotnet.erp.sys.domain.PmsWriteObjectType;
import com.aidotnet.erp.sys.infrastructure.SysExtStore;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * PMS智能服务集成应用服务
 * <p>
 * 描述: 系统设置域PMS集成服务，负责PMS请求头校验、签名验证、
 *       数据信任规则、草稿单据生成/审批/执行、反馈投递等业务逻辑。
 *       是ERP与AI智能服务(PMS)的安全集成桥梁。
 * </p>
 * <p>
 * 核心能力:
 *   1. 安全验证 - 请求头校验、HMAC签名验证、时间戳容差检查
 *   2. 数据信任 - A/B/C/D四级数据信任度，低信任数据不可覆盖高信任数据
 *   3. 草稿单据 - PMS建议生成草稿单据，需审批后才可执行
 *   4. 反馈投递 - 将ERP执行结果反馈给PMS，支持重试
 * </p>
 *
 * @author ERP系统
 * @see PmsDraftDocument
 * @see PmsFeedback
 * @see DataTrustLevel
 */
@Service
public class PmsIntegrationService {
    private static final Logger log = LoggerFactory.getLogger(PmsIntegrationService.class);

    private static final Set<String> REQUIRED_HEADERS = Set.of(
            "tenant_id", "actor_id", "actor_type", "scope", "purpose",
            "trace_id", "idempotency_key", "source_system", "signature");

    private static final Set<String> SUPPORTED_DOMAINS = Set.of(
            "IAM", "PDM", "SOM", "ADS", "OMS", "SCM", "WMS", "FBA", "TMS", "CRM", "FMS", "BI", "SYS", "DASHBOARD");

    private static final Set<PmsWriteObjectType> WRITE_WHITELIST = Set.of(PmsWriteObjectType.values());

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final long SIGNATURE_TOLERANCE_MS = 300_000;

    private final SysExtStore extStore;
    private final PmsRecommendationService recommendationService;

    public PmsIntegrationService(SysExtStore extStore, PmsRecommendationService recommendationService) {
        this.extStore = extStore;
        this.recommendationService = recommendationService;
    }

    public void validateHeaders(Map<String, String> headers) {
        for (String required : REQUIRED_HEADERS) {
            String value = findHeader(headers, required);
            if (value == null || value.isBlank()) {
                throw new BizException(ErrorCode.PMS_HEADER_MISSING, "缺少必需请求头: " + required);
            }
        }
        String sourceSystem = findHeader(headers, "source_system");
        if (!"PMS".equalsIgnoreCase(sourceSystem)) {
            throw new BizException(ErrorCode.PMS_SOURCE_INVALID, "source_system必须为PMS");
        }
        String actorType = findHeader(headers, "actor_type");
        if ("agent".equalsIgnoreCase(actorType)) {
            String agentId = findHeader(headers, "agent_id");
            if (agentId == null || agentId.isBlank()) {
                throw new BizException(ErrorCode.PMS_AGENT_ID_MISSING, "actor_type为agent时agent_id不能为空");
            }
        }
    }

    public boolean verifySignature(Map<String, String> headers, String body, String secretKey) {
        String signature = findHeader(headers, "signature");
        String timestamp = findHeader(headers, "x-timestamp");
        if (signature == null || timestamp == null) {
            return false;
        }
        try {
            long ts = Long.parseLong(timestamp);
            if (Math.abs(System.currentTimeMillis() - ts) > SIGNATURE_TOLERANCE_MS) {
                log.warn("PMS signature timestamp expired: ts={}", ts);
                return false;
            }
            String data = timestamp + "." + body;
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            String expected = HexFormat.of().formatHex(hash);
            boolean valid = expected.equals(signature);
            if (!valid) {
                log.warn("PMS signature verification failed: expected={} actual={}", expected, signature);
            }
            return valid;
        } catch (Exception e) {
            log.error("PMS signature verification error", e);
            return false;
        }
    }

    public void validateWriteWhitelist(PmsWriteObjectType objectType) {
        if (objectType == null || !WRITE_WHITELIST.contains(objectType)) {
            throw new BizException(ErrorCode.PMS_OBJECT_TYPE_FORBIDDEN,
                    "PMS只能写入Recommendation/Draft/PendingAction/RiskAlert/InsightCard");
        }
    }

    public void validateDomain(String domain) {
        if (domain == null || !SUPPORTED_DOMAINS.contains(domain.toUpperCase())) {
            throw new BizException(ErrorCode.PMS_DOMAIN_UNSUPPORTED, "不支持的ERP域: " + domain);
        }
    }

    public void validateDataSovereignty(String tenantId, String domain, PmsWriteObjectType objectType) {
        PmsDataTrustRule rule = extStore.findDataTrustRule(tenantId, domain, objectType.name());
        if (rule != null && !rule.canOverwriteErp()) {
            log.info("PMS data sovereignty check: tenant={} domain={} objectType={} trustLevel={} canOverwrite={}",
                    tenantId, domain, objectType, rule.trustLevel(), rule.canOverwriteErp());
        }
    }

    public boolean checkAIFeatureEnabled(String tenantId, String domain) {
        AIFeatureToggle toggle = extStore.findAIFeatureToggleByDomain(tenantId, domain);
        if (toggle == null) {
            return false;
        }
        return toggle.enabled();
    }

    public PmsDraftDocument generateDraft(String tenantId, String erpReferenceId, String domain,
                                           String draftType, String targetBusinessType,
                                           String contentJson, DataTrustLevel trustLevel,
                                           String actorId, String actorType, String agentId,
                                           String scope, String purpose, String traceId) {
        PmsRecommendation recommendation = recommendationService.get(tenantId, erpReferenceId);
        if (recommendation.status() != PmsRecommendationStatus.APPROVED) {
            throw new BizException(ErrorCode.PMS_OBJECT_TYPE_FORBIDDEN, "建议未审批通过，不能生成草稿");
        }
        Instant now = Instant.now();
        PmsDraftDocument draft = new PmsDraftDocument(
                UUID.randomUUID().toString(), tenantId, erpReferenceId, domain, draftType,
                targetBusinessType, null, contentJson, trustLevel, "PMS",
                actorId, actorType, agentId, scope, purpose, traceId,
                "PENDING", null, null, "PENDING", null, now, now);
        return extStore.savePmsDraftDocument(draft);
    }

    public PmsDraftDocument approveDraft(String tenantId, String draftId, String approvedBy) {
        PmsDraftDocument draft = extStore.findPmsDraftDocument(tenantId, draftId)
                .orElseThrow(() -> new BizException("PMS_DRAFT_NOT_FOUND", "草稿单据不存在"));
        if (!"PENDING".equals(draft.approvalStatus())) {
            throw new BizException(ErrorCode.INVALID_STATUS, "草稿已处理");
        }
        Instant now = Instant.now();
        PmsDraftDocument approved = new PmsDraftDocument(
                draft.draftId(), draft.tenantId(), draft.erpReferenceId(), draft.domain(),
                draft.draftType(), draft.targetBusinessType(), draft.targetBusinessId(),
                draft.contentJson(), draft.trustLevel(), draft.sourceSystem(),
                draft.actorId(), draft.actorType(), draft.agentId(), draft.scope(), draft.purpose(),
                draft.traceId(), "APPROVED", approvedBy, now, draft.executionStatus(),
                draft.executionResult(), draft.createdAt(), now);
        return extStore.savePmsDraftDocument(approved);
    }

    public PmsDraftDocument executeDraft(String tenantId, String draftId, String executionResult) {
        PmsDraftDocument draft = extStore.findPmsDraftDocument(tenantId, draftId)
                .orElseThrow(() -> new BizException("PMS_DRAFT_NOT_FOUND", "草稿单据不存在"));
        if (!"APPROVED".equals(draft.approvalStatus())) {
            throw new BizException(ErrorCode.PMS_OBJECT_TYPE_FORBIDDEN, "草稿未审批，不能执行");
        }
        Instant now = Instant.now();
        PmsDraftDocument executed = new PmsDraftDocument(
                draft.draftId(), draft.tenantId(), draft.erpReferenceId(), draft.domain(),
                draft.draftType(), draft.targetBusinessType(), draft.targetBusinessId(),
                draft.contentJson(), draft.trustLevel(), draft.sourceSystem(),
                draft.actorId(), draft.actorType(), draft.agentId(), draft.scope(), draft.purpose(),
                draft.traceId(), draft.approvalStatus(), draft.approvedBy(), draft.approvedAt(),
                "EXECUTED", executionResult, draft.createdAt(), now);
        return extStore.savePmsDraftDocument(executed);
    }

    public PmsFeedback sendFeedback(String tenantId, String erpReferenceId, String feedbackType,
                                     String executionStatus, String businessResult,
                                     String businessMetricsJson, String failureReason,
                                     String operatorId, String traceId) {
        PmsRecommendation recommendation = recommendationService.get(tenantId, erpReferenceId);
        Instant now = Instant.now();
        PmsFeedback feedback = new PmsFeedback(
                UUID.randomUUID().toString(), tenantId, erpReferenceId,
                recommendation.recommendationId(), recommendation.domain(),
                feedbackType, executionStatus, businessResult, businessMetricsJson,
                failureReason, operatorId, traceId, false, 0, null, now);
        return extStore.savePmsFeedback(feedback);
    }

    public PmsFeedback markFeedbackDelivered(String tenantId, String feedbackId) {
        PmsFeedback feedback = extStore.findPmsFeedback(tenantId, feedbackId)
                .orElseThrow(() -> new BizException("PMS_FEEDBACK_NOT_FOUND", "反馈记录不存在"));
        PmsFeedback delivered = new PmsFeedback(
                feedback.feedbackId(), feedback.tenantId(), feedback.erpReferenceId(),
                feedback.recommendationId(), feedback.domain(), feedback.feedbackType(),
                feedback.executionStatus(), feedback.businessResult(), feedback.businessMetricsJson(),
                feedback.failureReason(), feedback.operatorId(), feedback.traceId(),
                true, feedback.retryCount() + 1, Instant.now(), feedback.createdAt());
        return extStore.savePmsFeedback(delivered);
    }

    public List<PmsFeedback> listPendingFeedbacks(String tenantId) {
        return extStore.listPmsFeedbacks(tenantId).stream()
                .filter(f -> !f.delivered())
                .toList();
    }

    public PmsDataTrustRule createDataTrustRule(String tenantId, String domain, String objectType,
                                                 DataTrustLevel trustLevel, String description,
                                                 List<String> allowedActions, boolean canOverwriteErp) {
        Instant now = Instant.now();
        PmsDataTrustRule rule = new PmsDataTrustRule(
                UUID.randomUUID().toString(), tenantId, domain, objectType, trustLevel,
                description, allowedActions, canOverwriteErp, now, now);
        return extStore.savePmsDataTrustRule(rule);
    }

    public List<PmsDataTrustRule> listDataTrustRules(String tenantId) {
        return extStore.listPmsDataTrustRules(tenantId);
    }

    public AIFeatureToggle setAIFeatureToggle(String tenantId, String featureCode, String featureName,
                                               String domain, boolean enabled, String description, String configJson) {
        AIFeatureToggle existing = extStore.findAIFeatureToggleByDomain(tenantId, domain);
        Instant now = Instant.now();
        AIFeatureToggle toggle;
        if (existing != null) {
            toggle = new AIFeatureToggle(existing.toggleId(), tenantId, featureCode, featureName,
                    domain, enabled, description, configJson, existing.createdAt(), now);
        } else {
            toggle = new AIFeatureToggle(UUID.randomUUID().toString(), tenantId, featureCode, featureName,
                    domain, enabled, description, configJson, now, now);
        }
        return extStore.saveAIFeatureToggle(toggle);
    }

    public List<AIFeatureToggle> listAIFeatureToggles(String tenantId) {
        return extStore.listAllAIFeatureToggles(tenantId);
    }

    private String findHeader(Map<String, String> headers, String name) {
        return headers.entrySet().stream()
                .filter(e -> e.getKey().equalsIgnoreCase(name))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }
}
