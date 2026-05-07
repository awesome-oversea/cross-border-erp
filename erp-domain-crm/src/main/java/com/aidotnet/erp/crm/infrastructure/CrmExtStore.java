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
import com.aidotnet.erp.crm.infrastructure.data.CustomerBehaviorDO;
import com.aidotnet.erp.crm.infrastructure.data.CustomerProfileDO;
import com.aidotnet.erp.crm.infrastructure.data.CustomerTagDO;
import com.aidotnet.erp.crm.infrastructure.data.ReplyTemplateDO;
import com.aidotnet.erp.crm.infrastructure.mapper.CrmExtMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/**
 * CRM扩展数据存储
 * <p>
 * 描述: CRM域扩展功能数据持久化层，负责回复模板、客诉、客户标签、
 *       客户画像、客户行为等业务对象与数据库之间的转换和持久化操作。
 *       所有数据均通过MyBatis持久化到数据库，支持多租户隔离。
 * </p>
 * <p>
 * 数据转换: 领域模型(Domain) ↔ 数据对象(DO)，通过手动映射实现
 *            Map/Object类型字段通过Jackson序列化为JSON存储
 * </p>
 *
 * @author ERP系统
 * @see CrmExtMapper
 */
@Repository
public class CrmExtStore {

    private final CrmExtMapper mapper;
    private final ObjectMapper jsonMapper;

    public CrmExtStore(CrmExtMapper mapper, ObjectMapper jsonMapper) {
        this.mapper = mapper;
        this.jsonMapper = jsonMapper;
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

    public CustomerProfile saveCustomerProfile(CustomerProfile profile) {
        CustomerProfileDO existing = mapper.selectCustomerProfile(profile.tenantId(), profile.profileId());
        CustomerProfileDO data = toCustomerProfileData(profile);
        if (existing == null) {
            mapper.insertCustomerProfile(data);
        } else {
            mapper.updateCustomerProfile(data);
        }
        return profile;
    }

    public Optional<CustomerProfile> findCustomerProfile(String tenantId, String profileId) {
        return Optional.ofNullable(mapper.selectCustomerProfile(tenantId, profileId)).map(this::toCustomerProfileDomain);
    }

    public Optional<CustomerProfile> findCustomerProfileByCustomerId(String tenantId, String customerId) {
        return Optional.ofNullable(mapper.selectCustomerProfileByCustomerId(tenantId, customerId)).map(this::toCustomerProfileDomain);
    }

    public List<CustomerProfile> listCustomerProfiles(String tenantId, String segment) {
        return mapper.selectCustomerProfiles(tenantId, segment).stream().map(this::toCustomerProfileDomain).collect(Collectors.toList());
    }

    public CustomerBehavior saveCustomerBehavior(CustomerBehavior behavior) {
        mapper.insertCustomerBehavior(toCustomerBehaviorData(behavior));
        return behavior;
    }

    public List<CustomerBehavior> listCustomerBehaviors(String tenantId, String customerId, String behaviorType) {
        return mapper.selectCustomerBehaviors(tenantId, customerId, behaviorType).stream().map(this::toCustomerBehaviorDomain).collect(Collectors.toList());
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

    private CustomerProfileDO toCustomerProfileData(CustomerProfile p) {
        CustomerProfileDO data = new CustomerProfileDO();
        data.setProfileId(p.profileId());
        data.setTenantId(p.tenantId());
        data.setCustomerId(p.customerId());
        data.setSegment(p.segment());
        data.setLifetimeValue(p.lifetimeValue());
        data.setAvgOrderValue(p.avgOrderValue());
        data.setTotalOrders(p.totalOrders());
        data.setTotalReturns(p.totalReturns());
        data.setReturnRate(p.returnRate());
        data.setPreferredChannel(p.preferredChannel());
        data.setPreferredLanguage(p.preferredLanguage());
        data.setRiskLevel(p.riskLevel());
        data.setAttributes(toJson(p.attributes()));
        data.setFirstOrderAt(p.firstOrderAt());
        data.setLastOrderAt(p.lastOrderAt());
        data.setCreatedAt(p.createdAt() != null ? p.createdAt() : Instant.now());
        data.setUpdatedAt(p.updatedAt() != null ? p.updatedAt() : Instant.now());
        return data;
    }

    private CustomerProfile toCustomerProfileDomain(CustomerProfileDO d) {
        return new CustomerProfile(d.getProfileId(), d.getTenantId(), d.getCustomerId(), d.getSegment(),
                d.getLifetimeValue(), d.getAvgOrderValue(),
                d.getTotalOrders() != null ? d.getTotalOrders() : 0,
                d.getTotalReturns() != null ? d.getTotalReturns() : 0,
                d.getReturnRate(), d.getPreferredChannel(), d.getPreferredLanguage(), d.getRiskLevel(),
                fromJson(d.getAttributes(), new TypeReference<Map<String, Object>>() {}),
                d.getFirstOrderAt(), d.getLastOrderAt(), d.getCreatedAt(), d.getUpdatedAt());
    }

    private CustomerBehaviorDO toCustomerBehaviorData(CustomerBehavior b) {
        CustomerBehaviorDO data = new CustomerBehaviorDO();
        data.setBehaviorId(b.behaviorId());
        data.setTenantId(b.tenantId());
        data.setCustomerId(b.customerId());
        data.setBehaviorType(b.behaviorType());
        data.setChannel(b.channel());
        data.setObjectType(b.objectType());
        data.setObjectId(b.objectId());
        data.setContext(toJson(b.context()));
        data.setOccurredAt(b.occurredAt());
        data.setCreatedAt(b.createdAt() != null ? b.createdAt() : Instant.now());
        return data;
    }

    private CustomerBehavior toCustomerBehaviorDomain(CustomerBehaviorDO d) {
        return new CustomerBehavior(d.getBehaviorId(), d.getTenantId(), d.getCustomerId(), d.getBehaviorType(),
                d.getChannel(), d.getObjectType(), d.getObjectId(),
                fromJson(d.getContext(), new TypeReference<Map<String, Object>>() {}),
                d.getOccurredAt(), d.getCreatedAt());
    }

    private String toJson(Object obj) {
        if (obj == null) return null;
        try {
            return jsonMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private <T> T fromJson(String json, TypeReference<T> typeRef) {
        if (json == null || json.isBlank()) return null;
        try {
            return jsonMapper.readValue(json, typeRef);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
