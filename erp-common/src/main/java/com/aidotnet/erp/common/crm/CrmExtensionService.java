package com.aidotnet.erp.common.crm;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CrmExtensionService {

    private static final Logger log = LoggerFactory.getLogger(CrmExtensionService.class);
    private final Map<String, Customer> customers = new ConcurrentHashMap<>();
    private final Map<String, Review> reviews = new ConcurrentHashMap<>();
    private final Map<String, Complaint> complaints = new ConcurrentHashMap<>();
    private final Map<String, ReturnRequest> returnRequests = new ConcurrentHashMap<>();
    private final Map<String, EmailMessage> emailMessages = new ConcurrentHashMap<>();

    public Customer createCustomer(String tenantId, String customerName, String email, String phone,
                                    String platform, String countryCode, String tags) {
        String customerId = "CUS-" + System.currentTimeMillis();
        Customer customer = new Customer(customerId, tenantId, customerName, email, phone,
                platform, countryCode, tags, Instant.now(), Instant.now());
        customers.put(customerId, customer);
        log.info("Created customer: id={}, name={}, platform={}", customerId, customerName, platform);
        return customer;
    }

    public Customer updateCustomer(String customerId, String customerName, String email,
                                    String phone, String tags) {
        Customer existing = customers.get(customerId);
        if (existing == null) throw new IllegalArgumentException("Customer not found: " + customerId);
        Customer updated = new Customer(customerId, existing.tenantId(),
                customerName != null ? customerName : existing.customerName(),
                email != null ? email : existing.email(),
                phone != null ? phone : existing.phone(),
                existing.platform(), existing.countryCode(),
                tags != null ? tags : existing.tags(),
                existing.createdAt(), Instant.now());
        customers.put(customerId, updated);
        log.info("Updated customer: id={}", customerId);
        return updated;
    }

    public List<Customer> listCustomers(String tenantId, String platform) {
        return customers.values().stream()
                .filter(c -> tenantId == null || tenantId.equals(c.tenantId()))
                .filter(c -> platform == null || platform.equals(c.platform()))
                .toList();
    }

    public Review createReview(String tenantId, String orderId, String customerId, String sku,
                                String platform, int rating, String title, String content,
                                String sentiment) {
        String reviewId = "REV-" + System.currentTimeMillis();
        Review review = new Review(reviewId, tenantId, orderId, customerId, sku,
                platform, rating, title, content, sentiment, "PENDING", Instant.now());
        reviews.put(reviewId, review);
        log.info("Created review: id={}, sku={}, rating={}, sentiment={}", reviewId, sku, rating, sentiment);
        return review;
    }

    public Review updateReviewStatus(String reviewId, String status) {
        Review existing = reviews.get(reviewId);
        if (existing == null) throw new IllegalArgumentException("Review not found: " + reviewId);
        Review updated = new Review(reviewId, existing.tenantId(), existing.orderId(),
                existing.customerId(), existing.sku(), existing.platform(),
                existing.rating(), existing.title(), existing.content(),
                existing.sentiment(), status, existing.createdAt());
        reviews.put(reviewId, updated);
        log.info("Updated review status: id={}, status={}", reviewId, status);
        return updated;
    }

    public List<Review> listReviews(String tenantId, String sku, String sentiment) {
        return reviews.values().stream()
                .filter(r -> tenantId == null || tenantId.equals(r.tenantId()))
                .filter(r -> sku == null || sku.equals(r.sku()))
                .filter(r -> sentiment == null || sentiment.equals(r.sentiment()))
                .toList();
    }

    public Complaint createComplaint(String tenantId, String orderId, String customerId,
                                      String type, String priority, String subject, String description) {
        String complaintId = "CMP-" + System.currentTimeMillis();
        Complaint complaint = new Complaint(complaintId, tenantId, orderId, customerId,
                type, priority, subject, description, "OPEN", Instant.now(), Instant.now());
        complaints.put(complaintId, complaint);
        log.info("Created complaint: id={}, type={}, priority={}", complaintId, type, priority);
        return complaint;
    }

    public Complaint updateComplaintStatus(String complaintId, String status, String resolution) {
        Complaint existing = complaints.get(complaintId);
        if (existing == null) throw new IllegalArgumentException("Complaint not found: " + complaintId);
        Complaint updated = new Complaint(complaintId, existing.tenantId(), existing.orderId(),
                existing.customerId(), existing.type(), existing.priority(),
                existing.subject(), existing.description(), status,
                existing.createdAt(), Instant.now());
        complaints.put(complaintId, updated);
        log.info("Updated complaint status: id={}, status={}", complaintId, status);
        return updated;
    }

    public List<Complaint> listComplaints(String tenantId, String type, String status) {
        return complaints.values().stream()
                .filter(c -> tenantId == null || tenantId.equals(c.tenantId()))
                .filter(c -> type == null || type.equals(c.type()))
                .filter(c -> status == null || status.equals(c.status()))
                .toList();
    }

    public ReturnRequest createReturnRequest(String tenantId, String orderId, String customerId,
                                               String sku, int quantity, String reason,
                                               String returnType, BigDecimal refundAmount) {
        String requestId = "RET-" + System.currentTimeMillis();
        ReturnRequest request = new ReturnRequest(requestId, tenantId, orderId, customerId,
                sku, quantity, reason, returnType, refundAmount, "PENDING", Instant.now(), Instant.now());
        returnRequests.put(requestId, request);
        log.info("Created return request: id={}, sku={}, type={}, amount={}", requestId, sku, returnType, refundAmount);
        return request;
    }

    public ReturnRequest updateReturnStatus(String requestId, String status) {
        ReturnRequest existing = returnRequests.get(requestId);
        if (existing == null) throw new IllegalArgumentException("Return request not found: " + requestId);
        ReturnRequest updated = new ReturnRequest(requestId, existing.tenantId(), existing.orderId(),
                existing.customerId(), existing.sku(), existing.quantity(),
                existing.reason(), existing.returnType(), existing.refundAmount(),
                status, existing.createdAt(), Instant.now());
        returnRequests.put(requestId, updated);
        log.info("Updated return request status: id={}, status={}", requestId, status);
        return updated;
    }

    public List<ReturnRequest> listReturnRequests(String tenantId, String status) {
        return returnRequests.values().stream()
                .filter(r -> tenantId == null || tenantId.equals(r.tenantId()))
                .filter(r -> status == null || status.equals(r.status()))
                .toList();
    }

    public EmailMessage sendEmail(String tenantId, String customerId, String orderId,
                                   String subject, String body, String direction, String channel) {
        String emailId = "EMAIL-" + System.currentTimeMillis();
        EmailMessage email = new EmailMessage(emailId, tenantId, customerId, orderId,
                subject, body, direction, channel, "SENT", Instant.now());
        emailMessages.put(emailId, email);
        log.info("Sent email: id={}, direction={}, channel={}", emailId, direction, channel);
        return email;
    }

    public List<EmailMessage> listEmails(String tenantId, String customerId, String direction) {
        return emailMessages.values().stream()
                .filter(e -> tenantId == null || tenantId.equals(e.tenantId()))
                .filter(e -> customerId == null || customerId.equals(e.customerId()))
                .filter(e -> direction == null || direction.equals(e.direction()))
                .toList();
    }

    public record Customer(String customerId, String tenantId, String customerName, String email,
                            String phone, String platform, String countryCode, String tags,
                            Instant createdAt, Instant updatedAt) {}
    public record Review(String reviewId, String tenantId, String orderId, String customerId,
                          String sku, String platform, int rating, String title, String content,
                          String sentiment, String status, Instant createdAt) {}
    public record Complaint(String complaintId, String tenantId, String orderId, String customerId,
                             String type, String priority, String subject, String description,
                             String status, Instant createdAt, Instant updatedAt) {}
    public record ReturnRequest(String requestId, String tenantId, String orderId, String customerId,
                                 String sku, int quantity, String reason, String returnType,
                                 BigDecimal refundAmount, String status, Instant createdAt, Instant updatedAt) {}
    public record EmailMessage(String emailId, String tenantId, String customerId, String orderId,
                                String subject, String body, String direction, String channel,
                                String status, Instant sentAt) {}
}
