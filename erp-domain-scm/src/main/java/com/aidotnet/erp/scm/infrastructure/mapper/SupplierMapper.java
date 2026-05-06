package com.aidotnet.erp.scm.infrastructure.mapper;

import com.aidotnet.erp.scm.infrastructure.data.SupplierDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * SCM域供应商MyBatis映射器
 * <p>
 * 描述: 供应商数据访问层，提供供应商主数据的CRUD操作。
 * </p>
 *
 * @author ERP系统
 */
@Mapper
public interface SupplierMapper {

    /** 新增供应商 */
    void insert(SupplierDO supplier);

    /** 更新供应商 */
    void update(SupplierDO supplier);

    /** 按租户ID+供应商ID查询 */
    SupplierDO selectById(@Param("tenantId") String tenantId, @Param("supplierId") String supplierId);

    /** 按租户ID查询供应商列表 */
    List<SupplierDO> selectByTenant(@Param("tenantId") String tenantId);
}
