package com.coursehub.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI courserHubOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CourseHub API")
                        .version("1.0.0")
                        .description("CourseHub - Nền tảng Marketplace học trực tuyến. RESTful API documentation.")
                        .contact(new Contact()
                                .name("CourseHub Development Team")
                                .email("dev@coursehub.com"))
                        .license(new License().name("Proprietary").url("https://coursehub.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Development server"),
                        new Server().url("https://api.coursehub.com").description("Production server")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .name("Authorization")
                                        .description("Enter JWT access token: Bearer <token>")));
    }
}
