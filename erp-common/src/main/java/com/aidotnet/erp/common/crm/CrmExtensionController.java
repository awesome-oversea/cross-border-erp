package com.aidotnet.erp.common.crm;

import com.aidotnet.erp.common.api.Result;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/platform/crm/api/v1")
public class CrmExtensionController {

    private final CrmExtensionService crmExtService;

    public CrmExtensionController(CrmExtensionService crmExtService) {
        this.crmExtService = crmExtService;
    }

    @PostMapping("/customers")
    public Result<CrmExtensionService.Customer> createCustomer(@RequestBody Map<String, Object> request) {
        return Result.ok(crmExtService.createCustomer(
                (String) request.get("tenantId"),
                (String) request.get("customerName"),
                (String) request.getOrDefault("email", ""),
                (String) request.getOrDefault("phone", ""),
                (String) request.getOrDefault("platform", ""),
                (String) request.getOrDefault("countryCode", ""),
                (String) request.getOrDefault("tags", "")
        ));
    }

    @PutMapping("/customers/{customerId}")
    public Result<CrmExtensionService.Customer> updateCustomer(
            @PathVariable String customerId, @RequestBody Map<String, Object> request) {
        return Result.ok(crmExtService.updateCustomer(
                customerId,
                (String) request.get("customerName"),
                (String) request.get("email"),
                (String) request.get("phone"),
                (String) request.get("tags")
        ));
    }

    @GetMapping("/customers")
    public Result<List<CrmExtensionService.Customer>> listCustomers(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String platform) {
        return Result.ok(crmExtService.listCustomers(tenantId, platform));
    }

    @PostMapping("/reviews")
    public Result<CrmExtensionService.Review> createReview(@RequestBody Map<String, Object> request) {
        return Result.ok(crmExtService.createReview(
                (String) request.get("tenantId"),
                (String) request.getOrDefault("orderId", ""),
                (String) request.getOrDefault("customerId", ""),
                (String) request.get("sku"),
                (String) request.getOrDefault("platform", ""),
                ((Number) request.getOrDefault("rating", 3)).intValue(),
                (String) request.getOrDefault("title", ""),
                (String) request.getOrDefault("content", ""),
                (String) request.getOrDefault("sentiment", "NEUTRAL")
        ));
    }

    @PatchMapping("/reviews/{reviewId}/status")
    public Result<CrmExtensionService.Review> updateReviewStatus(
            @PathVariable String reviewId, @RequestBody Map<String, Object> request) {
        return Result.ok(crmExtService.updateReviewStatus(reviewId, (String) request.get("status")));
    }

    @GetMapping("/reviews")
    public Result<List<CrmExtensionService.Review>> listReviews(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String sku,
            @RequestParam(required = false) String sentiment) {
        return Result.ok(crmExtService.listReviews(tenantId, sku, sentiment));
    }

    @PostMapping("/complaints")
    public Result<CrmExtensionService.Complaint> createComplaint(@RequestBody Map<String, Object> request) {
        return Result.ok(crmExtService.createComplaint(
                (String) request.get("tenantId"),
                (String) request.getOrDefault("orderId", ""),
                (String) request.getOrDefault("customerId", ""),
                (String) request.getOrDefault("type", "GENERAL"),
                (String) request.getOrDefault("priority", "MEDIUM"),
                (String) request.get("subject"),
                (String) request.getOrDefault("description", "")
        ));
    }

    @PatchMapping("/complaints/{complaintId}/status")
    public Result<CrmExtensionService.Complaint> updateComplaintStatus(
            @PathVariable String complaintId, @RequestBody Map<String, Object> request) {
        return Result.ok(crmExtService.updateComplaintStatus(
                complaintId, (String) request.get("status"), (String) request.getOrDefault("resolution", "")));
    }

    @GetMapping("/complaints")
    public Result<List<CrmExtensionService.Complaint>> listComplaints(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        return Result.ok(crmExtService.listComplaints(tenantId, type, status));
    }

    @PostMapping("/returns")
    public Result<CrmExtensionService.ReturnRequest> createReturnRequest(@RequestBody Map<String, Object> request) {
        BigDecimal refundAmount = request.containsKey("refundAmount") ? new BigDecimal(request.get("refundAmount").toString()) : BigDecimal.ZERO;
        return Result.ok(crmExtService.createReturnRequest(
                (String) request.get("tenantId"),
                (String) request.getOrDefault("orderId", ""),
                (String) request.getOrDefault("customerId", ""),
                (String) request.get("sku"),
                ((Number) request.getOrDefault("quantity", 1)).intValue(),
                (String) request.getOrDefault("reason", ""),
                (String) request.getOrDefault("returnType", "REFUND"),
                refundAmount
        ));
    }

    @PatchMapping("/returns/{requestId}/status")
    public Result<CrmExtensionService.ReturnRequest> updateReturnStatus(
            @PathVariable String requestId, @RequestBody Map<String, Object> request) {
        return Result.ok(crmExtService.updateReturnStatus(requestId, (String) request.get("status")));
    }

    @GetMapping("/returns")
    public Result<List<CrmExtensionService.ReturnRequest>> listReturnRequests(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String status) {
        return Result.ok(crmExtService.listReturnRequests(tenantId, status));
    }

    @PostMapping("/emails")
    public Result<CrmExtensionService.EmailMessage> sendEmail(@RequestBody Map<String, Object> request) {
        return Result.ok(crmExtService.sendEmail(
                (String) request.get("tenantId"),
                (String) request.getOrDefault("customerId", ""),
                (String) request.getOrDefault("orderId", ""),
                (String) request.get("subject"),
                (String) request.getOrDefault("body", ""),
                (String) request.getOrDefault("direction", "OUTBOUND"),
                (String) request.getOrDefault("channel", "EMAIL")
        ));
    }

    @GetMapping("/emails")
    public Result<List<CrmExtensionService.EmailMessage>> listEmails(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String direction) {
        return Result.ok(crmExtService.listEmails(tenantId, customerId, direction));
    }
}
