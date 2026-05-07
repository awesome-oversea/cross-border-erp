package com.aidotnet.erp.sys.domain;

import java.time.Instant;

/**
 * 插头国标规则领域模型
 * <p>
 * 描述: 系统设置域规则引擎的一部分，定义各国家/地区使用的电源插头标准映射。
 *       当OMS处理订单时，通过跨域服务接口调用此规则，按收货国家自动匹配
 *       对应插头规格的SKU，确保出口产品符合目标国家的电器标准。
 * </p>
 * <p>
 * 业务规则:
 *   1. 同一国家+产品类目只能有一条有效规则
 *   2. OMS履约时通过SYS规则查询接口获取匹配的插头SKU
 *   3. 未匹配到规则时保留原SKU(非强制校验)
 * </p>
 * <p>
 * 插头标准:
 *   EU - 欧标(欧洲大陆，C/E/F型)
 *   US - 美标(美国/加拿大/日本，A/B型)
 *   UK - 英标(英国/香港/新加坡，G型)
 *   AU - 澳标(澳大利亚/新西兰，I型)
 *   CN - 国标(中国，A/C/I型)
 * </p>
 * <p>
 * 跨域调用:
 *   OMS通过Feign客户端调用SYS服务接口查询规则，
 *   SYS/${service-name}/api/in/v1/plug-rules/match
 * </p>
 *
 * @author ERP系统
 */
public record PlugStandardRule(
        String ruleId,
        String tenantId,
        /** 目标国家代码(ISO 3166-1 alpha-2) */
        String countryCode,
        /** 产品类目ID(可为空，空表示适用于所有类目) */
        String categoryId,
        /** 插头标准编码(EU/US/UK/AU/CN) */
        String plugStandard,
        /** 替换的目标SKU(插头规格SKU) */
        String targetSku,
        /** 优先级(数字越大优先级越高) */
        int priority,
        boolean enabled,
        Instant createdAt,
        Instant updatedAt
) {}
