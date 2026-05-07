package com.aidotnet.erp.tms.client;

import com.aidotnet.erp.common.api.Result;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * SCM客户端 - TMS域调用SCM域
 * <p>
 * 描述: TMS域通过此客户端调用SCM域的内部API，获取供应商信息。
 *       用于物流商选择和供应商关联。
 * </p>
 *
 * @author ERP系统
 */
@FeignClient(name = "erp-app", contextId = "tms-scm-client", path = "/scm/api/in/v1")
public interface ScmClient {

    @GetMapping("/suppliers")
    Result<List<SupplierResponse>> listSuppliers(@RequestParam String tenantId);

    @GetMapping("/suppliers/{supplierId}")
    Result<SupplierResponse> getSupplier(@PathVariable String supplierId);

    record SupplierResponse(String supplierId, String tenantId, String name, String code, String status) {}
}
