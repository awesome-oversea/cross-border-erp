package com.aidotnet.erp.common.cdp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

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

    public record CustomerProfile(String customerId, String name, String email, String platform,
                                   Map<String, Object> attributes, RfmScore rfmScore,
                                   List<String> tags, Instant updatedAt) {}
    public record RfmScore(int recency, int frequency, int monetary) {
        public int total() { return recency + frequency + monetary; }
    }
    public record CustomerSegment(String id, String name, String description,
                                   Map<String, Object> criteria, int customerCount) {}
}
