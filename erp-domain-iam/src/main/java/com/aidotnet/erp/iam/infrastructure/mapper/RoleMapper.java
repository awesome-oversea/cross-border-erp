package com.aidotnet.erp.iam.infrastructure.mapper;

import com.aidotnet.erp.iam.infrastructure.data.RoleDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 角色数据访问接口
 * <p>
 * 描述: 角色表的数据访问层，提供角色CRUD操作。
 *       支持按角色编码、角色ID、租户ID等多种查询方式。
 * </p>
 *
 * @author ERP系统
 */
@Mapper
public interface RoleMapper {

    /** 新增角色 */
    void insert(RoleDO role);

    /** 按角色编码查询角色 */
    RoleDO selectByCode(@Param("roleCode") String roleCode);

    /** 按角色ID查询角色 */
    RoleDO selectById(@Param("roleId") String roleId);

    /** 按租户ID查询角色列表(含系统全局角色) */
    List<RoleDO> selectByTenant(@Param("tenantId") String tenantId);

    /** 查询所有角色 */
    List<RoleDO> selectAll();

    /** 更新角色 */
    void update(RoleDO role);

    /** 删除角色 */
    void deleteById(@Param("roleId") String roleId);
}
