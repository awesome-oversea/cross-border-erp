package com.aidotnet.erp.sys.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.sys.application.SysExtService;
import com.aidotnet.erp.sys.application.SysExtService.CreateNotificationSettingCommand;
import com.aidotnet.erp.sys.application.SysExtService.CreateNotificationTemplateCommand;
import com.aidotnet.erp.sys.application.SysExtService.UpdateNotificationSettingCommand;
import com.aidotnet.erp.sys.application.SysExtService.UpdateNotificationTemplateCommand;
import com.aidotnet.erp.sys.domain.NotificationSetting;
import com.aidotnet.erp.sys.domain.NotificationTemplate;
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
@RequestMapping("/sys/api/in/v1/notification")
public class NotificationController {

    private final SysExtService sysExtService;

    public NotificationController(SysExtService sysExtService) {
        this.sysExtService = sysExtService;
    }

    @PostMapping("/settings")
    public Result<NotificationSetting> createSetting(@Valid @RequestBody CreateNotificationSettingRequest request) {
        return Result.ok(sysExtService.createNotificationSetting(currentTenant(), new CreateNotificationSettingCommand(
                request.channel(), request.channelName(), request.rules(), request.description())));
    }

    @PutMapping("/settings/{settingId}")
    public Result<NotificationSetting> updateSetting(@PathVariable String settingId,
                                                      @Valid @RequestBody UpdateNotificationSettingRequest request) {
        return Result.ok(sysExtService.updateNotificationSetting(currentTenant(), settingId,
                new UpdateNotificationSettingCommand(request.channelName(), request.rules(), request.description())));
    }

    @PatchMapping("/settings/{settingId}/toggle")
    public Result<NotificationSetting> toggleSetting(@PathVariable String settingId, @RequestParam boolean enabled) {
        return Result.ok(sysExtService.toggleNotificationSetting(currentTenant(), settingId, enabled));
    }

    @GetMapping("/settings")
    public Result<List<NotificationSetting>> listSettings(@RequestParam(required = false) String channel) {
        return Result.ok(sysExtService.listNotificationSettings(currentTenant(), channel));
    }

    @PostMapping("/templates")
    public Result<NotificationTemplate> createTemplate(@Valid @RequestBody CreateNotificationTemplateRequest request) {
        return Result.ok(sysExtService.createNotificationTemplate(currentTenant(), new CreateNotificationTemplateCommand(
                request.templateCode(), request.templateName(), request.channel(), request.subject(),
                request.content(), request.variables(), request.description())));
    }

    @PutMapping("/templates/{templateId}")
    public Result<NotificationTemplate> updateTemplate(@PathVariable String templateId,
                                                        @Valid @RequestBody UpdateNotificationTemplateRequest request) {
        return Result.ok(sysExtService.updateNotificationTemplate(currentTenant(), templateId,
                new UpdateNotificationTemplateCommand(request.templateName(), request.subject(),
                        request.content(), request.variables(), request.description())));
    }

    @GetMapping("/templates")
    public Result<List<NotificationTemplate>> listTemplates(@RequestParam(required = false) String channel) {
        return Result.ok(sysExtService.listNotificationTemplates(currentTenant(), channel));
    }

    @GetMapping("/templates/{templateId}")
    public Result<NotificationTemplate> getTemplate(@PathVariable String templateId) {
        return Result.ok(sysExtService.getNotificationTemplate(currentTenant(), templateId));
    }

    @PostMapping("/render")
    public Result<String> renderTemplate(@Valid @RequestBody RenderTemplateRequest request) {
        return Result.ok(sysExtService.renderTemplate(currentTenant(), request.templateCode(), request.params()));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record CreateNotificationSettingRequest(
            @NotBlank String channel, @NotBlank String channelName,
            Map<String, Object> rules, String description) {}
    public record UpdateNotificationSettingRequest(
            String channelName, Map<String, Object> rules, String description) {}
    public record CreateNotificationTemplateRequest(
            @NotBlank String templateCode, @NotBlank String templateName, @NotBlank String channel,
            String subject, @NotBlank String content, Map<String, String> variables, String description) {}
    public record UpdateNotificationTemplateRequest(
            String templateName, String subject, String content,
            Map<String, String> variables, String description) {}
    public record RenderTemplateRequest(@NotBlank String templateCode, Map<String, String> params) {}
}
