package com.villadictos.app.config;

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
        public OpenAPI hotelOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("API Sistema de Gestión Hotelera - Grupo 04")
                                                .version("1.0.0")
                                                .description("""
                                                                API REST para el sistema de gestión hotelera VillaDictos.

                                                                ## Endpoints Disponibles
                                                                - **Habitaciones:** Listado con filtros y detalle
                                                                - **Salas:** Listado y detalle de espacios comunes
                                                                - **Servicios:** Listado y detalle de servicios del hotel
                                                                - **Reservas:** Inicio de reserva con redirección al túnel

                                                                Todos los endpoints son públicos y no requieren autenticación.
                                                                """)
                                                .contact(new Contact()
                                                                .name("Grupo 04 - IW Hotel")
                                                                .email("grupo04@ua.es"))
                                                .license(new License()
                                                                .name("Apache 2.0")
                                                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                                .servers(List.of(
                                                new Server().url("http://localhost:8080")
                                                                .description("Development server")));
        }
}
