package com.fonchys.minimarket.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Fonchys Minimarket - API REST",
        version = "1.0.0",
        description = "Endpoints de productos, proveedores y ventas. Inicia sesion primero en /login " +
            "en este mismo navegador (admin@fonchys.com / admin123) para que 'Try it out' use tu sesion."
    )
)
public class OpenApiConfig {
}
