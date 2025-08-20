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
 * OpenAPI configuration class
 * Configures basic information and security authentication for Swagger documentation
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configure OpenAPI document information
     * @return OpenAPI configuration object
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Wanli Academy Online Assignment System API")
                        .description("RESTful API documentation for Wanli Academy online assignment submission, grading and management system\n\n" +
                                "## Sprint 4 New Features\n" +
                                "### Assignment Result Viewing\n" +
                                "- **GET /api/submissions/{submissionId}**: Get assignment submission result details\n" +
                                "  - Returns complete assignment results including question analysis and video explanations\n" +
                                "  - Supports students viewing detailed feedback on graded assignments\n" +
                                "  - Includes standard answers, text analysis and video explanation links for each question\n" +
                                "  - Permissions: ROLE_STUDENT (submitter only) or ROLE_TEACHER")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Wanli Academy Technical Team")
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
                                .description("JWT authentication token, format: Bearer <token>")));
    }
}
