package com.aidotnet.erp.crm.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.crm.application.CrmExtService;
import com.aidotnet.erp.crm.application.CrmExtService.AddCustomerTagCommand;
import com.aidotnet.erp.crm.application.CrmExtService.CreateComplaintCommand;
import com.aidotnet.erp.crm.application.CrmExtService.CreateReplyTemplateCommand;
import com.aidotnet.erp.crm.application.CrmExtService.HandleComplaintCommand;
import com.aidotnet.erp.crm.application.CrmExtService.ResolveComplaintCommand;
import com.aidotnet.erp.crm.application.CrmExtService.UpdateReplyTemplateCommand;
import com.aidotnet.erp.crm.domain.Complaint;
import com.aidotnet.erp.crm.domain.ComplaintSeverity;
import com.aidotnet.erp.crm.domain.ComplaintType;
import com.aidotnet.erp.crm.domain.CustomerTag;
import com.aidotnet.erp.crm.domain.ReplyTemplate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * CRM扩展业务控制器
 * <p>
 * 描述: CRM域扩展功能接口，提供回复模板管理、客诉管理、客户标签管理等能力。
 *       这些功能是对核心CRM业务的补充，支持客服效率提升和客户精细化管理。
 * </p>
 * <p>
 * 路径规范: /crm/api/in/v1 — 内部方向(in)，v1版本
 * </p>
 * <p>
 * 核心业务:
 *   1. 回复模板 - 创建/更新/启停/查询客服回复模板，提升客服响应效率
 *   2. 客诉管理 - 创建/处理/解决客户投诉，状态流转: OPEN→IN_PROGRESS→RESOLVED
 *   3. 客户标签 - 添加/删除/查询客户标签，支持客户精细化分群
 * </p>
 *
 * @author ERP系统
 * @see CrmExtService
 */
@RestController
@RequestMapping("/crm/api/in/v1")
public class CrmExtController {

    /** CRM扩展业务服务 */
    private final CrmExtService crmExtService;

    /** 构造函数注入CrmExtService */
    public CrmExtController(CrmExtService crmExtService) {
        this.crmExtService = crmExtService;
    }

    /** 创建回复模板 */
    @PostMapping("/reply-templates")
    public Result<ReplyTemplate> createReplyTemplate(@Valid @RequestBody CreateReplyTemplateRequest request) {
        return Result.ok(crmExtService.createReplyTemplate(currentTenant(), new CreateReplyTemplateCommand(
                request.name(), request.category(), request.content(), request.language())));
    }

    /** 更新回复模板 */
    @PutMapping("/reply-templates/{templateId}")
    public Result<ReplyTemplate> updateReplyTemplate(@PathVariable String templateId,
                                                     @Valid @RequestBody UpdateReplyTemplateRequest request) {
        return Result.ok(crmExtService.updateReplyTemplate(currentTenant(), templateId,
                new UpdateReplyTemplateCommand(request.name(), request.category(), request.content(), request.language())));
    }

    /** 启用/禁用回复模板 */
    @PostMapping("/reply-templates/{templateId}/toggle")
    public Result<ReplyTemplate> toggleReplyTemplate(@PathVariable String templateId, @RequestParam boolean enabled) {
        return Result.ok(crmExtService.toggleReplyTemplate(currentTenant(), templateId, enabled));
    }

    /** 查询回复模板列表(可按分类过滤) */
    @GetMapping("/reply-templates")
    public Result<List<ReplyTemplate>> listReplyTemplates(@RequestParam(required = false) String category) {
        return Result.ok(crmExtService.listReplyTemplates(currentTenant(), category));
    }

    /** 查询回复模板详情 */
    @GetMapping("/reply-templates/{templateId}")
    public Result<ReplyTemplate> getReplyTemplate(@PathVariable String templateId) {
        return Result.ok(crmExtService.getReplyTemplate(currentTenant(), templateId));
    }

    /** 创建客户投诉 */
    @PostMapping("/complaints")
    public Result<Complaint> createComplaint(@Valid @RequestBody CreateComplaintRequest request) {
        return Result.ok(crmExtService.createComplaint(currentTenant(), new CreateComplaintCommand(
                request.customerId(), request.orderId(), request.channel(), request.type(), request.severity(),
                request.subject(), request.description())));
    }

    /** 指派投诉处理人 */
    @PostMapping("/complaints/{complaintId}/handle")
    public Result<Complaint> handleComplaint(@PathVariable String complaintId,
                                             @Valid @RequestBody HandleComplaintRequest request) {
        return Result.ok(crmExtService.handleComplaint(currentTenant(), complaintId,
                new HandleComplaintCommand(request.handlerId())));
    }

    /** 解决客户投诉 */
    @PostMapping("/complaints/{complaintId}/resolve")
    public Result<Complaint> resolveComplaint(@PathVariable String complaintId,
                                              @Valid @RequestBody ResolveComplaintRequest request) {
        return Result.ok(crmExtService.resolveComplaint(currentTenant(), complaintId,
                new ResolveComplaintCommand(request.resolution())));
    }

    /** 查询投诉列表(可按状态过滤) */
    @GetMapping("/complaints")
    public Result<List<Complaint>> listComplaints(@RequestParam(required = false) String status) {
        return Result.ok(crmExtService.listComplaints(currentTenant(), status));
    }

    /** 按客户查询投诉列表 */
    @GetMapping("/customers/{customerId}/complaints")
    public Result<List<Complaint>> listComplaintsByCustomer(@PathVariable String customerId) {
        return Result.ok(crmExtService.listComplaintsByCustomer(currentTenant(), customerId));
    }

    /** 添加客户标签 */
    @PostMapping("/customer-tags")
    public Result<CustomerTag> addCustomerTag(@Valid @RequestBody AddCustomerTagRequest request) {
        return Result.ok(crmExtService.addCustomerTag(currentTenant(),
                new AddCustomerTagCommand(request.customerId(), request.tagName(), request.tagValue())));
    }

    /** 删除客户标签 */
    @DeleteMapping("/customer-tags/{tagId}")
    public Result<Void> removeCustomerTag(@PathVariable String tagId) {
        crmExtService.removeCustomerTag(currentTenant(), tagId);
        return Result.ok(null);
    }

    /** 按客户查询标签列表 */
    @GetMapping("/customers/{customerId}/tags")
    public Result<List<CustomerTag>> listCustomerTags(@PathVariable String customerId) {
        return Result.ok(crmExtService.listCustomerTags(currentTenant(), customerId));
    }

    /** 获取当前租户ID，为空则抛出业务异常 */
    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record CreateReplyTemplateRequest(@NotBlank String name, String category, @NotBlank String content, String language) {}
    public record UpdateReplyTemplateRequest(String name, String category, String content, String language) {}
    public record CreateComplaintRequest(@NotBlank String customerId, String orderId, String channel,
                                         ComplaintType type, ComplaintSeverity severity,
                                         @NotBlank String subject, String description) {}
    public record HandleComplaintRequest(@NotBlank String handlerId) {}
    public record ResolveComplaintRequest(@NotBlank String resolution) {}
    public record AddCustomerTagRequest(@NotBlank String customerId, @NotBlank String tagName, String tagValue) {}
}
