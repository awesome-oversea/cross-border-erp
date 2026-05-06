package com.aidotnet.erp.sys.infrastructure;

import com.aidotnet.erp.sys.domain.PmsRecommendation;
import com.aidotnet.erp.sys.domain.PmsRecommendationStatus;
import com.aidotnet.erp.sys.domain.PmsWriteObjectType;
import com.aidotnet.erp.sys.infrastructure.data.PmsRecommendationDO;
import com.aidotnet.erp.sys.infrastructure.mapper.PmsRecommendationMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/**
 * PMS推荐存储对象(PmsRecommendationStore)
 * <p>
 * 描述: PMS推荐的数据持久化层，负责PmsRecommendation领域对象的存取操作。
 *       基于MyBatis实现数据库持久化，替代原ConcurrentHashMap内存存储。
 *       支持按ERP引用ID查询、按幂等键去重、按域过滤、按状态查询。
 * </p>
 * <p>
 * 核心方法:
 *   - save: 保存推荐记录(新增或更新)，基于erpReferenceId判断
 *   - find: 按ERP引用ID查询
 *   - findByIdempotencyKey: 按幂等键查询(防重复提交)
 *   - list: 按域查询推荐列表
 *   - listByStatus: 按状态查询推荐列表
 *   - delete: 按ERP引用ID删除
 * </p>
 * <p>
 * 数据转换:
 *   - toData: 领域对象 → 数据对象(DO)，List字段序列化为JSON字符串
 *   - toDomain: 数据对象(DO) → 领域对象，JSON字符串反序列化为List
 * </p>
 *
 * @author ERP系统
 * @see PmsRecommendation
 * @see PmsRecommendationDO
 * @see PmsRecommendationMapper
 */
@Repository
public class PmsRecommendationStore {

    private final PmsRecommendationMapper mapper;
    private final ObjectMapper objectMapper;

    public PmsRecommendationStore(PmsRecommendationMapper mapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
    }

    public PmsRecommendation save(PmsRecommendation recommendation) {
        PmsRecommendationDO existing = mapper.selectByErpReferenceId(recommendation.tenantId(), recommendation.erpReferenceId());
        PmsRecommendationDO data = toData(recommendation);
        if (existing == null) {
            mapper.insert(data);
        } else {
            mapper.update(data);
        }
        return recommendation;
    }

    public Optional<PmsRecommendation> find(String tenantId, String erpReferenceId) {
        return Optional.ofNullable(mapper.selectByErpReferenceId(tenantId, erpReferenceId)).map(this::toDomain);
    }

    public Optional<PmsRecommendation> findByIdempotencyKey(String tenantId, String domain, String idempotencyKey) {
        return Optional.ofNullable(mapper.selectByIdempotencyKey(tenantId, domain, idempotencyKey)).map(this::toDomain);
    }

    public List<PmsRecommendation> list(String tenantId, String domain) {
        if (domain == null || domain.isBlank()) {
            return mapper.selectByTenant(tenantId).stream().map(this::toDomain)
                    .sorted(Comparator.comparing(PmsRecommendation::createdAt).reversed())
                    .collect(Collectors.toList());
        }
        return mapper.selectByDomain(tenantId, domain).stream().map(this::toDomain).collect(Collectors.toList());
    }

    public List<PmsRecommendation> listByStatus(String tenantId, String status) {
        return mapper.selectByStatus(tenantId, status).stream().map(this::toDomain).collect(Collectors.toList());
    }

    public void delete(String tenantId, String erpReferenceId) {
        mapper.deleteByErpReferenceId(tenantId, erpReferenceId);
    }

    private PmsRecommendationDO toData(PmsRecommendation r) {
        PmsRecommendationDO data = new PmsRecommendationDO();
        data.setErpReferenceId(r.erpReferenceId());
        data.setTenantId(r.tenantId());
        data.setRecommendationId(r.recommendationId());
        data.setDomain(r.domain());
        data.setRecommendationType(r.recommendationType());
        data.setObjectType(r.objectType() != null ? r.objectType().name() : null);
        data.setTargetObjectType(r.targetObjectType());
        data.setTargetObjectId(r.targetObjectId());
        data.setContent(r.content());
        data.setScore(r.score());
        data.setConfidence(r.confidence());
        data.setEvidenceChainId(r.evidenceChainId());
        data.setDataSources(toJson(r.dataSources()));
        data.setRiskFlags(toJson(r.riskFlags()));
        data.setExplainability(r.explainability());
        data.setRequestedAction(r.requestedAction());
        data.setApprovalPolicy(r.approvalPolicy());
        data.setStatus(r.status() != null ? r.status().name() : null);
        data.setRejectionReason(r.rejectionReason());
        data.setExecutionResult(r.executionResult());
        data.setMeasuredResult(r.measuredResult());
        data.setTraceId(r.traceId());
        data.setIdempotencyKey(r.idempotencyKey());
        data.setActorId(r.actorId());
        data.setActorType(r.actorType());
        data.setAgentId(r.agentId());
        data.setScope(r.scope());
        data.setPurpose(r.purpose());
        data.setSourceSystem(r.sourceSystem());
        data.setAuditId(r.auditId());
        data.setCreatedAt(r.createdAt() != null ? r.createdAt() : Instant.now());
        data.setUpdatedAt(r.updatedAt() != null ? r.updatedAt() : Instant.now());
        return data;
    }

    private PmsRecommendation toDomain(PmsRecommendationDO d) {
        return new PmsRecommendation(
                d.getErpReferenceId(), d.getRecommendationId(), d.getTenantId(), d.getDomain(),
                d.getRecommendationType(),
                d.getObjectType() != null ? PmsWriteObjectType.valueOf(d.getObjectType()) : null,
                d.getTargetObjectType(), d.getTargetObjectId(), d.getContent(),
                d.getScore(), d.getConfidence(), d.getEvidenceChainId(),
                fromJson(d.getDataSources(), new TypeReference<List<String>>() {}),
                fromJson(d.getRiskFlags(), new TypeReference<List<String>>() {}),
                d.getExplainability(), d.getRequestedAction(),
                d.getApprovalPolicy(),
                d.getStatus() != null ? PmsRecommendationStatus.valueOf(d.getStatus()) : null,
                d.getRejectionReason(), d.getExecutionResult(), d.getMeasuredResult(),
                d.getTraceId(), d.getIdempotencyKey(), d.getActorId(), d.getActorType(),
                d.getAgentId(), d.getScope(), d.getPurpose(), d.getSourceSystem(),
                d.getAuditId(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private String toJson(Object value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> T fromJson(String json, TypeReference<T> typeRef) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
