package com.inmobiliaria.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentación interactiva de la API REST (OpenAPI 3.0).
 * Declara el esquema Bearer JWT para poder autorizar peticiones
 * protegidas desde Swagger UI (/swagger-ui.html) con el botón "Authorize".
 */
@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_JWT = "bearerAuth";

    @Bean
    public OpenAPI inmobiliariaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sistema Web de Gestión Inmobiliaria (MVP)")
                        .description("API REST para la trazabilidad comercial: propiedades, agentes, "
                                + "operaciones de venta y alquiler.")
                        .version("V1"))
                .components(new Components().addSecuritySchemes(ESQUEMA_JWT,
                        new SecurityScheme()
                                .name(ESQUEMA_JWT)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_JWT));
    }
}
