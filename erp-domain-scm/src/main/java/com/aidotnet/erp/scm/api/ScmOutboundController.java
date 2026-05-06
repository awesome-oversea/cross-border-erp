package com.aidotnet.erp.scm.api;

import com.aidotnet.erp.common.api.Result;
import com.aidotnet.erp.common.exception.BizException;
import com.aidotnet.erp.common.tenant.TenantContext;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SCM域对外接口控制器
 * <p>
 * 描述: SCM域对外(Outbound)REST API，供外部供应商系统或第三方平台调用。
 *       路径前缀: /scm/api/out/v1 (外部接口)
 * </p>
 * <p>
 * 接口列表:
 *   - POST /purchase-orders/submit          - 向供应商提交采购单
 *   - GET  /suppliers/{supplierId}/products - 查询供应商产品列表
 *   - POST /suppliers/{supplierId}/inquiry  - 向供应商发送询价
 * </p>
 *
 * @author ERP系统
 */
@RestController
@RequestMapping("/scm/api/out/v1")
public class ScmOutboundController {

    /** 向供应商提交采购单 */
    @PostMapping("/purchase-orders/submit")
    public Result<Map<String, Object>> submitPurchaseOrderToSupplier(@RequestBody Map<String, Object> request) {
        return Result.ok(Map.of("status", "SUBMITTED", "poId", request.getOrDefault("poId", "")));
    }

    @GetMapping("/suppliers/{supplierId}/products")
    public Result<Map<String, Object>> fetchSupplierProducts(@PathVariable String supplierId) {
        return Result.ok(Map.of("supplierId", supplierId, "products", java.util.List.of()));
    }

    @PostMapping("/suppliers/{supplierId}/inquiry")
    public Result<Map<String, Object>> sendInquiryToSupplier(@PathVariable String supplierId,
                                                              @RequestBody Map<String, Object> request) {
        return Result.ok(Map.of("supplierId", supplierId, "inquirySent", true));
    }

    private String currentTenant() {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            throw new BizException("TENANT_REQUIRED", "租户不能为空");
        }
        return tenantId;
    }
}
