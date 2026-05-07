package com.aidotnet.erp.iam.infrastructure.mapper;

import com.aidotnet.erp.iam.infrastructure.data.UserRoleDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户-角色关联数据访问接口
 * <p>
 * 描述: 用户角色关联表的数据访问层，提供用户角色的增删查操作。
 *       支持按用户ID查询关联角色，按角色ID查询关联用户。
 * </p>
 *
 * @author ERP系统
 */
@Mapper
public interface UserRoleMapper {

    /** 新增用户-角色关联 */
    void insert(UserRoleDO userRole);

    /** 删除用户-角色关联 */
    void delete(@Param("userId") String userId, @Param("roleId") String roleId);

    /** 按用户ID查询关联的角色列表 */
    List<UserRoleDO> selectByUserId(@Param("userId") String userId);

    /** 按角色ID查询关联的用户角色列表 */
    List<UserRoleDO> selectByRoleId(@Param("roleId") String roleId);
}
