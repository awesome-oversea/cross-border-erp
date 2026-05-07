package com.aidotnet.erp.fms.client;

import com.aidotnet.erp.common.api.Result;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * SCM客户端 - FMS域调用SCM域
 * <p>
 * 描述: FMS域通过此客户端调用SCM域的内部API，获取供应商和采购单信息。
 *       用于应付账款关联采购单和供应商对账。
 * </p>
 *
 * @author ERP系统
 */
@FeignClient(name = "erp-app", contextId = "fms-scm-client", path = "/scm/api/in/v1")
public interface ScmClient {

    @GetMapping("/suppliers/{supplierId}")
    Result<SupplierResponse> getSupplier(@PathVariable String supplierId);

    @GetMapping("/purchase-orders/{poId}")
    Result<PurchaseOrderResponse> getPurchaseOrder(@PathVariable String poId);

    record SupplierResponse(String supplierId, String tenantId, String name, String code, String status) {}

    record PurchaseOrderResponse(String poId, String tenantId, String supplierId, String status, java.math.BigDecimal totalAmount) {}
}
