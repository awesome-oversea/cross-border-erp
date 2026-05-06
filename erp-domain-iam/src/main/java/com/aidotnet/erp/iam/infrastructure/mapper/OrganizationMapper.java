package com.aidotnet.erp.iam.infrastructure.mapper;

import com.aidotnet.erp.iam.infrastructure.data.OrganizationDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 组织数据访问接口
 * <p>
 * 描述: 组织架构表的数据访问层，提供组织CRUD操作。
 * </p>
 *
 * @author ERP系统
 */
@Mapper
public interface OrganizationMapper {

    /** 新增组织 */
    void insert(OrganizationDO organization);

    /** 按租户ID+组织ID查询组织 */
    OrganizationDO selectById(@Param("tenantId") String tenantId, @Param("orgId") String orgId);

    /** 按租户ID查询组织列表 */
    List<OrganizationDO> selectByTenant(@Param("tenantId") String tenantId);

    /** 更新组织 */
    void update(OrganizationDO organization);
}
