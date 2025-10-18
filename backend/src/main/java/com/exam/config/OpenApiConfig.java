package com.exam.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI (Swagger) 配置
 *
 * @author system
 * @since 2024-10-18
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI examRegistrationSystemOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("在线考试报名系统 API")
                        .description("基于 Spring Boot + React 的在线考试报名系统后端API文档")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("系统管理员")
                                .email("admin@example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .externalDocs(new ExternalDocumentation()
                        .description("项目文档")
                        .url("https://github.com/scguoi/exam-registration-system"))
                // JWT安全认证配置
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .name("Authorization")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("请在请求头中添加 JWT Token，格式：Bearer {token}")));
    }
}
