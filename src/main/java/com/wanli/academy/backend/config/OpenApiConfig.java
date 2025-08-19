package com.wanli.academy.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI配置类
 * 配置Swagger文档的基本信息和安全认证
 */
@Configuration
public class OpenApiConfig {

    /**
     * 配置OpenAPI文档信息
     * @return OpenAPI配置对象
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("万里书院在线作业系统 API")
                        .description("万里书院在线作业提交、批阅及管理系统的RESTful API文档\n\n" +
                                "## Sprint 4 新增功能\n" +
                                "### 作业结果查看\n" +
                                "- **GET /api/submissions/{submissionId}**: 获取作业提交结果详情\n" +
                                "  - 返回包含题目解析和视频讲解的完整作业结果\n" +
                                "  - 支持学生查看已批改作业的详细反馈\n" +
                                "  - 包含每道题的标准答案、文字解析和视频讲解链接\n" +
                                "  - 权限：ROLE_STUDENT（仅限提交者本人）或 ROLE_TEACHER")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("万里书院技术团队")
                                .email("tech@wanli.academy")
                                .url("https://www.wanli.academy"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT认证令牌，格式：Bearer <token>")));
    }
}