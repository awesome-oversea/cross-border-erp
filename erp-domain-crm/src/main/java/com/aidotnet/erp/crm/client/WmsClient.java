package com.aidotnet.erp.crm.client;

import com.aidotnet.erp.common.api.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * WMS仓储域Feign客户端
 * <p>
 * CRM售后域通过此客户端调用WMS域的库存操作接口，
 * 实现退换货入库的库存回写。
 * </p>
 */
@FeignClient(name = "erp-app", contextId = "crm-wms-client", path = "/wms/api/in/v1")
public interface WmsClient {

    /**
     * 退货入库：将退回商品重新入库到指定仓库
     */
    @PostMapping("/inventory/receive-return")
    Result<Void> receiveReturn(@RequestBody ReceiveReturnRequest request);

    record ReceiveReturnRequest(String warehouseId, String sellerSku, int quantity, String returnId) {}
}
