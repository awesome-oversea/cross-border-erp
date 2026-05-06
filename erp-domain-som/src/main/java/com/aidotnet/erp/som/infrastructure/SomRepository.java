package com.aidotnet.erp.som.infrastructure;

import com.aidotnet.erp.som.domain.AlertSeverity;
import com.aidotnet.erp.som.domain.ChannelSku;
import com.aidotnet.erp.som.domain.ChannelSkuStatus;
import com.aidotnet.erp.som.domain.Listing;
import com.aidotnet.erp.som.domain.ListingOptimization;
import com.aidotnet.erp.som.domain.ListingPerformance;
import com.aidotnet.erp.som.domain.ListingStatus;
import com.aidotnet.erp.som.domain.OptimizationType;
import com.aidotnet.erp.som.domain.PmsListingSuggestion;
import com.aidotnet.erp.som.domain.PriceRule;
import com.aidotnet.erp.som.domain.PriceRuleStatus;
import com.aidotnet.erp.som.domain.PriceRuleType;
import com.aidotnet.erp.som.domain.SalesAlert;
import com.aidotnet.erp.som.infrastructure.data.ChannelSkuDO;
import com.aidotnet.erp.som.infrastructure.data.ListingDO;
import com.aidotnet.erp.som.infrastructure.data.ListingOptimizationDO;
import com.aidotnet.erp.som.infrastructure.data.ListingPerformanceDO;
import com.aidotnet.erp.som.infrastructure.data.PmsListingSuggestionDO;
import com.aidotnet.erp.som.infrastructure.data.PriceRuleDO;
import com.aidotnet.erp.som.infrastructure.data.SalesAlertDO;
import com.aidotnet.erp.som.infrastructure.mapper.SomMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

/**
 * SOM域Listing数据存储
 * <p>
 * 描述: 销售运营域核心数据存储层，负责Listing、渠道SKU、价格规则、
 *       优化记录、表现数据、PMS建议、销售告警等实体的CRUD操作。
 *       基于MyBatis持久化存储，提供领域对象与数据对象的转换。
 * </p>
 *
 * @author ERP系统
 */
@Repository
public class SomRepository {

    /** Listing数据MyBatis映射器 */
    private final SomMapper mapper;

    /**
     * 构造函数 - 依赖注入映射器
     *
     * @param mapper SOM域MyBatis映射器
     */
    public SomRepository(SomMapper mapper) {
        this.mapper = mapper;
    }

    /** 保存Listing，存在则更新，不存在则新增 */
    public Listing saveListing(Listing listing) {
        ListingDO existing = mapper.selectListing(listing.tenantId(), listing.listingId());
        ListingDO data = toListingData(listing);
        if (existing == null) {
            mapper.insertListing(data);
        } else {
            mapper.updateListing(data);
        }
        return listing;
    }

    public Optional<Listing> findListing(String tenantId, String listingId) {
        return Optional.ofNullable(mapper.selectListing(tenantId, listingId)).map(this::toListingDomain);
    }

    public Optional<Listing> findListingByMarketplaceId(String tenantId, String platform, String marketplaceListingId) {
        return Optional.ofNullable(mapper.selectListingByMarketplaceId(tenantId, platform, marketplaceListingId)).map(this::toListingDomain);
    }

    public List<Listing> listListings(String tenantId) {
        return mapper.selectListings(tenantId).stream().map(this::toListingDomain).collect(Collectors.toList());
    }

    public List<Listing> listListingsByStore(String tenantId, String storeId) {
        return mapper.selectListingsByStore(tenantId, storeId).stream().map(this::toListingDomain).collect(Collectors.toList());
    }

    public List<Listing> listListingsByStatus(String tenantId, ListingStatus status) {
        return mapper.selectListingsByStatus(tenantId, status.name()).stream().map(this::toListingDomain).collect(Collectors.toList());
    }

    public ChannelSku saveChannelSku(ChannelSku channelSku) {
        ChannelSkuDO existing = mapper.selectChannelSku(channelSku.tenantId(), channelSku.channelSkuId());
        ChannelSkuDO data = toChannelSkuData(channelSku);
        if (existing == null) {
            mapper.insertChannelSku(data);
        } else {
            mapper.updateChannelSku(data);
        }
        return channelSku;
    }

    public Optional<ChannelSku> findChannelSku(String tenantId, String channelSkuId) {
        return Optional.ofNullable(mapper.selectChannelSku(tenantId, channelSkuId)).map(this::toChannelSkuDomain);
    }

    public Optional<ChannelSku> findChannelSkuByMapping(String tenantId, String productSku, String channel, String channelSku) {
        return Optional.ofNullable(mapper.selectChannelSkuByMapping(tenantId, productSku, channel, channelSku)).map(this::toChannelSkuDomain);
    }

    public Optional<ChannelSku> findChannelSkuByExternalSku(String tenantId, String channel, String storeId,
                                                            String marketplaceId, String channelSku) {
        return Optional.ofNullable(mapper.selectChannelSkuByExternalSku(tenantId, channel, storeId, marketplaceId, channelSku))
                .map(this::toChannelSkuDomain);
    }

    public List<ChannelSku> listChannelSkus(String tenantId) {
        return mapper.selectChannelSkus(tenantId).stream().map(this::toChannelSkuDomain).collect(Collectors.toList());
    }

    public List<ChannelSku> listChannelSkusByProductSku(String tenantId, String productSku) {
        return mapper.selectChannelSkusByProductSku(tenantId, productSku).stream().map(this::toChannelSkuDomain).collect(Collectors.toList());
    }

    public PriceRule savePriceRule(PriceRule priceRule) {
        PriceRuleDO existing = mapper.selectPriceRule(priceRule.tenantId(), priceRule.ruleId());
        PriceRuleDO data = toPriceRuleData(priceRule);
        if (existing == null) {
            mapper.insertPriceRule(data);
        } else {
            mapper.updatePriceRule(data);
        }
        return priceRule;
    }

    public Optional<PriceRule> findPriceRule(String tenantId, String ruleId) {
        return Optional.ofNullable(mapper.selectPriceRule(tenantId, ruleId)).map(this::toPriceRuleDomain);
    }

    public List<PriceRule> listPriceRules(String tenantId) {
        return mapper.selectPriceRules(tenantId).stream().map(this::toPriceRuleDomain).collect(Collectors.toList());
    }

    public List<PriceRule> listActivePriceRules(String tenantId) {
        return mapper.selectActivePriceRules(tenantId).stream().map(this::toPriceRuleDomain).collect(Collectors.toList());
    }

    public ListingOptimization saveListingOptimization(ListingOptimization optimization) {
        mapper.insertListingOptimization(toListingOptimizationData(optimization));
        return optimization;
    }

    public List<ListingOptimization> listListingOptimizations(String tenantId, String listingId) {
        return mapper.selectListingOptimizations(tenantId, listingId).stream().map(this::toListingOptimizationDomain).collect(Collectors.toList());
    }

    public ListingPerformance saveListingPerformance(ListingPerformance performance) {
        mapper.insertListingPerformance(toListingPerformanceData(performance));
        return performance;
    }

    public List<ListingPerformance> listListingPerformances(String tenantId, String listingId) {
        return mapper.selectListingPerformances(tenantId, listingId).stream().map(this::toListingPerformanceDomain).collect(Collectors.toList());
    }

    public List<ListingPerformance> listPerformancesByStore(String tenantId, String storeId) {
        return mapper.selectPerformancesByStore(tenantId, storeId).stream().map(this::toListingPerformanceDomain).collect(Collectors.toList());
    }

    public PmsListingSuggestion savePmsSuggestion(PmsListingSuggestion suggestion) {
        PmsListingSuggestionDO existing = mapper.selectPmsSuggestionByIdempotencyKey(suggestion.idempotencyKey());
        if (existing != null) {
            return toPmsSuggestionDomain(existing);
        }
        mapper.insertPmsSuggestion(toPmsSuggestionData(suggestion));
        return suggestion;
    }

    public Optional<PmsListingSuggestion> findPmsSuggestion(String tenantId, String suggestionId) {
        return Optional.ofNullable(mapper.selectPmsSuggestion(tenantId, suggestionId)).map(this::toPmsSuggestionDomain);
    }

    public List<PmsListingSuggestion> listPmsSuggestionsByListing(String tenantId, String listingId) {
        return mapper.selectPmsSuggestionsByListing(tenantId, listingId).stream().map(this::toPmsSuggestionDomain).collect(Collectors.toList());
    }

    public void updatePmsSuggestionStatus(String suggestionId, String status) {
        mapper.updatePmsSuggestionStatus(suggestionId, status);
    }

    public SalesAlert saveSalesAlert(SalesAlert alert) {
        mapper.insertSalesAlert(toSalesAlertData(alert));
        return alert;
    }

    public List<SalesAlert> listSalesAlerts(String tenantId, String storeId) {
        return mapper.selectSalesAlerts(tenantId, storeId).stream().map(this::toSalesAlertDomain).collect(Collectors.toList());
    }

    public List<SalesAlert> listUnacknowledgedAlerts(String tenantId) {
        return mapper.selectUnacknowledgedAlerts(tenantId).stream().map(this::toSalesAlertDomain).collect(Collectors.toList());
    }

    public void acknowledgeAlert(String alertId, String tenantId) {
        mapper.acknowledgeAlert(alertId, tenantId);
    }

    private ListingDO toListingData(Listing l) {
        ListingDO data = new ListingDO();
        data.setListingId(l.listingId());
        data.setTenantId(l.tenantId());
        data.setProductId(l.productId());
        data.setStoreId(l.storeId());
        data.setTitle(l.title());
        data.setDescription(l.description());
        data.setPrice(l.price());
        data.setOriginalPrice(l.originalPrice());
        data.setPlatform(l.platform());
        data.setMarketplace(l.marketplace());
        data.setMarketplaceListingId(l.marketplaceListingId());
        data.setQualityScore(l.qualityScore());
        data.setStatus(l.status().name());
        data.setCreatedAt(l.createdAt() != null ? l.createdAt() : Instant.now());
        data.setUpdatedAt(l.updatedAt() != null ? l.updatedAt() : Instant.now());
        return data;
    }

    private Listing toListingDomain(ListingDO d) {
        return new Listing(d.getListingId(), d.getTenantId(), d.getProductId(), d.getStoreId(), d.getTitle(),
                d.getDescription(), d.getPrice(), d.getOriginalPrice(), d.getPlatform(), d.getMarketplace(),
                d.getMarketplaceListingId(), d.getQualityScore(), ListingStatus.valueOf(d.getStatus()),
                d.getCreatedAt(), d.getUpdatedAt());
    }

    private ChannelSkuDO toChannelSkuData(ChannelSku c) {
        ChannelSkuDO data = new ChannelSkuDO();
        data.setChannelSkuId(c.channelSkuId());
        data.setTenantId(c.tenantId());
        data.setProductSku(c.productSku());
        data.setChannel(c.channel());
        data.setChannelSku(c.channelSku());
        data.setStoreId(c.storeId());
        data.setMarketplaceId(c.marketplaceId());
        data.setStatus(c.status().name());
        data.setCreatedAt(c.createdAt() != null ? c.createdAt() : Instant.now());
        data.setUpdatedAt(c.updatedAt() != null ? c.updatedAt() : Instant.now());
        return data;
    }

    private ChannelSku toChannelSkuDomain(ChannelSkuDO d) {
        return new ChannelSku(d.getChannelSkuId(), d.getTenantId(), d.getProductSku(), d.getChannel(),
                d.getChannelSku(), d.getStoreId(), d.getMarketplaceId(), ChannelSkuStatus.valueOf(d.getStatus()),
                d.getCreatedAt(), d.getUpdatedAt());
    }

    private PriceRuleDO toPriceRuleData(PriceRule r) {
        PriceRuleDO data = new PriceRuleDO();
        data.setRuleId(r.ruleId());
        data.setTenantId(r.tenantId());
        data.setName(r.name());
        data.setType(r.type().name());
        data.setConditions(r.conditions());
        data.setActions(r.actions());
        data.setMinPrice(r.minPrice());
        data.setMaxPrice(r.maxPrice());
        data.setStatus(r.status().name());
        data.setCreatedAt(r.createdAt() != null ? r.createdAt() : Instant.now());
        data.setUpdatedAt(r.updatedAt() != null ? r.updatedAt() : Instant.now());
        return data;
    }

    private PriceRule toPriceRuleDomain(PriceRuleDO d) {
        return new PriceRule(d.getRuleId(), d.getTenantId(), d.getName(), PriceRuleType.valueOf(d.getType()),
                d.getConditions(), d.getActions(), d.getMinPrice(), d.getMaxPrice(),
                PriceRuleStatus.valueOf(d.getStatus()), d.getCreatedAt(), d.getUpdatedAt());
    }

    private ListingOptimizationDO toListingOptimizationData(ListingOptimization o) {
        ListingOptimizationDO data = new ListingOptimizationDO();
        data.setOptimizationId(o.optimizationId());
        data.setTenantId(o.tenantId());
        data.setListingId(o.listingId());
        data.setType(o.type().name());
        data.setBeforeValue(o.beforeValue());
        data.setAfterValue(o.afterValue());
        data.setOperator(o.operator());
        data.setResult(o.result());
        data.setCreatedAt(o.createdAt() != null ? o.createdAt() : Instant.now());
        return data;
    }

    private ListingOptimization toListingOptimizationDomain(ListingOptimizationDO d) {
        return new ListingOptimization(d.getOptimizationId(), d.getTenantId(), d.getListingId(),
                OptimizationType.valueOf(d.getType()), d.getBeforeValue(),
                d.getAfterValue(), d.getOperator(), d.getResult(), d.getCreatedAt());
    }

    private ListingPerformanceDO toListingPerformanceData(ListingPerformance p) {
        ListingPerformanceDO data = new ListingPerformanceDO();
        data.setPerformanceId(p.performanceId());
        data.setTenantId(p.tenantId());
        data.setListingId(p.listingId());
        data.setStoreId(p.storeId());
        data.setPlatform(p.platform());
        data.setMarketplace(p.marketplace());
        data.setImpressions(p.impressions());
        data.setClicks(p.clicks());
        data.setCtr(p.ctr());
        data.setSpend(p.spend());
        data.setSales(p.sales());
        data.setAcos(p.acos());
        data.setOrders(p.orders());
        data.setConversionRate(p.conversionRate());
        data.setPeriodStart(p.periodStart());
        data.setPeriodEnd(p.periodEnd());
        data.setCreatedAt(p.createdAt() != null ? p.createdAt() : Instant.now());
        return data;
    }

    private ListingPerformance toListingPerformanceDomain(ListingPerformanceDO d) {
        return new ListingPerformance(d.getPerformanceId(), d.getTenantId(), d.getListingId(),
                d.getStoreId(), d.getPlatform(), d.getMarketplace(),
                d.getImpressions() != null ? d.getImpressions() : 0,
                d.getClicks() != null ? d.getClicks() : 0,
                d.getCtr(), d.getSpend(), d.getSales(), d.getAcos(),
                d.getOrders() != null ? d.getOrders() : 0,
                d.getConversionRate(), d.getPeriodStart(), d.getPeriodEnd(), d.getCreatedAt());
    }

    private PmsListingSuggestionDO toPmsSuggestionData(PmsListingSuggestion s) {
        PmsListingSuggestionDO data = new PmsListingSuggestionDO();
        data.setSuggestionId(s.suggestionId());
        data.setTenantId(s.tenantId());
        data.setListingId(s.listingId());
        data.setSuggestionType(s.suggestionType());
        data.setTitleSuggestion(s.titleSuggestion());
        data.setDescriptionSuggestion(s.descriptionSuggestion());
        data.setBulletPointsSuggestion(s.bulletPointsSuggestion());
        data.setPriceSuggestion(s.priceSuggestion());
        data.setReason(s.reason());
        data.setConfidence(s.confidence());
        data.setTraceId(s.traceId());
        data.setIdempotencyKey(s.idempotencyKey());
        data.setStatus(s.status());
        data.setCreatedAt(s.createdAt() != null ? s.createdAt() : Instant.now());
        return data;
    }

    private PmsListingSuggestion toPmsSuggestionDomain(PmsListingSuggestionDO d) {
        return new PmsListingSuggestion(d.getSuggestionId(), d.getTenantId(), d.getListingId(),
                d.getSuggestionType(), d.getTitleSuggestion(), d.getDescriptionSuggestion(),
                d.getBulletPointsSuggestion(), d.getPriceSuggestion(), d.getReason(),
                d.getConfidence(), d.getTraceId(), d.getIdempotencyKey(), d.getStatus(), d.getCreatedAt());
    }

    private SalesAlertDO toSalesAlertData(SalesAlert a) {
        SalesAlertDO data = new SalesAlertDO();
        data.setAlertId(a.alertId());
        data.setTenantId(a.tenantId());
        data.setStoreId(a.storeId());
        data.setAlertType(a.alertType());
        data.setSeverity(a.severity());
        data.setMessage(a.message());
        data.setRelatedSku(a.relatedSku());
        data.setMetricValue(a.metricValue());
        data.setThresholdValue(a.thresholdValue());
        data.setAcknowledged(a.acknowledged());
        data.setCreatedAt(a.createdAt() != null ? a.createdAt() : Instant.now());
        return data;
    }

    private SalesAlert toSalesAlertDomain(SalesAlertDO d) {
        return new SalesAlert(d.getAlertId(), d.getTenantId(), d.getStoreId(), d.getAlertType(),
                d.getSeverity(), d.getMessage(), d.getRelatedSku(), d.getMetricValue(),
                d.getThresholdValue(), d.getAcknowledged() != null ? d.getAcknowledged() : false, d.getCreatedAt());
    }
}
