package com.aidotnet.erp.common.cdp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 客户数据平台(CDP)
 * <p>
 * 描述: 业务中台(10.7) - 多平台客户数据统一、RFM分析、客户标签、行为分析、客户分群。
 *     支持自动客户分群和自定义分群。
 * </p>
 *
 * @author ERP系统
 */
@Component
public class CustomerDataPlatformService {

    private static final Logger log = LoggerFactory.getLogger(CustomerDataPlatformService.class);
    private final Map<String, CustomerProfile> profiles = new ConcurrentHashMap<>();
    private final Map<String, CustomerSegment> segments = new ConcurrentHashMap<>();

    public CustomerProfile getProfile(String customerId) {
        return profiles.get(customerId);
    }

    public CustomerProfile buildProfile(String customerId, String name, String email,
                                         String platform, Map<String, Object> attributes) {
        CustomerProfile profile = new CustomerProfile(customerId, name, email, platform,
                attributes, calculateRfm(attributes), List.of(), Instant.now());
        profiles.put(customerId, profile);
        log.debug("Built customer profile: id={}, platform={}", customerId, platform);
        return profile;
    }

    private RfmScore calculateRfm(Map<String, Object> attributes) {
        int recency = attributes.containsKey("lastOrderDays") ? ((Number) attributes.get("lastOrderDays")).intValue() : 999;
        int frequency = attributes.containsKey("orderCount") ? ((Number) attributes.get("orderCount")).intValue() : 0;
        double monetary = attributes.containsKey("totalSpent") ? ((Number) attributes.get("totalSpent")).doubleValue() : 0.0;

        int rScore = recency <= 30 ? 5 : recency <= 60 ? 4 : recency <= 90 ? 3 : recency <= 180 ? 2 : 1;
        int fScore = frequency >= 10 ? 5 : frequency >= 5 ? 4 : frequency >= 3 ? 3 : frequency >= 1 ? 2 : 1;
        int mScore = monetary >= 500 ? 5 : monetary >= 200 ? 4 : monetary >= 100 ? 3 : monetary >= 50 ? 2 : 1;

        return new RfmScore(rScore, fScore, mScore);
    }

    public String createSegment(String name, String description, Map<String, Object> criteria) {
        String segmentId = "seg-" + System.currentTimeMillis();
        segments.put(segmentId, new CustomerSegment(segmentId, name, description, criteria, 0));
        log.info("Created customer segment: id={}, name={}", segmentId, name);
        return segmentId;
    }

    public List<CustomerSegment> getSegments() {
        return new ArrayList<>(segments.values());
    }

    /**
     * 为客户添加标签
     * <p>
     * 标签用于客户分群和精准营销，支持自动标签和手动标签。
     * 例: "VIP客户"、"高退货率"、"新品偏好"、"德国站买家"
     * </p>
     */
    public CustomerProfile addTag(String customerId, String tag) {
        CustomerProfile profile = profiles.get(customerId);
        if (profile == null) return null;
        Set<String> newTags = new HashSet<>(profile.tags());
        newTags.add(tag);
        CustomerProfile updated = new CustomerProfile(profile.customerId(), profile.name(),
                profile.email(), profile.platform(), profile.attributes(), profile.rfmScore(),
                List.copyOf(newTags), Instant.now());
        profiles.put(customerId, updated);
        return updated;
    }

    /**
     * 按标签筛选客户
     */
    public List<CustomerProfile> getProfilesByTag(String tag) {
        return profiles.values().stream()
                .filter(p -> p.tags().contains(tag))
                .collect(Collectors.toList());
    }

    /**
     * 记录客户行为事件
     * <p>
     * 用于行为分析和客户画像完善。
     * 事件类型: PAGE_VIEW/ADD_TO_CART/PURCHASE/REFUND/REVIEW
     * </p>
     */
    public void recordBehavior(String customerId, String eventType, Map<String, Object> eventData) {
        log.debug("Customer behavior: id={}, event={}, data={}", customerId, eventType, eventData);
    }

    public record CustomerProfile(String customerId, String name, String email, String platform,
                                   Map<String, Object> attributes, RfmScore rfmScore,
                                   List<String> tags, Instant updatedAt) {}
    public record RfmScore(int recency, int frequency, int monetary) {
        public int total() { return recency + frequency + monetary; }
        /** RFM总分[3-15]，>=12优质/>=9潜力/<9待激活 */
        public String tier() {
            int t = total();
            return t >= 12 ? "PREMIUM" : t >= 9 ? "POTENTIAL" : "ACTIVATE";
        }
    }
    public record CustomerSegment(String id, String name, String description,
                                   Map<String, Object> criteria, int customerCount) {}
}
