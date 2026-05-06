package com.aidotnet.erp.iam.infrastructure.mapper;

import com.aidotnet.erp.iam.infrastructure.data.AuditLogDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 审计日志数据访问接口
 * <p>
 * 描述: 审计日志表的数据访问层，提供日志插入和按租户/模块/操作人查询。
 *       审计日志只增不改，无update/delete操作。
 * </p>
 *
 * @author ERP系统
 */
@Mapper
public interface AuditLogMapper {

    /** 新增审计日志 */
    void insert(AuditLogDO auditLog);

    /** 按租户ID查询审计日志 */
    List<AuditLogDO> selectByTenant(@Param("tenantId") String tenantId);

    /** 按租户ID+模块查询审计日志 */
    List<AuditLogDO> selectByTenantAndModule(@Param("tenantId") String tenantId, @Param("module") String module);

    /** 按租户ID+操作人查询审计日志 */
    List<AuditLogDO> selectByTenantAndActor(@Param("tenantId") String tenantId, @Param("actor") String actor);
}
