package com.aidotnet.erp.sys.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.sys.domain.SystemConfig;
import com.aidotnet.erp.sys.infrastructure.SystemConfigStore;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class SystemConfigService {

    private final SystemConfigStore store;

    public SystemConfigService(SystemConfigStore store) {
        this.store = store;
    }

    public SystemConfig create(String tenantId, SaveConfigCommand command) {
        store.findByKey(tenantId, command.configKey()).ifPresent(existing -> {
            throw new BizException("CONFIG_DUPLICATED", "配置键已存在");
        });
        Instant now = Instant.now();
        return store.save(new SystemConfig(UUID.randomUUID().toString(), tenantId, command.configKey(), command.configValue(),
                command.description(), true, now, now));
    }

    public SystemConfig update(String tenantId, String configId, SaveConfigCommand command) {
        SystemConfig config = get(tenantId, configId);
        store.findByKey(tenantId, command.configKey())
                .filter(existing -> !existing.configId().equals(configId))
                .ifPresent(existing -> {
                    throw new BizException("CONFIG_DUPLICATED", "配置键已存在");
                });
        return store.save(new SystemConfig(config.configId(), config.tenantId(), command.configKey(), command.configValue(),
                command.description(), config.enabled(), config.createdAt(), Instant.now()));
    }

    public SystemConfig enable(String tenantId, String configId) {
        SystemConfig config = get(tenantId, configId);
        return store.save(new SystemConfig(config.configId(), config.tenantId(), config.configKey(), config.configValue(),
                config.description(), true, config.createdAt(), Instant.now()));
    }

    public SystemConfig disable(String tenantId, String configId) {
        SystemConfig config = get(tenantId, configId);
        return store.save(new SystemConfig(config.configId(), config.tenantId(), config.configKey(), config.configValue(),
                config.description(), false, config.createdAt(), Instant.now()));
    }

    public SystemConfig getByKey(String tenantId, String configKey) {
        return store.findByKey(tenantId, configKey).filter(SystemConfig::enabled)
                .orElseThrow(() -> new BizException("CONFIG_NOT_FOUND", "配置不存在或已停用"));
    }

    public List<SystemConfig> list(String tenantId) {
        return store.list(tenantId);
    }

    private SystemConfig get(String tenantId, String configId) {
        return store.find(tenantId, configId).orElseThrow(() -> new BizException("CONFIG_NOT_FOUND", "配置不存在"));
    }

    public record SaveConfigCommand(String configKey, String configValue, String description) {}
}
