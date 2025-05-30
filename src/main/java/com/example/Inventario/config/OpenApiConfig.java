package com.example.Inventario.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Microservicio de Inventario API",
                version = "1.0",
                description = "API para la gestión de inventario y el proceso de compra.",
                contact = @Contact(
                        name = "Anderson",
                        email = "anderson.ramirez@linktic.com"
                )
        ),
        tags = {
                @Tag(name = "Inventario", description = "Operaciones de consulta y actualización de inventario"),
                @Tag(name = "Compras", description = "Proceso de compra de productos")
        },
        security = @SecurityRequirement(name = "X-API-KEY")
)
@SecurityScheme(
        name = "X-API-KEY",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = "X-API-KEY",
        description = "Requiere una X-API-KEY para acceder a los endpoints."
)
public class OpenApiConfig {
    // No se necesita lógica adicional
}