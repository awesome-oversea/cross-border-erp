package com.aidotnet.erp.iam.infrastructure.mapper;

import com.aidotnet.erp.iam.infrastructure.data.RoleDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 角色数据访问接口
 * <p>
 * 描述: 角色表的数据访问层，提供角色CRUD操作。
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

    /** 查询所有角色 */
    List<RoleDO> selectAll();

    /** 更新角色 */
    void update(RoleDO role);
}
