package com.aidotnet.erp.crm.infrastructure;

import com.aidotnet.erp.crm.domain.ReturnRefund;
import com.aidotnet.erp.crm.domain.ReturnRefund.ReturnStatus;
import com.aidotnet.erp.crm.domain.ServiceTicket;
import com.aidotnet.erp.crm.domain.TicketStatus;
import com.aidotnet.erp.crm.infrastructure.data.ReturnRefundDO;
import com.aidotnet.erp.crm.infrastructure.data.ServiceTicketDO;
import com.aidotnet.erp.crm.infrastructure.mapper.CrmAfterSaleMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/**
 * CRM售后数据存储
 * <p>
 * 描述: 售后业务独立数据持久化层，负责退货退款和客服工单的
 *       领域模型与数据对象之间的转换和持久化操作。
 * </p>
 * <p>
 * 数据转换: 领域模型(Domain) ↔ 数据对象(DO)，通过手动映射实现
 * </p>
 *
 * @author ERP系统
 * @see CrmAfterSaleMapper
 */
@Repository
public class CrmAfterSaleStore {

    private final CrmAfterSaleMapper mapper;

    public CrmAfterSaleStore(CrmAfterSaleMapper mapper) {
        this.mapper = mapper;
    }

    public ReturnRefund saveReturnRefund(ReturnRefund rf) {
        ReturnRefundDO existing = mapper.selectReturnRefund(rf.tenantId(), rf.returnId());
        ReturnRefundDO data = toReturnRefundData(rf);
        if (existing == null) {
            mapper.insertReturnRefund(data);
        } else {
            mapper.updateReturnRefund(data);
        }
        return rf;
    }

    public Optional<ReturnRefund> findReturnRefund(String tenantId, String returnId) {
        return Optional.ofNullable(mapper.selectReturnRefund(tenantId, returnId)).map(this::toReturnRefundDomain);
    }

    public List<ReturnRefund> listReturnRefunds(String tenantId, String orderId) {
        return mapper.selectReturnRefunds(tenantId, orderId).stream().map(this::toReturnRefundDomain).collect(Collectors.toList());
    }

    public ServiceTicket saveServiceTicket(ServiceTicket ticket) {
        ServiceTicketDO existing = mapper.selectServiceTicket(ticket.tenantId(), ticket.ticketId());
        ServiceTicketDO data = toServiceTicketData(ticket);
        if (existing == null) {
            mapper.insertServiceTicket(data);
        } else {
            mapper.updateServiceTicket(data);
        }
        return ticket;
    }

    public Optional<ServiceTicket> findServiceTicket(String tenantId, String ticketId) {
        return Optional.ofNullable(mapper.selectServiceTicket(tenantId, ticketId)).map(this::toServiceTicketDomain);
    }

    public List<ServiceTicket> listServiceTickets(String tenantId, TicketStatus status) {
        return mapper.selectServiceTickets(tenantId, status != null ? status.name() : null).stream()
                .map(this::toServiceTicketDomain).collect(Collectors.toList());
    }

    public List<ServiceTicket> listServiceTicketsByAssignee(String tenantId, String assigneeId) {
        return mapper.selectServiceTicketsByAssignee(tenantId, assigneeId).stream()
                .map(this::toServiceTicketDomain).collect(Collectors.toList());
    }

    private ReturnRefundDO toReturnRefundData(ReturnRefund rf) {
        ReturnRefundDO data = new ReturnRefundDO();
        data.setReturnId(rf.returnId());
        data.setTenantId(rf.tenantId());
        data.setOrderId(rf.orderId());
        data.setSellerSku(rf.sellerSku());
        data.setCustomerId(rf.customerId());
        data.setQuantity(rf.quantity());
        data.setRefundAmount(rf.refundAmount());
        data.setCurrency(rf.currency());
        data.setReason(rf.reason().name());
        data.setStatus(rf.status().name());
        data.setCreatedAt(rf.createdAt() != null ? rf.createdAt() : Instant.now());
        data.setUpdatedAt(rf.updatedAt() != null ? rf.updatedAt() : Instant.now());
        return data;
    }

    private ReturnRefund toReturnRefundDomain(ReturnRefundDO d) {
        return new ReturnRefund(d.getReturnId(), d.getTenantId(), d.getOrderId(), d.getSellerSku(),
                d.getCustomerId(), d.getQuantity(), d.getRefundAmount(), d.getCurrency(),
                ReturnRefund.ReturnReason.valueOf(d.getReason()), ReturnStatus.valueOf(d.getStatus()),
                d.getCreatedAt(), d.getUpdatedAt());
    }

    private ServiceTicketDO toServiceTicketData(ServiceTicket t) {
        ServiceTicketDO data = new ServiceTicketDO();
        data.setTicketId(t.ticketId());
        data.setTenantId(t.tenantId());
        data.setCustomerId(t.customerId());
        data.setSubject(t.subject());
        data.setDescription(t.description());
        data.setAssignee(t.assignee());
        data.setStatus(t.status().name());
        data.setResolution(t.resolution());
        data.setCreatedAt(t.createdAt() != null ? t.createdAt() : Instant.now());
        data.setUpdatedAt(t.updatedAt() != null ? t.updatedAt() : Instant.now());
        return data;
    }

    private ServiceTicket toServiceTicketDomain(ServiceTicketDO d) {
        return new ServiceTicket(d.getTicketId(), d.getTenantId(), d.getCustomerId(), d.getSubject(),
                d.getDescription(), d.getAssignee(), TicketStatus.valueOf(d.getStatus()),
                d.getResolution(), d.getCreatedAt(), d.getUpdatedAt());
    }
}
