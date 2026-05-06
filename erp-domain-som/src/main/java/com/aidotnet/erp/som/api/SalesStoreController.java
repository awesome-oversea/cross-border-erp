package com.aidotnet.erp.som.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.som.application.SalesStoreService;
import com.aidotnet.erp.som.application.SalesStoreService.RecordMetricsCommand;
import com.aidotnet.erp.som.application.SalesStoreService.RecordSalesCommand;
import com.aidotnet.erp.som.application.SalesStoreService.SaveStoreCommand;
import com.aidotnet.erp.som.domain.SalesStore;
import com.aidotnet.erp.som.domain.SalesTracking;
import com.aidotnet.erp.som.domain.StoreMetrics;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 店铺管理控制器
 * <p>
 * 描述: SOM域店铺管理REST API，提供多平台店铺创建/连接/停用、
 *       销售追踪和店铺指标记录等接口。
 *       路径前缀: /som/api/in/v1 (内部接口)
 * </p>
 * <p>
 * 接口分组:
 *   1. 店铺管理 - /stores (创建/连接/停用/列表)
 *   2. 销售追踪 - /stores/{id}/sales (记录/列表)
 *   3. 店铺指标 - /stores/{id}/metrics (记录/列表)
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/som/api/in/v1")
public class SalesStoreController {

    private final SalesStoreService salesStoreService;

    /**
     * 构造函数 - 依赖注入店铺服务
     *
     * @param salesStoreService 店铺管理应用服务
     */
    public SalesStoreController(SalesStoreService salesStoreService) {
        this.salesStoreService = salesStoreService;
    }

    @PostMapping("/stores")
    public Result<SalesStore> create(@Valid @RequestBody CreateStoreRequest request) {
        return Result.ok(salesStoreService.create(currentTenant(), new SaveStoreCommand(request.platform(), request.storeCode(), request.storeName())));
    }

    @GetMapping("/stores")
    public Result<List<SalesStore>> list() {
        return Result.ok(salesStoreService.list(currentTenant()));
    }

    @PatchMapping("/stores/{storeId}/connect")
    public Result<SalesStore> connect(@PathVariable String storeId) {
        return Result.ok(salesStoreService.connect(currentTenant(), storeId));
    }

    @PatchMapping("/stores/{storeId}/disable")
    public Result<SalesStore> disable(@PathVariable String storeId) {
        return Result.ok(salesStoreService.disable(currentTenant(), storeId));
    }

    @PostMapping("/stores/{storeId}/sales")
    public Result<SalesTracking> recordSales(@PathVariable String storeId, @Valid @RequestBody RecordSalesRequest request) {
        return Result.ok(salesStoreService.recordSales(currentTenant(), new RecordSalesCommand(storeId,
                request.sellerSku(), request.marketplaceId(), request.unitsSold(), request.revenue(),
                request.periodStart(), request.periodEnd())));
    }

    @GetMapping("/stores/{storeId}/sales")
    public Result<List<SalesTracking>> listSalesByStore(@PathVariable String storeId) {
        return Result.ok(salesStoreService.listSalesByStore(currentTenant(), storeId));
    }

    @GetMapping("/sales")
    public Result<List<SalesTracking>> listSalesBySku(@NotBlank String sellerSku) {
        return Result.ok(salesStoreService.listSalesBySku(currentTenant(), sellerSku));
    }

    @PostMapping("/stores/{storeId}/metrics")
    public Result<StoreMetrics> recordMetrics(@PathVariable String storeId, @Valid @RequestBody RecordMetricsRequest request) {
        return Result.ok(salesStoreService.recordMetrics(currentTenant(), new RecordMetricsCommand(storeId,
                request.marketplaceId(), request.totalRevenue(), request.totalOrders(),
                request.returnRate(), request.feedbackScore(), request.periodStart(), request.periodEnd())));
    }

    @GetMapping("/stores/{storeId}/metrics")
    public Result<List<StoreMetrics>> listMetrics(@PathVariable String storeId) {
        return Result.ok(salesStoreService.listMetrics(currentTenant(), storeId));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record CreateStoreRequest(@NotBlank String platform, @NotBlank String storeCode, @NotBlank String storeName) {}

    public record RecordSalesRequest(@NotBlank String sellerSku, String marketplaceId, @Positive int unitsSold,
                                     @Positive BigDecimal revenue, Instant periodStart, Instant periodEnd) {}

    public record RecordMetricsRequest(String marketplaceId, @Positive BigDecimal totalRevenue,
                                       @Positive BigDecimal totalOrders, BigDecimal returnRate,
                                       BigDecimal feedbackScore, Instant periodStart, Instant periodEnd) {}
}
