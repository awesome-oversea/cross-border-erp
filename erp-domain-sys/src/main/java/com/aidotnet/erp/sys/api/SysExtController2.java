package com.aidotnet.erp.sys.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.sys.application.BusinessAlertService;
import com.aidotnet.erp.sys.application.BusinessAlertService.CreateAlertCommand;
import com.aidotnet.erp.sys.application.DomainEventCatalogService;
import com.aidotnet.erp.sys.application.DomainEventCatalogService.RegisterEventCommand;
import com.aidotnet.erp.sys.application.DomainEventCatalogService.UpdateEventCommand;
import com.aidotnet.erp.sys.application.MasterDataInitService;
import com.aidotnet.erp.sys.application.PrintTemplateService;
import com.aidotnet.erp.sys.application.PrintTemplateService.CreatePrintTemplateCommand;
import com.aidotnet.erp.sys.application.PrintTemplateService.UpdatePrintTemplateCommand;
import com.aidotnet.erp.sys.domain.BusinessAlert;
import com.aidotnet.erp.sys.domain.DomainEventCatalog;
import com.aidotnet.erp.sys.domain.PrintTemplate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
@RequestMapping("/sys/api/in/v1")
public class SysExtController2 {

    private final PrintTemplateService printTemplateService;
    private final DomainEventCatalogService eventCatalogService;
    private final BusinessAlertService alertService;
    private final MasterDataInitService masterDataInitService;

    public SysExtController2(PrintTemplateService printTemplateService,
                             DomainEventCatalogService eventCatalogService,
                             BusinessAlertService alertService,
                             MasterDataInitService masterDataInitService) {
        this.printTemplateService = printTemplateService;
        this.eventCatalogService = eventCatalogService;
        this.alertService = alertService;
        this.masterDataInitService = masterDataInitService;
    }

    @PostMapping("/print-templates")
    public Result<PrintTemplate> createPrintTemplate(@Valid @RequestBody CreatePrintTemplateRequest request) {
        return Result.ok(printTemplateService.createTemplate(currentTenant(), new CreatePrintTemplateCommand(
                request.templateCode(), request.templateName(), request.templateType(),
                request.content(), request.paperSize(), request.orientation(),
                request.variables(), request.description())));
    }

    @PutMapping("/print-templates/{templateId}")
    public Result<PrintTemplate> updatePrintTemplate(@PathVariable String templateId,
                                                     @Valid @RequestBody UpdatePrintTemplateRequest request) {
        return Result.ok(printTemplateService.updateTemplate(currentTenant(), templateId, new UpdatePrintTemplateCommand(
                request.templateName(), request.templateType(), request.content(),
                request.paperSize(), request.orientation(), request.variables(), request.description())));
    }

    @PatchMapping("/print-templates/{templateId}/toggle")
    public Result<PrintTemplate> togglePrintTemplate(@PathVariable String templateId, @RequestParam boolean enabled) {
        return Result.ok(printTemplateService.toggleTemplate(currentTenant(), templateId, enabled));
    }

    @GetMapping("/print-templates")
    public Result<List<PrintTemplate>> listPrintTemplates(@RequestParam(required = false) String templateType) {
        return Result.ok(printTemplateService.listTemplates(currentTenant(), templateType));
    }

    @GetMapping("/print-templates/{templateId}")
    public Result<PrintTemplate> getPrintTemplate(@PathVariable String templateId) {
        return Result.ok(printTemplateService.getTemplate(currentTenant(), templateId));
    }

    @PostMapping("/print-templates/render")
    public Result<String> renderPrintTemplate(@Valid @RequestBody RenderTemplateRequest request) {
        return Result.ok(printTemplateService.renderTemplate(currentTenant(), request.templateCode(), request.params()));
    }

    @PostMapping("/event-catalog")
    public Result<DomainEventCatalog> registerEvent(@Valid @RequestBody RegisterEventRequest request) {
        return Result.ok(eventCatalogService.registerEvent(currentTenant(), new RegisterEventCommand(
                request.eventCode(), request.eventName(), request.domain(),
                request.aggregateType(), request.eventType(), request.description(),
                request.payloadSchema(), request.subscribers())));
    }

    @PutMapping("/event-catalog/{eventId}")
    public Result<DomainEventCatalog> updateEvent(@PathVariable String eventId,
                                                  @Valid @RequestBody UpdateEventRequest request) {
        return Result.ok(eventCatalogService.updateEvent(currentTenant(), eventId, new UpdateEventCommand(
                request.eventName(), request.description(), request.payloadSchema(), request.subscribers())));
    }

    @PostMapping("/event-catalog/{eventCode}/subscribe")
    public Result<DomainEventCatalog> subscribeEvent(@PathVariable String eventCode,
                                                     @Valid @RequestBody SubscribeRequest request) {
        return Result.ok(eventCatalogService.subscribe(currentTenant(), eventCode, request.subscriber()));
    }

    @PostMapping("/event-catalog/{eventCode}/unsubscribe")
    public Result<DomainEventCatalog> unsubscribeEvent(@PathVariable String eventCode,
                                                       @Valid @RequestBody SubscribeRequest request) {
        return Result.ok(eventCatalogService.unsubscribe(currentTenant(), eventCode, request.subscriber()));
    }

    @GetMapping("/event-catalog")
    public Result<List<DomainEventCatalog>> listEvents(@RequestParam(required = false) String domain) {
        return Result.ok(eventCatalogService.listEvents(currentTenant(), domain));
    }

    @GetMapping("/event-catalog/{eventId}")
    public Result<DomainEventCatalog> getEvent(@PathVariable String eventId) {
        return Result.ok(eventCatalogService.getEvent(currentTenant(), eventId));
    }

    @PostMapping("/alerts")
    public Result<BusinessAlert> createAlert(@Valid @RequestBody CreateAlertRequest request) {
        return Result.ok(alertService.createAlert(currentTenant(), new CreateAlertCommand(
                request.alertType(), request.alertCode(), request.severity(),
                request.domain(), request.title(), request.description(),
                request.sourceType(), request.sourceId())));
    }

    @PatchMapping("/alerts/{alertId}/acknowledge")
    public Result<BusinessAlert> acknowledgeAlert(@PathVariable String alertId,
                                                  @Valid @RequestBody AcknowledgeAlertRequest request) {
        return Result.ok(alertService.acknowledgeAlert(currentTenant(), alertId, request.assignedTo()));
    }

    @PatchMapping("/alerts/{alertId}/resolve")
    public Result<BusinessAlert> resolveAlert(@PathVariable String alertId,
                                              @Valid @RequestBody ResolveAlertRequest request) {
        return Result.ok(alertService.resolveAlert(currentTenant(), alertId, request.resolution()));
    }

    @PatchMapping("/alerts/{alertId}/dismiss")
    public Result<BusinessAlert> dismissAlert(@PathVariable String alertId) {
        return Result.ok(alertService.dismissAlert(currentTenant(), alertId));
    }

    @GetMapping("/alerts")
    public Result<List<BusinessAlert>> listAlerts(@RequestParam(required = false) String alertType,
                                                  @RequestParam(required = false) String status) {
        return Result.ok(alertService.listAlerts(currentTenant(), alertType, status));
    }

    @GetMapping("/alerts/open")
    public Result<List<BusinessAlert>> listOpenAlerts() {
        return Result.ok(alertService.listOpenAlerts(currentTenant()));
    }

    @GetMapping("/alerts/open/count")
    public Result<Long> countOpenAlerts() {
        return Result.ok(alertService.countOpenAlerts(currentTenant()));
    }

    @PostMapping("/master-data/init")
    public Result<String> initializeMasterData() {
        masterDataInitService.initializeMasterData(currentTenant());
        return Result.ok("主数据初始化完成");
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record CreatePrintTemplateRequest(@NotBlank String templateCode, @NotBlank String templateName,
                                             @NotBlank String templateType, String content,
                                             String paperSize, String orientation,
                                             Map<String, Object> variables, String description) {}
    public record UpdatePrintTemplateRequest(String templateName, String templateType, String content,
                                             String paperSize, String orientation,
                                             Map<String, Object> variables, String description) {}
    public record RenderTemplateRequest(@NotBlank String templateCode, Map<String, String> params) {}
    public record RegisterEventRequest(@NotBlank String eventCode, @NotBlank String eventName,
                                       @NotBlank String domain, String aggregateType, String eventType,
                                       String description, String payloadSchema, List<String> subscribers) {}
    public record UpdateEventRequest(String eventName, String description, String payloadSchema,
                                     List<String> subscribers) {}
    public record SubscribeRequest(@NotBlank String subscriber) {}
    public record CreateAlertRequest(@NotBlank String alertType, @NotBlank String alertCode,
                                     @NotBlank String severity, @NotBlank String domain,
                                     @NotBlank String title, String description,
                                     String sourceType, String sourceId) {}
    public record AcknowledgeAlertRequest(@NotBlank String assignedTo) {}
    public record ResolveAlertRequest(@NotBlank String resolution) {}
}
