package com.aidotnet.erp.crm.client;

import com.aidotnet.erp.common.api.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * ADS客户端 - CRM域调用ADS域
 * <p>
 * 描述: CRM域通过此客户端调用ADS域的内部API，获取广告投放数据。
 *       用于客户行为分析和营销效果评估。
 * </p>
 *
 * @author ERP系统
 */
@FeignClient(name = "erp-app", contextId = "crm-ads-client", path = "/ads/api/in/v1")
public interface AdsClient {

    @GetMapping("/campaigns/{campaignId}")
    Result<CampaignResponse> getCampaign(@PathVariable String campaignId);

    @GetMapping("/campaigns")
    Result<java.util.List<CampaignResponse>> listCampaigns(@RequestParam String tenantId);

    record CampaignResponse(String campaignId, String tenantId, String name, String status, String targetingType) {}
}
