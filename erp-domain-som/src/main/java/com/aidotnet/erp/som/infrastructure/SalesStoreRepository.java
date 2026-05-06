package com.aidotnet.erp.som.infrastructure;

import com.aidotnet.erp.som.domain.SalesStore;
import com.aidotnet.erp.som.domain.SalesTracking;
import com.aidotnet.erp.som.domain.StoreMetrics;
import com.aidotnet.erp.som.domain.StoreStatus;
import com.aidotnet.erp.som.infrastructure.data.SalesStoreDO;
import com.aidotnet.erp.som.infrastructure.data.SalesTrackingDO;
import com.aidotnet.erp.som.infrastructure.data.StoreMetricsDO;
import com.aidotnet.erp.som.infrastructure.mapper.SalesStoreMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;

@Repository
public class SalesStoreRepository {

    private final SalesStoreMapper mapper;

    public SalesStoreRepository(SalesStoreMapper mapper) {
        this.mapper = mapper;
    }

    public SalesStore save(SalesStore store) {
        SalesStoreDO existing = mapper.selectStore(store.tenantId(), store.storeId());
        SalesStoreDO data = toStoreData(store);
        if (existing == null) {
            mapper.insertStore(data);
        } else {
            mapper.updateStore(data);
        }
        return store;
    }

    public Optional<SalesStore> find(String tenantId, String storeId) {
        return Optional.ofNullable(mapper.selectStore(tenantId, storeId))
                .map(this::toStoreDomain);
    }

    public Optional<SalesStore> findByCode(String tenantId, String platform, String storeCode) {
        return Optional.ofNullable(mapper.selectStoreByCode(tenantId, platform, storeCode))
                .map(this::toStoreDomain);
    }

    public List<SalesStore> list(String tenantId) {
        return mapper.selectStores(tenantId).stream()
                .map(this::toStoreDomain).collect(Collectors.toList());
    }

    public SalesTracking saveTracking(SalesTracking tracking) {
        mapper.insertTracking(toTrackingData(tracking));
        return tracking;
    }

    public List<SalesTracking> listTrackings(String tenantId, String storeId) {
        return mapper.selectTrackingsByStore(tenantId, storeId).stream()
                .map(this::toTrackingDomain).collect(Collectors.toList());
    }

    public List<SalesTracking> listTrackingsBySku(String tenantId, String sellerSku) {
        return mapper.selectTrackingsBySku(tenantId, sellerSku).stream()
                .map(this::toTrackingDomain).collect(Collectors.toList());
    }

    public StoreMetrics saveMetrics(StoreMetrics m) {
        mapper.insertMetrics(toMetricsData(m));
        return m;
    }

    public List<StoreMetrics> listMetrics(String tenantId, String storeId) {
        return mapper.selectMetricsByStore(tenantId, storeId).stream()
                .map(this::toMetricsDomain).collect(Collectors.toList());
    }

    private SalesStoreDO toStoreData(SalesStore s) {
        SalesStoreDO data = new SalesStoreDO();
        data.setStoreId(s.storeId());
        data.setTenantId(s.tenantId());
        data.setPlatform(s.platform());
        data.setStoreCode(s.storeCode());
        data.setStoreName(s.storeName());
        data.setStatus(s.status().name());
        data.setCreatedAt(s.createdAt() != null ? s.createdAt() : Instant.now());
        data.setUpdatedAt(Instant.now());
        return data;
    }

    private SalesStore toStoreDomain(SalesStoreDO d) {
        return new SalesStore(d.getStoreId(), d.getTenantId(), d.getPlatform(), d.getStoreCode(),
                d.getStoreName(), StoreStatus.valueOf(d.getStatus()), d.getCreatedAt(), d.getUpdatedAt());
    }

    private SalesTrackingDO toTrackingData(SalesTracking t) {
        SalesTrackingDO data = new SalesTrackingDO();
        data.setTrackingId(t.trackingId());
        data.setTenantId(t.tenantId());
        data.setStoreId(t.storeId());
        data.setSellerSku(t.sellerSku());
        data.setMarketplaceId(t.marketplaceId());
        data.setUnitsSold(t.unitsSold());
        data.setRevenue(t.revenue());
        data.setAveragePrice(t.averagePrice());
        data.setPeriodStart(t.periodStart());
        data.setPeriodEnd(t.periodEnd());
        data.setCreatedAt(t.createdAt() != null ? t.createdAt() : Instant.now());
        return data;
    }

    private SalesTracking toTrackingDomain(SalesTrackingDO d) {
        return new SalesTracking(d.getTrackingId(), d.getTenantId(), d.getStoreId(), d.getSellerSku(),
                d.getMarketplaceId(), d.getUnitsSold() != null ? d.getUnitsSold() : 0,
                d.getRevenue(), d.getAveragePrice(), d.getPeriodStart(), d.getPeriodEnd(), d.getCreatedAt());
    }

    private StoreMetricsDO toMetricsData(StoreMetrics m) {
        StoreMetricsDO data = new StoreMetricsDO();
        data.setMetricsId(m.metricsId());
        data.setTenantId(m.tenantId());
        data.setStoreId(m.storeId());
        data.setMarketplaceId(m.marketplaceId());
        data.setTotalRevenue(m.totalRevenue());
        data.setTotalOrders(m.totalOrders());
        data.setAverageOrderValue(m.averageOrderValue());
        data.setReturnRate(m.returnRate());
        data.setFeedbackScore(m.feedbackScore());
        data.setPeriodStart(m.periodStart());
        data.setPeriodEnd(m.periodEnd());
        data.setCreatedAt(m.createdAt() != null ? m.createdAt() : Instant.now());
        return data;
    }

    private StoreMetrics toMetricsDomain(StoreMetricsDO d) {
        return new StoreMetrics(d.getMetricsId(), d.getTenantId(), d.getStoreId(), d.getMarketplaceId(),
                d.getTotalRevenue(), d.getTotalOrders(), d.getAverageOrderValue(),
                d.getReturnRate(), d.getFeedbackScore(), d.getPeriodStart(), d.getPeriodEnd(), d.getCreatedAt());
    }
}
