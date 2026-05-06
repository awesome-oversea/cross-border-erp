package com.aidotnet.erp.crm.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.crm.domain.CustomerBehavior;
import com.aidotnet.erp.crm.domain.CustomerProfile;
import com.aidotnet.erp.crm.infrastructure.CrmExtStore;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 客户数据平台(CDP)应用服务
 * <p>
 * 描述: 客户画像与行为数据管理服务，支持客户360度视图构建。
 *       提供客户画像创建/更新、风险等级管理、客户分群、行为记录等能力。
 *       退货率自动计算: returnRate = totalReturns / totalOrders
 * </p>
 * <p>
 * 核心概念:
 *   1. 客户画像(CustomerProfile) - 客户的360度视图，包含消费统计、偏好、风险等级
 *   2. 客户行为(CustomerBehavior) - 客户的浏览、购买、退货等行为事件
 *   3. 风险等级 - LOW/MEDIUM/HIGH，用于退货风控
 * </p>
 *
 * @author ERP系统
 * @see CrmExtStore
 */
@Service
public class CustomerDataService {

    private static final Logger log = LoggerFactory.getLogger(CustomerDataService.class);

    private final CrmExtStore extStore;

    public CustomerDataService(CrmExtStore extStore) {
        this.extStore = extStore;
    }

    @Transactional
    public CustomerProfile createOrUpdateProfile(String tenantId, CreateProfileCommand command) {
        CustomerProfile existing = extStore.findCustomerProfileByCustomerId(tenantId, command.customerId())
                .orElse(null);
        Instant now = Instant.now();
        if (existing == null) {
            CustomerProfile profile = new CustomerProfile(
                    UUID.randomUUID().toString(), tenantId, command.customerId(),
                    command.segment(), command.lifetimeValue(), command.avgOrderValue(),
                    command.totalOrders(), command.totalReturns(), BigDecimal.ZERO,
                    command.preferredChannel(), command.preferredLanguage(), "LOW",
                    command.attributes(), command.firstOrderAt(), command.lastOrderAt(), now, now);
            CustomerProfile saved = extStore.saveCustomerProfile(profile);
            log.info("Customer profile created: customerId={}", command.customerId());
            return saved;
        }
        BigDecimal returnRate = existing.totalOrders() > 0
                ? BigDecimal.valueOf(existing.totalReturns()).divide(BigDecimal.valueOf(existing.totalOrders()), 4, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        CustomerProfile updated = new CustomerProfile(
                existing.profileId(), existing.tenantId(), existing.customerId(),
                command.segment() != null ? command.segment() : existing.segment(),
                command.lifetimeValue() != null ? command.lifetimeValue() : existing.lifetimeValue(),
                command.avgOrderValue() != null ? command.avgOrderValue() : existing.avgOrderValue(),
                command.totalOrders() >= 0 ? command.totalOrders() : existing.totalOrders(),
                command.totalReturns() >= 0 ? command.totalReturns() : existing.totalReturns(),
                returnRate,
                command.preferredChannel() != null ? command.preferredChannel() : existing.preferredChannel(),
                command.preferredLanguage() != null ? command.preferredLanguage() : existing.preferredLanguage(),
                existing.riskLevel(),
                command.attributes() != null ? command.attributes() : existing.attributes(),
                command.firstOrderAt() != null ? command.firstOrderAt() : existing.firstOrderAt(),
                command.lastOrderAt() != null ? command.lastOrderAt() : existing.lastOrderAt(),
                existing.createdAt(), now);
        CustomerProfile saved = extStore.saveCustomerProfile(updated);
        log.info("Customer profile updated: customerId={}", command.customerId());
        return saved;
    }

    @Transactional
    public CustomerProfile updateRiskLevel(String tenantId, String customerId, String riskLevel) {
        CustomerProfile existing = extStore.findCustomerProfileByCustomerId(tenantId, customerId)
                .orElseThrow(() -> new BizException("PROFILE_NOT_FOUND", "客户画像不存在"));
        Instant now = Instant.now();
        CustomerProfile updated = new CustomerProfile(
                existing.profileId(), existing.tenantId(), existing.customerId(), existing.segment(),
                existing.lifetimeValue(), existing.avgOrderValue(), existing.totalOrders(),
                existing.totalReturns(), existing.returnRate(), existing.preferredChannel(),
                existing.preferredLanguage(), riskLevel, existing.attributes(),
                existing.firstOrderAt(), existing.lastOrderAt(), existing.createdAt(), now);
        return extStore.saveCustomerProfile(updated);
    }

    @Transactional
    public CustomerProfile updateSegment(String tenantId, String customerId, String segment) {
        CustomerProfile existing = extStore.findCustomerProfileByCustomerId(tenantId, customerId)
                .orElseThrow(() -> new BizException("PROFILE_NOT_FOUND", "客户画像不存在"));
        Instant now = Instant.now();
        CustomerProfile updated = new CustomerProfile(
                existing.profileId(), existing.tenantId(), existing.customerId(), segment,
                existing.lifetimeValue(), existing.avgOrderValue(), existing.totalOrders(),
                existing.totalReturns(), existing.returnRate(), existing.preferredChannel(),
                existing.preferredLanguage(), existing.riskLevel(), existing.attributes(),
                existing.firstOrderAt(), existing.lastOrderAt(), existing.createdAt(), now);
        return extStore.saveCustomerProfile(updated);
    }

    @Transactional
    public CustomerBehavior recordBehavior(String tenantId, RecordBehaviorCommand command) {
        Instant now = Instant.now();
        CustomerBehavior behavior = new CustomerBehavior(
                UUID.randomUUID().toString(), tenantId, command.customerId(), command.behaviorType(),
                command.channel(), command.objectType(), command.objectId(), command.context(),
                command.occurredAt() != null ? command.occurredAt() : now, now);
        return extStore.saveCustomerBehavior(behavior);
    }

    public CustomerProfile getProfile(String tenantId, String customerId) {
        return extStore.findCustomerProfileByCustomerId(tenantId, customerId)
                .orElseThrow(() -> new BizException("PROFILE_NOT_FOUND", "客户画像不存在"));
    }

    public List<CustomerProfile> listProfiles(String tenantId, String segment) {
        return extStore.listCustomerProfiles(tenantId, segment);
    }

    public List<CustomerBehavior> listBehaviors(String tenantId, String customerId, String behaviorType) {
        return extStore.listCustomerBehaviors(tenantId, customerId, behaviorType);
    }

    public record CreateProfileCommand(String customerId, String segment, BigDecimal lifetimeValue,
                                       BigDecimal avgOrderValue, int totalOrders, int totalReturns,
                                       String preferredChannel, String preferredLanguage,
                                       Map<String, Object> attributes, Instant firstOrderAt,
                                       Instant lastOrderAt) {}
    public record RecordBehaviorCommand(String customerId, String behaviorType, String channel,
                                        String objectType, String objectId, Map<String, Object> context,
                                        Instant occurredAt) {}
}
