package com.aidotnet.erp.crm.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.crm.application.CrmService;
import com.aidotnet.erp.crm.application.CrmService.AnalyzeReviewsCommand;
import com.aidotnet.erp.crm.application.CrmService.AssignTicketCommand;
import com.aidotnet.erp.crm.application.CrmService.CreateCustomerCommand;
import com.aidotnet.erp.crm.application.CrmService.CreateEmailCampaignCommand;
import com.aidotnet.erp.crm.application.CrmService.CreateEmailRuleCommand;
import com.aidotnet.erp.crm.application.CrmService.CreateMessageCommand;
import com.aidotnet.erp.crm.application.CrmService.CreateQualityIssueCommand;
import com.aidotnet.erp.crm.application.CrmService.CreateReviewCommand;
import com.aidotnet.erp.crm.application.CrmService.CreateReviewRequestCommand;
import com.aidotnet.erp.crm.application.CrmService.CreateReturnCommand;
import com.aidotnet.erp.crm.application.CrmService.CreateTicketCommand;
import com.aidotnet.erp.crm.application.CrmService.CreateVacationReplyCommand;
import com.aidotnet.erp.crm.application.CrmService.ResolveTicketCommand;
import com.aidotnet.erp.crm.application.CrmService.UpdateCustomerCommand;
import com.aidotnet.erp.crm.application.CrmService.UpdateEmailRuleCommand;
import com.aidotnet.erp.crm.application.CrmService.UpdateVacationReplyCommand;
import com.aidotnet.erp.crm.domain.Customer;
import com.aidotnet.erp.crm.domain.EmailCampaign;
import com.aidotnet.erp.crm.domain.EmailRule;
import com.aidotnet.erp.crm.domain.Message;
import com.aidotnet.erp.crm.domain.Message.MessageDirection;
import com.aidotnet.erp.crm.domain.QualityIssue;
import com.aidotnet.erp.crm.domain.ReturnRefund;
import com.aidotnet.erp.crm.domain.Review;
import com.aidotnet.erp.crm.domain.ReviewAnalysis;
import com.aidotnet.erp.crm.domain.ReviewRequest;
import com.aidotnet.erp.crm.domain.ServiceTicket;
import com.aidotnet.erp.crm.domain.VacationAutoReply;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * CRM内部API控制器
 * <p>
 * 描述: 客户关系管理域核心REST接口，提供客户管理、工单管理、
 *       退货退款、评价管理、消息管理、邮件规则、假期自动回复、
 *       邮件营销、评价请求、质量问题和评价分析等全量业务操作。
 * </p>
 * <p>
 * 路径规范: /crm/api/in/v1 — 内部方向(in)，v1版本
 * </p>
 * <p>
 * 核心业务流程:
 *   1. 客户CRUD: 创建/更新/查询客户
 *   2. 工单流转: 创建→指派→解决→关闭
 *   3. 退货退款: 申请→审批/拒绝→退款
 *   4. 评价管理: 创建评价、AI情感分析、回复评价
 *   5. 消息管理: 创建/分配/已读/回复客户消息
 *   6. 邮件规则: 创建/更新/删除自动分配规则
 *   7. 假期自动回复: 管理假期期间的自动回复
 *   8. 邮件营销: 创建/发送营销活动
 *   9. 评价请求: 创建产品评价邀请
 *   10. 质量问题: 创建/解决产品质量问题
 * </p>
 *
 * @author ERP系统
 * @see CrmService
 */
@RestController
@RequestMapping("/crm/api/in/v1")
public class CrmController {

    /** 客户管理应用服务 */
    private final CrmService crmService;

    /** 构造函数注入CrmService */
    public CrmController(CrmService crmService) {
        this.crmService = crmService;
    }

    /** 创建客户 */
    @PostMapping("/customers")
    public Result<Customer> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        return Result.ok(crmService.createCustomer(currentTenant(), new CreateCustomerCommand(request.name(),
                request.email(), request.phone(), request.countryCode(), request.platform(), request.storeId())));
    }

    /** 查询客户列表 */
    @GetMapping("/customers")
    public Result<List<Customer>> listCustomers() {
        return Result.ok(crmService.listCustomers(currentTenant()));
    }

    /** 更新客户信息 */
    @PutMapping("/customers/{customerId}")
    public Result<Customer> updateCustomer(@PathVariable String customerId, @Valid @RequestBody UpdateCustomerRequest request) {
        return Result.ok(crmService.updateCustomer(currentTenant(), customerId, new UpdateCustomerCommand(
                request.name(), request.email(), request.phone(), request.countryCode(), request.platform(), request.storeId())));
    }

    /** 创建客服工单 */
    @PostMapping("/tickets")
    public Result<ServiceTicket> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        return Result.ok(crmService.createTicket(currentTenant(), new CreateTicketCommand(request.customerId(), request.subject(), request.description())));
    }

    /** 查询工单列表 */
    @GetMapping("/tickets")
    public Result<List<ServiceTicket>> listTickets() {
        return Result.ok(crmService.listTickets(currentTenant()));
    }

    /** 指派工单处理人 */
    @PatchMapping("/tickets/{ticketId}/assign")
    public Result<ServiceTicket> assign(@PathVariable String ticketId, @Valid @RequestBody AssignRequest request) {
        return Result.ok(crmService.assign(currentTenant(), ticketId, new AssignTicketCommand(request.assignee())));
    }

    /** 解决工单 */
    @PatchMapping("/tickets/{ticketId}/resolve")
    public Result<ServiceTicket> resolve(@PathVariable String ticketId, @Valid @RequestBody ResolveRequest request) {
        return Result.ok(crmService.resolve(currentTenant(), ticketId, new ResolveTicketCommand(request.resolution())));
    }

    /** 关闭工单 */
    @PatchMapping("/tickets/{ticketId}/close")
    public Result<ServiceTicket> close(@PathVariable String ticketId) {
        return Result.ok(crmService.close(currentTenant(), ticketId));
    }

    /** 创建退货退款申请 */
    @PostMapping("/returns")
    public Result<ReturnRefund> createReturn(@Valid @RequestBody CreateReturnRequest request) {
        return Result.ok(crmService.createReturn(currentTenant(), new CreateReturnCommand(request.orderId(),
                request.sellerSku(), request.customerId(), request.quantity(), request.refundAmount(),
                request.currency(), request.reason())));
    }

    /** 查询退货退款列表 */
    @GetMapping("/returns")
    public Result<List<ReturnRefund>> listReturns() {
        return Result.ok(crmService.listReturns(currentTenant()));
    }

    /** 审批通过退货申请 */
    @PatchMapping("/returns/{returnId}/approve")
    public Result<ReturnRefund> approveReturn(@PathVariable String returnId) {
        return Result.ok(crmService.approveReturn(currentTenant(), returnId));
    }

    /** 拒绝退货申请 */
    @PatchMapping("/returns/{returnId}/reject")
    public Result<ReturnRefund> rejectReturn(@PathVariable String returnId) {
        return Result.ok(crmService.rejectReturn(currentTenant(), returnId));
    }

    /** 标记退货已退款 */
    @PatchMapping("/returns/{returnId}/refund")
    public Result<ReturnRefund> markRefunded(@PathVariable String returnId) {
        return Result.ok(crmService.markRefunded(currentTenant(), returnId));
    }

    /** 创建评价分析(AI情感分析) */
    @PostMapping("/review-analyses")
    public Result<ReviewAnalysis> analyzeReviews(@Valid @RequestBody AnalyzeReviewsRequest request) {
        return Result.ok(crmService.analyzeReviews(currentTenant(), new AnalyzeReviewsCommand(
                request.sellerSku(), request.marketplaceId(), request.averageRating(), request.totalReviews(),
                request.positiveCount(), request.neutralCount(), request.negativeCount(), request.sentimentSummary())));
    }

    /** 查询评价分析列表 */
    @GetMapping("/review-analyses")
    public Result<List<ReviewAnalysis>> listReviewAnalyses() {
        return Result.ok(crmService.listReviewAnalyses(currentTenant()));
    }

    /** 按SKU查询评价分析 */
    @GetMapping("/review-analyses/by-sku")
    public Result<List<ReviewAnalysis>> listReviewAnalysesBySku(@NotBlank String sellerSku) {
        return Result.ok(crmService.listReviewAnalysesBySku(currentTenant(), sellerSku));
    }

    /** 创建评价 */
    @PostMapping("/reviews")
    public Result<Review> createReview(@Valid @RequestBody CreateReviewRequest request) {
        return Result.ok(crmService.createReview(currentTenant(), new CreateReviewCommand(
                request.customerId(), request.productId(), request.orderId(), request.platform(),
                request.rating(), request.title(), request.text(), request.sentiment())));
    }

    /** 查询评价列表 */
    @GetMapping("/reviews")
    public Result<List<Review>> listReviews() {
        return Result.ok(crmService.listReviews(currentTenant()));
    }

    /** 查询差评列表 */
    @GetMapping("/reviews/negative")
    public Result<List<Review>> listNegativeReviews() {
        return Result.ok(crmService.listNegativeReviews(currentTenant()));
    }

    /** 按客户查询评价 */
    @GetMapping("/customers/{customerId}/reviews")
    public Result<List<Review>> listReviewsByCustomer(@PathVariable String customerId) {
        return Result.ok(crmService.listReviewsByCustomer(currentTenant(), customerId));
    }

    /** 回复评价 */
    @PatchMapping("/reviews/{reviewId}/respond")
    public Result<Review> respondToReview(@PathVariable String reviewId) {
        return Result.ok(crmService.respondToReview(currentTenant(), reviewId));
    }

    /** 创建客户消息 */
    @PostMapping("/messages")
    public Result<Message> createMessage(@Valid @RequestBody CreateMessageRequest request) {
        return Result.ok(crmService.createMessage(currentTenant(), new CreateMessageCommand(
                request.platform(), request.customerId(), request.subject(), request.body(), request.direction())));
    }

    /** 按客户查询消息 */
    @GetMapping("/customers/{customerId}/messages")
    public Result<List<Message>> listMessages(@PathVariable String customerId) {
        return Result.ok(crmService.listMessages(currentTenant(), customerId));
    }

    /** 查询未读消息列表 */
    @GetMapping("/messages/unread")
    public Result<List<Message>> listUnreadMessages() {
        return Result.ok(crmService.listUnreadMessages(currentTenant()));
    }

    /** 分配消息处理人 */
    @PatchMapping("/messages/{messageId}/assign")
    public Result<Message> assignMessage(@PathVariable String messageId, @Valid @RequestBody AssignMessageRequest request) {
        return Result.ok(crmService.assignMessage(currentTenant(), messageId, request.assignTo()));
    }

    /** 标记消息已读 */
    @PatchMapping("/messages/{messageId}/read")
    public Result<Message> markMessageRead(@PathVariable String messageId) {
        return Result.ok(crmService.markMessageRead(currentTenant(), messageId));
    }

    /** 回复消息 */
    @PatchMapping("/messages/{messageId}/reply")
    public Result<Message> replyMessage(@PathVariable String messageId, @Valid @RequestBody ReplyMessageRequest request) {
        return Result.ok(crmService.replyMessage(currentTenant(), messageId, request.replyBody()));
    }

    /** 查询邮件分配规则列表 */
    @GetMapping("/email-rules")
    public Result<List<EmailRule>> listEmailRules() {
        return Result.ok(crmService.listEmailRules(currentTenant()));
    }

    /** 创建邮件分配规则 */
    @PostMapping("/email-rules")
    public Result<EmailRule> createEmailRule(@Valid @RequestBody CreateEmailRuleRequest request) {
        return Result.ok(crmService.createEmailRule(currentTenant(), new CreateEmailRuleCommand(
                request.conditions(), request.assignTo(), request.priority())));
    }

    /** 更新邮件分配规则 */
    @PutMapping("/email-rules/{ruleId}")
    public Result<EmailRule> updateEmailRule(@PathVariable String ruleId, @Valid @RequestBody UpdateEmailRuleRequest request) {
        return Result.ok(crmService.updateEmailRule(currentTenant(), ruleId, new UpdateEmailRuleCommand(
                request.conditions(), request.assignTo(), request.priority())));
    }

    /** 删除邮件分配规则 */
    @DeleteMapping("/email-rules/{ruleId}")
    public Result<Void> deleteEmailRule(@PathVariable String ruleId) {
        crmService.deleteEmailRule(currentTenant(), ruleId);
        return Result.ok();
    }

    /** 查询假期自动回复列表 */
    @GetMapping("/vacation-auto-replies")
    public Result<List<VacationAutoReply>> listVacationAutoReplies() {
        return Result.ok(crmService.listVacationAutoReplies(currentTenant()));
    }

    /** 创建假期自动回复 */
    @PostMapping("/vacation-auto-replies")
    public Result<VacationAutoReply> createVacationAutoReply(@Valid @RequestBody CreateVacationReplyRequest request) {
        return Result.ok(crmService.createVacationAutoReply(currentTenant(), new CreateVacationReplyCommand(
                request.startDate(), request.endDate(), request.replyContent(), request.language())));
    }

    /** 更新假期自动回复 */
    @PutMapping("/vacation-auto-replies/{vacationId}")
    public Result<VacationAutoReply> updateVacationAutoReply(@PathVariable String vacationId,
                                                              @Valid @RequestBody UpdateVacationReplyRequest request) {
        return Result.ok(crmService.updateVacationAutoReply(currentTenant(), vacationId,
                new UpdateVacationReplyCommand(request.startDate(), request.endDate(), request.replyContent(), request.language())));
    }

    /** 查询评价请求列表 */
    @GetMapping("/review-requests")
    public Result<List<ReviewRequest>> listReviewRequests() {
        return Result.ok(crmService.listReviewRequests(currentTenant()));
    }

    /** 创建评价请求(邀请客户评价) */
    @PostMapping("/review-requests")
    public Result<ReviewRequest> createReviewRequest(@Valid @RequestBody CreateReviewRequestRequest request) {
        return Result.ok(crmService.createReviewRequest(currentTenant(), new CreateReviewRequestCommand(
                request.orderId(), request.platform(), request.excludeRefunded(), request.excludeNegative())));
    }

    /** 查询邮件营销活动列表 */
    @GetMapping("/email-campaigns")
    public Result<List<EmailCampaign>> listEmailCampaigns() {
        return Result.ok(crmService.listEmailCampaigns(currentTenant()));
    }

    /** 创建邮件营销活动 */
    @PostMapping("/email-campaigns")
    public Result<EmailCampaign> createEmailCampaign(@Valid @RequestBody CreateEmailCampaignRequest request) {
        return Result.ok(crmService.createEmailCampaign(currentTenant(), new CreateEmailCampaignCommand(
                request.name(), request.subject(), request.content(), request.targetSegment())));
    }

    /** 发送邮件营销活动 */
    @PostMapping("/email-campaigns/{campaignId}/send")
    public Result<EmailCampaign> sendEmailCampaign(@PathVariable String campaignId) {
        return Result.ok(crmService.sendEmailCampaign(currentTenant(), campaignId));
    }

    /** 查询质量问题列表(可按产品过滤) */
    @GetMapping("/quality-issues")
    public Result<List<QualityIssue>> listQualityIssues(@RequestParam(required = false) String productId) {
        if (productId != null && !productId.isBlank()) {
            return Result.ok(crmService.listQualityIssuesByProduct(currentTenant(), productId));
        }
        return Result.ok(crmService.listQualityIssues(currentTenant()));
    }

    /** 创建质量问题 */
    @PostMapping("/quality-issues")
    public Result<QualityIssue> createQualityIssue(@Valid @RequestBody CreateQualityIssueRequest request) {
        return Result.ok(crmService.createQualityIssue(currentTenant(), new CreateQualityIssueCommand(
                request.productId(), request.source(), request.description(), request.severity())));
    }

    /** 解决质量问题 */
    @PatchMapping("/quality-issues/{issueId}/resolve")
    public Result<QualityIssue> resolveQualityIssue(@PathVariable String issueId) {
        return Result.ok(crmService.resolveQualityIssue(currentTenant(), issueId));
    }

    /** 获取当前租户ID，为空则抛出业务异常 */
    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record CreateCustomerRequest(@NotBlank String name, String email, String phone,
                                        String countryCode, String platform, String storeId) {}

    public record UpdateCustomerRequest(String name, String email, String phone,
                                        String countryCode, String platform, String storeId) {}

    public record CreateTicketRequest(@NotBlank String customerId, @NotBlank String subject, @NotBlank String description) {}

    public record AssignRequest(@NotBlank String assignee) {}

    public record ResolveRequest(@NotBlank String resolution) {}

    public record CreateReturnRequest(@NotBlank String orderId, @NotBlank String sellerSku, @NotBlank String customerId,
                                      @Positive int quantity, @Positive BigDecimal refundAmount,
                                      @NotBlank String currency, ReturnRefund.ReturnReason reason) {}

    public record AnalyzeReviewsRequest(@NotBlank String sellerSku, String marketplaceId, double averageRating,
                                        int totalReviews, int positiveCount, int neutralCount,
                                        int negativeCount, String sentimentSummary) {}

    public record CreateReviewRequest(@NotBlank String customerId, String productId, String orderId, String platform,
                                      int rating, String title, String text, String sentiment) {}

    public record CreateMessageRequest(@NotBlank String platform, @NotBlank String customerId,
                                       String subject, @NotBlank String body, MessageDirection direction) {}

    public record AssignMessageRequest(@NotBlank String assignTo) {}

    public record ReplyMessageRequest(@NotBlank String replyBody) {}

    public record CreateEmailRuleRequest(Map<String, Object> conditions, @NotBlank String assignTo, int priority) {}
    public record UpdateEmailRuleRequest(Map<String, Object> conditions, String assignTo, int priority) {}
    public record CreateVacationReplyRequest(Instant startDate, Instant endDate, @NotBlank String replyContent, String language) {}
    public record UpdateVacationReplyRequest(Instant startDate, Instant endDate, String replyContent, String language) {}
    public record CreateReviewRequestRequest(@NotBlank String orderId, String platform, boolean excludeRefunded, boolean excludeNegative) {}
    public record CreateEmailCampaignRequest(@NotBlank String name, @NotBlank String subject, @NotBlank String content, String targetSegment) {}
    public record CreateQualityIssueRequest(@NotBlank String productId, String source, @NotBlank String description, String severity) {}
}
