package com.aidotnet.erp.iam.infrastructure.mapper;

import com.aidotnet.erp.iam.infrastructure.data.PositionDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 岗位数据访问接口
 * <p>
 * 描述: 岗位表的数据访问层，提供岗位CRUD操作。
 * </p>
 *
 * @author ERP系统
 */
@Mapper
public interface PositionMapper {

    /** 新增岗位 */
    void insert(PositionDO position);

    /** 按租户ID+岗位ID查询岗位 */
    PositionDO selectById(@Param("tenantId") String tenantId, @Param("positionId") String positionId);

    /** 按租户ID查询岗位列表 */
    List<PositionDO> selectByTenant(@Param("tenantId") String tenantId);

    /** 按租户ID+组织ID查询岗位列表 */
    List<PositionDO> selectByOrg(@Param("tenantId") String tenantId, @Param("orgId") String orgId);

    /** 更新岗位 */
    void update(PositionDO position);

    /** 删除岗位 */
    void deleteById(@Param("positionId") String positionId);
}
