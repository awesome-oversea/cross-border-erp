package com.aidotnet.erp.common.api;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("apiGroupedOpenApiConfig")
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("跨境电商ERP系统API")
                        .version("v1.0")
                        .description("跨境电商ERP系统API文档，遵循 /{service-name}/api/{direction}/v1/{resource} 路径规范"))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .schemaRequirement("BearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));
    }

    @Bean
    public GroupedOpenApi iamApi() {
        return GroupedOpenApi.builder().group("IAM-组织权限")
                .pathsToMatch("/iam/api/**").build();
    }

    @Bean
    public GroupedOpenApi pdmApi() {
        return GroupedOpenApi.builder().group("PDM-产品开发")
                .pathsToMatch("/pdm/api/**").build();
    }

    @Bean
    public GroupedOpenApi somApi() {
        return GroupedOpenApi.builder().group("SOM-销售运营")
                .pathsToMatch("/som/api/**").build();
    }

    @Bean
    public GroupedOpenApi adsApi() {
        return GroupedOpenApi.builder().group("ADS-广告管理")
                .pathsToMatch("/ads/api/**").build();
    }

    @Bean
    public GroupedOpenApi omsApi() {
        return GroupedOpenApi.builder().group("OMS-订单")
                .pathsToMatch("/oms/api/**").build();
    }

    @Bean
    public GroupedOpenApi scmApi() {
        return GroupedOpenApi.builder().group("SCM-供应链")
                .pathsToMatch("/scm/api/**").build();
    }

    @Bean
    public GroupedOpenApi wmsApi() {
        return GroupedOpenApi.builder().group("WMS-仓储")
                .pathsToMatch("/wms/api/**").build();
    }

    @Bean
    public GroupedOpenApi fbaApi() {
        return GroupedOpenApi.builder().group("FBA-海外仓")
                .pathsToMatch("/fba/api/**").build();
    }

    @Bean
    public GroupedOpenApi tmsApi() {
        return GroupedOpenApi.builder().group("TMS-物流")
                .pathsToMatch("/tms/api/**").build();
    }

    @Bean
    public GroupedOpenApi crmApi() {
        return GroupedOpenApi.builder().group("CRM-客服售后")
                .pathsToMatch("/crm/api/**").build();
    }

    @Bean
    public GroupedOpenApi fmsApi() {
        return GroupedOpenApi.builder().group("FMS-财务")
                .pathsToMatch("/fms/api/**").build();
    }

    @Bean
    public GroupedOpenApi biApi() {
        return GroupedOpenApi.builder().group("BI-商业智能")
                .pathsToMatch("/bi/api/**").build();
    }

    @Bean
    public GroupedOpenApi sysApi() {
        return GroupedOpenApi.builder().group("SYS-系统设置")
                .pathsToMatch("/sys/api/**").build();
    }

    @Bean
    public GroupedOpenApi dashboardApi() {
        return GroupedOpenApi.builder().group("DASHBOARD-工作台")
                .pathsToMatch("/dashboard/api/**").build();
    }
}
