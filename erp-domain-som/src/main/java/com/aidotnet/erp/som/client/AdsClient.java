package com.aidotnet.erp.som.client;

import com.aidotnet.erp.common.api.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * ADS客户端 - SOM域调用ADS域
 * <p>
 * 描述: SOM域通过此客户端调用ADS域的内部API，获取广告策略信息。
 *       用于Listing优化与广告策略联动。
 * </p>
 *
 * @author ERP系统
 */
@FeignClient(name = "erp-app", contextId = "som-ads-client", path = "/ads/api/in/v1")
public interface AdsClient {

    @GetMapping("/campaigns/{campaignId}")
    Result<CampaignResponse> getCampaign(@PathVariable String campaignId);

    record CampaignResponse(String campaignId, String tenantId, String name, String status, String targetingType) {}
}
