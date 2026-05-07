package com.aidotnet.erp.som.application;

import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.som.domain.BuyboxMonitor;
import com.aidotnet.erp.som.domain.HijackAlert;
import com.aidotnet.erp.som.domain.SalesStore;
import com.aidotnet.erp.som.domain.SalesTeam;
import com.aidotnet.erp.som.domain.SalesTracking;
import com.aidotnet.erp.som.domain.StoreMetrics;
import com.aidotnet.erp.som.domain.StoreStatus;
import com.aidotnet.erp.som.infrastructure.SalesStoreRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * 店铺管理应用服务
 * <p>
 * 描述: 销售运营域店铺管理服务，负责多平台店铺的创建/连接/停用，
 *       以及店铺销售追踪和指标记录。是销售运营域的基础设施服务。
 * </p>
 * <p>
 * 核心能力:
 *   1. 店铺管理 - 创建/连接/停用多平台店铺(Amazon/eBay/Shopify等)
 *   2. 销售追踪 - 按店铺/SKU记录销售数据(销量/收入/均价)
 *   3. 店铺指标 - 记录店铺维度运营指标(营收/订单量/AOV/退货率/评分)
 * </p>
 * <p>
 * 业务规则:
 *   1. 同一平台同一storeCode不可重复创建店铺
 *   2. 店铺状态流转: CREATED -> CONNECTED -> DISABLED
 *   3. 记录销售和指标数据前需校验店铺存在性
 * </p>
 *
 * @author ERP系统
 * @see SalesStore
 * @see SalesStoreRepository
 */
@Service
public class SalesStoreService {

    private final SalesStoreRepository repository;

    /**
     * 构造函数 - 依赖注入店铺存储
     *
     * @param repository 店铺数据存储
     */
    public SalesStoreService(SalesStoreRepository repository) {
        this.repository = repository;
    }

    /**
     * 创建店铺
     * <p>
     * 校验平台店铺编码唯一性后创建新店铺，初始状态为CREATED。
     * </p>
     *
     * @param tenantId 租户ID
     * @param command  创建店铺命令
     * @return 新创建的店铺实体
     * @throws BizException STORE_DUPLICATED - 店铺已存在
     */
    public SalesStore create(String tenantId, SaveStoreCommand command) {
        repository.findByCode(tenantId, command.platform(), command.storeCode()).ifPresent(existing -> {
            throw new BizException("STORE_DUPLICATED", "店铺已存在");
        });
        Instant now = Instant.now();
        return repository.save(new SalesStore(UUID.randomUUID().toString(), tenantId, command.platform(), command.storeCode(),
                command.storeName(), StoreStatus.CREATED, now, now));
    }

    public SalesStore connect(String tenantId, String storeId) {
        SalesStore store = get(tenantId, storeId);
        return repository.save(new SalesStore(store.storeId(), store.tenantId(), store.platform(), store.storeCode(),
                store.storeName(), StoreStatus.CONNECTED, store.createdAt(), Instant.now()));
    }

    public SalesStore disable(String tenantId, String storeId) {
        SalesStore store = get(tenantId, storeId);
        return repository.save(new SalesStore(store.storeId(), store.tenantId(), store.platform(), store.storeCode(),
                store.storeName(), StoreStatus.DISABLED, store.createdAt(), Instant.now()));
    }

    public List<SalesStore> list(String tenantId) {
        return repository.list(tenantId);
    }

    public SalesTracking recordSales(String tenantId, RecordSalesCommand command) {
        ensureStore(tenantId, command.storeId());
        BigDecimal avgPrice = command.unitsSold() > 0
                ? command.revenue().divide(BigDecimal.valueOf(command.unitsSold()), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        return repository.saveTracking(new SalesTracking(UUID.randomUUID().toString(), tenantId,
                command.storeId(), command.sellerSku(), command.marketplaceId(), command.unitsSold(),
                command.revenue(), avgPrice, command.periodStart(), command.periodEnd(), Instant.now()));
    }

    public List<SalesTracking> listSalesByStore(String tenantId, String storeId) {
        return repository.listTrackings(tenantId, storeId);
    }

    public List<SalesTracking> listSalesBySku(String tenantId, String sellerSku) {
        return repository.listTrackingsBySku(tenantId, sellerSku);
    }

    public StoreMetrics recordMetrics(String tenantId, RecordMetricsCommand command) {
        ensureStore(tenantId, command.storeId());
        BigDecimal aov = command.totalOrders().compareTo(BigDecimal.ZERO) > 0
                ? command.totalRevenue().divide(command.totalOrders(), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        return repository.saveMetrics(new StoreMetrics(UUID.randomUUID().toString(), tenantId,
                command.storeId(), command.marketplaceId(), command.totalRevenue(), command.totalOrders(),
                aov, command.returnRate(), command.feedbackScore(),
                command.periodStart(), command.periodEnd(), Instant.now()));
    }

    public List<StoreMetrics> listMetrics(String tenantId, String storeId) {
        return repository.listMetrics(tenantId, storeId);
    }

    private SalesStore get(String tenantId, String storeId) {
        return repository.find(tenantId, storeId).orElseThrow(() -> new BizException("STORE_NOT_FOUND", "店铺不存在"));
    }

    private void ensureStore(String tenantId, String storeId) {
        repository.find(tenantId, storeId).orElseThrow(() -> new BizException("STORE_NOT_FOUND", "店铺不存在"));
    }

    public record SaveStoreCommand(String platform, String storeCode, String storeName) {}

    public record RecordSalesCommand(String storeId, String sellerSku, String marketplaceId, int unitsSold,
                                     BigDecimal revenue, Instant periodStart, Instant periodEnd) {}

    public record RecordMetricsCommand(String storeId, String marketplaceId, BigDecimal totalRevenue,
                                       BigDecimal totalOrders, BigDecimal returnRate, BigDecimal feedbackScore,
                                       Instant periodStart, Instant periodEnd) {}

    // ========== Buybox监控管理(内存存储) ==========

    private final Map<String, BuyboxMonitor> buyboxStore = new ConcurrentHashMap<>();
    private final Map<String, HijackAlert> hijackStore = new ConcurrentHashMap<>();
    private final Map<String, SalesTeam> salesTeamStore = new ConcurrentHashMap<>();

    /**
     * 记录Buybox采集信息
     * <p>
     * 用于追踪Listing的Buybox持有者和价格变化，
     * 支持自动调价策略的数据基础。
     * </p>
     */
    public BuyboxMonitor recordBuybox(String tenantId, RecordBuyboxCommand command) {
        Instant now = Instant.now();
        BuyboxMonitor monitor = new BuyboxMonitor(UUID.randomUUID().toString(), tenantId,
                command.listingId(), command.platform(), command.marketplace(),
                command.winnerName(), command.winnerPrice(), command.ourPrice(),
                false, null, command.hijackerCount(), now, now);
        buyboxStore.put(monitor.monitorId(), monitor);
        return monitor;
    }

    public List<BuyboxMonitor> listBuyboxByListing(String tenantId, String listingId) {
        return buyboxStore.values().stream()
                .filter(b -> b.tenantId().equals(tenantId) && b.listingId().equals(listingId))
                .collect(Collectors.toList());
    }

    /**
     * 查询买盒监控信号
     * <p>
     * 运营监控、BI 聚合等场景需要按租户统一拉取买盒信号，支持按 Listing 维度过滤。
     * 返回结果按采集时间倒序排列，优先消费最近一次有效快照。
     * </p>
     */
    public List<BuyboxMonitor> listBuyboxMonitors(String tenantId, String listingId) {
        return buyboxStore.values().stream()
                .filter(item -> item.tenantId().equals(tenantId))
                .filter(item -> listingId == null || listingId.isBlank() || item.listingId().equals(listingId))
                .sorted(Comparator.comparing(BuyboxMonitor::collectedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(BuyboxMonitor::monitorId, Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList());
    }

    /**
     * 创建跟卖告警
     * <p>
     * 检测到新的跟卖者时自动创建告警记录，
     * 运营人员确认后可标记为已处理。
     * </p>
     */
    public HijackAlert createHijackAlert(String tenantId, CreateHijackAlertCommand command) {
        Instant now = Instant.now();
        HijackAlert alert = new HijackAlert(UUID.randomUUID().toString(), tenantId,
                command.listingId(), command.platform(), command.marketplace(),
                command.hijackerName(), command.hijackerPrice(), command.ourPrice(),
                command.severity(), "OPEN", null, null, now, null, now, now);
        hijackStore.put(alert.alertId(), alert);
        return alert;
    }

    public List<HijackAlert> listHijackAlerts(String tenantId, String listingId) {
        return hijackStore.values().stream()
                .filter(h -> h.tenantId().equals(tenantId) && h.listingId().equals(listingId))
                .collect(Collectors.toList());
    }

    /**
     * 查询跟卖告警
     * <p>
     * 支持按 Listing 过滤，也支持租户级统一拉取，用于 BI 运营监控和人工处置队列。
     * 返回结果按最近更新时间倒序排列，便于优先处理最新风险。
     * </p>
     */
    public List<HijackAlert> listHijackAlerts(String tenantId, String listingId, boolean includeResolved) {
        return hijackStore.values().stream()
                .filter(item -> item.tenantId().equals(tenantId))
                .filter(item -> listingId == null || listingId.isBlank() || item.listingId().equals(listingId))
                .filter(item -> includeResolved || !"RESOLVED".equalsIgnoreCase(item.status()))
                .sorted(Comparator.comparing(HijackAlert::updatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(HijackAlert::alertId, Comparator.nullsLast(String::compareTo)))
                .collect(Collectors.toList());
    }

    /**
     * 确认跟卖告警已处理
     */
    public HijackAlert acknowledgeHijackAlert(String tenantId, String alertId, String handledBy, String note) {
        HijackAlert alert = hijackStore.get(alertId);
        if (alert == null || !alert.tenantId().equals(tenantId)) {
            throw new BizException("ALERT_NOT_FOUND", "跟卖告警不存在");
        }
        HijackAlert updated = new HijackAlert(alert.alertId(), alert.tenantId(), alert.listingId(),
                alert.platform(), alert.marketplace(), alert.hijackerName(), alert.hijackerPrice(),
                alert.ourPrice(), alert.severity(), "RESOLVED", handledBy, note,
                alert.detectedAt(), Instant.now(), alert.createdAt(), Instant.now());
        hijackStore.put(alertId, updated);
        return updated;
    }

    // ========== 销售小组管理(内存存储) ==========

    /**
     * 创建销售小组
     */
    public SalesTeam createSalesTeam(String tenantId, String teamName, String leaderId,
                                      List<String> memberIds, List<String> storeIds) {
        Instant now = Instant.now();
        SalesTeam team = new SalesTeam(UUID.randomUUID().toString(), tenantId, teamName, leaderId,
                memberIds != null ? memberIds : List.of(),
                storeIds != null ? storeIds : List.of(), true, now, now);
        salesTeamStore.put(team.teamId(), team);
        return team;
    }

    public List<SalesTeam> listSalesTeams(String tenantId) {
        return salesTeamStore.values().stream()
                .filter(t -> t.tenantId().equals(tenantId))
                .collect(Collectors.toList());
    }

    public record RecordBuyboxCommand(String listingId, String platform, String marketplace,
                                       String winnerName, BigDecimal winnerPrice, BigDecimal ourPrice,
                                       int hijackerCount) {}

    public record CreateHijackAlertCommand(String listingId, String platform, String marketplace,
                                            String hijackerName, BigDecimal hijackerPrice, BigDecimal ourPrice,
                                            String severity) {}
}
