package com.aidotnet.erp.sys.infrastructure;

import com.aidotnet.erp.sys.domain.SystemConfig;
import com.aidotnet.erp.sys.infrastructure.data.SystemConfigDO;
import com.aidotnet.erp.sys.infrastructure.mapper.SystemConfigMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/**
 * 系统配置存储对象(SystemConfigStore)
 * <p>
 * 描述: 系统配置的数据持久化层，负责SystemConfig领域对象的存取操作。
 *       基于MyBatis实现数据库持久化，替代原ConcurrentHashMap内存存储。
 *       支持按配置ID和配置键查询，按租户隔离数据。
 * </p>
 * <p>
 * 核心方法:
 *   - save: 保存配置(新增或更新)，基于configId判断
 *   - find: 按配置ID查询
 *   - findByKey: 按配置键查询(租户内唯一)
 *   - list: 查询租户下所有配置
 *   - delete: 按配置ID删除
 * </p>
 * <p>
 * 数据转换:
 *   - toData: 领域对象 → 数据对象(DO)
 *   - toDomain: 数据对象(DO) → 领域对象
 * </p>
 *
 * @author ERP系统
 * @see SystemConfig
 * @see SystemConfigDO
 * @see SystemConfigMapper
 */
@Repository
public class SystemConfigStore {

    private final SystemConfigMapper mapper;

    public SystemConfigStore(SystemConfigMapper mapper) {
        this.mapper = mapper;
    }

    public SystemConfig save(SystemConfig config) {
        SystemConfigDO existing = mapper.selectByConfigId(config.tenantId(), config.configId());
        SystemConfigDO data = toData(config);
        if (existing == null) {
            mapper.insert(data);
        } else {
            mapper.update(data);
        }
        return config;
    }

    public Optional<SystemConfig> find(String tenantId, String configId) {
        return Optional.ofNullable(mapper.selectByConfigId(tenantId, configId)).map(this::toDomain);
    }

    public Optional<SystemConfig> findByKey(String tenantId, String configKey) {
        return Optional.ofNullable(mapper.selectByConfigKey(tenantId, configKey)).map(this::toDomain);
    }

    public List<SystemConfig> list(String tenantId) {
        return mapper.selectByTenant(tenantId).stream().map(this::toDomain).collect(Collectors.toList());
    }

    public void delete(String tenantId, String configId) {
        mapper.deleteByConfigId(tenantId, configId);
    }

    private SystemConfigDO toData(SystemConfig c) {
        SystemConfigDO data = new SystemConfigDO();
        data.setConfigId(c.configId());
        data.setTenantId(c.tenantId());
        data.setConfigKey(c.configKey());
        data.setConfigValue(c.configValue());
        data.setDescription(c.description());
        data.setEnabled(c.enabled());
        data.setCreatedAt(c.createdAt() != null ? c.createdAt() : Instant.now());
        data.setUpdatedAt(c.updatedAt() != null ? c.updatedAt() : Instant.now());
        return data;
    }

    private SystemConfig toDomain(SystemConfigDO d) {
        return new SystemConfig(d.getConfigId(), d.getTenantId(), d.getConfigKey(), d.getConfigValue(),
                d.getDescription(), d.getEnabled(), d.getCreatedAt(), d.getUpdatedAt());
    }
}
