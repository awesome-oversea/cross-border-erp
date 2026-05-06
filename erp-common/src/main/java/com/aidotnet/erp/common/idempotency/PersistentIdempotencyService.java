package com.aidotnet.erp.common.idempotency;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.exception.ErrorCode;
import com.aidotnet.erp.common.persistence.entity.IdempotencyRecordEntity;
import com.aidotnet.erp.common.persistence.mapper.IdempotencyRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PersistentIdempotencyService {

    private final IdempotencyRecordMapper mapper;
    private final ObjectMapper objectMapper;
    private final Duration ttl;

    @Autowired
    public PersistentIdempotencyService(IdempotencyRecordMapper mapper, ObjectMapper objectMapper) {
        this(mapper, objectMapper, Duration.ofMinutes(10));
    }

    PersistentIdempotencyService(IdempotencyRecordMapper mapper, ObjectMapper objectMapper, Duration ttl) {
        this.mapper = mapper;
        this.objectMapper = objectMapper;
        this.ttl = ttl;
    }

    @Transactional
    public <T> T execute(String tenantId, String idempotencyKey, String fingerprint, Class<T> resultType,
                         Supplier<T> action) {
        LocalDateTime now = LocalDateTime.now();
        IdempotencyRecordEntity existing = mapper.selectOne(new LambdaQueryWrapper<IdempotencyRecordEntity>()
                .eq(IdempotencyRecordEntity::getTenantId, tenantId)
                .eq(IdempotencyRecordEntity::getIdempotencyKey, idempotencyKey));
        if (existing != null && existing.getExpiresAt().isAfter(now)) {
            if (!Objects.equals(existing.getFingerprint(), fingerprint)) {
                throw new BizException(ErrorCode.IDEMPOTENCY_CONFLICT, "idempotency key was used by a different request");
            }
            return read(existing.getResultJson(), resultType);
        }
        T result = action.get();
        IdempotencyRecordEntity entity = existing == null ? new IdempotencyRecordEntity() : existing;
        entity.setTenantId(tenantId);
        entity.setIdempotencyKey(idempotencyKey);
        entity.setFingerprint(fingerprint);
        entity.setResultJson(write(result));
        entity.setExpiresAt(now.plus(ttl));
        entity.setCreatedAt(existing == null ? now : existing.getCreatedAt());
        if (existing == null) {
            mapper.insert(entity);
        } else {
            mapper.updateById(entity);
        }
        return result;
    }

    private <T> T read(String resultJson, Class<T> resultType) {
        try {
            return objectMapper.readValue(resultJson, resultType);
        } catch (JsonProcessingException ex) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "failed to read idempotency result");
        }
    }

    private String write(Object result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException ex) {
            throw new BizException(ErrorCode.INTERNAL_ERROR, "failed to write idempotency result");
        }
    }
}
