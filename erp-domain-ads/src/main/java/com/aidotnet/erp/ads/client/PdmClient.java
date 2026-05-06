package com.aidotnet.erp.ads.client;

import com.aidotnet.erp.common.api.Result;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * PDM域Feign客户端，用于广告域与产品域的跨域通信。
 * <p>
 * 描述: 广告域通过此客户端查询PDM域的产品信息，
 *       用于广告活动创建时关联产品、获取产品关键词建议等。
 * </p>
 * <p>
 * 跨域关联:
 *   - ADS → PDM: 查询产品信息(用于广告活动关联产品)
 *   - ADS → PDM: 按SKU查询产品(用于广告关键词自动生成)
 * </p>
 *
 * @author ERP系统
 */
@FeignClient(name = "erp-app", contextId = "pdm-client-ads", path = "/pdm/api/in/v1")
public interface PdmClient {

    /**
     * 按产品ID查询产品信息。
     *
     * @param productId 产品ID
     * @return 产品信息(包含产品名称、SKU、类目等)
     */
    @GetMapping("/products/{productId}")
    Result<Map<String, Object>> getProduct(@PathVariable String productId);

    /**
     * 按卖家SKU查询产品信息。
     *
     * @param sellerSku 卖家SKU
     * @return 产品信息(包含产品名称、SKU、类目等)
     */
    @GetMapping("/products/sku/{sellerSku}")
    Result<Map<String, Object>> getProductBySku(@PathVariable String sellerSku);
}
