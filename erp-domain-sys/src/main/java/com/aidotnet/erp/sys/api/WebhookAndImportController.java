package com.aidotnet.erp.sys.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.sys.application.ManualImportService;
import com.aidotnet.erp.sys.application.ManualImportService.CreateImportCommand;
import com.aidotnet.erp.sys.application.ManualImportService.ImportResult;
import com.aidotnet.erp.sys.application.WebhookService;
import com.aidotnet.erp.sys.application.WebhookService.CreateWebhookCommand;
import com.aidotnet.erp.sys.application.WebhookService.UpdateWebhookCommand;
import com.aidotnet.erp.sys.domain.ManualImportTask;
import com.aidotnet.erp.sys.domain.WebhookDelivery;
import com.aidotnet.erp.sys.domain.WebhookEndpoint;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sys/api/in/v1")
public class WebhookAndImportController {

    private final WebhookService webhookService;
    private final ManualImportService manualImportService;

    public WebhookAndImportController(WebhookService webhookService, ManualImportService manualImportService) {
        this.webhookService = webhookService;
        this.manualImportService = manualImportService;
    }

    @PostMapping("/webhooks")
    public Result<WebhookEndpoint> createWebhook(@Valid @RequestBody CreateWebhookRequest request) {
        return Result.ok(webhookService.createEndpoint(currentTenant(), new CreateWebhookCommand(
                request.name(), request.url(), request.eventType(), request.headers(),
                request.retryCount(), request.timeoutSeconds(), request.subscribedEvents())));
    }

    @PutMapping("/webhooks/{endpointId}")
    public Result<WebhookEndpoint> updateWebhook(@PathVariable String endpointId,
                                                  @Valid @RequestBody UpdateWebhookRequest request) {
        return Result.ok(webhookService.updateEndpoint(currentTenant(), endpointId, new UpdateWebhookCommand(
                request.name(), request.url(), request.eventType(), request.headers(),
                request.active(), request.subscribedEvents())));
    }

    @DeleteMapping("/webhooks/{endpointId}")
    public Result<Void> deleteWebhook(@PathVariable String endpointId) {
        webhookService.deleteEndpoint(currentTenant(), endpointId);
        return Result.ok(null);
    }

    @GetMapping("/webhooks")
    public Result<List<WebhookEndpoint>> listWebhooks() {
        return Result.ok(webhookService.listEndpoints(currentTenant()));
    }

    @GetMapping("/webhooks/{endpointId}")
    public Result<WebhookEndpoint> getWebhook(@PathVariable String endpointId) {
        return Result.ok(webhookService.getEndpoint(currentTenant(), endpointId));
    }

    @PostMapping("/webhooks/deliver")
    public Result<List<WebhookDelivery>> deliverWebhook(@Valid @RequestBody DeliverWebhookRequest request) {
        return Result.ok(webhookService.deliver(currentTenant(), request.eventType(), request.payload()));
    }

    @GetMapping("/webhooks/{endpointId}/deliveries")
    public Result<List<WebhookDelivery>> listDeliveries(@PathVariable String endpointId) {
        return Result.ok(webhookService.listDeliveries(currentTenant(), endpointId));
    }

    @PostMapping("/webhooks/deliveries/{deliveryId}/redeliver")
    public Result<WebhookDelivery> redeliver(@PathVariable String deliveryId) {
        return Result.ok(webhookService.redeliver(currentTenant(), deliveryId));
    }

    @PostMapping("/imports")
    public Result<ManualImportTask> createImportTask(@Valid @RequestBody CreateImportRequest request) {
        return Result.ok(manualImportService.createImportTask(currentTenant(), new CreateImportCommand(
                request.importType(), request.fileName(), request.fileSize(),
                request.columnMapping(), request.importedBy())));
    }

    @PostMapping("/imports/{taskId}/start")
    public Result<ManualImportTask> startImport(@PathVariable String taskId) {
        return Result.ok(manualImportService.startImport(currentTenant(), taskId));
    }

    @PostMapping("/imports/{taskId}/complete")
    public Result<ManualImportTask> completeImport(@PathVariable String taskId,
                                                    @Valid @RequestBody CompleteImportRequest request) {
        return Result.ok(manualImportService.completeImport(currentTenant(), taskId, new ImportResult(
                request.totalRows(), request.successRows(), request.failedRows(), request.errorReportUrl())));
    }

    @PostMapping("/imports/{taskId}/fail")
    public Result<ManualImportTask> failImport(@PathVariable String taskId, @RequestBody FailImportRequest request) {
        return Result.ok(manualImportService.failImport(currentTenant(), taskId, request.errorMessage()));
    }

    @GetMapping("/imports")
    public Result<List<ManualImportTask>> listImports(@RequestParam(required = false) String importType) {
        return Result.ok(manualImportService.listTasks(currentTenant(), importType));
    }

    @GetMapping("/imports/{taskId}")
    public Result<ManualImportTask> getImportTask(@PathVariable String taskId) {
        return Result.ok(manualImportService.getTask(currentTenant(), taskId));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record CreateWebhookRequest(@NotBlank String name, @NotBlank String url, String eventType,
                                       Map<String, String> headers, int retryCount, int timeoutSeconds,
                                       List<String> subscribedEvents) {}
    public record UpdateWebhookRequest(String name, String url, String eventType,
                                       Map<String, String> headers, Boolean active,
                                       List<String> subscribedEvents) {}
    public record DeliverWebhookRequest(@NotBlank String eventType, Map<String, Object> payload) {}
    public record CreateImportRequest(@NotBlank String importType, @NotBlank String fileName,
                                      long fileSize, Map<String, Object> columnMapping, String importedBy) {}
    public record CompleteImportRequest(int totalRows, int successRows, int failedRows, String errorReportUrl) {}
    public record FailImportRequest(String errorMessage) {}
}
