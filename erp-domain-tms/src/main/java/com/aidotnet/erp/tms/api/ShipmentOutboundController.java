package com.aidotnet.erp.tms.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import com.aidotnet.erp.tms.application.ShipmentService;
import com.aidotnet.erp.tms.application.ShipmentService.RecommendCarrierCommand;
import com.aidotnet.erp.tms.domain.CarrierRecommendation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TMS域对外接口控制器
 * <p>
 * 描述: TMS域对外(Outbound)REST API，供外部系统创建发货、查询轨迹、取消发货和获取承运商推荐。
 *       路径前缀: /tms/api/out/v1 (外部接口)
 * </p>
 * <p>
 * 接口列表:
 *   - POST /shipments/create                     - 向承运商创建发货
 *   - GET  /shipments/{trackingNo}/tracking       - 查询物流轨迹
 *   - POST /shipments/{trackingNo}/cancel         - 取消发货
 *   - GET  /carriers/{carrierId}/rates            - 查询承运商费率
 *   - POST /carriers/recommendations              - 获取承运商推荐
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/tms/api/out/v1")
public class ShipmentOutboundController {

    private final ShipmentService shipmentService;

    /**
     * 构造函数 - 依赖注入发货服务
     *
     * @param shipmentService 发货管理应用服务
     */
    public ShipmentOutboundController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    @PostMapping("/shipments/create")
    public Result<Map<String, Object>> createShipmentToCarrier(@RequestBody Map<String, Object> request) {
        return Result.ok(Map.of("status", "CREATED", "carrierId", request.getOrDefault("carrierId", "")));
    }

    @GetMapping("/shipments/{trackingNo}/tracking")
    public Result<Map<String, Object>> fetchTrackingInfo(@PathVariable String trackingNo) {
        return Result.ok(Map.of("trackingNo", trackingNo, "status", "IN_TRANSIT", "location", ""));
    }

    @PostMapping("/shipments/{trackingNo}/cancel")
    public Result<Map<String, Object>> cancelShipmentWithCarrier(@PathVariable String trackingNo) {
        return Result.ok(Map.of("trackingNo", trackingNo, "cancelled", true));
    }

    @GetMapping("/carriers/{carrierId}/rates")
    public Result<Map<String, Object>> fetchCarrierRates(@PathVariable String carrierId) {
        return Result.ok(Map.of("carrierId", carrierId, "rates", java.util.List.of()));
    }

    @PostMapping("/carriers/recommendations")
    public Result<List<CarrierRecommendation>> recommendCarriers(@Valid @RequestBody RecommendCarrierRequest request) {
        return Result.ok(shipmentService.recommendCarriers(currentTenant(), new RecommendCarrierCommand(
                request.destinationCountry(),
                request.warehouseCountry(),
                request.packageQuantity(),
                request.packageAmount(),
                request.splitShipment())));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "绉熸埛涓嶈兘涓虹┖");
        }
        return tenantId;
    }

    public record RecommendCarrierRequest(@NotBlank String destinationCountry, @NotBlank String warehouseCountry,
                                          @Positive int packageQuantity, BigDecimal packageAmount,
                                          boolean splitShipment) {}
}
