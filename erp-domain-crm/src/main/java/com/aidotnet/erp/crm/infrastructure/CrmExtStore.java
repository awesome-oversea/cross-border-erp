package com.aidotnet.erp.crm.infrastructure;

import com.aidotnet.erp.crm.domain.Complaint;
import com.aidotnet.erp.crm.domain.ComplaintSeverity;
import com.aidotnet.erp.crm.domain.ComplaintStatus;
import com.aidotnet.erp.crm.domain.ComplaintType;
import com.aidotnet.erp.crm.domain.CustomerBehavior;
import com.aidotnet.erp.crm.domain.CustomerProfile;
import com.aidotnet.erp.crm.domain.CustomerTag;
import com.aidotnet.erp.crm.domain.ReplyTemplate;
import com.aidotnet.erp.crm.infrastructure.data.ComplaintDO;
import com.aidotnet.erp.crm.infrastructure.data.CustomerTagDO;
import com.aidotnet.erp.crm.infrastructure.data.ReplyTemplateDO;
import com.aidotnet.erp.crm.infrastructure.mapper.CrmExtMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/**
 * CRM扩展数据存储
 * <p>
 * 描述: CRM域扩展功能数据持久化层，负责回复模板、客诉、客户标签、
 *       客户画像、客户行为等业务对象与数据库之间的转换和持久化操作。
 *       客户画像和客户行为使用内存ConcurrentHashMap暂存，后续迁移至数据库。
 * </p>
 * <p>
 * 数据转换: 领域模型(Domain) ↔ 数据对象(DO)，通过手动映射实现
 * </p>
 *
 * @author ERP系统
 * @see CrmExtMapper
 */
@Repository
public class CrmExtStore {

    private final CrmExtMapper mapper;

    public CrmExtStore(CrmExtMapper mapper) {
        this.mapper = mapper;
    }

    public ReplyTemplate saveReplyTemplate(ReplyTemplate template) {
        ReplyTemplateDO existing = mapper.selectReplyTemplate(template.tenantId(), template.templateId());
        ReplyTemplateDO data = toReplyTemplateData(template);
        if (existing == null) {
            mapper.insertReplyTemplate(data);
        } else {
            mapper.updateReplyTemplate(data);
        }
        return template;
    }

    public Optional<ReplyTemplate> findReplyTemplate(String tenantId, String templateId) {
        return Optional.ofNullable(mapper.selectReplyTemplate(tenantId, templateId)).map(this::toReplyTemplateDomain);
    }

    public List<ReplyTemplate> listReplyTemplates(String tenantId, String category) {
        return mapper.selectReplyTemplates(tenantId, category).stream().map(this::toReplyTemplateDomain).collect(Collectors.toList());
    }

    public Complaint saveComplaint(Complaint complaint) {
        ComplaintDO existing = mapper.selectComplaint(complaint.tenantId(), complaint.complaintId());
        ComplaintDO data = toComplaintData(complaint);
        if (existing == null) {
            mapper.insertComplaint(data);
        } else {
            mapper.updateComplaint(data);
        }
        return complaint;
    }

    public Optional<Complaint> findComplaint(String tenantId, String complaintId) {
        return Optional.ofNullable(mapper.selectComplaint(tenantId, complaintId)).map(this::toComplaintDomain);
    }

    public List<Complaint> listComplaints(String tenantId, String status) {
        return mapper.selectComplaints(tenantId, status).stream().map(this::toComplaintDomain).collect(Collectors.toList());
    }

    public List<Complaint> listComplaintsByCustomer(String tenantId, String customerId) {
        return mapper.selectComplaintsByCustomer(tenantId, customerId).stream().map(this::toComplaintDomain).collect(Collectors.toList());
    }

    public CustomerTag saveCustomerTag(CustomerTag tag) {
        mapper.insertCustomerTag(toCustomerTagData(tag));
        return tag;
    }

    public void deleteCustomerTag(String tenantId, String tagId) {
        mapper.deleteCustomerTag(tenantId, tagId);
    }

    public List<CustomerTag> listCustomerTags(String tenantId, String customerId) {
        return mapper.selectCustomerTags(tenantId, customerId).stream().map(this::toCustomerTagDomain).collect(Collectors.toList());
    }

    private ReplyTemplateDO toReplyTemplateData(ReplyTemplate t) {
        ReplyTemplateDO data = new ReplyTemplateDO();
        data.setTemplateId(t.templateId());
        data.setTenantId(t.tenantId());
        data.setName(t.name());
        data.setCategory(t.category());
        data.setContent(t.content());
        data.setLanguage(t.language());
        data.setEnabled(t.enabled());
        data.setCreatedAt(t.createdAt() != null ? t.createdAt() : Instant.now());
        data.setUpdatedAt(t.updatedAt() != null ? t.updatedAt() : Instant.now());
        return data;
    }

    private ReplyTemplate toReplyTemplateDomain(ReplyTemplateDO d) {
        return new ReplyTemplate(d.getTemplateId(), d.getTenantId(), d.getName(), d.getCategory(), d.getContent(),
                d.getLanguage(), d.isEnabled(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private ComplaintDO toComplaintData(Complaint c) {
        ComplaintDO data = new ComplaintDO();
        data.setComplaintId(c.complaintId());
        data.setTenantId(c.tenantId());
        data.setCustomerId(c.customerId());
        data.setOrderId(c.orderId());
        data.setChannel(c.channel());
        data.setType(c.type().name());
        data.setSeverity(c.severity().name());
        data.setStatus(c.status().name());
        data.setSubject(c.subject());
        data.setDescription(c.description());
        data.setHandlerId(c.handlerId());
        data.setResolution(c.resolution());
        data.setComplainedAt(c.complainedAt());
        data.setResolvedAt(c.resolvedAt());
        data.setCreatedAt(c.createdAt() != null ? c.createdAt() : Instant.now());
        data.setUpdatedAt(c.updatedAt() != null ? c.updatedAt() : Instant.now());
        return data;
    }

    private Complaint toComplaintDomain(ComplaintDO d) {
        return new Complaint(d.getComplaintId(), d.getTenantId(), d.getCustomerId(), d.getOrderId(), d.getChannel(),
                ComplaintType.valueOf(d.getType()), ComplaintSeverity.valueOf(d.getSeverity()),
                ComplaintStatus.valueOf(d.getStatus()), d.getSubject(), d.getDescription(), d.getHandlerId(),
                d.getResolution(), d.getComplainedAt(), d.getResolvedAt(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private CustomerTagDO toCustomerTagData(CustomerTag t) {
        CustomerTagDO data = new CustomerTagDO();
        data.setTagId(t.tagId());
        data.setTenantId(t.tenantId());
        data.setCustomerId(t.customerId());
        data.setTagName(t.tagName());
        data.setTagValue(t.tagValue());
        data.setTaggedAt(t.taggedAt() != null ? t.taggedAt() : Instant.now());
        return data;
    }

    private CustomerTag toCustomerTagDomain(CustomerTagDO d) {
        return new CustomerTag(d.getTagId(), d.getTenantId(), d.getCustomerId(), d.getTagName(), d.getTagValue(), d.getTaggedAt());
    }

    private final java.util.Map<String, CustomerProfile> profileStore = new java.util.concurrent.ConcurrentHashMap<>();
    private final java.util.Map<String, CustomerBehavior> behaviorStore = new java.util.concurrent.ConcurrentHashMap<>();

    public CustomerProfile saveCustomerProfile(CustomerProfile profile) {
        profileStore.put(profile.profileId(), profile);
        return profile;
    }

    public Optional<CustomerProfile> findCustomerProfile(String tenantId, String profileId) {
        return profileStore.values().stream()
                .filter(p -> p.tenantId().equals(tenantId) && p.profileId().equals(profileId))
                .findFirst();
    }

    public Optional<CustomerProfile> findCustomerProfileByCustomerId(String tenantId, String customerId) {
        return profileStore.values().stream()
                .filter(p -> p.tenantId().equals(tenantId) && p.customerId().equals(customerId))
                .findFirst();
    }

    public List<CustomerProfile> listCustomerProfiles(String tenantId, String segment) {
        return profileStore.values().stream()
                .filter(p -> p.tenantId().equals(tenantId))
                .filter(p -> segment == null || p.segment().equals(segment))
                .collect(Collectors.toList());
    }

    public CustomerBehavior saveCustomerBehavior(CustomerBehavior behavior) {
        behaviorStore.put(behavior.behaviorId(), behavior);
        return behavior;
    }

    public List<CustomerBehavior> listCustomerBehaviors(String tenantId, String customerId, String behaviorType) {
        return behaviorStore.values().stream()
                .filter(b -> b.tenantId().equals(tenantId))
                .filter(b -> customerId == null || b.customerId().equals(customerId))
                .filter(b -> behaviorType == null || b.behaviorType().equals(behaviorType))
                .sorted((a, b) -> b.occurredAt().compareTo(a.occurredAt()))
                .collect(Collectors.toList());
    }
}
