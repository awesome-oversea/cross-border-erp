package com.aidotnet.erp.sys.client;

import com.aidotnet.erp.common.api.Result;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * ADS域Feign客户端，用于系统设置域与广告域的跨域通信。
 * <p>
 * 描述: SYS域通过此客户端查询ADS域的广告数据，
 *       用于广告超支预警关联、AI功能开关状态同步和PMS数据源。
 * </p>
 * <p>
 * 跨域关联:
 *   - SYS → ADS: 查询广告活动详情(用于超支预警关联)
 *   - SYS → ADS: 查询广告策略(用于AI功能开关状态同步)
 * </p>
 *
 * @author ERP系统
 */
@FeignClient(name = "erp-app", contextId = "ads-client-sys", path = "/ads/api/in/v1")
public interface AdsClient {

    /**
     * 按广告活动ID查询广告活动详情。
     *
     * @param tenantId   租户ID
     * @param campaignId 广告活动ID
     * @return 广告活动信息
     */
    @GetMapping("/campaigns/{campaignId}")
    Result<Map<String, Object>> getCampaign(@RequestHeader("X-Tenant-Id") String tenantId,
                                            @PathVariable String campaignId);

    /**
     * 查询广告策略列表。
     *
     * @param tenantId 租户ID
     * @return 广告策略列表
     */
    @GetMapping("/strategies")
    Result<Map<String, Object>> listStrategies(@RequestHeader("X-Tenant-Id") String tenantId);
}
