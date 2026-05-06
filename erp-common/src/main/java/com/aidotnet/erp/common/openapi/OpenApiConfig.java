package com.aidotnet.erp.common.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration("legacyOpenApiConfig")
@Profile("docs-legacy")
public class OpenApiConfig {

    @Bean
    public OpenAPI crossBorderErpOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Cross-Border ERP API")
                .version("0.1.0")
                .description("跨境电商ERP模块化单体接口基线"));
    }

    @Bean
    public GroupedOpenApi dashboardApi() {
        return domainApi("dashboard");
    }

    @Bean
    public GroupedOpenApi iamApi() {
        return domainApi("iam");
    }

    @Bean
    public GroupedOpenApi pdmApi() {
        return domainApi("pdm");
    }

    @Bean
    public GroupedOpenApi somApi() {
        return domainApi("som");
    }

    @Bean
    public GroupedOpenApi adsApi() {
        return domainApi("ads");
    }

    @Bean
    public GroupedOpenApi omsApi() {
        return domainApi("oms");
    }

    @Bean
    public GroupedOpenApi scmApi() {
        return domainApi("scm");
    }

    @Bean
    public GroupedOpenApi wmsApi() {
        return domainApi("wms");
    }

    @Bean
    public GroupedOpenApi fbaApi() {
        return domainApi("fba");
    }

    @Bean
    public GroupedOpenApi tmsApi() {
        return domainApi("tms");
    }

    @Bean
    public GroupedOpenApi crmApi() {
        return domainApi("crm");
    }

    @Bean
    public GroupedOpenApi fmsApi() {
        return domainApi("fms");
    }

    @Bean
    public GroupedOpenApi biApi() {
        return domainApi("bi");
    }

    @Bean
    public GroupedOpenApi sysApi() {
        return domainApi("sys");
    }

    private GroupedOpenApi domainApi(String domain) {
        return GroupedOpenApi.builder()
                .group(domain)
                .pathsToMatch("/" + domain + "/api/**")
                .build();
    }
}
