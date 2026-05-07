package com.aidotnet.erp.bi.application;

import com.aidotnet.erp.bi.client.CrmClient;
import com.aidotnet.erp.bi.client.FbaClient;
import com.aidotnet.erp.bi.client.FmsClient;
import com.aidotnet.erp.bi.client.SomClient;
import com.aidotnet.erp.bi.client.WmsClient;
import com.aidotnet.erp.bi.domain.AlertCenterReport;
import com.aidotnet.erp.bi.domain.AlertCondition;
import com.aidotnet.erp.bi.domain.AlertRule;
import com.aidotnet.erp.bi.domain.FbaShipmentAnalysisReport;
import com.aidotnet.erp.bi.domain.FbaShipmentAnalysisReport.ShipmentAnalysisItem;
import com.aidotnet.erp.bi.domain.FbaShipmentAnalysisReport.Summary;
import com.aidotnet.erp.bi.domain.KpiAlert;
import com.aidotnet.erp.bi.domain.OperationMonitorReport;
import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * BI 经营分析服务
 * <p>
 * 当前承担两类高价值读模型:
 * 1. FBA 货件时效/异常/成本分析
 * 2. 运营监控聚合视图，汇总 SOM/WMS/CRM/BI 多域信号
 * </p>
 */
@Service
public class BiOperationalService {

    private static final List<String> SHIPMENT_COST_SOURCE_TYPES = List.of("SHIPMENT", "FBA_SHIPMENT");
    private static final String OPERATION_MONITOR_DOMAIN = "operation-monitor";
    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String RESOLVED_STATUS = "RESOLVED";
    private static final String CLOSED_STATUS = "CLOSED";
    private static final String SHIPPED_STATUS = "SHIPPED";
    private static final String RECEIVED_STATUS = "RECEIVED";
    private static final String METRIC_LISTING_ORDERS = "listing_orders";
    private static final String METRIC_PRICE_GAP_RATE = "price_gap_rate";
    private static final String METRIC_BUYBOX_HIJACKER_COUNT = "buybox_hijacker_count";
    private static final String METRIC_INVENTORY_AVAILABLE = "inventory_available";
    private static final String METRIC_AVERAGE_RATING = "average_rating";
    private static final String ANOMALY_SALES = "sales";
    private static final String ANOMALY_PRICE = "price";
    private static final String ANOMALY_BUYBOX = "buybox";
    private static final String ANOMALY_INVENTORY = "inventory";
    private static final String ANOMALY_REVIEW = "review";
    private static final String ALERT_STATUS_OPEN = "OPEN";
    private static final String ALERT_STATUS_ACKNOWLEDGED = "ACKNOWLEDGED";
    private static final String ALERT_STATUS_RESOLVED = "RESOLVED";
    private static final String ALERT_CATEGORY_PROFIT = "profit";
    private static final String ALERT_CATEGORY_KPI = "kpi";
    private static final String ALERT_CATEGORY_LOGISTICS = "logistics";
    private static final String SOURCE_DOMAIN_BI = "BI";
    private static final String SOURCE_DOMAIN_SOM = "SOM";
    private static final String SOURCE_DOMAIN_WMS = "WMS";
    private static final String SOURCE_DOMAIN_CRM = "CRM";
    private static final String SOURCE_DOMAIN_FMS = "FMS";
    private static final String SOURCE_DOMAIN_FBA = "FBA";

    private final FbaClient fbaClient;
    private final FmsClient fmsClient;
    private final SomClient somClient;
    private final WmsClient wmsClient;
    private final CrmClient crmClient;
    private final BiExtService biExtService;
    private final ReportService reportService;

    public BiOperationalService(FbaClient fbaClient, FmsClient fmsClient, SomClient somClient,
                                WmsClient wmsClient, CrmClient crmClient, BiExtService biExtService,
                                ReportService reportService) {
        this.fbaClient = fbaClient;
        this.fmsClient = fmsClient;
        this.somClient = somClient;
        this.wmsClient = wmsClient;
        this.crmClient = crmClient;
        this.biExtService = biExtService;
        this.reportService = reportService;
    }

    public FbaShipmentAnalysisReport analyzeFbaShipments(String tenantId) {
        List<FbaClient.FbaShipmentResponse> shipments = safeList(requireData(
                fbaClient.listShipments(null),
                "BI_FBA_SHIPMENT_ANALYSIS_SOURCE_FAILED",
                "获取 FBA 货件数据失败"));

        List<ShipmentAnalysisItem> items = shipments.stream()
                .map(this::buildShipmentAnalysisItem)
                .sorted(Comparator.comparing(ShipmentAnalysisItem::updatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(ShipmentAnalysisItem::shipmentId, Comparator.nullsLast(String::compareTo)))
                .toList();

        return new FbaShipmentAnalysisReport(buildSummary(items), items, Instant.now());
    }

    /**
     * 生成运营监控聚合视图
     * <p>
     * 该视图只读聚合多域信号，不回写业务库。告警判定优先采用 BI 已启用规则，
     * 同时结合 SOM 已产生的价格/跟卖告警计算待处理量，形成面向运营的统一口径。
     * </p>
     */
    public OperationMonitorReport getOperationMonitor(String tenantId) {
        List<AlertRule> activeRules = biExtService.listAlertRules(tenantId, OPERATION_MONITOR_DOMAIN).stream()
                .filter(AlertRule::enabled)
                .sorted(Comparator.comparing(AlertRule::metricCode, Comparator.nullsLast(String::compareTo))
                        .thenComparing(AlertRule::ruleName, Comparator.nullsLast(String::compareTo)))
                .toList();

        List<SomClient.ListingResponse> activeListings = safeList(requireData(
                somClient.listListings(ACTIVE_STATUS),
                "BI_OPERATION_MONITOR_SOURCE_FAILED",
                "获取 SOM 在售 Listing 失败"));
        List<SomClient.SalesAlertResponse> openSalesAlerts = safeList(requireData(
                somClient.listUnacknowledgedAlerts(),
                "BI_OPERATION_MONITOR_SOURCE_FAILED",
                "获取 SOM 销售预警失败"));
        List<SomClient.BuyboxMonitorResponse> buyboxSignals = safeList(requireData(
                somClient.listBuyboxMonitors(null),
                "BI_OPERATION_MONITOR_SOURCE_FAILED",
                "获取 SOM Buybox 监控失败"));
        List<SomClient.HijackAlertResponse> hijackAlerts = safeList(requireData(
                somClient.listHijackAlerts(null, true),
                "BI_OPERATION_MONITOR_SOURCE_FAILED",
                "获取 SOM 跟卖告警失败"));

        Map<String, List<SomClient.SalesAlertResponse>> salesAlertsByStore = openSalesAlerts.stream()
                .collect(Collectors.groupingBy(SomClient.SalesAlertResponse::storeId, LinkedHashMap::new, Collectors.toList()));
        Map<String, SomClient.BuyboxMonitorResponse> latestBuyboxByListing = latestBy(
                buyboxSignals, SomClient.BuyboxMonitorResponse::listingId, SomClient.BuyboxMonitorResponse::collectedAt);
        Map<String, List<SomClient.HijackAlertResponse>> hijackAlertsByListing = hijackAlerts.stream()
                .collect(Collectors.groupingBy(SomClient.HijackAlertResponse::listingId, LinkedHashMap::new, Collectors.toList()));

        Map<String, List<SomClient.ListingPerformanceResponse>> performanceCache = new HashMap<>();
        Map<String, SomClient.ChannelSkuResponse> channelSkuCache = new HashMap<>();
        Map<String, WmsClient.InventoryAvailabilityResponse> inventoryCache = new HashMap<>();
        Map<String, CrmClient.ReviewAnalysisResponse> reviewCache = new HashMap<>();

        List<ListingMonitorSnapshot> snapshots = activeListings.stream()
                .map(listing -> buildListingMonitorSnapshot(
                        listing,
                        activeRules,
                        salesAlertsByStore,
                        latestBuyboxByListing,
                        hijackAlertsByListing,
                        performanceCache,
                        channelSkuCache,
                        inventoryCache,
                        reviewCache))
                .sorted(Comparator.comparingInt((ListingMonitorSnapshot snapshot) -> severityRank(snapshot.item().highestSeverity())).reversed()
                        .thenComparing(snapshot -> snapshot.item().listingId(), Comparator.nullsLast(String::compareTo)))
                .toList();

        List<OperationMonitorReport.ListingMonitorItem> listings = snapshots.stream()
                .map(ListingMonitorSnapshot::item)
                .toList();
        int pendingAlertCount = snapshots.stream()
                .mapToInt(snapshot -> snapshot.pendingSalesAlertCount() + snapshot.pendingHijackAlertCount())
                .sum();

        return new OperationMonitorReport(
                buildOperationSummary(listings, pendingAlertCount, activeRules.size()),
                activeRules.stream().map(this::toActiveRule).toList(),
                listings,
                Instant.now());
    }

    /**
     * 经营预警中心。
     * <p>
     * 聚合四类高价值预警:
     * 1. SOM/WMS/CRM 规则驱动运营异常
     * 2. FMS 利润偏差预警
     * 3. BI KPI 异常预警
     * 4. FBA 物流异常预警
     * </p>
     */
    public AlertCenterReport getAlertCenter(String tenantId, String category, String severity,
                                            String status, String sourceDomain) {
        Instant now = Instant.now();
        List<AlertCenterReport.AlertItem> alerts = new ArrayList<>();
        alerts.addAll(buildOperationAlertItems(tenantId, now));
        alerts.addAll(buildProfitAlertItems(tenantId, now));
        alerts.addAll(buildKpiAlertItems(tenantId, now));
        alerts.addAll(buildLogisticsAlertItems(tenantId, now));

        String categoryFilter = normalizeFilter(category);
        String severityFilter = normalizeFilter(severity);
        String statusFilter = normalizeFilter(status);
        String sourceDomainFilter = normalizeFilter(sourceDomain);

        List<AlertCenterReport.AlertItem> filteredAlerts = alerts.stream()
                .filter(alert -> matchesFilter(alert.category(), categoryFilter))
                .filter(alert -> matchesFilter(alert.severity(), severityFilter))
                .filter(alert -> matchesFilter(alert.status(), statusFilter))
                .filter(alert -> matchesFilter(alert.sourceDomain(), sourceDomainFilter))
                .sorted(Comparator.comparingInt((AlertCenterReport.AlertItem alert) -> severityRank(alert.severity())).reversed()
                        .thenComparing(AlertCenterReport.AlertItem::detectedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(AlertCenterReport.AlertItem::category, Comparator.nullsLast(String::compareTo))
                        .thenComparing(AlertCenterReport.AlertItem::alertId, Comparator.nullsLast(String::compareTo)))
                .toList();

        return new AlertCenterReport(buildAlertCenterSummary(filteredAlerts), filteredAlerts, now);
    }

    private List<AlertCenterReport.AlertItem> buildOperationAlertItems(String tenantId, Instant generatedAt) {
        OperationMonitorReport monitorReport = getOperationMonitor(tenantId);
        List<AlertCenterReport.AlertItem> alerts = new ArrayList<>();
        for (OperationMonitorReport.ListingMonitorItem listing : monitorReport.listings()) {
            for (OperationMonitorReport.Anomaly anomaly : listing.anomalies()) {
                String category = normalizeAlertCategory(anomaly.anomalyType());
                alerts.add(new AlertCenterReport.AlertItem(
                        buildOperationAlertId(listing.listingId(), anomaly.ruleId(), anomaly.metricCode()),
                        category,
                        resolveOperationSourceDomain(category),
                        upperOrDefault(anomaly.severity(), "WARNING"),
                        ALERT_STATUS_OPEN,
                        null,
                        anomaly.ruleName(),
                        anomaly.message(),
                        anomaly.metricCode(),
                        scaleNullable(anomaly.currentValue()),
                        scaleNullable(anomaly.thresholdValue()),
                        listing.listingId(),
                        null,
                        null,
                        null,
                        listing.storeId(),
                        listing.sellerSku(),
                        generatedAt));
            }
        }
        return alerts;
    }

    private List<AlertCenterReport.AlertItem> buildProfitAlertItems(String tenantId, Instant generatedAt) {
        List<FmsClient.ProfitDeviationAlertResponse> profitAlerts = safeList(requireData(
                fmsClient.listProfitDeviationAlerts(null),
                "BI_ALERT_CENTER_SOURCE_FAILED",
                "获取 FMS 利润偏差预警失败"));
        return profitAlerts.stream()
                .map(alert -> new AlertCenterReport.AlertItem(
                        alert.alertId(),
                        ALERT_CATEGORY_PROFIT,
                        SOURCE_DOMAIN_FMS,
                        upperOrDefault(alert.severity(), "WARNING"),
                        upperOrDefault(alert.status(), ALERT_STATUS_OPEN),
                        null,
                        "Profit Margin Deviation",
                        buildProfitAlertMessage(alert),
                        "gross_margin",
                        scaleNullable(alert.actualMargin()),
                        scaleNullable(alert.expectedMargin()),
                        null,
                        null,
                        alert.dimensionType(),
                        alert.dimensionId(),
                        "STORE".equalsIgnoreCase(alert.dimensionType()) ? alert.dimensionId() : null,
                        alert.sellerSku(),
                        defaultInstant(alert.detectedAt(), generatedAt)))
                .toList();
    }

    private List<AlertCenterReport.AlertItem> buildKpiAlertItems(String tenantId, Instant generatedAt) {
        return reportService.detectKpiAnomalies(tenantId).stream()
                .map(alert -> new AlertCenterReport.AlertItem(
                        buildKpiAlertId(alert),
                        ALERT_CATEGORY_KPI,
                        SOURCE_DOMAIN_BI,
                        upperOrDefault(alert.severity(), "WARNING"),
                        ALERT_STATUS_OPEN,
                        alert.status() != null ? alert.status().name() : null,
                        alert.kpiName(),
                        alert.message(),
                        alert.kpiCode(),
                        scaleNullable(alert.currentValue()),
                        scaleNullable(alert.targetValue()),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        defaultInstant(alert.detectedAt(), generatedAt)))
                .toList();
    }

    private List<AlertCenterReport.AlertItem> buildLogisticsAlertItems(String tenantId, Instant generatedAt) {
        return analyzeFbaShipments(tenantId).shipments().stream()
                .filter(item -> item.openExceptionCount() > 0)
                .map(item -> new AlertCenterReport.AlertItem(
                        buildLogisticsAlertId(item.shipmentId()),
                        ALERT_CATEGORY_LOGISTICS,
                        SOURCE_DOMAIN_FBA,
                        determineLogisticsSeverity(item),
                        ALERT_STATUS_OPEN,
                        item.status(),
                        "FBA Shipment Exception",
                        buildLogisticsAlertMessage(item),
                        "shipment_open_exception_count",
                        BigDecimal.valueOf(item.openExceptionCount()).setScale(2, RoundingMode.HALF_UP),
                        BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                        null,
                        item.shipmentId(),
                        null,
                        null,
                        null,
                        null,
                        defaultInstant(item.updatedAt(), generatedAt)))
                .toList();
    }

    private AlertCenterReport.Summary buildAlertCenterSummary(List<AlertCenterReport.AlertItem> alerts) {
        int totalAlertCount = alerts.size();
        int openAlertCount = countAlertsByStatus(alerts, ALERT_STATUS_OPEN);
        int acknowledgedAlertCount = countAlertsByStatus(alerts, ALERT_STATUS_ACKNOWLEDGED);
        int resolvedAlertCount = countAlertsByStatus(alerts, ALERT_STATUS_RESOLVED);
        int criticalAlertCount = (int) alerts.stream()
                .filter(alert -> "CRITICAL".equalsIgnoreCase(alert.severity()))
                .count();
        int warningAlertCount = (int) alerts.stream()
                .filter(alert -> "WARNING".equalsIgnoreCase(alert.severity()))
                .count();
        return new AlertCenterReport.Summary(
                totalAlertCount,
                openAlertCount,
                acknowledgedAlertCount,
                resolvedAlertCount,
                criticalAlertCount,
                warningAlertCount,
                countAlertsByDimension(alerts, AlertCenterReport.AlertItem::category),
                countAlertsByDimension(alerts, AlertCenterReport.AlertItem::sourceDomain));
    }

    private int countAlertsByStatus(List<AlertCenterReport.AlertItem> alerts, String status) {
        return (int) alerts.stream()
                .filter(alert -> status.equalsIgnoreCase(alert.status()))
                .count();
    }

    private Map<String, Integer> countAlertsByDimension(
            List<AlertCenterReport.AlertItem> alerts,
            Function<AlertCenterReport.AlertItem, String> extractor) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (AlertCenterReport.AlertItem alert : alerts) {
            String key = extractor.apply(alert);
            if (!hasText(key)) {
                continue;
            }
            counts.merge(key, 1, Integer::sum);
        }
        return counts;
    }

    private String resolveOperationSourceDomain(String category) {
        return switch (category) {
            case ANOMALY_INVENTORY -> SOURCE_DOMAIN_WMS;
            case ANOMALY_REVIEW -> SOURCE_DOMAIN_CRM;
            default -> SOURCE_DOMAIN_SOM;
        };
    }

    private String normalizeAlertCategory(String category) {
        return hasText(category) ? category.trim().toLowerCase(Locale.ROOT) : "unknown";
    }

    private String normalizeFilter(String filter) {
        return hasText(filter) ? filter.trim() : null;
    }

    private boolean matchesFilter(String actualValue, String filter) {
        if (!hasText(filter)) {
            return true;
        }
        return hasText(actualValue) && actualValue.trim().equalsIgnoreCase(filter);
    }

    private String buildOperationAlertId(String listingId, String ruleId, String metricCode) {
        return "OPS#" + safeToken(ruleId) + "#" + safeToken(metricCode) + "#" + safeToken(listingId);
    }

    private String buildKpiAlertId(KpiAlert alert) {
        return "KPI#" + safeToken(alert.kpiCode()) + "#" + safeToken(alert.status() != null ? alert.status().name() : null)
                + "#" + safeToken(alert.currentValue() != null ? alert.currentValue().stripTrailingZeros().toPlainString() : null);
    }

    private String buildLogisticsAlertId(String shipmentId) {
        return "LOGISTICS#" + safeToken(shipmentId);
    }

    private String buildProfitAlertMessage(FmsClient.ProfitDeviationAlertResponse alert) {
        return "Actual margin " + scaleNullable(alert.actualMargin())
                + " is below expected threshold " + scaleNullable(alert.expectedMargin());
    }

    private String determineLogisticsSeverity(ShipmentAnalysisItem item) {
        // FBA 在途异常会直接影响入库、补货与利润核算，默认按高优先级输出。
        return item.openExceptionCount() > 0 ? "CRITICAL" : "WARNING";
    }

    private String buildLogisticsAlertMessage(ShipmentAnalysisItem item) {
        String exceptionTypes = item.exceptionTypes().isEmpty()
                ? "UNKNOWN"
                : String.join("/", item.exceptionTypes());
        return "Shipment has " + item.openExceptionCount() + " open exceptions: " + exceptionTypes;
    }

    private String upperOrDefault(String value, String defaultValue) {
        return hasText(value) ? upper(value) : defaultValue;
    }

    private BigDecimal scaleNullable(BigDecimal value) {
        return value == null ? null : scale(value);
    }

    private Instant defaultInstant(Instant value, Instant defaultValue) {
        return value != null ? value : defaultValue;
    }

    private String safeToken(String value) {
        return hasText(value) ? value.trim() : "NA";
    }

    private ListingMonitorSnapshot buildListingMonitorSnapshot(
            SomClient.ListingResponse listing,
            List<AlertRule> activeRules,
            Map<String, List<SomClient.SalesAlertResponse>> salesAlertsByStore,
            Map<String, SomClient.BuyboxMonitorResponse> latestBuyboxByListing,
            Map<String, List<SomClient.HijackAlertResponse>> hijackAlertsByListing,
            Map<String, List<SomClient.ListingPerformanceResponse>> performanceCache,
            Map<String, SomClient.ChannelSkuResponse> channelSkuCache,
            Map<String, WmsClient.InventoryAvailabilityResponse> inventoryCache,
            Map<String, CrmClient.ReviewAnalysisResponse> reviewCache) {

        SomClient.ListingPerformanceResponse performance = latestPerformance(listing.listingId(), performanceCache);
        SomClient.ChannelSkuResponse channelSku = resolveChannelSku(listing, channelSkuCache);
        String sellerSku = channelSku != null ? channelSku.channelSku() : null;
        WmsClient.InventoryAvailabilityResponse inventory = resolveInventory(sellerSku, inventoryCache);
        CrmClient.ReviewAnalysisResponse review = resolveReview(sellerSku, reviewCache);
        SomClient.BuyboxMonitorResponse buybox = latestBuyboxByListing.get(listing.listingId());

        List<SomClient.HijackAlertResponse> relevantHijackAlerts = safeList(hijackAlertsByListing.get(listing.listingId())).stream()
                .filter(alert -> !RESOLVED_STATUS.equalsIgnoreCase(alert.status()))
                .toList();
        List<SomClient.SalesAlertResponse> relevantSalesAlerts = safeList(salesAlertsByStore.get(listing.storeId())).stream()
                .filter(alert -> sellerSku == null || alert.relatedSku() == null || alert.relatedSku().equalsIgnoreCase(sellerSku))
                .toList();

        MetricBundle metrics = new MetricBundle(
                performance != null ? BigDecimal.valueOf(performance.orders()) : null,
                calculatePriceGapRate(buybox),
                resolveHijackerCount(buybox, relevantHijackAlerts),
                inventory != null ? BigDecimal.valueOf(inventory.available()) : null,
                review != null ? BigDecimal.valueOf(review.averageRating()) : null);

        List<OperationMonitorReport.Anomaly> anomalies = new ArrayList<>();
        List<String> anomalyTypes = new ArrayList<>();
        for (AlertRule rule : activeRules) {
            BigDecimal threshold = parseDecimal(rule.threshold());
            BigDecimal currentValue = metrics.valueOf(rule.metricCode());
            if (threshold == null || currentValue == null || !matches(rule.condition(), currentValue, threshold)) {
                continue;
            }
            String anomalyType = anomalyType(rule.metricCode());
            anomalyTypes.add(anomalyType);
            anomalies.add(new OperationMonitorReport.Anomaly(
                    anomalyType,
                    rule.metricCode(),
                    rule.severity().name(),
                    rule.ruleId(),
                    rule.ruleName(),
                    buildAnomalyMessage(rule, relevantSalesAlerts, relevantHijackAlerts),
                    scale(currentValue),
                    scale(threshold)));
        }

        List<String> distinctAnomalyTypes = anomalyTypes.stream().distinct().toList();
        String highestSeverity = resolveHighestSeverity(anomalies);
        int inventoryAvailable = inventory != null ? inventory.available() : 0;
        double averageRating = review != null ? review.averageRating() : 0D;
        int negativeReviewCount = review != null ? review.negativeCount() : 0;
        int orders = performance != null ? performance.orders() : 0;
        BigDecimal priceGapRate = metrics.priceGapRate() != null ? scale(metrics.priceGapRate()) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal winnerPrice = buybox != null ? defaultDecimal(buybox.winnerPrice()) : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        BigDecimal ourPrice = buybox != null && buybox.ourPrice() != null
                ? scale(buybox.ourPrice()) : defaultDecimal(listing.price());
        int hijackerCount = metrics.buyboxHijackerCount() != null ? metrics.buyboxHijackerCount().intValue() : 0;

        OperationMonitorReport.ListingMonitorItem item = new OperationMonitorReport.ListingMonitorItem(
                listing.listingId(),
                listing.storeId(),
                listing.title(),
                listing.platform(),
                listing.marketplace(),
                sellerSku,
                defaultDecimal(listing.price()),
                orders,
                priceGapRate,
                winnerPrice,
                ourPrice,
                inventoryAvailable,
                averageRating,
                negativeReviewCount,
                hijackerCount,
                highestSeverity,
                distinctAnomalyTypes,
                anomalies);
        return new ListingMonitorSnapshot(item, relevantSalesAlerts.size(), relevantHijackAlerts.size());
    }

    private SomClient.ListingPerformanceResponse latestPerformance(String listingId,
                                                                  Map<String, List<SomClient.ListingPerformanceResponse>> cache) {
        List<SomClient.ListingPerformanceResponse> performances = cache.computeIfAbsent(listingId, key -> safeList(requireData(
                somClient.listPerformances(key, null),
                "BI_OPERATION_MONITOR_SOURCE_FAILED",
                "获取 SOM Listing 表现失败")));
        return performances.stream()
                .sorted(Comparator.comparing(SomClient.ListingPerformanceResponse::periodEnd, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(SomClient.ListingPerformanceResponse::createdAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .findFirst()
                .orElse(null);
    }

    private SomClient.ChannelSkuResponse resolveChannelSku(SomClient.ListingResponse listing,
                                                           Map<String, SomClient.ChannelSkuResponse> cache) {
        String cacheKey = String.join("|",
                listing.productId(),
                listing.storeId() != null ? listing.storeId() : "",
                listing.marketplace() != null ? listing.marketplace() : "");
        return cache.computeIfAbsent(cacheKey, key -> {
            List<SomClient.ChannelSkuResponse> channelSkus = safeList(requireData(
                    somClient.listChannelSkusByProductSku(listing.productId()),
                    "BI_OPERATION_MONITOR_SOURCE_FAILED",
                    "获取 SOM 渠道 SKU 映射失败"));
            return channelSkus.stream()
                    .filter(item -> ACTIVE_STATUS.equalsIgnoreCase(item.status()))
                    .filter(item -> item.storeId() == null || item.storeId().equals(listing.storeId()))
                    .filter(item -> item.marketplaceId() == null || item.marketplaceId().equals(listing.marketplace()))
                    .findFirst()
                    .orElseGet(() -> channelSkus.stream()
                            .filter(item -> ACTIVE_STATUS.equalsIgnoreCase(item.status()))
                            .findFirst()
                            .orElse(null));
        });
    }

    private WmsClient.InventoryAvailabilityResponse resolveInventory(
            String sellerSku, Map<String, WmsClient.InventoryAvailabilityResponse> cache) {
        if (!hasText(sellerSku)) {
            return null;
        }
        return cache.computeIfAbsent(sellerSku, key -> requireData(
                wmsClient.checkAvailability(key),
                "BI_OPERATION_MONITOR_SOURCE_FAILED",
                "获取 WMS 可用库存失败"));
    }

    private CrmClient.ReviewAnalysisResponse resolveReview(
            String sellerSku, Map<String, CrmClient.ReviewAnalysisResponse> cache) {
        if (!hasText(sellerSku)) {
            return null;
        }
        return cache.computeIfAbsent(sellerSku, key -> safeList(requireData(
                        crmClient.listReviewAnalysesBySku(key),
                        "BI_OPERATION_MONITOR_SOURCE_FAILED",
                        "获取 CRM 评价分析失败")).stream()
                .sorted(Comparator.comparing(CrmClient.ReviewAnalysisResponse::analyzedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .findFirst()
                .orElse(null));
    }

    private OperationMonitorReport.Summary buildOperationSummary(
            List<OperationMonitorReport.ListingMonitorItem> listings,
            int pendingAlertCount,
            int enabledRuleCount) {
        int monitoredListingCount = listings.size();
        int abnormalListingCount = (int) listings.stream().filter(item -> !item.anomalies().isEmpty()).count();
        int criticalListingCount = (int) listings.stream()
                .filter(item -> "CRITICAL".equalsIgnoreCase(item.highestSeverity()))
                .count();
        int salesAnomalyCount = countListingsByAnomalyType(listings, ANOMALY_SALES);
        int priceAnomalyCount = countListingsByAnomalyType(listings, ANOMALY_PRICE);
        int buyboxAnomalyCount = countListingsByAnomalyType(listings, ANOMALY_BUYBOX);
        int inventoryAnomalyCount = countListingsByAnomalyType(listings, ANOMALY_INVENTORY);
        int reviewAnomalyCount = countListingsByAnomalyType(listings, ANOMALY_REVIEW);
        return new OperationMonitorReport.Summary(
                monitoredListingCount,
                abnormalListingCount,
                criticalListingCount,
                salesAnomalyCount,
                priceAnomalyCount,
                buyboxAnomalyCount,
                inventoryAnomalyCount,
                reviewAnomalyCount,
                pendingAlertCount,
                enabledRuleCount);
    }

    private int countListingsByAnomalyType(List<OperationMonitorReport.ListingMonitorItem> listings, String anomalyType) {
        return (int) listings.stream()
                .filter(item -> item.anomalyTypes().contains(anomalyType))
                .count();
    }

    private OperationMonitorReport.ActiveRule toActiveRule(AlertRule rule) {
        return new OperationMonitorReport.ActiveRule(
                rule.ruleId(),
                rule.ruleName(),
                rule.metricCode(),
                rule.threshold(),
                rule.condition() != null ? rule.condition().name() : null,
                rule.severity() != null ? rule.severity().name() : null,
                rule.notifyChannel(),
                rule.notifyTargets());
    }

    private String buildAnomalyMessage(AlertRule rule,
                                       List<SomClient.SalesAlertResponse> salesAlerts,
                                       List<SomClient.HijackAlertResponse> hijackAlerts) {
        if (METRIC_PRICE_GAP_RATE.equalsIgnoreCase(rule.metricCode())) {
            SomClient.SalesAlertResponse priceAlert = salesAlerts.stream()
                    .filter(alert -> "price_anomaly".equalsIgnoreCase(alert.alertType()))
                    .findFirst()
                    .orElse(null);
            if (priceAlert != null && hasText(priceAlert.message())) {
                return priceAlert.message();
            }
        }
        if (METRIC_BUYBOX_HIJACKER_COUNT.equalsIgnoreCase(rule.metricCode())) {
            SomClient.HijackAlertResponse hijackAlert = hijackAlerts.stream().findFirst().orElse(null);
            if (hijackAlert != null && hasText(hijackAlert.hijackerName())) {
                return "检测到跟卖风险: " + hijackAlert.hijackerName();
            }
        }
        return rule.ruleName() + "触发";
    }

    private BigDecimal calculatePriceGapRate(SomClient.BuyboxMonitorResponse buybox) {
        if (buybox == null || buybox.winnerPrice() == null || buybox.ourPrice() == null
                || buybox.winnerPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        BigDecimal gap = buybox.ourPrice().subtract(buybox.winnerPrice());
        if (gap.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return gap.multiply(BigDecimal.valueOf(100))
                .divide(buybox.winnerPrice(), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal resolveHijackerCount(SomClient.BuyboxMonitorResponse buybox,
                                            List<SomClient.HijackAlertResponse> hijackAlerts) {
        int hijackerCount = buybox != null ? buybox.hijackerCount() : 0;
        hijackerCount = Math.max(hijackerCount, hijackAlerts.size());
        return BigDecimal.valueOf(hijackerCount);
    }

    private boolean matches(AlertCondition condition, BigDecimal currentValue, BigDecimal threshold) {
        if (condition == null || currentValue == null || threshold == null) {
            return false;
        }
        return switch (condition) {
            case GREATER_THAN -> currentValue.compareTo(threshold) > 0;
            case LESS_THAN -> currentValue.compareTo(threshold) < 0;
            case EQUALS -> currentValue.compareTo(threshold) == 0;
            case NOT_EQUALS -> currentValue.compareTo(threshold) != 0;
            case PERCENTAGE_CHANGE_UP -> currentValue.compareTo(threshold) > 0;
            case PERCENTAGE_CHANGE_DOWN -> currentValue.compareTo(threshold) < 0;
        };
    }

    private String anomalyType(String metricCode) {
        return switch (metricCode) {
            case METRIC_LISTING_ORDERS -> ANOMALY_SALES;
            case METRIC_PRICE_GAP_RATE -> ANOMALY_PRICE;
            case METRIC_BUYBOX_HIJACKER_COUNT -> ANOMALY_BUYBOX;
            case METRIC_INVENTORY_AVAILABLE -> ANOMALY_INVENTORY;
            case METRIC_AVERAGE_RATING -> ANOMALY_REVIEW;
            default -> upper(metricCode);
        };
    }

    private String resolveHighestSeverity(List<OperationMonitorReport.Anomaly> anomalies) {
        return anomalies.stream()
                .map(OperationMonitorReport.Anomaly::severity)
                .max(Comparator.comparingInt(this::severityRank))
                .orElse("NORMAL");
    }

    private int severityRank(String severity) {
        if ("CRITICAL".equalsIgnoreCase(severity)) {
            return 5;
        }
        if ("HIGH".equalsIgnoreCase(severity)) {
            return 4;
        }
        if ("WARNING".equalsIgnoreCase(severity) || "MEDIUM".equalsIgnoreCase(severity)) {
            return 3;
        }
        if ("INFO".equalsIgnoreCase(severity) || "LOW".equalsIgnoreCase(severity)) {
            return 2;
        }
        return 0;
    }

    private <T, K> Map<K, T> latestBy(List<T> items, Function<T, K> keyExtractor, Function<T, Instant> timeExtractor) {
        Map<K, T> latest = new LinkedHashMap<>();
        for (T item : items) {
            K key = keyExtractor.apply(item);
            if (key == null) {
                continue;
            }
            T existing = latest.get(key);
            if (existing == null || compareInstant(timeExtractor.apply(item), timeExtractor.apply(existing)) > 0) {
                latest.put(key, item);
            }
        }
        return latest;
    }

    private int compareInstant(Instant left, Instant right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return -1;
        }
        if (right == null) {
            return 1;
        }
        return left.compareTo(right);
    }

    private ShipmentAnalysisItem buildShipmentAnalysisItem(FbaClient.FbaShipmentResponse shipment) {
        List<FbaClient.ShipmentExceptionResponse> exceptions = safeList(requireData(
                fbaClient.listShipmentExceptions(shipment.fbaShipmentId()),
                "BI_FBA_SHIPMENT_ANALYSIS_SOURCE_FAILED",
                "获取 FBA 货件异常数据失败"));
        List<FmsClient.CostEventResponse> costEvents = listShipmentCostEvents(shipment.fbaShipmentId());

        BigDecimal shippingCost = sumCostByType(costEvents, "SHIPPING_COST");
        BigDecimal fbaFee = sumCostByType(costEvents, "FBA_FEE");
        BigDecimal totalCost = shippingCost.add(fbaFee);
        int openExceptionCount = (int) exceptions.stream()
                .filter(item -> !RESOLVED_STATUS.equalsIgnoreCase(item.status()))
                .count();

        return new ShipmentAnalysisItem(
                shipment.fbaShipmentId(),
                shipment.amazonShipmentId(),
                shipment.destinationFc(),
                shipment.carrier(),
                shipment.trackingNo(),
                shipment.status(),
                shipment.plannedQuantity(),
                shipment.receivedQuantity(),
                percentage(shipment.receivedQuantity(), shipment.plannedQuantity()),
                shipment.cartonCount(),
                defaultDecimal(shipment.totalWeight()),
                shippingCost,
                fbaFee,
                totalCost,
                exceptions.size(),
                openExceptionCount,
                exceptions.stream()
                        .map(FbaClient.ShipmentExceptionResponse::type)
                        .filter(this::hasText)
                        .map(this::upper)
                        .distinct()
                        .sorted()
                        .toList(),
                hoursBetween(shipment.createdAt(), shipment.updatedAt()),
                computeTransitHours(shipment),
                shipment.createdAt(),
                shipment.packedAt(),
                shipment.shippedAt(),
                shipment.updatedAt());
    }

    private Summary buildSummary(List<ShipmentAnalysisItem> items) {
        int shipmentCount = items.size();
        int closedShipmentCount = (int) items.stream()
                .filter(item -> CLOSED_STATUS.equalsIgnoreCase(item.status()))
                .count();
        int inTransitShipmentCount = (int) items.stream()
                .filter(item -> SHIPPED_STATUS.equalsIgnoreCase(item.status())
                        || RECEIVED_STATUS.equalsIgnoreCase(item.status()))
                .count();
        int openShipmentCount = shipmentCount - closedShipmentCount;
        int exceptionShipmentCount = (int) items.stream()
                .filter(item -> item.exceptionCount() > 0)
                .count();
        int exceptionCount = items.stream()
                .mapToInt(ShipmentAnalysisItem::exceptionCount)
                .sum();
        int totalPlannedQuantity = items.stream()
                .mapToInt(ShipmentAnalysisItem::plannedQuantity)
                .sum();
        int totalReceivedQuantity = items.stream()
                .mapToInt(ShipmentAnalysisItem::receivedQuantity)
                .sum();
        BigDecimal totalShippingCost = items.stream()
                .map(ShipmentAnalysisItem::shippingCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalFbaFee = items.stream()
                .map(ShipmentAnalysisItem::fbaFee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalLogisticsCost = items.stream()
                .map(ShipmentAnalysisItem::totalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<BigDecimal> transitHours = items.stream()
                .map(ShipmentAnalysisItem::transitHours)
                .filter(value -> value != null)
                .toList();

        String currency = resolveCurrency(items);
        return new Summary(
                shipmentCount,
                closedShipmentCount,
                openShipmentCount,
                inTransitShipmentCount,
                exceptionShipmentCount,
                exceptionCount,
                totalPlannedQuantity,
                totalReceivedQuantity,
                percentage(totalReceivedQuantity, totalPlannedQuantity),
                average(transitHours),
                scale(totalShippingCost),
                scale(totalFbaFee),
                scale(totalLogisticsCost),
                currency);
    }

    private List<FmsClient.CostEventResponse> listShipmentCostEvents(String shipmentId) {
        Map<String, FmsClient.CostEventResponse> merged = new LinkedHashMap<>();
        for (String sourceType : SHIPMENT_COST_SOURCE_TYPES) {
            List<FmsClient.CostEventResponse> costEvents = safeList(requireData(
                    fmsClient.listCostEventsBySource(sourceType, shipmentId),
                    "BI_FBA_SHIPMENT_ANALYSIS_SOURCE_FAILED",
                    "获取 FMS 货件成本事件失败"));
            for (FmsClient.CostEventResponse costEvent : costEvents) {
                merged.putIfAbsent(costEvent.costEventId(), costEvent);
            }
        }
        return new ArrayList<>(merged.values());
    }

    private BigDecimal computeTransitHours(FbaClient.FbaShipmentResponse shipment) {
        if (shipment.shippedAt() == null) {
            return null;
        }
        Instant transitEnd = shipment.updatedAt() != null ? shipment.updatedAt() : Instant.now();
        return hoursBetween(shipment.shippedAt(), transitEnd);
    }

    private BigDecimal sumCostByType(List<FmsClient.CostEventResponse> costEvents, String costType) {
        return scale(costEvents.stream()
                .filter(item -> costType.equalsIgnoreCase(item.costType()))
                .map(FmsClient.CostEventResponse::amount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private BigDecimal percentage(int numerator, int denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal hoursBetween(Instant start, Instant end) {
        if (start == null || end == null || end.isBefore(start)) {
            return null;
        }
        return BigDecimal.valueOf(Duration.between(start, end).toMillis())
                .divide(BigDecimal.valueOf(3_600_000L), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal average(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal total = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }

    private String resolveCurrency(List<ShipmentAnalysisItem> items) {
        List<String> currencies = items.stream()
                .filter(item -> item.totalCost().compareTo(BigDecimal.ZERO) > 0)
                .map(item -> "USD")
                .distinct()
                .toList();
        if (currencies.isEmpty()) {
            return null;
        }
        return currencies.size() == 1 ? currencies.get(0) : "MULTI";
    }

    private BigDecimal parseDecimal(String value) {
        if (!hasText(value)) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : scale(value);
    }

    private BigDecimal scale(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : value.setScale(2, RoundingMode.HALF_UP);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String upper(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private <T> List<T> safeList(List<T> data) {
        return data == null ? List.of() : data;
    }

    private <T> T requireData(Result<T> result, String code, String message) {
        if (result == null) {
            throw new BizException(code, message);
        }
        if (!result.success()) {
            throw new BizException(code, hasText(result.message()) ? result.message() : message);
        }
        return result.data();
    }

    private record ListingMonitorSnapshot(
            OperationMonitorReport.ListingMonitorItem item,
            int pendingSalesAlertCount,
            int pendingHijackAlertCount
    ) {}

    private record MetricBundle(
            BigDecimal listingOrders,
            BigDecimal priceGapRate,
            BigDecimal buyboxHijackerCount,
            BigDecimal inventoryAvailable,
            BigDecimal averageRating
    ) {
        private BigDecimal valueOf(String metricCode) {
            return switch (metricCode) {
                case METRIC_LISTING_ORDERS -> listingOrders;
                case METRIC_PRICE_GAP_RATE -> priceGapRate;
                case METRIC_BUYBOX_HIJACKER_COUNT -> buyboxHijackerCount;
                case METRIC_INVENTORY_AVAILABLE -> inventoryAvailable;
                case METRIC_AVERAGE_RATING -> averageRating;
                default -> null;
            };
        }
    }
}
