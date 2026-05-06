package com.aidotnet.erp.iam.infrastructure.mapper;

import com.aidotnet.erp.iam.infrastructure.data.UserDataScopeDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户数据权限范围数据访问接口
 * <p>
 * 描述: 数据权限范围表的数据访问层，提供数据权限的增删查操作。
 * </p>
 *
 * @author ERP系统
 */
@Mapper
public interface UserDataScopeMapper {

    /** 新增数据权限范围 */
    void insert(UserDataScopeDO dataScope);

    /** 按用户ID查询数据权限范围列表 */
    List<UserDataScopeDO> selectByUserId(@Param("userId") String userId);

    /** 按用户ID+资源类型删除数据权限范围(用于更新) */
    void deleteByUserIdAndResourceType(@Param("userId") String userId, @Param("resourceType") String resourceType);
}
