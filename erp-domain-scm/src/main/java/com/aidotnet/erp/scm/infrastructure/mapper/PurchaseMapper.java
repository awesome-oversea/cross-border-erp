package com.aidotnet.erp.scm.infrastructure.mapper;

import com.aidotnet.erp.scm.infrastructure.data.PurchaseOrderDO;
import com.aidotnet.erp.scm.infrastructure.data.PurchaseOrderLineDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * SCM域采购单MyBatis映射器
 * <p>
 * 描述: 采购单数据访问层，提供采购单和采购单行的数据操作。
 * </p>
 *
 * @author ERP系统
 */
@Mapper
public interface PurchaseMapper {

    /** 新增采购单 */
    void insertOrder(PurchaseOrderDO order);

    /** 更新采购单 */
    void updateOrder(PurchaseOrderDO order);

    /** 按租户ID+采购单ID查询 */
    PurchaseOrderDO selectOrder(@Param("tenantId") String tenantId, @Param("poId") String poId);

    /** 按租户ID查询采购单列表 */
    List<PurchaseOrderDO> selectOrders(@Param("tenantId") String tenantId);

    /** 新增采购单行 */
    void insertLine(PurchaseOrderLineDO line);

    /** 按采购单ID查询行列表 */
    List<PurchaseOrderLineDO> selectLines(@Param("poId") String poId);

    /** 按采购单ID删除行(更新时先删后插) */
    void deleteLines(@Param("poId") String poId);
}
