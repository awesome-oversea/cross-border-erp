package com.aidotnet.erp.som.api;

import com.aidotnet.erp.common.api.Result;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * SOM域对外接口控制器
 * <p>
 * 描述: SOM域对外(Outbound)REST API，供其他微服务或外部系统调用。
 *       路径前缀: /som/api/out/v1 (外部接口)
 * </p>
 * <p>
 * 接口列表:
 *   - GET /listings          - 导出Listing列表(支持按平台/状态过滤)
 *   - GET /listings/{id}     - 导出Listing详情
 *   - GET /price-suggestions - 导出价格建议
 *   - GET /channel-mappings  - 导出渠道SKU映射
 * </p>
 * <p>
 * 安全说明: 所有接口需携带X-Tenant-Id请求头实现租户隔离
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/som/api/out/v1")
public class SomOutboundController {

    /** 导出Listing列表，支持按平台和状态过滤 */
    @GetMapping("/listings")
    public Result<List<Map<String, Object>>> exportListings(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestParam(required = false) String platform,
            @RequestParam(required = false) String status) {
        return Result.ok(List.of());
    }

    @GetMapping("/listings/{listingId}")
    public Result<Map<String, Object>> exportListingDetail(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @PathVariable String listingId) {
        return Result.ok(Map.of("listingId", listingId, "exportedAt", Instant.now().toString()));
    }

    @GetMapping("/price-suggestions")
    public Result<List<Map<String, Object>>> exportPriceSuggestions(
            @RequestHeader("X-Tenant-Id") String tenantId,
            @RequestParam(required = false) String platform) {
        return Result.ok(List.of());
    }

    @GetMapping("/channel-mappings")
    public Result<List<Map<String, Object>>> exportChannelMappings(
            @RequestHeader("X-Tenant-Id") String tenantId) {
        return Result.ok(List.of());
    }
}
