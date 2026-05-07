package com.aidotnet.erp.bi.client;

import com.aidotnet.erp.common.api.Result;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * FMS客户端 - BI域调用FMS域
 * <p>
 * 描述: BI域通过此客户端调用FMS域的内部API，获取财务数据用于利润分析报表。
 * </p>
 *
 * @author ERP系统
 */
@FeignClient(name = "erp-app", contextId = "bi-fms-client", path = "/fms/api/in/v1")
public interface FmsClient {

    @GetMapping("/settlements")
    Result<List<SettlementResponse>> listSettlements(@RequestParam String tenantId);

    @GetMapping("/settlements/{settlementId}")
    Result<SettlementResponse> getSettlement(@PathVariable String settlementId);

    @GetMapping("/cost-events/by-source")
    Result<List<CostEventResponse>> listCostEventsBySource(@RequestParam String sourceType,
                                                           @RequestParam String sourceId);

    @GetMapping("/engine/profit-deviation-alerts")
    Result<List<ProfitDeviationAlertResponse>> listProfitDeviationAlerts(@RequestParam(required = false) String status);

    record SettlementResponse(String settlementId, String tenantId, String orderId, java.math.BigDecimal revenue, java.math.BigDecimal cost, java.math.BigDecimal profit) {}

    record CostEventResponse(String costEventId,
                             String tenantId,
                             String costType,
                             String sourceType,
                             String sourceId,
                             String sellerSku,
                             String storeId,
                             String channelCode,
                             String marketplaceId,
                             String currency,
                             BigDecimal amount,
                             Instant occurredAt,
                             Instant createdAt) {}

    record ProfitDeviationAlertResponse(String alertId,
                                        String tenantId,
                                        String dimensionType,
                                        String dimensionId,
                                        String sellerSku,
                                        BigDecimal expectedMargin,
                                        BigDecimal actualMargin,
                                        BigDecimal deviation,
                                        BigDecimal deviationThreshold,
                                        String severity,
                                        String status,
                                        Instant detectedAt,
                                        Instant resolvedAt) {}
}
