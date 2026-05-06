package com.aidotnet.erp.crm.infrastructure.mapper;

import com.aidotnet.erp.crm.infrastructure.data.ReturnRefundDO;
import com.aidotnet.erp.crm.infrastructure.data.ServiceTicketDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * CRM售后数据Mapper接口
 * <p>
 * 描述: 售后业务独立数据访问层，负责退货退款和客服工单的CRUD操作。
 * </p>
 *
 * @author ERP系统
 */
@Mapper
public interface CrmAfterSaleMapper {

    /** 新增退货退款 */
    void insertReturnRefund(ReturnRefundDO data);
    /** 更新退货退款 */
    void updateReturnRefund(ReturnRefundDO data);
    /** 按租户和退货ID查询退货退款 */
    ReturnRefundDO selectReturnRefund(@Param("tenantId") String tenantId, @Param("returnId") String returnId);
    /** 按租户和订单号查询退货退款列表 */
    List<ReturnRefundDO> selectReturnRefunds(@Param("tenantId") String tenantId, @Param("orderId") String orderId);

    /** 新增客服工单 */
    void insertServiceTicket(ServiceTicketDO data);
    /** 更新客服工单 */
    void updateServiceTicket(ServiceTicketDO data);
    /** 按租户和工单ID查询客服工单 */
    ServiceTicketDO selectServiceTicket(@Param("tenantId") String tenantId, @Param("ticketId") String ticketId);
    /** 按租户和状态查询客服工单列表 */
    List<ServiceTicketDO> selectServiceTickets(@Param("tenantId") String tenantId, @Param("status") String status);
    /** 按租户和处理人查询客服工单列表 */
    List<ServiceTicketDO> selectServiceTicketsByAssignee(@Param("tenantId") String tenantId, @Param("assigneeId") String assigneeId);
}
