package com.aidotnet.erp.crm.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.crm.domain.Complaint;
import com.aidotnet.erp.crm.domain.ComplaintSeverity;
import com.aidotnet.erp.crm.domain.ComplaintStatus;
import com.aidotnet.erp.crm.domain.ComplaintType;
import com.aidotnet.erp.crm.domain.CustomerTag;
import com.aidotnet.erp.crm.domain.ReplyTemplate;
import com.aidotnet.erp.crm.infrastructure.CrmExtStore;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * CRM扩展业务应用服务
 * <p>
 * 描述: CRM域扩展功能服务，提供回复模板管理、客诉管理、客户标签管理等能力。
 *       所有写操作均使用@Transactional保证事务一致性。
 * </p>
 * <p>
 * 核心业务:
 *   1. 回复模板 - 创建/更新/启停客服回复模板，提升客服响应效率
 *   2. 客诉管理 - 创建/处理/解决客户投诉，状态流转: OPEN→IN_PROGRESS→RESOLVED
 *   3. 客户标签 - 添加/删除客户标签，支持客户精细化分群
 * </p>
 *
 * @author ERP系统
 * @see CrmExtStore
 */
@Service
public class CrmExtService {

    private final CrmExtStore extStore;

    public CrmExtService(CrmExtStore extStore) {
        this.extStore = extStore;
    }

    @Transactional
    public ReplyTemplate createReplyTemplate(String tenantId, CreateReplyTemplateCommand command) {
        Instant now = Instant.now();
        ReplyTemplate template = new ReplyTemplate(UUID.randomUUID().toString(), tenantId, command.name(),
                command.category(), command.content(), command.language(), true, now, now);
        return extStore.saveReplyTemplate(template);
    }

    @Transactional
    public ReplyTemplate updateReplyTemplate(String tenantId, String templateId, UpdateReplyTemplateCommand command) {
        ReplyTemplate existing = getReplyTemplate(tenantId, templateId);
        return extStore.saveReplyTemplate(new ReplyTemplate(existing.templateId(), existing.tenantId(),
                command.name() != null ? command.name() : existing.name(),
                command.category() != null ? command.category() : existing.category(),
                command.content() != null ? command.content() : existing.content(),
                command.language() != null ? command.language() : existing.language(),
                existing.enabled(), existing.createdAt(), Instant.now()));
    }

    @Transactional
    public ReplyTemplate toggleReplyTemplate(String tenantId, String templateId, boolean enabled) {
        ReplyTemplate existing = getReplyTemplate(tenantId, templateId);
        return extStore.saveReplyTemplate(new ReplyTemplate(existing.templateId(), existing.tenantId(),
                existing.name(), existing.category(), existing.content(), existing.language(), enabled,
                existing.createdAt(), Instant.now()));
    }

    public List<ReplyTemplate> listReplyTemplates(String tenantId, String category) {
        return extStore.listReplyTemplates(tenantId, category);
    }

    public ReplyTemplate getReplyTemplate(String tenantId, String templateId) {
        return extStore.findReplyTemplate(tenantId, templateId)
                .orElseThrow(() -> new BizException("REPLY_TEMPLATE_NOT_FOUND", "回复模板不存在"));
    }

    @Transactional
    public Complaint createComplaint(String tenantId, CreateComplaintCommand command) {
        Instant now = Instant.now();
        Complaint complaint = new Complaint(UUID.randomUUID().toString(), tenantId, command.customerId(),
                command.orderId(), command.channel(), command.type(), command.severity(), ComplaintStatus.OPEN,
                command.subject(), command.description(), null, null, now, null, now, now);
        return extStore.saveComplaint(complaint);
    }

    @Transactional
    public Complaint handleComplaint(String tenantId, String complaintId, HandleComplaintCommand command) {
        Complaint complaint = getComplaint(tenantId, complaintId);
        if (complaint.status() != ComplaintStatus.OPEN && complaint.status() != ComplaintStatus.IN_PROGRESS) {
            throw new BizException("COMPLAINT_STATUS_INVALID", "客诉状态不允许处理");
        }
        return extStore.saveComplaint(new Complaint(complaint.complaintId(), complaint.tenantId(), complaint.customerId(),
                complaint.orderId(), complaint.channel(), complaint.type(), complaint.severity(),
                ComplaintStatus.IN_PROGRESS, complaint.subject(), complaint.description(), command.handlerId(),
                complaint.resolution(), complaint.complainedAt(), complaint.resolvedAt(), complaint.createdAt(), Instant.now()));
    }

    @Transactional
    public Complaint resolveComplaint(String tenantId, String complaintId, ResolveComplaintCommand command) {
        Complaint complaint = getComplaint(tenantId, complaintId);
        if (complaint.status() != ComplaintStatus.IN_PROGRESS) {
            throw new BizException("COMPLAINT_STATUS_INVALID", "只有处理中的客诉可以解决");
        }
        return extStore.saveComplaint(new Complaint(complaint.complaintId(), complaint.tenantId(), complaint.customerId(),
                complaint.orderId(), complaint.channel(), complaint.type(), complaint.severity(),
                ComplaintStatus.RESOLVED, complaint.subject(), complaint.description(), complaint.handlerId(),
                command.resolution(), complaint.complainedAt(), Instant.now(), complaint.createdAt(), Instant.now()));
    }

    public List<Complaint> listComplaints(String tenantId, String status) {
        return extStore.listComplaints(tenantId, status);
    }

    public List<Complaint> listComplaintsByCustomer(String tenantId, String customerId) {
        return extStore.listComplaintsByCustomer(tenantId, customerId);
    }

    public Complaint getComplaint(String tenantId, String complaintId) {
        return extStore.findComplaint(tenantId, complaintId)
                .orElseThrow(() -> new BizException("COMPLAINT_NOT_FOUND", "客诉不存在"));
    }

    @Transactional
    public CustomerTag addCustomerTag(String tenantId, AddCustomerTagCommand command) {
        return extStore.saveCustomerTag(new CustomerTag(UUID.randomUUID().toString(), tenantId,
                command.customerId(), command.tagName(), command.tagValue(), Instant.now()));
    }

    @Transactional
    public void removeCustomerTag(String tenantId, String tagId) {
        extStore.deleteCustomerTag(tenantId, tagId);
    }

    public List<CustomerTag> listCustomerTags(String tenantId, String customerId) {
        return extStore.listCustomerTags(tenantId, customerId);
    }

    public record CreateReplyTemplateCommand(String name, String category, String content, String language) {}
    public record UpdateReplyTemplateCommand(String name, String category, String content, String language) {}
    public record CreateComplaintCommand(String customerId, String orderId, String channel, ComplaintType type,
                                         ComplaintSeverity severity, String subject, String description) {}
    public record HandleComplaintCommand(String handlerId) {}
    public record ResolveComplaintCommand(String resolution) {}
    public record AddCustomerTagCommand(String customerId, String tagName, String tagValue) {}
}
