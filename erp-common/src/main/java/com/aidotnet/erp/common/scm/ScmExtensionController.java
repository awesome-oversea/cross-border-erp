package com.aidotnet.erp.common.scm;

import com.aidotnet.erp.common.api.Result;
import java.math.BigDecimal;
import java.time.Instant;
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
@RequestMapping("/platform/scm/api/v1")
public class ScmExtensionController {

    private final ScmExtensionService scmExtService;

    public ScmExtensionController(ScmExtensionService scmExtService) {
        this.scmExtService = scmExtService;
    }

    @PostMapping("/suppliers")
    public Result<ScmExtensionService.Supplier> createSupplier(@RequestBody Map<String, Object> request) {
        return Result.ok(scmExtService.createSupplier(
                (String) request.get("tenantId"),
                (String) request.get("supplierName"),
                (String) request.getOrDefault("contactPerson", ""),
                (String) request.getOrDefault("phone", ""),
                (String) request.getOrDefault("email", ""),
                (String) request.getOrDefault("address", ""),
                (String) request.getOrDefault("category", "GENERAL")
        ));
    }

    @PutMapping("/suppliers/{supplierId}")
    public Result<ScmExtensionService.Supplier> updateSupplier(
            @PathVariable String supplierId, @RequestBody Map<String, Object> request) {
        return Result.ok(scmExtService.updateSupplier(
                supplierId,
                (String) request.get("supplierName"),
                (String) request.get("contactPerson"),
                (String) request.get("phone"),
                (String) request.get("email"),
                request.containsKey("active") && (Boolean) request.get("active")
        ));
    }

    @GetMapping("/suppliers")
    public Result<List<ScmExtensionService.Supplier>> listSuppliers(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String category) {
        return Result.ok(scmExtService.listSuppliers(tenantId, category));
    }

    @PostMapping("/quotes")
    public Result<ScmExtensionService.Quote> createQuote(@RequestBody Map<String, Object> request) {
        return Result.ok(scmExtService.createQuote(
                (String) request.get("tenantId"),
                (String) request.get("supplierId"),
                (String) request.get("sku"),
                new BigDecimal(request.get("quantity").toString()),
                new BigDecimal(request.get("unitPrice").toString()),
                (String) request.getOrDefault("currency", "CNY"),
                (String) request.getOrDefault("validUntil", ""),
                (String) request.getOrDefault("requester", "")
        ));
    }

    @PatchMapping("/quotes/{quoteId}/respond")
    public Result<ScmExtensionService.Quote> respondQuote(
            @PathVariable String quoteId, @RequestBody Map<String, Object> request) {
        BigDecimal unitPrice = request.containsKey("unitPrice") ? new BigDecimal(request.get("unitPrice").toString()) : null;
        return Result.ok(scmExtService.respondQuote(quoteId, unitPrice, (String) request.get("status")));
    }

    @GetMapping("/quotes")
    public Result<List<ScmExtensionService.Quote>> listQuotes(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String supplierId) {
        return Result.ok(scmExtService.listQuotes(tenantId, supplierId));
    }

    @PostMapping("/supplier-scores")
    public Result<ScmExtensionService.SupplierScore> scoreSupplier(@RequestBody Map<String, Object> request) {
        return Result.ok(scmExtService.scoreSupplier(
                (String) request.get("tenantId"),
                (String) request.get("supplierId"),
                new BigDecimal(request.get("qualityScore").toString()),
                new BigDecimal(request.get("deliveryScore").toString()),
                new BigDecimal(request.get("priceScore").toString()),
                new BigDecimal(request.get("serviceScore").toString())
        ));
    }

    @GetMapping("/supplier-scores")
    public Result<List<ScmExtensionService.SupplierScore>> listSupplierScores(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String supplierId) {
        return Result.ok(scmExtService.listSupplierScores(tenantId, supplierId));
    }

    @PostMapping("/processing-orders")
    public Result<ScmExtensionService.ProcessingOrder> createProcessingOrder(@RequestBody Map<String, Object> request) {
        Instant dueDate = request.containsKey("dueDate") ? Instant.parse((String) request.get("dueDate")) : null;
        return Result.ok(scmExtService.createProcessingOrder(
                (String) request.get("tenantId"),
                (String) request.get("supplierId"),
                (String) request.get("sku"),
                new BigDecimal(request.get("quantity").toString()),
                (String) request.getOrDefault("processingType", "CUSTOM"),
                (String) request.getOrDefault("description", ""),
                dueDate
        ));
    }

    @PatchMapping("/processing-orders/{orderId}/status")
    public Result<ScmExtensionService.ProcessingOrder> updateProcessingOrderStatus(
            @PathVariable String orderId, @RequestBody Map<String, Object> request) {
        return Result.ok(scmExtService.updateProcessingOrderStatus(orderId, (String) request.get("status")));
    }

    @GetMapping("/processing-orders")
    public Result<List<ScmExtensionService.ProcessingOrder>> listProcessingOrders(
            @RequestParam(required = false) String tenantId,
            @RequestParam(required = false) String supplierId) {
        return Result.ok(scmExtService.listProcessingOrders(tenantId, supplierId));
    }
}
