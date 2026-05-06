package com.aidotnet.erp.iam.infrastructure.mapper;

import com.aidotnet.erp.iam.infrastructure.data.TenantDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 租户数据访问接口
 * <p>
 * 描述: 租户表的数据访问层，提供租户CRUD操作。
 * </p>
 *
 * @author ERP系统
 */
@Mapper
public interface TenantMapper {

    /** 新增租户 */
    void insert(TenantDO tenant);

    /** 按租户ID查询租户 */
    TenantDO selectById(@Param("tenantId") String tenantId);

    /** 查询所有租户 */
    List<TenantDO> selectAll();

    /** 更新租户 */
    void update(TenantDO tenant);
}
