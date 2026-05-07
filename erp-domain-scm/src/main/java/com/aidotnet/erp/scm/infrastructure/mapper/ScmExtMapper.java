package com.aidotnet.erp.scm.infrastructure.mapper;

import com.aidotnet.erp.scm.infrastructure.data.PurchaseApprovalDO;
import com.aidotnet.erp.scm.infrastructure.data.PurchaseExceptionDO;
import com.aidotnet.erp.scm.infrastructure.data.PurchaseTrackingDO;
import com.aidotnet.erp.scm.infrastructure.data.ProcessingOrderDO;
import com.aidotnet.erp.scm.infrastructure.data.QuoteDO;
import com.aidotnet.erp.scm.infrastructure.data.SupplierScoreDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * SCM域扩展数据MyBatis映射器
 * <p>
 * 描述: 供应链域扩展数据访问层，提供报价、供应商评分、审批、加工单、
 *       采购跟踪、采购异常等表的数据操作。
 * </p>
 *
 * @author ERP系统
 */
@Mapper
public interface ScmExtMapper {

    // ---- 报价 ----

    /** 新增报价 */
    void insertQuote(QuoteDO quote);
    /** 更新报价 */
    void updateQuote(QuoteDO quote);
    /** 按报价ID查询 */
    QuoteDO selectQuote(@Param("tenantId") String tenantId, @Param("quoteId") String quoteId);
    /** 按供应商ID查询报价列表 */
    List<QuoteDO> selectQuotes(@Param("tenantId") String tenantId, @Param("supplierId") String supplierId);
    /** 按SKU查询报价列表(比价) */
    List<QuoteDO> selectQuotesBySku(@Param("tenantId") String tenantId, @Param("sellerSku") String sellerSku);

    // ---- 供应商评分 ----

    /** 新增供应商综合评分 */
    void insertSupplierScore(SupplierScoreDO score);
    /** 更新供应商综合评分 */
    void updateSupplierScore(SupplierScoreDO score);
    /** 按供应商ID查询综合评分 */
    SupplierScoreDO selectSupplierScore(@Param("tenantId") String tenantId, @Param("supplierId") String supplierId);

    // ---- 采购审批 ----

    /** 新增采购审批记录 */
    void insertPurchaseApproval(PurchaseApprovalDO approval);
    /** 更新采购审批记录 */
    void updatePurchaseApproval(PurchaseApprovalDO approval);
    /** 按审批ID查询采购审批记录 */
    PurchaseApprovalDO selectPurchaseApproval(@Param("tenantId") String tenantId, @Param("approvalId") String approvalId);
    /** 按采购单ID查询审批列表 */
    List<PurchaseApprovalDO> selectApprovalsByPo(@Param("tenantId") String tenantId, @Param("poId") String poId);
    /** 按审批人查询待审批列表 */
    List<PurchaseApprovalDO> selectPendingApprovals(@Param("tenantId") String tenantId, @Param("approverId") String approverId);

    // ---- 加工单 ----

    /** 新增加工单 */
    void insertProcessingOrder(ProcessingOrderDO order);
    /** 更新加工单 */
    void updateProcessingOrder(ProcessingOrderDO order);
    /** 按加工单ID查询 */
    ProcessingOrderDO selectProcessingOrder(@Param("tenantId") String tenantId, @Param("processId") String processId);
    /** 按租户ID查询加工单列表 */
    List<ProcessingOrderDO> selectProcessingOrders(@Param("tenantId") String tenantId);

    // ---- 采购跟踪 ----

    /** 新增采购跟踪 */
    void insertTracking(PurchaseTrackingDO tracking);
    /** 更新采购跟踪 */
    void updateTracking(PurchaseTrackingDO tracking);
    /** 按采购单ID+行ID查询跟踪 */
    PurchaseTrackingDO selectTracking(@Param("tenantId") String tenantId, @Param("poId") String poId, @Param("lineId") String lineId);
    /** 按采购单ID查询跟踪列表 */
    List<PurchaseTrackingDO> selectTrackings(@Param("tenantId") String tenantId, @Param("poId") String poId);

    // ---- 采购异常 ----

    /** 新增采购异常 */
    void insertException(PurchaseExceptionDO exception);
    /** 更新采购异常 */
    void updateException(PurchaseExceptionDO exception);
    /** 按异常ID查询 */
    PurchaseExceptionDO selectException(@Param("tenantId") String tenantId, @Param("exceptionId") String exceptionId);
    /** 按采购单ID查询异常列表 */
    List<PurchaseExceptionDO> selectExceptions(@Param("tenantId") String tenantId, @Param("poId") String poId);
    /** 按租户ID查询待处理异常 */
    List<PurchaseExceptionDO> selectPendingExceptions(@Param("tenantId") String tenantId);
}
