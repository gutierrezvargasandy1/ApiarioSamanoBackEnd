package com.ApiarioSamano.MicroServiceUsuario.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Gestión de Usuarios - Apiario Samano")
                        .version("1.0.0")
                        .description("Microservicio para la gestión de usuarios del sistema Apiario Samano")
                        .contact(new Contact()
                                .name("Equipo Apiario Samano")
                                .email("contacto@apiariosam ano.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Servidor de Desarrollo"),
                        new Server()
                                .url("https://api.apiariosam ano.com")
                                .description("Servidor de Producción")));
    }
}