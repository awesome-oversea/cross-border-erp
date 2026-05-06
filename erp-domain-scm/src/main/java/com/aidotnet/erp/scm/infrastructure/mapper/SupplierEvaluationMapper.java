package com.aidotnet.erp.scm.infrastructure.mapper;

import com.aidotnet.erp.scm.infrastructure.data.SupplierEvaluationDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * SCM域供应商评估MyBatis映射器
 * <p>
 * 描述: 供应商评估数据访问层，提供评估记录的查询操作。
 * </p>
 *
 * @author ERP系统
 */
@Mapper
public interface SupplierEvaluationMapper {

    /** 新增供应商评估记录 */
    void insert(SupplierEvaluationDO evaluation);

    /** 按供应商ID查询评估列表 */
    List<SupplierEvaluationDO> selectBySupplier(@Param("tenantId") String tenantId, @Param("supplierId") String supplierId);

    /** 查询供应商最新评估 */
    SupplierEvaluationDO selectLatest(@Param("tenantId") String tenantId, @Param("supplierId") String supplierId);
}
