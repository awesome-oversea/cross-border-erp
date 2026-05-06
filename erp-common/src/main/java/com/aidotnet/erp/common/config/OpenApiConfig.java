package com.aidotnet.erp.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration("erpOpenApiBaseConfig")
public class OpenApiConfig {

    @Bean
    @Primary
    public OpenAPI erpOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("跨境电商ERP系统 API")
                        .description("Cross-Border E-Commerce ERP System - RESTful API Documentation")
                        .version("v1.0.0"))
                .servers(List.of(
                        new Server().url("http://localhost:8000").description("Kong External Gateway"),
                        new Server().url("http://localhost:8080").description("Direct ERP App")
                ))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Token obtained from /iam/api/in/v1/auth/login"))
                        .addHeaders("X-Tenant-Id", new io.swagger.v3.oas.models.headers.Header()
                                .description("Tenant ID for multi-tenancy")
                                .schema(new io.swagger.v3.oas.models.media.StringSchema())));
    }
}
