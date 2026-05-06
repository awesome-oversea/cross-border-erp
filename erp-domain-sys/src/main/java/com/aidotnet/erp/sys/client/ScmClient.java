package com.aidotnet.erp.sys.client;

import com.aidotnet.erp.common.api.Result;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * SCM域Feign客户端，用于系统设置域与供应链域的跨域通信。
 * <p>
 * 描述: SYS域通过此客户端查询SCM域的采购和供应商数据，
 *       用于供应商风险预警关联、审批流配置和PMS数据源。
 * </p>
 * <p>
 * 跨域关联:
 *   - SYS → SCM: 查询供应商详情(用于风险预警关联)
 *   - SYS → SCM: 查询采购单详情(用于审批流关联)
 * </p>
 *
 * @author ERP系统
 */
@FeignClient(name = "erp-app", contextId = "scm-client-sys", path = "/scm/api/in/v1")
public interface ScmClient {

    /**
     * 按供应商ID查询供应商详情。
     *
     * @param tenantId   租户ID
     * @param supplierId 供应商ID
     * @return 供应商信息
     */
    @GetMapping("/suppliers/{supplierId}")
    Result<Map<String, Object>> getSupplier(@RequestHeader("X-Tenant-Id") String tenantId,
                                            @PathVariable String supplierId);

    /**
     * 按采购单ID查询采购单详情。
     *
     * @param tenantId   租户ID
     * @param purchaseId 采购单ID
     * @return 采购单信息
     */
    @GetMapping("/purchases/{purchaseId}")
    Result<Map<String, Object>> getPurchase(@RequestHeader("X-Tenant-Id") String tenantId,
                                            @PathVariable String purchaseId);
}
