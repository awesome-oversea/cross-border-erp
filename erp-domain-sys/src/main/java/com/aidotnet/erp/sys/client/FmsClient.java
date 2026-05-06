package com.aidotnet.erp.sys.client;

import com.aidotnet.erp.common.api.Result;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * FMS域Feign客户端，用于系统设置域与财务域的跨域通信。
 * <p>
 * 描述: SYS域通过此客户端查询FMS域的财务数据，
 *       用于发票设置关联、财务预警和PMS财务数据源。
 * </p>
 * <p>
 * 跨域关联:
 *   - SYS → FMS: 查询付款详情(用于逾期预警关联)
 *   - SYS → FMS: 查询财务摘要(用于发票设置和PMS数据源)
 * </p>
 *
 * @author ERP系统
 */
@FeignClient(name = "erp-app", contextId = "fms-client-sys", path = "/fms/api/in/v1")
public interface FmsClient {

    /**
     * 按付款ID查询付款详情。
     *
     * @param tenantId  租户ID
     * @param paymentId 付款ID
     * @return 付款信息
     */
    @GetMapping("/payments/{paymentId}")
    Result<Map<String, Object>> getPayment(@RequestHeader("X-Tenant-Id") String tenantId,
                                           @PathVariable String paymentId);

    /**
     * 查询财务摘要。
     *
     * @param tenantId  租户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 财务摘要数据
     */
    @GetMapping("/finance/summary")
    Result<Map<String, Object>> getFinanceSummary(@RequestHeader("X-Tenant-Id") String tenantId,
                                                   @RequestParam String startDate,
                                                   @RequestParam String endDate);
}
