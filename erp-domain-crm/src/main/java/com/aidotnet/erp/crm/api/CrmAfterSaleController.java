package com.aidotnet.erp.crm.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.crm.application.CrmAfterSaleService;
import com.aidotnet.erp.crm.application.CrmAfterSaleService.CreateReturnCommand;
import com.aidotnet.erp.crm.application.CrmAfterSaleService.CreateTicketCommand;
import com.aidotnet.erp.crm.domain.ReturnRefund;
import com.aidotnet.erp.crm.domain.ReturnRefund.ReturnReason;
import com.aidotnet.erp.crm.domain.ServiceTicket;
import com.aidotnet.erp.crm.domain.TicketStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * CRM售后专用控制器
 * <p>
 * 描述: 售后业务独立接口，提供退货退款全流程和客服工单管理。
 *       与CrmController中的售后接口不同，此控制器专注售后场景，
 *       支持更细粒度的状态流转和按条件查询。
 * </p>
 * <p>
 * 路径规范: /crm/api/in/v1/after-sale — 内部方向(in)，v1版本，售后子模块
 * </p>
 * <p>
 * 退货退款状态流转: REQUESTED → APPROVED/REJECTED → RECEIVED → REFUNDED
 * 工单状态流转: OPEN → IN_PROGRESS → RESOLVED → CLOSED
 * </p>
 *
 * @author ERP系统
 * @see CrmAfterSaleService
 */
@RestController
@RequestMapping("/crm/api/in/v1/after-sale")
public class CrmAfterSaleController {

    /** 售后应用服务 */
    private final CrmAfterSaleService service;

    /** 构造函数注入CrmAfterSaleService */
    public CrmAfterSaleController(CrmAfterSaleService service) {
        this.service = service;
    }

    /** 创建退货退款申请 */
    @PostMapping("/returns")
    public Result<ReturnRefund> createReturn(@Valid @RequestBody CreateReturnRequest request) {
        CreateReturnCommand command = new CreateReturnCommand(request.orderId(), request.sellerSku(),
                request.customerId(), request.quantity(), request.refundAmount(), request.currency(), request.reason());
        return Result.ok(service.createReturnRequest(currentTenant(), command));
    }

    /** 审批通过退货申请 */
    @PatchMapping("/returns/{returnId}/approve")
    public Result<ReturnRefund> approveReturn(@PathVariable String returnId) {
        return Result.ok(service.approveReturn(currentTenant(), returnId));
    }

    /** 拒绝退货申请 */
    @PatchMapping("/returns/{returnId}/reject")
    public Result<ReturnRefund> rejectReturn(@PathVariable String returnId) {
        return Result.ok(service.rejectReturn(currentTenant(), returnId));
    }

    /** 确认收到退货商品 */
    @PatchMapping("/returns/{returnId}/receive")
    public Result<ReturnRefund> receiveReturn(@PathVariable String returnId) {
        return Result.ok(service.receiveReturn(currentTenant(), returnId));
    }

    /** 执行退款操作 */
    @PatchMapping("/returns/{returnId}/refund")
    public Result<ReturnRefund> refundReturn(@PathVariable String returnId) {
        return Result.ok(service.refundReturn(currentTenant(), returnId));
    }

    /** 查询退货退款列表(可按订单号过滤) */
    @GetMapping("/returns")
    public Result<List<ReturnRefund>> listReturns(@RequestParam(required = false) String orderId) {
        return Result.ok(service.listReturnRefunds(currentTenant(), orderId));
    }

    /** 查询退货退款详情 */
    @GetMapping("/returns/{returnId}")
    public Result<ReturnRefund> getReturn(@PathVariable String returnId) {
        return Result.ok(service.getReturnRefund(currentTenant(), returnId));
    }

    /** 创建客服工单 */
    @PostMapping("/tickets")
    public Result<ServiceTicket> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        CreateTicketCommand command = new CreateTicketCommand(request.customerId(), request.subject(), request.description());
        return Result.ok(service.createTicket(currentTenant(), command));
    }

    /** 指派工单处理人 */
    @PatchMapping("/tickets/{ticketId}/assign")
    public Result<ServiceTicket> assignTicket(@PathVariable String ticketId, @RequestParam String assigneeId) {
        return Result.ok(service.assignTicket(currentTenant(), ticketId, assigneeId));
    }

    /** 解决工单 */
    @PatchMapping("/tickets/{ticketId}/resolve")
    public Result<ServiceTicket> resolveTicket(@PathVariable String ticketId, @RequestParam String resolution) {
        return Result.ok(service.resolveTicket(currentTenant(), ticketId, resolution));
    }

    /** 关闭工单 */
    @PatchMapping("/tickets/{ticketId}/close")
    public Result<ServiceTicket> closeTicket(@PathVariable String ticketId) {
        return Result.ok(service.closeTicket(currentTenant(), ticketId));
    }

    /** 查询工单列表(可按状态过滤) */
    @GetMapping("/tickets")
    public Result<List<ServiceTicket>> listTickets(@RequestParam(required = false) TicketStatus status) {
        return Result.ok(service.listTickets(currentTenant(), status));
    }

    /** 按处理人查询工单列表 */
    @GetMapping("/tickets/assignee/{assigneeId}")
    public Result<List<ServiceTicket>> listTicketsByAssignee(@PathVariable String assigneeId) {
        return Result.ok(service.listTicketsByAssignee(currentTenant(), assigneeId));
    }

    /** 获取当前租户ID，为空则抛出业务异常 */
    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record CreateReturnRequest(@NotBlank String orderId, @NotBlank String sellerSku,
                                       @NotBlank String customerId, @Positive int quantity,
                                       BigDecimal refundAmount, String currency, ReturnReason reason) {}
    public record CreateTicketRequest(@NotBlank String customerId, @NotBlank String subject, String description) {}
}
