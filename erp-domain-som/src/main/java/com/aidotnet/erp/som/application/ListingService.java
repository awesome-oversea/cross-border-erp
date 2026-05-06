package com.aidotnet.erp.som.application;

import com.aidotnet.erp.common.context.TraceContext;
import com.aidotnet.erp.common.event.DomainEventPublisher;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.som.domain.ChannelSku;
import com.aidotnet.erp.som.domain.ChannelSkuStatus;
import com.aidotnet.erp.som.domain.Listing;
import com.aidotnet.erp.som.domain.ListingOptimization;
import com.aidotnet.erp.som.domain.ListingPerformance;
import com.aidotnet.erp.som.domain.ListingPublishedEvent;
import com.aidotnet.erp.som.domain.ListingStatus;
import com.aidotnet.erp.som.domain.OptimizationType;
import com.aidotnet.erp.som.domain.PmsListingSuggestion;
import com.aidotnet.erp.som.domain.PriceRule;
import com.aidotnet.erp.som.domain.PriceRuleStatus;
import com.aidotnet.erp.som.domain.PriceRuleType;
import com.aidotnet.erp.som.domain.SalesAlert;
import com.aidotnet.erp.som.infrastructure.SomRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Listing管理应用服务
 * <p>
 * 描述: 销售运营域核心服务，负责Listing商品管理、渠道SKU映射、价格规则、
 *       销售告警、PMS智能建议等业务逻辑。是连接产品主数据与各销售平台的桥梁。
 * </p>
 * <p>
 * 核心能力:
 *   1. Listing管理 - 创建/更新/上架/下架/归档商品Listing，支持批量状态变更
 *   2. 渠道SKU映射 - 建立产品SKU与渠道SKU的映射关系，支持多渠道分发
 *   3. 价格规则 - 创建/激活/停用价格规则，支持固定价和百分比两种类型
 *   4. Listing优化 - 记录Listing优化操作历史，追踪优化效果
 *   5. 销售表现 - 记录Listing在各平台的表现数据(曝光/点击/转化/ACOS)
 *   6. PMS建议 - 接收/应用/拒绝PMS智能优化建议(标题/描述/价格)
 *   7. 销售告警 - 创建/确认销售异常告警(库存不足/价格异常等)
 * </p>
 * <p>
 * 业务规则:
 *   1. 同一平台同一marketplaceListingId不可重复创建Listing
 *   2. 只有DRAFT/INACTIVE状态的Listing可以上架，上架时发布ListingPublishedEvent
 *   3. 只有ACTIVE状态的Listing可以下架，下架后才可归档
 *   4. 渠道SKU映射在同一租户+渠道下唯一
 *   5. 价格规则按顺序应用，固定价取最低价，百分比取保底价
 *   6. PMS建议只有pending状态可应用或拒绝
 * </p>
 *
 * @author ERP系统
 * @see ListingPublishedEvent
 * @see SomRepository
 */
@Service
public class ListingService {

    private final SomRepository somRepository;
    private final DomainEventPublisher eventPublisher;

    /**
     * 构造函数 - 依赖注入Listing存储和事件发布器
     *
     * @param somRepository  Listing数据存储
     * @param eventPublisher 领域事件发布器，用于发布Listing上架事件
     */
    public ListingService(SomRepository somRepository, DomainEventPublisher eventPublisher) {
        this.somRepository = somRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * 创建Listing
     * <p>
     * 校验平台Listing唯一性后创建新Listing，初始状态为DRAFT。
     * </p>
     *
     * @param tenantId 租户ID
     * @param command  创建Listing命令
     * @return 新创建的Listing实体
     * @throws BizException LISTING_DUPLICATED - 平台Listing已存在
     */
    @Transactional
    public Listing createListing(String tenantId, CreateListingCommand command) {
        if (command.marketplaceListingId() != null && !command.marketplaceListingId().isBlank()) {
            somRepository.findListingByMarketplaceId(tenantId, command.platform(), command.marketplaceListingId())
                    .ifPresent(existing -> {
                        throw new BizException("LISTING_DUPLICATED", "平台Listing已存在");
                    });
        }
        Instant now = Instant.now();
        return somRepository.saveListing(new Listing(UUID.randomUUID().toString(), tenantId, command.productId(),
                command.storeId(), command.title(), command.description(), command.price(), command.originalPrice(),
                command.platform(), command.marketplace(), command.marketplaceListingId(),
                null, ListingStatus.DRAFT, now, now));
    }

    @Transactional
    public Listing updateListing(String tenantId, String listingId, UpdateListingCommand command) {
        Listing listing = getListing(tenantId, listingId);
        Instant now = Instant.now();
        return somRepository.saveListing(new Listing(listing.listingId(), listing.tenantId(), listing.productId(),
                listing.storeId(), valueOrDefault(command.title(), listing.title()),
                valueOrDefault(command.description(), listing.description()),
                command.price() != null ? command.price() : listing.price(),
                command.originalPrice() != null ? command.originalPrice() : listing.originalPrice(),
                listing.platform(), listing.marketplace(), listing.marketplaceListingId(),
                listing.qualityScore(), listing.status(), listing.createdAt(), now));
    }

    @Transactional
    public Listing publishListing(String tenantId, String listingId) {
        Listing listing = getListing(tenantId, listingId);
        if (listing.status() == ListingStatus.ACTIVE) {
            throw new BizException("LISTING_ALREADY_ACTIVE", "Listing已上架");
        }
        Listing published = updateStatus(listing, ListingStatus.ACTIVE);
        eventPublisher.publish(new ListingPublishedEvent(UUID.randomUUID().toString(), tenantId,
                TraceContext.getTraceId(), listing.listingId(), listing.platform(), listing.marketplace(), Instant.now()));
        return published;
    }

    @Transactional
    public Listing unpublishListing(String tenantId, String listingId) {
        Listing listing = getListing(tenantId, listingId);
        if (listing.status() != ListingStatus.ACTIVE) {
            throw new BizException("LISTING_STATUS_INVALID", "只有上架Listing可以下架");
        }
        return updateStatus(listing, ListingStatus.INACTIVE);
    }

    @Transactional
    public Listing archiveListing(String tenantId, String listingId) {
        Listing listing = getListing(tenantId, listingId);
        if (listing.status() == ListingStatus.ACTIVE) {
            throw new BizException("LISTING_STATUS_INVALID", "上架Listing不可归档，请先下架");
        }
        return updateStatus(listing, ListingStatus.ARCHIVED);
    }

    @Transactional
    public Listing updateQualityScore(String tenantId, String listingId, BigDecimal qualityScore) {
        Listing listing = getListing(tenantId, listingId);
        Instant now = Instant.now();
        return somRepository.saveListing(new Listing(listing.listingId(), listing.tenantId(), listing.productId(),
                listing.storeId(), listing.title(), listing.description(), listing.price(), listing.originalPrice(),
                listing.platform(), listing.marketplace(), listing.marketplaceListingId(),
                qualityScore, listing.status(), listing.createdAt(), now));
    }

    @Transactional
    public List<Listing> batchUpdateStatus(String tenantId, List<String> listingIds, ListingStatus targetStatus) {
        return listingIds.stream().map(id -> {
            Listing listing = getListing(tenantId, id);
            if (targetStatus == ListingStatus.ACTIVE && listing.status() != ListingStatus.DRAFT
                    && listing.status() != ListingStatus.INACTIVE) {
                throw new BizException("LISTING_STATUS_INVALID", "Listing " + id + " 状态不允许上架");
            }
            Listing updated = updateStatus(listing, targetStatus);
            if (targetStatus == ListingStatus.ACTIVE) {
                eventPublisher.publish(new ListingPublishedEvent(UUID.randomUUID().toString(), tenantId,
                        TraceContext.getTraceId(), listing.listingId(), listing.platform(), listing.marketplace(), Instant.now()));
            }
            return updated;
        }).toList();
    }

    public List<Listing> listListings(String tenantId) {
        return somRepository.listListings(tenantId);
    }

    public List<Listing> listListingsByStore(String tenantId, String storeId) {
        return somRepository.listListingsByStore(tenantId, storeId);
    }

    public List<Listing> listListingsByStatus(String tenantId, ListingStatus status) {
        return somRepository.listListingsByStatus(tenantId, status);
    }

    public Listing getListing(String tenantId, String listingId) {
        return somRepository.findListing(tenantId, listingId)
                .orElseThrow(() -> new BizException("LISTING_NOT_FOUND", "Listing不存在"));
    }

    public ChannelSku createChannelSku(String tenantId, CreateChannelSkuCommand command) {
        ChannelSku scopedMapping = somRepository.findChannelSkuByExternalSku(
                tenantId, command.channel(), command.storeId(), command.marketplaceId(), command.channelSku())
                .orElse(null);
        if (scopedMapping != null) {
            if (scopedMapping.productSku().equals(command.productSku())) {
                if (scopedMapping.status() == ChannelSkuStatus.ACTIVE) {
                    throw new BizException("CHANNEL_SKU_DUPLICATED", "渠道SKU映射已存在");
                }
                return somRepository.saveChannelSku(new ChannelSku(
                        scopedMapping.channelSkuId(),
                        scopedMapping.tenantId(),
                        scopedMapping.productSku(),
                        scopedMapping.channel(),
                        scopedMapping.channelSku(),
                        scopedMapping.storeId(),
                        scopedMapping.marketplaceId(),
                        ChannelSkuStatus.ACTIVE,
                        scopedMapping.createdAt(),
                        Instant.now()));
            }
            if (scopedMapping.status() == ChannelSkuStatus.ACTIVE) {
                throw new BizException("CHANNEL_SKU_CONFLICT", "该店铺Seller SKU已绑定其他内部SKU");
            }
        }
        Instant now = Instant.now();
        return somRepository.saveChannelSku(new ChannelSku(UUID.randomUUID().toString(), tenantId, command.productSku(),
                command.channel(), command.channelSku(), command.storeId(), command.marketplaceId(), ChannelSkuStatus.ACTIVE, now, now));
    }

    public ChannelSku deactivateChannelSku(String tenantId, String channelSkuId) {
        ChannelSku channelSku = getChannelSku(tenantId, channelSkuId);
        return somRepository.saveChannelSku(new ChannelSku(channelSku.channelSkuId(), channelSku.tenantId(),
                channelSku.productSku(), channelSku.channel(), channelSku.channelSku(), channelSku.storeId(),
                channelSku.marketplaceId(), ChannelSkuStatus.INACTIVE, channelSku.createdAt(), Instant.now()));
    }

    public List<ChannelSku> listChannelSkus(String tenantId) {
        return somRepository.listChannelSkus(tenantId);
    }

    public List<ChannelSku> listChannelSkusByProductSku(String tenantId, String productSku) {
        return somRepository.listChannelSkusByProductSku(tenantId, productSku);
    }

    public PriceRule createPriceRule(String tenantId, CreatePriceRuleCommand command) {
        Instant now = Instant.now();
        return somRepository.savePriceRule(new PriceRule(UUID.randomUUID().toString(), tenantId, command.name(),
                command.type(), command.conditions(), command.actions(), command.minPrice(), command.maxPrice(),
                PriceRuleStatus.ACTIVE, now, now));
    }

    public PriceRule updatePriceRule(String tenantId, String ruleId, UpdatePriceRuleCommand command) {
        PriceRule rule = getPriceRule(tenantId, ruleId);
        Instant now = Instant.now();
        return somRepository.savePriceRule(new PriceRule(rule.ruleId(), rule.tenantId(),
                valueOrDefault(command.name(), rule.name()),
                command.type() != null ? command.type() : rule.type(),
                valueOrDefault(command.conditions(), rule.conditions()),
                valueOrDefault(command.actions(), rule.actions()),
                command.minPrice() != null ? command.minPrice() : rule.minPrice(),
                command.maxPrice() != null ? command.maxPrice() : rule.maxPrice(),
                rule.status(), rule.createdAt(), now));
    }

    public PriceRule activatePriceRule(String tenantId, String ruleId) {
        PriceRule rule = getPriceRule(tenantId, ruleId);
        return updatePriceRuleStatus(rule, PriceRuleStatus.ACTIVE);
    }

    public PriceRule deactivatePriceRule(String tenantId, String ruleId) {
        PriceRule rule = getPriceRule(tenantId, ruleId);
        return updatePriceRuleStatus(rule, PriceRuleStatus.INACTIVE);
    }

    public List<PriceRule> listPriceRules(String tenantId) {
        return somRepository.listPriceRules(tenantId);
    }

    public List<PriceRule> listActivePriceRules(String tenantId) {
        return somRepository.listActivePriceRules(tenantId);
    }

    public PriceRule getPriceRule(String tenantId, String ruleId) {
        return somRepository.findPriceRule(tenantId, ruleId)
                .orElseThrow(() -> new BizException("PRICE_RULE_NOT_FOUND", "价格规则不存在"));
    }

    public BigDecimal calculatePrice(String tenantId, BigDecimal basePrice, String channel, String marketplace) {
        List<PriceRule> activeRules = somRepository.listActivePriceRules(tenantId);
        BigDecimal result = basePrice;
        for (PriceRule rule : activeRules) {
            result = applyRule(rule, result, basePrice);
        }
        return result;
    }

    public PriceQuote calculatePriceQuote(String tenantId, CalculatePriceCommand command) {
        BigDecimal baseCost = defaultZero(command.procurementCost())
                .add(defaultZero(command.shippingFee()))
                .add(defaultZero(command.otherCost()));
        BigDecimal denominator = BigDecimal.ONE
                .subtract(defaultZero(command.commissionRate()))
                .subtract(defaultZero(command.targetProfitRate()));
        if (denominator.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("PRICE_CALCULATION_INVALID", "佣金率与目标利润率之和必须小于1");
        }

        BigDecimal suggestedPrice = baseCost.divide(denominator, 2, RoundingMode.HALF_UP);
        List<String> appliedRules = new java.util.ArrayList<>();
        for (PriceRule rule : somRepository.listActivePriceRules(tenantId)) {
            BigDecimal adjustedPrice = applyRule(rule, suggestedPrice, baseCost);
            if (adjustedPrice.compareTo(suggestedPrice) != 0) {
                appliedRules.add(rule.ruleId());
                suggestedPrice = adjustedPrice;
            }
        }

        BigDecimal estimatedProfit = calculateEstimatedProfit(
                suggestedPrice,
                command.procurementCost(),
                command.shippingFee(),
                command.otherCost(),
                command.commissionRate());
        BigDecimal estimatedProfitRate = suggestedPrice.compareTo(BigDecimal.ZERO) <= 0
                ? BigDecimal.ZERO
                : estimatedProfit.divide(suggestedPrice, 4, RoundingMode.HALF_UP);
        boolean lowProfit = estimatedProfitRate.compareTo(defaultZero(command.targetProfitRate())) < 0;

        return new PriceQuote(
                suggestedPrice,
                estimatedProfit,
                estimatedProfitRate,
                lowProfit,
                List.copyOf(appliedRules));
    }

    public ListingOptimization recordOptimization(String tenantId, String listingId, RecordOptimizationCommand command) {
        getListing(tenantId, listingId);
        return somRepository.saveListingOptimization(new ListingOptimization(UUID.randomUUID().toString(), tenantId,
                listingId, command.type(), command.beforeValue(), command.afterValue(), command.operator(), command.result(), Instant.now()));
    }

    public List<ListingOptimization> listOptimizations(String tenantId, String listingId) {
        return somRepository.listListingOptimizations(tenantId, listingId);
    }

    @Transactional
    public ListingPerformance recordPerformance(String tenantId, RecordPerformanceCommand command) {
        return somRepository.saveListingPerformance(new ListingPerformance(UUID.randomUUID().toString(), tenantId,
                command.listingId(), command.storeId(), command.platform(), command.marketplace(),
                command.impressions(), command.clicks(), command.ctr(), command.spend(), command.sales(),
                command.acos(), command.orders(), command.conversionRate(),
                command.periodStart(), command.periodEnd(), Instant.now()));
    }

    public List<ListingPerformance> listPerformances(String tenantId, String listingId) {
        return somRepository.listListingPerformances(tenantId, listingId);
    }

    public List<ListingPerformance> listPerformancesByStore(String tenantId, String storeId) {
        return somRepository.listPerformancesByStore(tenantId, storeId);
    }

    @Transactional
    public PmsListingSuggestion receivePmsSuggestion(String tenantId, ReceivePmsSuggestionCommand command) {
        PmsListingSuggestion suggestion = new PmsListingSuggestion(UUID.randomUUID().toString(), tenantId,
                command.listingId(), command.suggestionType(), command.titleSuggestion(),
                command.descriptionSuggestion(), command.bulletPointsSuggestion(), command.priceSuggestion(),
                command.reason(), command.confidence(), command.traceId(), command.idempotencyKey(),
                "pending", Instant.now());
        return somRepository.savePmsSuggestion(suggestion);
    }

    @Transactional
    public Listing applyPmsSuggestion(String tenantId, String suggestionId) {
        PmsListingSuggestion suggestion = somRepository.findPmsSuggestion(tenantId, suggestionId)
                .orElseThrow(() -> new BizException("SUGGESTION_NOT_FOUND", "PMS建议不存在"));
        if (!"pending".equals(suggestion.status())) {
            throw new BizException("SUGGESTION_STATUS_INVALID", "只有待处理建议可以应用");
        }
        Listing listing = getListing(tenantId, suggestion.listingId());
        Instant now = Instant.now();
        Listing updated = new Listing(listing.listingId(), listing.tenantId(), listing.productId(),
                listing.storeId(),
                suggestion.titleSuggestion() != null ? suggestion.titleSuggestion() : listing.title(),
                suggestion.descriptionSuggestion() != null ? suggestion.descriptionSuggestion() : listing.description(),
                suggestion.priceSuggestion() != null ? suggestion.priceSuggestion() : listing.price(),
                listing.originalPrice(), listing.platform(), listing.marketplace(), listing.marketplaceListingId(),
                listing.qualityScore(), listing.status(), listing.createdAt(), now);
        somRepository.updatePmsSuggestionStatus(suggestionId, "applied");
        return somRepository.saveListing(updated);
    }

    @Transactional
    public void rejectPmsSuggestion(String tenantId, String suggestionId) {
        PmsListingSuggestion suggestion = somRepository.findPmsSuggestion(tenantId, suggestionId)
                .orElseThrow(() -> new BizException("SUGGESTION_NOT_FOUND", "PMS建议不存在"));
        if (!"pending".equals(suggestion.status())) {
            throw new BizException("SUGGESTION_STATUS_INVALID", "只有待处理建议可以拒绝");
        }
        somRepository.updatePmsSuggestionStatus(suggestionId, "rejected");
    }

    public List<PmsListingSuggestion> listPmsSuggestions(String tenantId, String listingId) {
        return somRepository.listPmsSuggestionsByListing(tenantId, listingId);
    }

    @Transactional
    public SalesAlert createAlert(String tenantId, CreateAlertCommand command) {
        return somRepository.saveSalesAlert(new SalesAlert(UUID.randomUUID().toString(), tenantId,
                command.storeId(), command.alertType(), command.severity(), command.message(),
                command.relatedSku(), command.metricValue(), command.thresholdValue(),
                false, Instant.now()));
    }

    public List<SalesAlert> listAlerts(String tenantId, String storeId) {
        return somRepository.listSalesAlerts(tenantId, storeId);
    }

    public List<SalesAlert> listUnacknowledgedAlerts(String tenantId) {
        return somRepository.listUnacknowledgedAlerts(tenantId);
    }

    @Transactional
    public void acknowledgeAlert(String tenantId, String alertId) {
        somRepository.acknowledgeAlert(alertId, tenantId);
    }

    private Listing updateStatus(Listing listing, ListingStatus status) {
        return somRepository.saveListing(new Listing(listing.listingId(), listing.tenantId(), listing.productId(),
                listing.storeId(), listing.title(), listing.description(), listing.price(), listing.originalPrice(),
                listing.platform(), listing.marketplace(), listing.marketplaceListingId(),
                listing.qualityScore(), status, listing.createdAt(), Instant.now()));
    }

    private PriceRule updatePriceRuleStatus(PriceRule rule, PriceRuleStatus status) {
        return somRepository.savePriceRule(new PriceRule(rule.ruleId(), rule.tenantId(), rule.name(), rule.type(),
                rule.conditions(), rule.actions(), rule.minPrice(), rule.maxPrice(), status, rule.createdAt(), Instant.now()));
    }

    private ChannelSku getChannelSku(String tenantId, String channelSkuId) {
        return somRepository.findChannelSku(tenantId, channelSkuId)
                .orElseThrow(() -> new BizException("CHANNEL_SKU_NOT_FOUND", "渠道SKU不存在"));
    }

    private BigDecimal applyRule(PriceRule rule, BigDecimal currentPrice, BigDecimal baseCost) {
        if (rule.type() == PriceRuleType.FIXED && rule.maxPrice() != null) {
            return currentPrice.min(rule.maxPrice()).setScale(2, RoundingMode.HALF_UP);
        }
        if (rule.type() == PriceRuleType.PERCENTAGE) {
            BigDecimal percentage = extractDecimal(rule.actions(), "rate");
            if (percentage != null) {
                BigDecimal adjusted = currentPrice.multiply(BigDecimal.ONE.add(percentage));
                return applyBounds(adjusted, rule);
            }
            if (rule.minPrice() != null) {
                return currentPrice.max(rule.minPrice()).setScale(2, RoundingMode.HALF_UP);
            }
        }
        if (rule.type() == PriceRuleType.COST_PLUS) {
            BigDecimal markupRate = extractDecimal(rule.actions(), "markupRate");
            if (markupRate != null) {
                BigDecimal adjusted = baseCost.multiply(BigDecimal.ONE.add(markupRate));
                return applyBounds(currentPrice.max(adjusted), rule);
            }
        }
        if (rule.minPrice() != null && currentPrice.compareTo(rule.minPrice()) < 0) {
            return rule.minPrice().setScale(2, RoundingMode.HALF_UP);
        }
        return currentPrice;
    }

    private BigDecimal applyBounds(BigDecimal value, PriceRule rule) {
        BigDecimal adjusted = value;
        if (rule.minPrice() != null && adjusted.compareTo(rule.minPrice()) < 0) {
            adjusted = rule.minPrice();
        }
        if (rule.maxPrice() != null && adjusted.compareTo(rule.maxPrice()) > 0) {
            adjusted = rule.maxPrice();
        }
        return adjusted.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateEstimatedProfit(BigDecimal salePrice,
                                                BigDecimal procurementCost,
                                                BigDecimal shippingFee,
                                                BigDecimal otherCost,
                                                BigDecimal commissionRate) {
        BigDecimal commissionAmount = salePrice.multiply(defaultZero(commissionRate)).setScale(2, RoundingMode.HALF_UP);
        return salePrice
                .subtract(defaultZero(procurementCost))
                .subtract(defaultZero(shippingFee))
                .subtract(defaultZero(otherCost))
                .subtract(commissionAmount)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private BigDecimal extractDecimal(String jsonLikeContent, String fieldName) {
        if (jsonLikeContent == null || jsonLikeContent.isBlank()) {
            return null;
        }
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("\"" + java.util.regex.Pattern.quote(fieldName) + "\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)")
                .matcher(jsonLikeContent);
        if (!matcher.find()) {
            return null;
        }
        return new BigDecimal(matcher.group(1));
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value != null ? value : defaultValue;
    }

    public record CreateListingCommand(String productId, String storeId, String title, String description,
                                       BigDecimal price, BigDecimal originalPrice, String platform, String marketplace,
                                       String marketplaceListingId) {}

    public record UpdateListingCommand(String title, String description, BigDecimal price, BigDecimal originalPrice) {}

    public record CreateChannelSkuCommand(String productSku, String channel, String channelSku,
                                          String storeId, String marketplaceId) {}

    public record CreatePriceRuleCommand(String name, PriceRuleType type, String conditions, String actions,
                                         BigDecimal minPrice, BigDecimal maxPrice) {}

    public record UpdatePriceRuleCommand(String name, PriceRuleType type, String conditions, String actions,
                                         BigDecimal minPrice, BigDecimal maxPrice) {}

    public record CalculatePriceCommand(String platform, String marketplace, BigDecimal procurementCost,
                                        BigDecimal shippingFee, BigDecimal otherCost, BigDecimal commissionRate,
                                        BigDecimal targetProfitRate) {}

    public record PriceQuote(BigDecimal suggestedPrice, BigDecimal estimatedProfit, BigDecimal estimatedProfitRate,
                             boolean lowProfit, List<String> appliedRules) {}

    public record RecordOptimizationCommand(OptimizationType type, String beforeValue, String afterValue,
                                            String operator, String result) {}

    public record RecordPerformanceCommand(String listingId, String storeId, String platform, String marketplace,
                                           int impressions, int clicks, BigDecimal ctr, BigDecimal spend,
                                           BigDecimal sales, BigDecimal acos, int orders, BigDecimal conversionRate,
                                           Instant periodStart, Instant periodEnd) {}

    public record ReceivePmsSuggestionCommand(String listingId, String suggestionType, String titleSuggestion,
                                              String descriptionSuggestion, String bulletPointsSuggestion,
                                              BigDecimal priceSuggestion, String reason, String confidence,
                                              String traceId, String idempotencyKey) {}

    public record CreateAlertCommand(String storeId, String alertType, String severity, String message,
                                     String relatedSku, BigDecimal metricValue, BigDecimal thresholdValue) {}
}
