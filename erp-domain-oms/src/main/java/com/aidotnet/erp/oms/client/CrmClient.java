package com.aidotnet.erp.oms.client;

import com.aidotnet.erp.common.api.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * CRM客户端 - OMS域调用CRM域
 * <p>
 * 描述: OMS域通过此客户端调用CRM域的内部API，获取客户信息。
 *       用于订单创建时校验客户数据和获取客户偏好。
 * </p>
 *
 * @author ERP系统
 */
@FeignClient(name = "erp-app", contextId = "oms-crm-client", path = "/crm/api/in/v1")
public interface CrmClient {

    @GetMapping("/customers/{customerId}")
    Result<CustomerResponse> getCustomer(@PathVariable String customerId);

    @GetMapping("/customers/{customerId}/profile")
    Result<CustomerProfileResponse> getCustomerProfile(@RequestParam String tenantId, @PathVariable String customerId);

    record CustomerResponse(String customerId, String tenantId, String name, String email, String segment) {}

    record CustomerProfileResponse(String profileId, String customerId, String segment, String riskLevel) {}
}
