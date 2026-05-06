package com.aidotnet.erp.sys.infrastructure.mapper;

import com.aidotnet.erp.sys.infrastructure.data.SystemConfigDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 系统配置Mapper接口(SystemConfigMapper)
 * <p>
 * 描述: 系统配置表的MyBatis数据访问接口，提供系统配置的CRUD操作。
 *       支持按租户隔离查询、按配置键精确查找、按启用状态过滤。
 * </p>
 * <p>
 * 方法说明:
 *   - insert: 新增系统配置，config_key租户内唯一
 *   - update: 更新配置值和描述，基于configId+tenantId
 *   - selectByConfigId: 按配置ID精确查询
 *   - selectByConfigKey: 按配置键精确查询(租户内唯一)
 *   - selectByTenant: 查询租户下所有配置
 *   - deleteByConfigId: 按配置ID删除
 * </p>
 *
 * @author ERP系统
 * @see SystemConfigDO
 */
@Mapper
public interface SystemConfigMapper {

    void insert(SystemConfigDO config);

    void update(SystemConfigDO config);

    SystemConfigDO selectByConfigId(@Param("tenantId") String tenantId, @Param("configId") String configId);

    SystemConfigDO selectByConfigKey(@Param("tenantId") String tenantId, @Param("configKey") String configKey);

    List<SystemConfigDO> selectByTenant(@Param("tenantId") String tenantId);

    void deleteByConfigId(@Param("tenantId") String tenantId, @Param("configId") String configId);
}
