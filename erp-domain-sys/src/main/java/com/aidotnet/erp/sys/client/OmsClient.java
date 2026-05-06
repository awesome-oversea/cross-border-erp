package com.aidotnet.erp.sys.client;

import com.aidotnet.erp.common.api.Result;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * OMS域Feign客户端，用于系统设置域与订单域的跨域通信。
 * <p>
 * 描述: SYS域通过此客户端查询OMS域的订单数据，
 *       用于业务预警关联、PMS草稿单据执行和操作日志记录。
 * </p>
 * <p>
 * 跨域关联:
 *   - SYS → OMS: 查询订单详情(用于预警关联和草稿执行)
 *   - SYS → OMS: 查询订单统计(用于PMS智能推荐数据源)
 * </p>
 *
 * @author ERP系统
 */
@FeignClient(name = "erp-app", contextId = "oms-client-sys", path = "/oms/api/in/v1")
public interface OmsClient {

    /**
     * 按订单ID查询订单详情。
     *
     * @param tenantId 租户ID
     * @param orderId  订单ID
     * @return 订单信息
     */
    @GetMapping("/orders/{orderId}")
    Result<Map<String, Object>> getOrder(@RequestHeader("X-Tenant-Id") String tenantId,
                                         @PathVariable String orderId);

    /**
     * 按状态查询订单数量。
     *
     * @param tenantId 租户ID
     * @param status   订单状态
     * @return 订单数量统计
     */
    @GetMapping("/orders/count")
    Result<Map<String, Object>> countByStatus(@RequestHeader("X-Tenant-Id") String tenantId,
                                              @RequestParam String status);
}
