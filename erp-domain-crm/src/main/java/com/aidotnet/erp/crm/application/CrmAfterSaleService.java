package com.aidotnet.erp.crm.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.crm.client.WmsClient;
import com.aidotnet.erp.crm.domain.ReturnRefund;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.aidotnet.erp.crm.domain.ReturnRefund.ReturnReason;
import com.aidotnet.erp.crm.domain.ReturnRefund.ReturnStatus;
import com.aidotnet.erp.crm.domain.ServiceTicket;
import com.aidotnet.erp.crm.domain.TicketStatus;
import com.aidotnet.erp.crm.infrastructure.CrmAfterSaleStore;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CRM售后应用服务
 * <p>
 * 描述: 售后业务独立服务，提供退货退款全流程和客服工单管理。
 *       与CrmService中的售后方法互为补充，支持更细粒度的状态流转。
 *       所有写操作均使用@Transactional保证事务一致性。
 * </p>
 * <p>
 * 退货退款状态流转: REQUESTED → APPROVED/REJECTED → RECEIVED → REFUNDED
 * 工单状态流转: OPEN → IN_PROGRESS → RESOLVED → CLOSED
 * </p>
 *
 * @author ERP系统
 * @see CrmAfterSaleStore
 */
@Service
public class CrmAfterSaleService {

    private static final Logger log = LoggerFactory.getLogger(CrmAfterSaleService.class);

    /** 售后数据存储 */
    private final CrmAfterSaleStore afterSaleStore;

    /** WMS仓储客户端，用于退货入库时回写库存 */
    private final WmsClient wmsClient;

    /** 构造函数注入CrmAfterSaleStore和WmsClient */
    public CrmAfterSaleService(CrmAfterSaleStore afterSaleStore, WmsClient wmsClient) {
        this.afterSaleStore = afterSaleStore;
        this.wmsClient = wmsClient;
    }

    /** 创建退货退款申请，初始状态为REQUESTED */
    @Transactional
    public ReturnRefund createReturnRequest(String tenantId, CreateReturnCommand command) {
        Instant now = Instant.now();
        ReturnRefund returnRefund = new ReturnRefund(UUID.randomUUID().toString(), tenantId, command.orderId(),
                command.sellerSku(), command.customerId(), command.quantity(), command.refundAmount(),
                command.currency(), command.reason(), ReturnStatus.REQUESTED, now, now);
        return afterSaleStore.saveReturnRefund(returnRefund);
    }

    /** 审批通过退货申请，仅REQUESTED状态可审批 */
    @Transactional
    public ReturnRefund approveReturn(String tenantId, String returnId) {
        ReturnRefund rf = getReturnRefund(tenantId, returnId);
        if (rf.status() != ReturnStatus.REQUESTED) {
            throw new BizException("RETURN_STATUS_INVALID", "退货单状态不允许审批");
        }
        return afterSaleStore.saveReturnRefund(new ReturnRefund(rf.returnId(), rf.tenantId(), rf.orderId(),
                rf.sellerSku(), rf.customerId(), rf.quantity(), rf.refundAmount(), rf.currency(),
                rf.reason(), ReturnStatus.APPROVED, rf.createdAt(), Instant.now()));
    }

    /** 拒绝退货申请，仅REQUESTED状态可拒绝 */
    @Transactional
    public ReturnRefund rejectReturn(String tenantId, String returnId) {
        ReturnRefund rf = getReturnRefund(tenantId, returnId);
        if (rf.status() != ReturnStatus.REQUESTED) {
            throw new BizException("RETURN_STATUS_INVALID", "退货单状态不允许拒绝");
        }
        return afterSaleStore.saveReturnRefund(new ReturnRefund(rf.returnId(), rf.tenantId(), rf.orderId(),
                rf.sellerSku(), rf.customerId(), rf.quantity(), rf.refundAmount(), rf.currency(),
                rf.reason(), ReturnStatus.REJECTED, rf.createdAt(), Instant.now()));
    }

    /**
     * 确认收到退货商品，仅APPROVED状态可确认收货
     * <p>
     * 业务闭环:
     *   1. 校验退货单状态为APPROVED
     *   2. 将退回商品回写到WMS库存(可用库存增加)
     *   3. 更新退货单状态为RECEIVED
     *   4. WMS回写失败时记录警告但不阻断流程(允许后续手工处理)
     * </p>
     */
    @Transactional
    public ReturnRefund receiveReturn(String tenantId, String returnId) {
        ReturnRefund rf = getReturnRefund(tenantId, returnId);
        if (rf.status() != ReturnStatus.APPROVED) {
            throw new BizException("RETURN_STATUS_INVALID", "退货单状态不允许确认收货");
        }
        // 退回商品回写WMS库存，确保库存准确性
        try {
            wmsClient.receiveReturn(new WmsClient.ReceiveReturnRequest(
                    null, rf.sellerSku(), rf.quantity(), returnId));
        } catch (Exception e) {
            log.warn("WMS inventory restock failed for returnId={}, sellerSku={}, will retry later",
                    returnId, rf.sellerSku(), e);
        }
        return afterSaleStore.saveReturnRefund(new ReturnRefund(rf.returnId(), rf.tenantId(), rf.orderId(),
                rf.sellerSku(), rf.customerId(), rf.quantity(), rf.refundAmount(), rf.currency(),
                rf.reason(), ReturnStatus.RECEIVED, rf.createdAt(), Instant.now()));
    }

    /** 执行退款，仅RECEIVED状态可退款 */
    @Transactional
    public ReturnRefund refundReturn(String tenantId, String returnId) {
        ReturnRefund rf = getReturnRefund(tenantId, returnId);
        if (rf.status() != ReturnStatus.RECEIVED) {
            throw new BizException("RETURN_STATUS_INVALID", "退货单状态不允许退款");
        }
        return afterSaleStore.saveReturnRefund(new ReturnRefund(rf.returnId(), rf.tenantId(), rf.orderId(),
                rf.sellerSku(), rf.customerId(), rf.quantity(), rf.refundAmount(), rf.currency(),
                rf.reason(), ReturnStatus.REFUNDED, rf.createdAt(), Instant.now()));
    }

    /** 查询退货退款详情 */
    public ReturnRefund getReturnRefund(String tenantId, String returnId) {
        return afterSaleStore.findReturnRefund(tenantId, returnId)
                .orElseThrow(() -> new BizException("RETURN_NOT_FOUND", "退货单不存在"));
    }

    /** 查询退货退款列表(可按订单号过滤) */
    public List<ReturnRefund> listReturnRefunds(String tenantId, String orderId) {
        return afterSaleStore.listReturnRefunds(tenantId, orderId);
    }

    /** 创建客服工单，初始状态为OPEN */
    @Transactional
    public ServiceTicket createTicket(String tenantId, CreateTicketCommand command) {
        Instant now = Instant.now();
        ServiceTicket ticket = new ServiceTicket(UUID.randomUUID().toString(), tenantId, command.customerId(),
                command.subject(), command.description(), null, TicketStatus.OPEN, null, now, now);
        return afterSaleStore.saveServiceTicket(ticket);
    }

    /** 指派工单处理人，状态变更为IN_PROGRESS */
    @Transactional
    public ServiceTicket assignTicket(String tenantId, String ticketId, String assigneeId) {
        ServiceTicket ticket = getTicket(tenantId, ticketId);
        return afterSaleStore.saveServiceTicket(new ServiceTicket(ticket.ticketId(), ticket.tenantId(),
                ticket.customerId(), ticket.subject(), ticket.description(), assigneeId,
                TicketStatus.IN_PROGRESS, ticket.resolution(), ticket.createdAt(), Instant.now()));
    }

    /** 解决工单，仅IN_PROGRESS状态可解决 */
    @Transactional
    public ServiceTicket resolveTicket(String tenantId, String ticketId, String resolution) {
        ServiceTicket ticket = getTicket(tenantId, ticketId);
        if (ticket.status() != TicketStatus.IN_PROGRESS) {
            throw new BizException("TICKET_STATUS_INVALID", "工单状态不允许解决");
        }
        return afterSaleStore.saveServiceTicket(new ServiceTicket(ticket.ticketId(), ticket.tenantId(),
                ticket.customerId(), ticket.subject(), ticket.description(), ticket.assignee(),
                TicketStatus.RESOLVED, resolution, ticket.createdAt(), Instant.now()));
    }

    /** 关闭工单 */
    @Transactional
    public ServiceTicket closeTicket(String tenantId, String ticketId) {
        ServiceTicket ticket = getTicket(tenantId, ticketId);
        return afterSaleStore.saveServiceTicket(new ServiceTicket(ticket.ticketId(), ticket.tenantId(),
                ticket.customerId(), ticket.subject(), ticket.description(), ticket.assignee(),
                TicketStatus.CLOSED, ticket.resolution(), ticket.createdAt(), Instant.now()));
    }

    /** 查询工单详情 */
    public ServiceTicket getTicket(String tenantId, String ticketId) {
        return afterSaleStore.findServiceTicket(tenantId, ticketId)
                .orElseThrow(() -> new BizException("TICKET_NOT_FOUND", "工单不存在"));
    }

    /** 查询工单列表(可按状态过滤) */
    public List<ServiceTicket> listTickets(String tenantId, TicketStatus status) {
        return afterSaleStore.listServiceTickets(tenantId, status);
    }

    /** 按处理人查询工单列表 */
    public List<ServiceTicket> listTicketsByAssignee(String tenantId, String assigneeId) {
        return afterSaleStore.listServiceTicketsByAssignee(tenantId, assigneeId);
    }

    public record CreateReturnCommand(String orderId, String sellerSku, String customerId, int quantity,
                                       BigDecimal refundAmount, String currency, ReturnReason reason) {}
    public record CreateTicketCommand(String customerId, String subject, String description) {}
}
