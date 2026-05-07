package com.aidotnet.erp.som.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.som.application.SalesStoreService;
import com.aidotnet.erp.som.application.SalesStoreService.CreateHijackAlertCommand;
import com.aidotnet.erp.som.application.SalesStoreService.RecordBuyboxCommand;
import com.aidotnet.erp.som.domain.BuyboxMonitor;
import com.aidotnet.erp.som.domain.HijackAlert;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 店铺运营监控控制器
 * <p>
 * 承载 Buybox 监控、跟卖告警等高频运营信号接口，同时兼容 `/som/api/v1` 与 `/som/api/in/v1`。
 * 该控制器只编排应用服务，不改变原有 SOM 业务语义。
 * </p>
 */
@RestController
@RequestMapping({"/som/api/v1", "/som/api/in/v1"})
public class SalesStoreMonitorController {

    private final SalesStoreService salesStoreService;

    public SalesStoreMonitorController(SalesStoreService salesStoreService) {
        this.salesStoreService = salesStoreService;
    }

    @PostMapping("/buybox-monitors")
    public Result<BuyboxMonitor> recordBuyboxMonitor(@Valid @RequestBody RecordBuyboxMonitorRequest request) {
        return Result.ok(salesStoreService.recordBuybox(currentTenant(), new RecordBuyboxCommand(
                request.listingId(), request.platform(), request.marketplace(), request.winnerName(),
                request.winnerPrice(), request.ourPrice(), request.hijackerCount())));
    }

    @GetMapping("/buybox-monitors")
    public Result<List<BuyboxMonitor>> listBuyboxMonitors(@RequestParam(required = false) String listingId) {
        return Result.ok(salesStoreService.listBuyboxMonitors(currentTenant(), listingId));
    }

    @PostMapping("/hijack-alerts")
    public Result<HijackAlert> createHijackAlert(@Valid @RequestBody CreateHijackAlertRequest request) {
        return Result.ok(salesStoreService.createHijackAlert(currentTenant(), new CreateHijackAlertCommand(
                request.listingId(), request.platform(), request.marketplace(), request.hijackerName(),
                request.hijackerPrice(), request.ourPrice(), request.severity())));
    }

    @GetMapping("/hijack-alerts")
    public Result<List<HijackAlert>> listHijackAlerts(@RequestParam(required = false) String listingId,
                                                      @RequestParam(defaultValue = "true") boolean includeResolved) {
        return Result.ok(salesStoreService.listHijackAlerts(currentTenant(), listingId, includeResolved));
    }

    @PatchMapping("/hijack-alerts/{alertId}/acknowledge")
    public Result<HijackAlert> acknowledgeHijackAlert(@PathVariable String alertId,
                                                      @Valid @RequestBody AcknowledgeHijackAlertRequest request) {
        return Result.ok(salesStoreService.acknowledgeHijackAlert(currentTenant(), alertId,
                request.handledBy(), request.handleNote()));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record RecordBuyboxMonitorRequest(@NotBlank String listingId, @NotBlank String platform,
                                             @NotBlank String marketplace, @NotBlank String winnerName,
                                             @Positive BigDecimal winnerPrice, @Positive BigDecimal ourPrice,
                                             @PositiveOrZero int hijackerCount) {}

    public record CreateHijackAlertRequest(@NotBlank String listingId, @NotBlank String platform,
                                           @NotBlank String marketplace, @NotBlank String hijackerName,
                                           @Positive BigDecimal hijackerPrice, @Positive BigDecimal ourPrice,
                                           @NotBlank String severity) {}

    public record AcknowledgeHijackAlertRequest(@NotBlank String handledBy, String handleNote) {}
}
