package com.aidotnet.erp.crm.client;

import com.aidotnet.erp.common.api.Result;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * PDM客户端 - CRM域调用PDM域
 * <p>
 * 描述: CRM域通过此客户端调用PDM域的内部API，获取产品信息。
 *       用于客户评价关联产品数据。
 * </p>
 *
 * @author ERP系统
 */
@FeignClient(name = "erp-app", contextId = "crm-pdm-client", path = "/pdm/api/in/v1")
public interface PdmClient {

    @GetMapping("/products/{productId}")
    Result<ProductResponse> getProduct(@PathVariable String productId);

    @GetMapping("/products")
    Result<List<ProductResponse>> listProducts(@RequestParam String tenantId);

    record ProductResponse(String productId, String tenantId, String name, String category, String status) {}
}
