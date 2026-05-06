package com.aidotnet.erp.iam.infrastructure.mapper;

import com.aidotnet.erp.iam.infrastructure.data.PermissionDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 权限数据访问接口
 * <p>
 * 描述: 权限定义表的数据访问层，提供权限查询和模块列表获取。
 * </p>
 *
 * @author ERP系统
 */
@Mapper
public interface PermissionMapper {

    /** 新增权限 */
    void insert(PermissionDO permission);

    /** 按权限编码查询权限 */
    PermissionDO selectByCode(@Param("permissionCode") String permissionCode);

    /** 查询所有权限 */
    List<PermissionDO> selectAll();

    /** 按模块查询权限列表 */
    List<PermissionDO> selectByModule(@Param("module") String module);

    /** 查询去重后的模块列表 */
    List<String> selectDistinctModules();
}
