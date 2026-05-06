package com.aidotnet.erp.iam.infrastructure.mapper;

import com.aidotnet.erp.iam.infrastructure.data.UserAccountDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * IAM用户数据访问接口
 * <p>
 * 描述: 用户表的数据访问层，提供用户CRUD操作。
 * </p>
 *
 * @author ERP系统
 */
@Mapper
public interface IamMapper {

    /** 新增用户 */
    void insertUser(UserAccountDO user);

    /** 按租户ID+用户名查询用户 */
    UserAccountDO selectUserByTenantAndUsername(@Param("tenantId") String tenantId, @Param("username") String username);

    /** 按租户ID+用户ID查询用户 */
    UserAccountDO selectUserById(@Param("tenantId") String tenantId, @Param("userId") String userId);

    /** 按租户ID查询用户列表 */
    List<UserAccountDO> selectUsersByTenant(@Param("tenantId") String tenantId);
}
