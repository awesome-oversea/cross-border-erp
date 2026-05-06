package com.aidotnet.erp.iam.infrastructure.mapper;

import com.aidotnet.erp.iam.infrastructure.data.DepartmentDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 部门数据访问接口
 * <p>
 * 描述: 部门表的数据访问层，提供部门CRUD操作，支持按组织过滤。
 * </p>
 *
 * @author ERP系统
 */
@Mapper
public interface DepartmentMapper {

    /** 新增部门 */
    void insert(DepartmentDO department);

    /** 按租户ID+部门ID查询部门 */
    DepartmentDO selectById(@Param("tenantId") String tenantId, @Param("deptId") String deptId);

    /** 按租户ID查询部门列表 */
    List<DepartmentDO> selectByTenant(@Param("tenantId") String tenantId);

    /** 按租户ID+组织ID查询部门列表 */
    List<DepartmentDO> selectByOrg(@Param("tenantId") String tenantId, @Param("orgId") String orgId);

    /** 更新部门 */
    void update(DepartmentDO department);
}
