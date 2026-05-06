package com.aidotnet.erp.crm.infrastructure.mapper;

import com.aidotnet.erp.crm.infrastructure.data.CustomerDO;
import com.aidotnet.erp.crm.infrastructure.data.ServiceTicketDO;
import com.aidotnet.erp.crm.infrastructure.data.ReturnRefundDO;
import com.aidotnet.erp.crm.infrastructure.data.ReviewAnalysisDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * CRM核心数据Mapper接口
 * <p>
 * 描述: 客户、工单、退货退款、评价分析等核心业务的数据访问层。
 *       基于MyBatis-Plus注解方式，SQL映射在resources/mapper目录下。
 * </p>
 *
 * @author ERP系统
 */
@Mapper
public interface CrmMapper {

    /** 新增客户 */
    void insertCustomer(CustomerDO customer);
    /** 按租户和客户ID查询客户 */
    CustomerDO selectCustomer(@Param("tenantId") String tenantId, @Param("customerId") String customerId);
    /** 按租户查询客户列表 */
    List<CustomerDO> selectCustomers(@Param("tenantId") String tenantId);

    /** 新增工单 */
    void insertTicket(ServiceTicketDO ticket);
    /** 更新工单 */
    void updateTicket(ServiceTicketDO ticket);
    /** 按租户和工单ID查询工单 */
    ServiceTicketDO selectTicket(@Param("tenantId") String tenantId, @Param("ticketId") String ticketId);
    /** 按租户查询工单列表 */
    List<ServiceTicketDO> selectTickets(@Param("tenantId") String tenantId);

    /** 新增退货退款 */
    void insertReturn(ReturnRefundDO returnRefund);
    /** 更新退货退款 */
    void updateReturn(ReturnRefundDO returnRefund);
    /** 按租户和退货ID查询退货退款 */
    ReturnRefundDO selectReturn(@Param("tenantId") String tenantId, @Param("returnId") String returnId);
    /** 按租户查询退货退款列表 */
    List<ReturnRefundDO> selectReturns(@Param("tenantId") String tenantId);

    /** 新增评价分析 */
    void insertReviewAnalysis(ReviewAnalysisDO analysis);
    /** 按租户查询评价分析列表 */
    List<ReviewAnalysisDO> selectReviewAnalyses(@Param("tenantId") String tenantId);
    /** 按租户和SKU查询评价分析列表 */
    List<ReviewAnalysisDO> selectReviewAnalysesBySku(@Param("tenantId") String tenantId, @Param("sellerSku") String sellerSku);
}
