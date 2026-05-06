package com.aidotnet.erp.crm.infrastructure;

import com.aidotnet.erp.crm.domain.Customer;
import com.aidotnet.erp.crm.domain.CustomerTag;
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
import com.aidotnet.erp.crm.infrastructure.data.CustomerDO;
import com.aidotnet.erp.crm.infrastructure.data.MessageDO;
import com.aidotnet.erp.crm.infrastructure.data.ReturnRefundDO;
import com.aidotnet.erp.crm.infrastructure.data.ReviewAnalysisDO;
import com.aidotnet.erp.crm.infrastructure.data.ReviewDO;
import com.aidotnet.erp.crm.infrastructure.data.ServiceTicketDO;
import com.aidotnet.erp.crm.infrastructure.mapper.CrmMapper;
import com.aidotnet.erp.crm.infrastructure.mapper.CrmExtMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/**
 * CRM核心数据存储
 * <p>
 * 描述: CRM域核心数据持久化层，负责客户、工单、退货退款、评价分析、
 *       评价、消息等核心业务对象与数据库之间的转换和持久化操作。
 *       邮件规则、假期自动回复、评价请求、邮件营销、质量问题等
 *       使用内存ConcurrentHashMap暂存，后续迁移至数据库。
 * </p>
 * <p>
 * 数据转换: 领域模型(Domain) ↔ 数据对象(DO)，通过手动映射实现
 * 标签序列化: CustomerTag列表使用Jackson序列化为JSON字符串存储
 * </p>
 *
 * @author ERP系统
 * @see CrmMapper
 * @see CrmExtMapper
 */
@Repository
public class CrmStore {

    private final CrmMapper mapper;
    private final CrmExtMapper extMapper;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, EmailRule> emailRuleStore = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, VacationAutoReply> vacationReplyStore = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ReviewRequest> reviewRequestStore = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, EmailCampaign> emailCampaignStore = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, QualityIssue> qualityIssueStore = new ConcurrentHashMap<>();

    public CrmStore(CrmMapper mapper, CrmExtMapper extMapper, ObjectMapper objectMapper) {
        this.mapper = mapper;
        this.extMapper = extMapper;
        this.objectMapper = objectMapper;
    }

    public Customer saveCustomer(Customer customer) {
        CustomerDO existing = mapper.selectCustomer(customer.tenantId(), customer.customerId());
        CustomerDO data = toCustomerData(customer);
        if (existing == null) {
            mapper.insertCustomer(data);
        }
        return customer;
    }

    public Optional<Customer> findCustomer(String tenantId, String customerId) {
        return Optional.ofNullable(mapper.selectCustomer(tenantId, customerId))
                .map(this::toCustomerDomain);
    }

    public List<Customer> listCustomers(String tenantId) {
        return mapper.selectCustomers(tenantId).stream()
                .map(this::toCustomerDomain).collect(Collectors.toList());
    }

    public ServiceTicket saveTicket(ServiceTicket ticket) {
        ServiceTicketDO existing = mapper.selectTicket(ticket.tenantId(), ticket.ticketId());
        ServiceTicketDO data = toTicketData(ticket);
        if (existing == null) {
            mapper.insertTicket(data);
        } else {
            mapper.updateTicket(data);
        }
        return ticket;
    }

    public Optional<ServiceTicket> findTicket(String tenantId, String ticketId) {
        return Optional.ofNullable(mapper.selectTicket(tenantId, ticketId))
                .map(this::toTicketDomain);
    }

    public List<ServiceTicket> listTickets(String tenantId) {
        return mapper.selectTickets(tenantId).stream()
                .map(this::toTicketDomain).collect(Collectors.toList());
    }

    public ReturnRefund saveReturn(ReturnRefund r) {
        ReturnRefundDO existing = mapper.selectReturn(r.tenantId(), r.returnId());
        ReturnRefundDO data = toReturnData(r);
        if (existing == null) {
            mapper.insertReturn(data);
        } else {
            mapper.updateReturn(data);
        }
        return r;
    }

    public Optional<ReturnRefund> findReturn(String tenantId, String returnId) {
        return Optional.ofNullable(mapper.selectReturn(tenantId, returnId))
                .map(this::toReturnDomain);
    }

    public List<ReturnRefund> listReturns(String tenantId) {
        return mapper.selectReturns(tenantId).stream()
                .map(this::toReturnDomain).collect(Collectors.toList());
    }

    public ReviewAnalysis saveReviewAnalysis(ReviewAnalysis analysis) {
        mapper.insertReviewAnalysis(toReviewData(analysis));
        return analysis;
    }

    public List<ReviewAnalysis> listReviewAnalyses(String tenantId) {
        return mapper.selectReviewAnalyses(tenantId).stream()
                .map(this::toReviewDomain).collect(Collectors.toList());
    }

    public List<ReviewAnalysis> listReviewAnalysesBySku(String tenantId, String sellerSku) {
        return mapper.selectReviewAnalysesBySku(tenantId, sellerSku).stream()
                .map(this::toReviewDomain).collect(Collectors.toList());
    }

    private CustomerDO toCustomerData(Customer c) {
        CustomerDO data = new CustomerDO();
        data.setCustomerId(c.customerId());
        data.setTenantId(c.tenantId());
        data.setName(c.name());
        data.setEmail(c.email());
        data.setPhone(c.phone());
        data.setCountryCode(c.countryCode());
        data.setPlatform(c.platform());
        data.setStoreId(c.storeId());
        data.setTotalOrders(c.totalOrders());
        data.setTotalSpent(c.totalSpent());
        data.setLastOrderAt(c.lastOrderAt());
        data.setTags(serializeTags(c.tags()));
        data.setCreatedAt(c.createdAt() != null ? c.createdAt() : Instant.now());
        data.setUpdatedAt(c.updatedAt() != null ? c.updatedAt() : Instant.now());
        return data;
    }

    private Customer toCustomerDomain(CustomerDO d) {
        return new Customer(d.getCustomerId(), d.getTenantId(), d.getName(), d.getEmail(), d.getPhone(),
                d.getCountryCode(), d.getPlatform(), d.getStoreId(),
                d.getTotalOrders() != null ? d.getTotalOrders() : 0,
                d.getTotalSpent() != null ? d.getTotalSpent() : BigDecimal.ZERO,
                d.getLastOrderAt(),
                deserializeTags(d.getTags()),
                d.getCreatedAt(), d.getUpdatedAt());
    }

    private String serializeTags(List<CustomerTag> tags) {
        if (tags == null || tags.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(tags);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private List<CustomerTag> deserializeTags(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json, new TypeReference<List<CustomerTag>>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }

    private ServiceTicketDO toTicketData(ServiceTicket t) {
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
        data.setUpdatedAt(Instant.now());
        return data;
    }

    private ServiceTicket toTicketDomain(ServiceTicketDO d) {
        return new ServiceTicket(d.getTicketId(), d.getTenantId(), d.getCustomerId(), d.getSubject(),
                d.getDescription(), d.getAssignee(), TicketStatus.valueOf(d.getStatus()),
                d.getResolution(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private ReturnRefundDO toReturnData(ReturnRefund r) {
        ReturnRefundDO data = new ReturnRefundDO();
        data.setReturnId(r.returnId());
        data.setTenantId(r.tenantId());
        data.setOrderId(r.orderId());
        data.setSellerSku(r.sellerSku());
        data.setCustomerId(r.customerId());
        data.setQuantity(r.quantity());
        data.setRefundAmount(r.refundAmount());
        data.setCurrency(r.currency());
        data.setReason(r.reason().name());
        data.setStatus(r.status().name());
        data.setCreatedAt(r.createdAt() != null ? r.createdAt() : Instant.now());
        data.setUpdatedAt(Instant.now());
        return data;
    }

    private ReturnRefund toReturnDomain(ReturnRefundDO d) {
        return new ReturnRefund(d.getReturnId(), d.getTenantId(), d.getOrderId(), d.getSellerSku(),
                d.getCustomerId(), d.getQuantity() != null ? d.getQuantity() : 0,
                d.getRefundAmount(), d.getCurrency(),
                ReturnRefund.ReturnReason.valueOf(d.getReason()),
                ReturnRefund.ReturnStatus.valueOf(d.getStatus()),
                d.getCreatedAt(), d.getUpdatedAt());
    }

    private ReviewAnalysisDO toReviewData(ReviewAnalysis a) {
        ReviewAnalysisDO data = new ReviewAnalysisDO();
        data.setAnalysisId(a.analysisId());
        data.setTenantId(a.tenantId());
        data.setSellerSku(a.sellerSku());
        data.setMarketplaceId(a.marketplaceId());
        data.setAverageRating(BigDecimal.valueOf(a.averageRating()));
        data.setTotalReviews(a.totalReviews());
        data.setPositiveCount(a.positiveCount());
        data.setNeutralCount(a.neutralCount());
        data.setNegativeCount(a.negativeCount());
        data.setSentimentSummary(a.sentimentSummary());
        data.setAnalyzedAt(a.analyzedAt() != null ? a.analyzedAt() : Instant.now());
        return data;
    }

    private ReviewAnalysis toReviewDomain(ReviewAnalysisDO d) {
        return new ReviewAnalysis(d.getAnalysisId(), d.getTenantId(), d.getSellerSku(), d.getMarketplaceId(),
                d.getAverageRating() != null ? d.getAverageRating().doubleValue() : 0.0,
                d.getTotalReviews() != null ? d.getTotalReviews() : 0,
                d.getPositiveCount() != null ? d.getPositiveCount() : 0,
                d.getNeutralCount() != null ? d.getNeutralCount() : 0,
                d.getNegativeCount() != null ? d.getNegativeCount() : 0,
                d.getSentimentSummary(), d.getAnalyzedAt());
    }

    public Review saveReview(Review review) {
        ReviewDO existing = extMapper.selectReview(review.tenantId(), review.reviewId());
        ReviewDO data = toReviewData(review);
        if (existing == null) {
            extMapper.insertReview(data);
        } else {
            extMapper.updateReview(data);
        }
        return review;
    }

    public Optional<Review> findReview(String tenantId, String reviewId) {
        return Optional.ofNullable(extMapper.selectReview(tenantId, reviewId)).map(this::toReviewDomain);
    }

    public List<Review> listReviews(String tenantId) {
        return extMapper.selectReviews(tenantId).stream().map(this::toReviewDomain).collect(Collectors.toList());
    }

    public List<Review> listReviewsByCustomer(String tenantId, String customerId) {
        return extMapper.selectReviewsByCustomer(tenantId, customerId).stream().map(this::toReviewDomain).collect(Collectors.toList());
    }

    public List<Review> listNegativeReviews(String tenantId) {
        return extMapper.selectNegativeReviews(tenantId).stream().map(this::toReviewDomain).collect(Collectors.toList());
    }

    private ReviewDO toReviewData(Review r) {
        ReviewDO data = new ReviewDO();
        data.setReviewId(r.reviewId());
        data.setTenantId(r.tenantId());
        data.setCustomerId(r.customerId());
        data.setProductId(r.productId());
        data.setOrderId(r.orderId());
        data.setPlatform(r.platform());
        data.setRating(r.rating());
        data.setTitle(r.title());
        data.setText(r.text());
        data.setSentiment(r.sentiment());
        data.setVerified(r.verified());
        data.setResponded(r.responded());
        data.setReviewDate(r.reviewDate());
        data.setCreatedAt(r.createdAt() != null ? r.createdAt() : Instant.now());
        data.setUpdatedAt(r.updatedAt() != null ? r.updatedAt() : Instant.now());
        return data;
    }

    private Review toReviewDomain(ReviewDO d) {
        return new Review(d.getReviewId(), d.getTenantId(), d.getCustomerId(), d.getProductId(), d.getOrderId(),
                d.getPlatform(), d.getRating(), d.getTitle(), d.getText(),
                d.getSentiment(), d.getVerified() != null && d.getVerified(),
                d.getResponded() != null && d.getResponded(), d.getReviewDate(), d.getCreatedAt(), d.getUpdatedAt());
    }

    public Message saveMessage(Message message) {
        MessageDO existing = extMapper.selectMessage(message.tenantId(), message.messageId());
        MessageDO data = toMessageData(message);
        if (existing == null) {
            extMapper.insertMessage(data);
        } else {
            extMapper.updateMessage(data);
        }
        return message;
    }

    public Optional<Message> findMessage(String tenantId, String messageId) {
        return Optional.ofNullable(extMapper.selectMessage(tenantId, messageId)).map(this::toMessageDomain);
    }

    public List<Message> listMessages(String tenantId, String customerId) {
        return extMapper.selectMessages(tenantId, customerId).stream().map(this::toMessageDomain).collect(Collectors.toList());
    }

    public List<Message> listUnreadMessages(String tenantId) {
        return extMapper.selectUnreadMessages(tenantId).stream().map(this::toMessageDomain).collect(Collectors.toList());
    }

    private MessageDO toMessageData(Message m) {
        MessageDO data = new MessageDO();
        data.setMessageId(m.messageId());
        data.setTenantId(m.tenantId());
        data.setPlatform(m.platform());
        data.setCustomerId(m.customerId());
        data.setSubject(m.subject());
        data.setBody(m.body());
        data.setDirection(m.direction().name());
        data.setAssignedTo(m.assignedTo());
        data.setStatus(m.status().name());
        data.setMessageDate(m.messageDate());
        data.setCreatedAt(m.createdAt() != null ? m.createdAt() : Instant.now());
        data.setUpdatedAt(m.updatedAt() != null ? m.updatedAt() : Instant.now());
        return data;
    }

    private Message toMessageDomain(MessageDO d) {
        return new Message(d.getMessageId(), d.getTenantId(), d.getPlatform(), d.getCustomerId(), d.getSubject(),
                d.getBody(), MessageDirection.valueOf(d.getDirection()), d.getAssignedTo(),
                MessageStatus.valueOf(d.getStatus()), d.getMessageDate(), d.getCreatedAt(), d.getUpdatedAt());
    }

    public EmailRule saveEmailRule(EmailRule rule) {
        emailRuleStore.put(rule.ruleId(), rule);
        return rule;
    }

    public List<EmailRule> listEmailRules(String tenantId) {
        return emailRuleStore.values().stream()
                .filter(r -> r.tenantId().equals(tenantId))
                .sorted((a, b) -> Integer.compare(b.priority(), a.priority()))
                .collect(Collectors.toList());
    }

    public Optional<EmailRule> findEmailRule(String tenantId, String ruleId) {
        return Optional.ofNullable(emailRuleStore.get(ruleId))
                .filter(r -> r.tenantId().equals(tenantId));
    }

    public void removeEmailRule(String ruleId) {
        emailRuleStore.remove(ruleId);
    }

    public VacationAutoReply saveVacationAutoReply(VacationAutoReply reply) {
        vacationReplyStore.put(reply.vacationId(), reply);
        return reply;
    }

    public List<VacationAutoReply> listVacationAutoReplies(String tenantId) {
        return vacationReplyStore.values().stream()
                .filter(r -> r.tenantId().equals(tenantId))
                .collect(Collectors.toList());
    }

    public Optional<VacationAutoReply> findVacationAutoReply(String tenantId, String vacationId) {
        return Optional.ofNullable(vacationReplyStore.get(vacationId))
                .filter(r -> r.tenantId().equals(tenantId));
    }

    public ReviewRequest saveReviewRequest(ReviewRequest request) {
        reviewRequestStore.put(request.requestId(), request);
        return request;
    }

    public List<ReviewRequest> listReviewRequests(String tenantId) {
        return reviewRequestStore.values().stream()
                .filter(r -> r.tenantId().equals(tenantId))
                .collect(Collectors.toList());
    }

    public EmailCampaign saveEmailCampaign(EmailCampaign campaign) {
        emailCampaignStore.put(campaign.campaignId(), campaign);
        return campaign;
    }

    public List<EmailCampaign> listEmailCampaigns(String tenantId) {
        return emailCampaignStore.values().stream()
                .filter(c -> c.tenantId().equals(tenantId))
                .collect(Collectors.toList());
    }

    public Optional<EmailCampaign> findEmailCampaign(String tenantId, String campaignId) {
        return Optional.ofNullable(emailCampaignStore.get(campaignId))
                .filter(c -> c.tenantId().equals(tenantId));
    }

    public QualityIssue saveQualityIssue(QualityIssue issue) {
        qualityIssueStore.put(issue.issueId(), issue);
        return issue;
    }

    public List<QualityIssue> listQualityIssues(String tenantId) {
        return qualityIssueStore.values().stream()
                .filter(i -> i.tenantId().equals(tenantId))
                .collect(Collectors.toList());
    }

    public List<QualityIssue> listQualityIssuesByProduct(String tenantId, String productId) {
        return qualityIssueStore.values().stream()
                .filter(i -> i.tenantId().equals(tenantId) && i.productId().equals(productId))
                .collect(Collectors.toList());
    }
}
