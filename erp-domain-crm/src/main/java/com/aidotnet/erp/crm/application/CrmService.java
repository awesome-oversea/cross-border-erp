package com.aidotnet.erp.crm.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.crm.client.OmsClient;
import com.aidotnet.erp.crm.domain.Customer;
import com.aidotnet.erp.crm.domain.EmailCampaign;
import com.aidotnet.erp.crm.domain.EmailRule;
import com.aidotnet.erp.crm.domain.Message;
import com.aidotnet.erp.crm.domain.Message.MessageDirection;
import com.aidotnet.erp.crm.domain.Message.MessageStatus;
import com.aidotnet.erp.crm.domain.QualityIssue;
import com.aidotnet.erp.crm.domain.ReturnRefund;
import com.aidotnet.erp.crm.domain.Review;
import com.aidotnet.erp.crm.domain.ReviewAnalysis;
import com.aidotnet.erp.crm.domain.ReviewRequest;
import com.aidotnet.erp.crm.domain.ServiceTicket;
import com.aidotnet.erp.crm.domain.TicketStatus;
import com.aidotnet.erp.crm.domain.VacationAutoReply;
import com.aidotnet.erp.crm.infrastructure.CrmStore;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 客户关系管理应用服务
 * <p>
 * 描述: 客服售后域核心服务，负责客户管理、工单管理、消息管理、
 *       退货退款、评价管理、邮件规则、假期自动回复、邮件营销等业务逻辑。
 *       是连接订单域(OMS)的客户服务中枢，支持AI情感分析能力。
 * </p>
 * <p>
 * 核心能力:
 *   1. 客户管理 - 创建/更新客户，维护客户订单统计和标签
 *   2. 工单管理 - 创建/指派/解决/关闭客服工单
 *   3. 消息管理 - 创建/分配/标记已读/回复客户消息
 *   4. 退货退款 - 创建/审批/拒绝/退款退货申请，自动同步OMS
 *   5. 评价管理 - 创建/回复评价，AI情感分析
 *   6. 邮件规则 - 创建/更新/删除邮件自动分配规则
 *   7. 假期自动回复 - 管理假期期间的自动回复设置
 *   8. 邮件营销 - 创建/发送邮件营销活动
 *   9. 评价请求 - 创建产品评价邀请
 * </p>
 * <p>
 * 业务规则:
 *   1. 工单状态流转: OPEN -> ASSIGNED -> RESOLVED -> CLOSED
 *   2. 退货状态流转: REQUESTED -> APPROVED/REJECTED -> RECEIVED -> REFUNDED
 *   3. 审批退货时自动向OMS发起退款请求
 *   4. 邮件规则按优先级匹配，首个匹配规则生效
 *   5. 只有草稿状态的营销活动可以发送
 * </p>
 *
 * @author ERP系统
 * @see Customer
 * @see ServiceTicket
 * @see CrmStore
 */
@Service
public class CrmService {

    private static final Logger log = LoggerFactory.getLogger(CrmService.class);

    private final CrmStore crmStore;
    private final OmsClient omsClient;

    public CrmService(CrmStore crmStore, OmsClient omsClient) {
        this.crmStore = crmStore;
        this.omsClient = omsClient;
    }

    public Customer createCustomer(String tenantId, CreateCustomerCommand command) {
        Instant now = Instant.now();
        return crmStore.saveCustomer(new Customer(UUID.randomUUID().toString(), tenantId, command.name(),
                command.email(), command.phone(), command.countryCode(), command.platform(), command.storeId(),
                0, BigDecimal.ZERO, null, Collections.emptyList(), now, now));
    }

    public Customer updateCustomer(String tenantId, String customerId, UpdateCustomerCommand command) {
        Customer existing = crmStore.findCustomer(tenantId, customerId)
                .orElseThrow(() -> new BizException("CUSTOMER_NOT_FOUND", "客户不存在"));
        return crmStore.saveCustomer(new Customer(existing.customerId(), existing.tenantId(),
                command.name() != null ? command.name() : existing.name(),
                command.email() != null ? command.email() : existing.email(),
                command.phone() != null ? command.phone() : existing.phone(),
                command.countryCode() != null ? command.countryCode() : existing.countryCode(),
                command.platform() != null ? command.platform() : existing.platform(),
                command.storeId() != null ? command.storeId() : existing.storeId(),
                existing.totalOrders(), existing.totalSpent(), existing.lastOrderAt(),
                existing.tags(), existing.createdAt(), Instant.now()));
    }

    public Customer updateCustomerOrderStats(String tenantId, String customerId, BigDecimal orderAmount) {
        Customer existing = crmStore.findCustomer(tenantId, customerId)
                .orElseThrow(() -> new BizException("CUSTOMER_NOT_FOUND", "客户不存在"));
        int newTotalOrders = existing.totalOrders() + 1;
        BigDecimal newTotalSpent = existing.totalSpent().add(orderAmount);
        return crmStore.saveCustomer(new Customer(existing.customerId(), existing.tenantId(), existing.name(),
                existing.email(), existing.phone(), existing.countryCode(), existing.platform(), existing.storeId(),
                newTotalOrders, newTotalSpent, Instant.now(), existing.tags(), existing.createdAt(), Instant.now()));
    }

    public ServiceTicket createTicket(String tenantId, CreateTicketCommand command) {
        crmStore.findCustomer(tenantId, command.customerId()).orElseThrow(() -> new BizException("CUSTOMER_NOT_FOUND", "客户不存在"));
        Instant now = Instant.now();
        return crmStore.saveTicket(new ServiceTicket(UUID.randomUUID().toString(), tenantId, command.customerId(),
                command.subject(), command.description(), null, TicketStatus.OPEN, null, now, now));
    }

    public ServiceTicket assign(String tenantId, String ticketId, AssignTicketCommand command) {
        ServiceTicket ticket = getTicket(tenantId, ticketId);
        if (ticket.status() == TicketStatus.CLOSED) {
            throw new BizException("TICKET_STATUS_INVALID", "已关闭工单不可指派");
        }
        return update(ticket, command.assignee(), TicketStatus.ASSIGNED, ticket.resolution());
    }

    public ServiceTicket resolve(String tenantId, String ticketId, ResolveTicketCommand command) {
        ServiceTicket ticket = getTicket(tenantId, ticketId);
        if (ticket.status() != TicketStatus.ASSIGNED) {
            throw new BizException("TICKET_STATUS_INVALID", "只有已指派工单可以解决");
        }
        return update(ticket, ticket.assignee(), TicketStatus.RESOLVED, command.resolution());
    }

    public ServiceTicket close(String tenantId, String ticketId) {
        ServiceTicket ticket = getTicket(tenantId, ticketId);
        if (ticket.status() != TicketStatus.RESOLVED) {
            throw new BizException("TICKET_STATUS_INVALID", "只有已解决工单可以关闭");
        }
        return update(ticket, ticket.assignee(), TicketStatus.CLOSED, ticket.resolution());
    }

    public List<Customer> listCustomers(String tenantId) {
        return crmStore.listCustomers(tenantId);
    }

    public List<ServiceTicket> listTickets(String tenantId) {
        return crmStore.listTickets(tenantId);
    }

    public ReturnRefund createReturn(String tenantId, CreateReturnCommand command) {
        Instant now = Instant.now();
        return crmStore.saveReturn(new ReturnRefund(UUID.randomUUID().toString(), tenantId, command.orderId(),
                command.sellerSku(), command.customerId(), command.quantity(), command.refundAmount(),
                command.currency(), command.reason(), ReturnRefund.ReturnStatus.REQUESTED, now, now));
    }

    public ReturnRefund approveReturn(String tenantId, String returnId) {
        ReturnRefund r = getReturn(tenantId, returnId);
        if (r.status() != ReturnRefund.ReturnStatus.REQUESTED) {
            throw new BizException("RETURN_STATUS_INVALID", "只有申请中的退货可以审批");
        }
        ReturnRefund approved = updateReturn(r, ReturnRefund.ReturnStatus.APPROVED);
        requestOmsRefund(approved);
        return approved;
    }

    public ReturnRefund rejectReturn(String tenantId, String returnId) {
        ReturnRefund r = getReturn(tenantId, returnId);
        if (r.status() != ReturnRefund.ReturnStatus.REQUESTED) {
            throw new BizException("RETURN_STATUS_INVALID", "只有申请中的退货可以拒绝");
        }
        return updateReturn(r, ReturnRefund.ReturnStatus.REJECTED);
    }

    public ReturnRefund markRefunded(String tenantId, String returnId) {
        ReturnRefund r = getReturn(tenantId, returnId);
        if (r.status() != ReturnRefund.ReturnStatus.RECEIVED) {
            throw new BizException("RETURN_STATUS_INVALID", "只有已收货的退货可以退款");
        }
        return updateReturn(r, ReturnRefund.ReturnStatus.REFUNDED);
    }

    public List<ReturnRefund> listReturns(String tenantId) {
        return crmStore.listReturns(tenantId);
    }

    public ReviewAnalysis analyzeReviews(String tenantId, AnalyzeReviewsCommand command) {
        return crmStore.saveReviewAnalysis(new ReviewAnalysis(UUID.randomUUID().toString(), tenantId,
                command.sellerSku(), command.marketplaceId(), command.averageRating(), command.totalReviews(),
                command.positiveCount(), command.neutralCount(), command.negativeCount(),
                command.sentimentSummary(), Instant.now()));
    }

    public List<ReviewAnalysis> listReviewAnalyses(String tenantId) {
        return crmStore.listReviewAnalyses(tenantId);
    }

    public List<ReviewAnalysis> listReviewAnalysesBySku(String tenantId, String sellerSku) {
        return crmStore.listReviewAnalysesBySku(tenantId, sellerSku);
    }

    public ServiceTicket getTicket(String tenantId, String ticketId) {
        return crmStore.findTicket(tenantId, ticketId).orElseThrow(() -> new BizException("TICKET_NOT_FOUND", "工单不存在"));
    }

    private ServiceTicket update(ServiceTicket ticket, String assignee, TicketStatus status, String resolution) {
        return crmStore.saveTicket(new ServiceTicket(ticket.ticketId(), ticket.tenantId(), ticket.customerId(),
                ticket.subject(), ticket.description(), assignee, status, resolution, ticket.createdAt(), Instant.now()));
    }

    private ReturnRefund getReturn(String tenantId, String returnId) {
        return crmStore.findReturn(tenantId, returnId).orElseThrow(() -> new BizException("RETURN_NOT_FOUND", "退货记录不存在"));
    }

    private void requestOmsRefund(ReturnRefund r) {
        try {
            omsClient.requestRefund(r.orderId(), new OmsClient.RefundRequest(
                    r.reason().name(), r.refundAmount(), "FULL"));
        } catch (Exception ex) {
            log.warn("Request OMS refund failed. returnId={}, orderId={}", r.returnId(), r.orderId(), ex);
        }
    }

    private ReturnRefund updateReturn(ReturnRefund r, ReturnRefund.ReturnStatus status) {
        return crmStore.saveReturn(new ReturnRefund(r.returnId(), r.tenantId(), r.orderId(), r.sellerSku(),
                r.customerId(), r.quantity(), r.refundAmount(), r.currency(), r.reason(), status, r.createdAt(), Instant.now()));
    }

    public record CreateCustomerCommand(String name, String email, String phone, String countryCode,
                                        String platform, String storeId) {}

    public record UpdateCustomerCommand(String name, String email, String phone, String countryCode,
                                        String platform, String storeId) {}

    public record CreateTicketCommand(String customerId, String subject, String description) {}

    public record AssignTicketCommand(String assignee) {}

    public record ResolveTicketCommand(String resolution) {}

    public record CreateReturnCommand(String orderId, String sellerSku, String customerId, int quantity,
                                      BigDecimal refundAmount, String currency, ReturnRefund.ReturnReason reason) {}

    public record AnalyzeReviewsCommand(String sellerSku, String marketplaceId, double averageRating,
                                        int totalReviews, int positiveCount, int neutralCount,
                                        int negativeCount, String sentimentSummary) {}

    public Review createReview(String tenantId, CreateReviewCommand command) {
        Instant now = Instant.now();
        return crmStore.saveReview(new Review(UUID.randomUUID().toString(), tenantId, command.customerId(),
                command.productId(), command.orderId(), command.platform(), command.rating(),
                command.title(), command.text(), command.sentiment(), false, false, now, now, now));
    }

    public Review respondToReview(String tenantId, String reviewId) {
        Review review = crmStore.findReview(tenantId, reviewId)
                .orElseThrow(() -> new BizException("REVIEW_NOT_FOUND", "评价不存在"));
        return crmStore.saveReview(new Review(review.reviewId(), review.tenantId(), review.customerId(),
                review.productId(), review.orderId(), review.platform(), review.rating(), review.title(),
                review.text(), review.sentiment(), review.verified(), true, review.reviewDate(),
                review.createdAt(), Instant.now()));
    }

    public List<Review> listReviews(String tenantId) {
        return crmStore.listReviews(tenantId);
    }

    public List<Review> listReviewsByCustomer(String tenantId, String customerId) {
        return crmStore.listReviewsByCustomer(tenantId, customerId);
    }

    public List<Review> listNegativeReviews(String tenantId) {
        return crmStore.listNegativeReviews(tenantId);
    }

    public Message createMessage(String tenantId, CreateMessageCommand command) {
        Instant now = Instant.now();
        return crmStore.saveMessage(new Message(UUID.randomUUID().toString(), tenantId, command.platform(),
                command.customerId(), command.subject(), command.body(), command.direction(),
                null, MessageStatus.UNREAD, now, now, now));
    }

    public Message assignMessage(String tenantId, String messageId, String assignTo) {
        Message msg = crmStore.findMessage(tenantId, messageId)
                .orElseThrow(() -> new BizException("MESSAGE_NOT_FOUND", "消息不存在"));
        return crmStore.saveMessage(new Message(msg.messageId(), msg.tenantId(), msg.platform(), msg.customerId(),
                msg.subject(), msg.body(), msg.direction(), assignTo, msg.status(), msg.messageDate(),
                msg.createdAt(), Instant.now()));
    }

    public Message markMessageRead(String tenantId, String messageId) {
        Message msg = crmStore.findMessage(tenantId, messageId)
                .orElseThrow(() -> new BizException("MESSAGE_NOT_FOUND", "消息不存在"));
        return crmStore.saveMessage(new Message(msg.messageId(), msg.tenantId(), msg.platform(), msg.customerId(),
                msg.subject(), msg.body(), msg.direction(), msg.assignedTo(), MessageStatus.READ,
                msg.messageDate(), msg.createdAt(), Instant.now()));
    }

    public Message replyMessage(String tenantId, String messageId, String replyBody) {
        Message msg = crmStore.findMessage(tenantId, messageId)
                .orElseThrow(() -> new BizException("MESSAGE_NOT_FOUND", "消息不存在"));
        return crmStore.saveMessage(new Message(msg.messageId(), msg.tenantId(), msg.platform(), msg.customerId(),
                msg.subject(), msg.body(), msg.direction(), msg.assignedTo(), MessageStatus.REPLIED,
                msg.messageDate(), msg.createdAt(), Instant.now()));
    }

    public List<Message> listMessages(String tenantId, String customerId) {
        return crmStore.listMessages(tenantId, customerId);
    }

    public List<Message> listUnreadMessages(String tenantId) {
        return crmStore.listUnreadMessages(tenantId);
    }

    public record CreateReviewCommand(String customerId, String productId, String orderId, String platform,
                                      int rating, String title, String text, String sentiment) {}

    public record CreateMessageCommand(String platform, String customerId, String subject, String body,
                                       MessageDirection direction) {}

    public EmailRule createEmailRule(String tenantId, CreateEmailRuleCommand command) {
        Instant now = Instant.now();
        return crmStore.saveEmailRule(new EmailRule(UUID.randomUUID().toString(), tenantId,
                command.conditions(), command.assignTo(), command.priority(), "active", now, now));
    }

    public EmailRule updateEmailRule(String tenantId, String ruleId, UpdateEmailRuleCommand command) {
        EmailRule existing = crmStore.findEmailRule(tenantId, ruleId)
                .orElseThrow(() -> new BizException("EMAIL_RULE_NOT_FOUND", "邮件分配规则不存在"));
        return crmStore.saveEmailRule(new EmailRule(existing.ruleId(), tenantId,
                command.conditions() != null ? command.conditions() : existing.conditions(),
                command.assignTo() != null ? command.assignTo() : existing.assignTo(),
                command.priority() > 0 ? command.priority() : existing.priority(),
                existing.status(), existing.createdAt(), Instant.now()));
    }

    public void deleteEmailRule(String tenantId, String ruleId) {
        crmStore.findEmailRule(tenantId, ruleId)
                .orElseThrow(() -> new BizException("EMAIL_RULE_NOT_FOUND", "邮件分配规则不存在"));
        crmStore.removeEmailRule(ruleId);
    }

    public List<EmailRule> listEmailRules(String tenantId) {
        return crmStore.listEmailRules(tenantId);
    }

    public String assignMessageByRules(String tenantId, Message message) {
        List<EmailRule> rules = crmStore.listEmailRules(tenantId);
        for (EmailRule rule : rules) {
            if (!"active".equals(rule.status())) continue;
            Map<String, Object> conditions = rule.conditions();
            if (conditions == null) continue;
            boolean matched = true;
            if (conditions.containsKey("platform") && !conditions.get("platform").equals(message.platform())) {
                matched = false;
            }
            if (conditions.containsKey("subjectContains") && message.subject() != null) {
                String keyword = (String) conditions.get("subjectContains");
                if (!message.subject().toLowerCase().contains(keyword.toLowerCase())) {
                    matched = false;
                }
            }
            if (matched) {
                return rule.assignTo();
            }
        }
        return null;
    }

    public VacationAutoReply createVacationAutoReply(String tenantId, CreateVacationReplyCommand command) {
        Instant now = Instant.now();
        return crmStore.saveVacationAutoReply(new VacationAutoReply(UUID.randomUUID().toString(), tenantId,
                command.startDate(), command.endDate(), command.replyContent(), command.language(),
                "active", now, now));
    }

    public List<VacationAutoReply> listVacationAutoReplies(String tenantId) {
        return crmStore.listVacationAutoReplies(tenantId);
    }

    public VacationAutoReply updateVacationAutoReply(String tenantId, String vacationId, UpdateVacationReplyCommand command) {
        VacationAutoReply existing = crmStore.findVacationAutoReply(tenantId, vacationId)
                .orElseThrow(() -> new BizException("VACATION_REPLY_NOT_FOUND", "假期自动回复不存在"));
        return crmStore.saveVacationAutoReply(new VacationAutoReply(existing.vacationId(), tenantId,
                command.startDate() != null ? command.startDate() : existing.startDate(),
                command.endDate() != null ? command.endDate() : existing.endDate(),
                command.replyContent() != null ? command.replyContent() : existing.replyContent(),
                command.language() != null ? command.language() : existing.language(),
                existing.status(), existing.createdAt(), Instant.now()));
    }

    public ReviewRequest createReviewRequest(String tenantId, CreateReviewRequestCommand command) {
        Instant now = Instant.now();
        return crmStore.saveReviewRequest(new ReviewRequest(UUID.randomUUID().toString(), tenantId,
                command.orderId(), command.platform(), command.excludeRefunded(), command.excludeNegative(),
                "pending", now, now));
    }

    public List<ReviewRequest> listReviewRequests(String tenantId) {
        return crmStore.listReviewRequests(tenantId);
    }

    public EmailCampaign createEmailCampaign(String tenantId, CreateEmailCampaignCommand command) {
        Instant now = Instant.now();
        return crmStore.saveEmailCampaign(new EmailCampaign(UUID.randomUUID().toString(), tenantId,
                command.name(), command.subject(), command.content(), command.targetSegment(),
                "draft", null, now, now));
    }

    public List<EmailCampaign> listEmailCampaigns(String tenantId) {
        return crmStore.listEmailCampaigns(tenantId);
    }

    public EmailCampaign sendEmailCampaign(String tenantId, String campaignId) {
        EmailCampaign campaign = crmStore.findEmailCampaign(tenantId, campaignId)
                .orElseThrow(() -> new BizException("EMAIL_CAMPAIGN_NOT_FOUND", "邮件营销活动不存在"));
        if (!"draft".equals(campaign.status())) {
            throw new BizException("CAMPAIGN_STATUS_INVALID", "只有草稿状态的营销活动可以发送");
        }
        return crmStore.saveEmailCampaign(new EmailCampaign(campaign.campaignId(), campaign.tenantId(),
                campaign.name(), campaign.subject(), campaign.content(), campaign.targetSegment(),
                "sent", Instant.now(), campaign.createdAt(), Instant.now()));
    }

    public QualityIssue createQualityIssue(String tenantId, CreateQualityIssueCommand command) {
        Instant now = Instant.now();
        return crmStore.saveQualityIssue(new QualityIssue(UUID.randomUUID().toString(), tenantId,
                command.productId(), command.source(), command.description(), command.severity(),
                "open", now, now));
    }

    public List<QualityIssue> listQualityIssues(String tenantId) {
        return crmStore.listQualityIssues(tenantId);
    }

    public List<QualityIssue> listQualityIssuesByProduct(String tenantId, String productId) {
        return crmStore.listQualityIssuesByProduct(tenantId, productId);
    }

    public QualityIssue resolveQualityIssue(String tenantId, String issueId) {
        QualityIssue issue = crmStore.listQualityIssues(tenantId).stream()
                .filter(i -> i.issueId().equals(issueId))
                .findFirst()
                .orElseThrow(() -> new BizException("QUALITY_ISSUE_NOT_FOUND", "质量问题不存在"));
        Instant now = Instant.now();
        return crmStore.saveQualityIssue(new QualityIssue(issue.issueId(), issue.tenantId(),
                issue.productId(), issue.source(), issue.description(), issue.severity(),
                "resolved", issue.createdAt(), now));
    }

    public record CreateEmailRuleCommand(Map<String, Object> conditions, String assignTo, int priority) {}
    public record UpdateEmailRuleCommand(Map<String, Object> conditions, String assignTo, int priority) {}
    public record CreateVacationReplyCommand(Instant startDate, Instant endDate, String replyContent, String language) {}
    public record UpdateVacationReplyCommand(Instant startDate, Instant endDate, String replyContent, String language) {}
    public record CreateReviewRequestCommand(String orderId, String platform, boolean excludeRefunded, boolean excludeNegative) {}
    public record CreateEmailCampaignCommand(String name, String subject, String content, String targetSegment) {}
    public record CreateQualityIssueCommand(String productId, String source, String description, String severity) {}
}
