package com.aidotnet.erp.scm.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.scm.application.PurchaseTrackingService;
import com.aidotnet.erp.scm.domain.PurchaseException;
import com.aidotnet.erp.scm.domain.PurchaseExceptionStatus;
import com.aidotnet.erp.scm.domain.PurchaseExceptionType;
import com.aidotnet.erp.scm.domain.PurchaseTracking;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 采购跟踪控制器
 * <p>
 * 描述: SCM域采购跟踪REST API，提供收货记录、异常处理等接口。
 *       路径前缀: /scm/api/in/v1/purchase-tracking (内部接口)
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/scm/api/in/v1/purchase-tracking")
public class PurchaseTrackingController {

    private final PurchaseTrackingService trackingService;

    public PurchaseTrackingController(PurchaseTrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @PostMapping("/{poId}/init")
    public Result<List<PurchaseTracking>> initTracking(@PathVariable String poId) {
        trackingService.initTracking(currentTenant(), poId);
        return Result.ok(trackingService.listTrackings(currentTenant(), poId));
    }

    @PostMapping("/{poId}/lines/{lineId}/receipt")
    public Result<PurchaseTracking> recordReceipt(@PathVariable String poId, @PathVariable String lineId,
                                                  @Valid @RequestBody RecordReceiptRequest request) {
        return Result.ok(trackingService.recordReceipt(currentTenant(), poId, lineId,
                request.receivedQuantity(), request.actualUnitCost()));
    }

    @PostMapping("/{poId}/lines/{lineId}/damage")
    public Result<PurchaseTracking> recordDamage(@PathVariable String poId, @PathVariable String lineId,
                                                 @Valid @RequestBody RecordDamageRequest request) {
        return Result.ok(trackingService.recordDamage(currentTenant(), poId, lineId,
                request.damagedQuantity(), request.reason()));
    }

    @PostMapping("/{poId}/lines/{lineId}/return")
    public Result<PurchaseTracking> recordReturn(@PathVariable String poId, @PathVariable String lineId,
                                                 @Valid @RequestBody RecordReturnRequest request) {
        return Result.ok(trackingService.recordReturn(currentTenant(), poId, lineId,
                request.returnedQuantity(), request.reason()));
    }

    @PostMapping("/{poId}/exceptions")
    public Result<PurchaseException> createException(@PathVariable String poId,
                                                     @Valid @RequestBody CreateExceptionRequest request) {
        return Result.ok(trackingService.createException(currentTenant(), poId, request.lineId(),
                request.sellerSku(), request.exceptionType(), request.expectedValue(),
                request.actualValue(), request.description()));
    }

    @PatchMapping("/exceptions/{exceptionId}/handle")
    public Result<PurchaseException> handleException(@PathVariable String exceptionId,
                                                     @Valid @RequestBody HandleExceptionRequest request) {
        return Result.ok(trackingService.handleException(currentTenant(), exceptionId,
                request.handlerId(), request.handlerNote(), request.resolution()));
    }

    @GetMapping("/{poId}/trackings")
    public Result<List<PurchaseTracking>> listTrackings(@PathVariable String poId) {
        return Result.ok(trackingService.listTrackings(currentTenant(), poId));
    }

    @GetMapping("/{poId}/exceptions")
    public Result<List<PurchaseException>> listExceptions(@PathVariable String poId) {
        return Result.ok(trackingService.listExceptions(currentTenant(), poId));
    }

    @GetMapping("/exceptions/pending")
    public Result<List<PurchaseException>> listPendingExceptions() {
        return Result.ok(trackingService.listPendingExceptions(currentTenant()));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }

    public record RecordReceiptRequest(@Positive int receivedQuantity, BigDecimal actualUnitCost) {}
    public record RecordDamageRequest(@Positive int damagedQuantity, @NotBlank String reason) {}
    public record RecordReturnRequest(@Positive int returnedQuantity, @NotBlank String reason) {}
    public record CreateExceptionRequest(String lineId, @NotBlank String sellerSku,
                                         @NotNull PurchaseExceptionType exceptionType,
                                         BigDecimal expectedValue, BigDecimal actualValue,
                                         @NotBlank String description) {}
    public record HandleExceptionRequest(@NotBlank String handlerId, String handlerNote,
                                         @NotNull PurchaseExceptionStatus resolution) {}
}
