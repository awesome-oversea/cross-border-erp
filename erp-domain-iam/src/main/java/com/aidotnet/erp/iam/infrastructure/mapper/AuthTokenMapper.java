package com.aidotnet.erp.iam.infrastructure.mapper;

import com.aidotnet.erp.iam.infrastructure.data.AuthTokenDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 认证令牌数据访问接口
 * <p>
 * 描述: 认证令牌表的数据访问层，提供令牌的增删查操作。
 *       Token用于JWT认证，支持主动注销和过期检查。
 * </p>
 *
 * @author ERP系统
 */
@Mapper
public interface AuthTokenMapper {

    /** 新增认证令牌 */
    void insert(AuthTokenDO token);

    /** 按Token字符串查询令牌 */
    AuthTokenDO selectByToken(@Param("token") String token);

    /** 删除令牌(用于登出) */
    void deleteByToken(@Param("token") String token);

    /** 按用户ID删除所有令牌(用于强制下线) */
    void deleteByUserId(@Param("tenantId") String tenantId, @Param("userId") String userId);
}
