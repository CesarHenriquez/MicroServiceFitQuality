package com.microservicio.autenticarusuario.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {
     @Bean
    public OpenAPI apiInfo(){
        return new OpenAPI()
        .info(new Info()
            .title("API de Autenticación")
            .version("1.0")
            .description("Gestión de usuarios y autenticación en el sistema"));
    }


}
