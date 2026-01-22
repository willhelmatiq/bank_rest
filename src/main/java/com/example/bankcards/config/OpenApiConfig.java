package com.example.bankcards.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация OpenAPI (Swagger) для REST API приложения.
 *
 * <p>Настраивает схему аутентификации с использованием JWT Bearer Token,
 * что позволяет Swagger UI корректно отправлять авторизационный заголовок
 * {@code Authorization: Bearer <token>} при выполнении защищённых запросов.</p>
 *
 * <p>Используется для документирования и тестирования API через Swagger UI.</p>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                );
    }
}