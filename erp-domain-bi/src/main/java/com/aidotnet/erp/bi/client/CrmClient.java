package com.aidotnet.erp.bi.client;

import com.aidotnet.erp.common.api.Result;
import java.time.Instant;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * CRM 客户端
 * <p>
 * 为 BI 运营监控提供评价分析只读视图。
 * </p>
 */
@FeignClient(name = "erp-app", contextId = "bi-crm-client", path = "/crm/api/in/v1")
public interface CrmClient {

    @GetMapping("/review-analyses/by-sku")
    Result<List<ReviewAnalysisResponse>> listReviewAnalysesBySku(@RequestParam String sellerSku);

    record ReviewAnalysisResponse(String analysisId, String tenantId, String sellerSku,
                                  String marketplaceId, double averageRating, int totalReviews,
                                  int positiveCount, int neutralCount, int negativeCount,
                                  String sentimentSummary, Instant analyzedAt) {}
}
