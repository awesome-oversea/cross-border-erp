package com.aidotnet.erp.common.som;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SomExtensionService {

    private static final Logger log = LoggerFactory.getLogger(SomExtensionService.class);
    private final Map<String, Listing> listings = new ConcurrentHashMap<>();
    private final Map<String, ChannelSkuMapping> channelSkuMappings = new ConcurrentHashMap<>();
    private final Map<String, PriceRule> priceRules = new ConcurrentHashMap<>();

    public Listing createListing(String tenantId, String storeId, String platform, String channelSku,
                                  String internalSku, String title, BigDecimal price, String currency,
                                  String status, Map<String, Object> attributes) {
        String listingId = "LST-" + System.currentTimeMillis();
        Listing listing = new Listing(listingId, tenantId, storeId, platform, channelSku,
                internalSku, title, price, currency, status != null ? status : "DRAFT",
                attributes, Instant.now(), Instant.now());
        listings.put(listingId, listing);
        log.info("Created listing: id={}, store={}, channelSku={}", listingId, storeId, channelSku);
        return listing;
    }

    public Listing updateListing(String listingId, BigDecimal price, String status,
                                  Map<String, Object> attributes) {
        Listing listing = listings.get(listingId);
        if (listing == null) throw new IllegalArgumentException("Listing not found: " + listingId);
        Listing updated = new Listing(listing.listingId(), listing.tenantId(), listing.storeId(),
                listing.platform(), listing.channelSku(), listing.internalSku(), listing.title(),
                price != null ? price : listing.price(), listing.currency(),
                status != null ? status : listing.status(),
                attributes != null ? attributes : listing.attributes(),
                listing.createdAt(), Instant.now());
        listings.put(listingId, updated);
        log.info("Updated listing: id={}, price={}, status={}", listingId, price, status);
        return updated;
    }

    public List<Listing> listListings(String tenantId, String storeId, String platform) {
        return listings.values().stream()
                .filter(l -> tenantId == null || tenantId.equals(l.tenantId()))
                .filter(l -> storeId == null || storeId.equals(l.storeId()))
                .filter(l -> platform == null || platform.equals(l.platform()))
                .toList();
    }

    public Listing getListingByChannelSku(String tenantId, String platform, String channelSku) {
        return listings.values().stream()
                .filter(l -> tenantId.equals(l.tenantId()) && platform.equals(l.platform()) && channelSku.equals(l.channelSku()))
                .findFirst()
                .orElse(null);
    }

    public ChannelSkuMapping createChannelSkuMapping(String tenantId, String platform, String channelSku,
                                                       String internalSku, String channelAsin,
                                                       String productName) {
        String mappingId = "MAP-" + System.currentTimeMillis();
        ChannelSkuMapping mapping = new ChannelSkuMapping(mappingId, tenantId, platform,
                channelSku, internalSku, channelAsin, productName, true, Instant.now());
        channelSkuMappings.put(mappingId, mapping);
        log.info("Created channel SKU mapping: id={}, platform={}, channelSku={}, internalSku={}",
                mappingId, platform, channelSku, internalSku);
        return mapping;
    }

    public ChannelSkuMapping updateChannelSkuMapping(String mappingId, String internalSku, boolean active) {
        ChannelSkuMapping mapping = channelSkuMappings.get(mappingId);
        if (mapping == null) throw new IllegalArgumentException("Mapping not found: " + mappingId);
        ChannelSkuMapping updated = new ChannelSkuMapping(mapping.mappingId(), mapping.tenantId(),
                mapping.platform(), mapping.channelSku(), internalSku != null ? internalSku : mapping.internalSku(),
                mapping.channelAsin(), mapping.productName(), active, mapping.createdAt());
        channelSkuMappings.put(mappingId, updated);
        log.info("Updated channel SKU mapping: id={}", mappingId);
        return updated;
    }

    public List<ChannelSkuMapping> listChannelSkuMappings(String tenantId, String platform) {
        return channelSkuMappings.values().stream()
                .filter(m -> tenantId == null || tenantId.equals(m.tenantId()))
                .filter(m -> platform == null || platform.equals(m.platform()))
                .toList();
    }

    public ChannelSkuMapping resolveInternalSku(String tenantId, String platform, String channelSku) {
        return channelSkuMappings.values().stream()
                .filter(m -> tenantId.equals(m.tenantId()) && platform.equals(m.platform())
                        && channelSku.equals(m.channelSku()) && m.active())
                .findFirst()
                .orElse(null);
    }

    public PriceRule createPriceRule(String tenantId, String ruleName, String ruleType, String platform,
                                      String marketplaceId, BigDecimal basePrice, BigDecimal minPrice,
                                      BigDecimal maxPrice, Map<String, Object> conditions) {
        String ruleId = "PR-" + System.currentTimeMillis();
        PriceRule rule = new PriceRule(ruleId, tenantId, ruleName, ruleType, platform,
                marketplaceId, basePrice, minPrice, maxPrice, conditions, true, Instant.now());
        priceRules.put(ruleId, rule);
        log.info("Created price rule: id={}, name={}, type={}", ruleId, ruleName, ruleType);
        return rule;
    }

    public PriceCalculationResult calculatePrice(String ruleId, BigDecimal costPrice, Map<String, BigDecimal> factors) {
        PriceRule rule = priceRules.get(ruleId);
        if (rule == null) throw new IllegalArgumentException("Price rule not found: " + ruleId);

        BigDecimal calculatedPrice = rule.basePrice();
        switch (rule.ruleType()) {
            case "MARKUP" -> {
                BigDecimal markupRate = factors.getOrDefault("markupRate", new BigDecimal("1.3"));
                calculatedPrice = costPrice.multiply(markupRate).setScale(2, RoundingMode.HALF_UP);
            }
            case "FIXED_MARGIN" -> {
                BigDecimal marginRate = factors.getOrDefault("marginRate", new BigDecimal("0.30"));
                calculatedPrice = costPrice.divide(BigDecimal.ONE.subtract(marginRate), 2, RoundingMode.HALF_UP);
            }
            case "COMPETITIVE" -> {
                BigDecimal competitorPrice = factors.getOrDefault("competitorPrice", rule.basePrice());
                BigDecimal adjustment = factors.getOrDefault("adjustment", new BigDecimal("0.95"));
                calculatedPrice = competitorPrice.multiply(adjustment).setScale(2, RoundingMode.HALF_UP);
            }
            case "DYNAMIC" -> {
                BigDecimal demandFactor = factors.getOrDefault("demandFactor", BigDecimal.ONE);
                BigDecimal supplyFactor = factors.getOrDefault("supplyFactor", BigDecimal.ONE);
                calculatedPrice = rule.basePrice().multiply(demandFactor).divide(supplyFactor, 2, RoundingMode.HALF_UP);
            }
        }

        if (rule.minPrice() != null && calculatedPrice.compareTo(rule.minPrice()) < 0) {
            calculatedPrice = rule.minPrice();
        }
        if (rule.maxPrice() != null && calculatedPrice.compareTo(rule.maxPrice()) > 0) {
            calculatedPrice = rule.maxPrice();
        }

        BigDecimal margin = calculatedPrice.subtract(costPrice);
        BigDecimal marginRate = calculatedPrice.compareTo(BigDecimal.ZERO) > 0
                ? margin.divide(calculatedPrice, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        log.info("Calculated price: rule={}, cost={}, calculated={}, margin={}%",
                ruleId, costPrice, calculatedPrice, marginRate);
        return new PriceCalculationResult(ruleId, rule.ruleType(), costPrice, calculatedPrice,
                margin, marginRate, rule.minPrice(), rule.maxPrice());
    }

    public List<PriceRule> listPriceRules(String tenantId, String platform) {
        return priceRules.values().stream()
                .filter(r -> tenantId == null || tenantId.equals(r.tenantId()))
                .filter(r -> platform == null || platform.equals(r.platform()))
                .toList();
    }

    public record Listing(String listingId, String tenantId, String storeId, String platform,
                           String channelSku, String internalSku, String title, BigDecimal price,
                           String currency, String status, Map<String, Object> attributes,
                           Instant createdAt, Instant updatedAt) {}
    public record ChannelSkuMapping(String mappingId, String tenantId, String platform, String channelSku,
                                     String internalSku, String channelAsin, String productName,
                                     boolean active, Instant createdAt) {}
    public record PriceRule(String ruleId, String tenantId, String ruleName, String ruleType, String platform,
                             String marketplaceId, BigDecimal basePrice, BigDecimal minPrice, BigDecimal maxPrice,
                             Map<String, Object> conditions, boolean active, Instant createdAt) {}
    public record PriceCalculationResult(String ruleId, String ruleType, BigDecimal costPrice,
                                          BigDecimal calculatedPrice, BigDecimal margin,
                                          BigDecimal marginRate, BigDecimal minPrice, BigDecimal maxPrice) {}
}
