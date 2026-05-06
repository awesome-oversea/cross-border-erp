package com.aidotnet.erp.common.integration;

import cn.hutool.crypto.digest.DigestUtil;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.persistence.entity.ApiClientEntity;
import com.aidotnet.erp.common.persistence.entity.ConnectorRegistrationEntity;
import com.aidotnet.erp.common.persistence.mapper.ApiClientMapper;
import com.aidotnet.erp.common.persistence.mapper.ConnectorRegistrationMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PersistentIntegrationRegistry {

    private final ConnectorRegistrationMapper connectorMapper;
    private final ApiClientMapper apiClientMapper;

    public PersistentIntegrationRegistry(ConnectorRegistrationMapper connectorMapper, ApiClientMapper apiClientMapper) {
        this.connectorMapper = connectorMapper;
        this.apiClientMapper = apiClientMapper;
    }

    @Transactional
    public ConnectorRegistration registerConnector(String tenantId, String connectorCode, String name) {
        LocalDateTime now = LocalDateTime.now();
        ConnectorRegistrationEntity entity = new ConnectorRegistrationEntity();
        entity.setTenantId(tenantId);
        entity.setConnectorCode(connectorCode);
        entity.setName(name);
        entity.setEnabled(true);
        entity.setRegisteredAt(now);
        connectorMapper.insert(entity);
        return new ConnectorRegistration(tenantId, connectorCode, name, true, now.toInstant(ZoneOffset.UTC));
    }

    @Transactional
    public ConnectorRegistration disableConnector(String tenantId, String connectorCode) {
        ConnectorRegistrationEntity entity = mustGetConnector(tenantId, connectorCode);
        entity.setEnabled(false);
        connectorMapper.updateById(entity);
        return toConnector(entity);
    }

    @Transactional
    public ApiClient createApiClient(String tenantId, String clientId) {
        String plainSecret = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime now = LocalDateTime.now();
        ApiClientEntity entity = new ApiClientEntity();
        entity.setTenantId(tenantId);
        entity.setClientId(clientId);
        entity.setSecretHash(DigestUtil.sha256Hex(plainSecret));
        entity.setEnabled(true);
        entity.setCreatedAt(now);
        apiClientMapper.insert(entity);
        return new ApiClient(tenantId, clientId, entity.getSecretHash(), true, plainSecret);
    }

    public void authenticate(String tenantId, String clientId, String plainSecret) {
        ApiClientEntity entity = apiClientMapper.selectOne(new LambdaQueryWrapper<ApiClientEntity>()
                .eq(ApiClientEntity::getTenantId, tenantId)
                .eq(ApiClientEntity::getClientId, clientId));
        if (entity == null || !Boolean.TRUE.equals(entity.getEnabled())
                || !entity.getSecretHash().equals(DigestUtil.sha256Hex(plainSecret))) {
            throw new BizException("UNAUTHORIZED", "api client authentication failed");
        }
    }

    private ConnectorRegistrationEntity mustGetConnector(String tenantId, String connectorCode) {
        ConnectorRegistrationEntity entity = connectorMapper.selectOne(new LambdaQueryWrapper<ConnectorRegistrationEntity>()
                .eq(ConnectorRegistrationEntity::getTenantId, tenantId)
                .eq(ConnectorRegistrationEntity::getConnectorCode, connectorCode));
        if (entity == null) {
            throw new BizException("NOT_FOUND", "connector registration not found");
        }
        return entity;
    }

    private ConnectorRegistration toConnector(ConnectorRegistrationEntity entity) {
        return new ConnectorRegistration(entity.getTenantId(), entity.getConnectorCode(), entity.getName(),
                Boolean.TRUE.equals(entity.getEnabled()), entity.getRegisteredAt().toInstant(ZoneOffset.UTC));
    }
}
