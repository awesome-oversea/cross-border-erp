package com.aidotnet.erp.iam.infrastructure.mapper;

import com.aidotnet.erp.iam.infrastructure.data.ObjectPermissionDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 对象权限数据访问接口
 * <p>
 * 描述: 对象权限表的数据访问层，提供对象权限的增删查操作。
 * </p>
 *
 * @author ERP系统
 */
@Mapper
public interface ObjectPermissionMapper {

    /** 新增对象权限 */
    void insert(ObjectPermissionDO permission);

    /** 按对象权限ID查询 */
    ObjectPermissionDO selectById(@Param("objPermId") String objPermId);

    /** 按租户ID+用户ID查询对象权限列表 */
    List<ObjectPermissionDO> selectByUserId(@Param("tenantId") String tenantId, @Param("userId") String userId);

    /** 按租户ID查询所有对象权限 */
    List<ObjectPermissionDO> selectByTenant(@Param("tenantId") String tenantId);

    /** 删除对象权限 */
    void deleteById(@Param("objPermId") String objPermId);
}
