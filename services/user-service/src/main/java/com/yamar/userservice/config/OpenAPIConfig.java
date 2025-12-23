package com.yamar.userservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.servlet.ServletContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Value("${server.port}")
    private int port;

    @Bean
    public OpenAPI customOpenAPI(ServletContext servletContext) {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(apiInfo())
                .externalDocs(externalDocs())
                .servers(apiServers(servletContext))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(
                        new Components()
                                .addSecuritySchemes(securitySchemeName,
                                        new SecurityScheme()
                                                .name(securitySchemeName)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                );
    }

    private Info apiInfo() {
        return new Info()
                .title("User Service API")
                .description("RESTful microservice to manage user profiles and addresses")
                .version("1.0.0")
                .license(new License()
                        .name("Apache License 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0"))
                .contact(new Contact()
                        .name("Derain Dev Team")
                        .email("xmor@gmail.com"));
    }

    private ExternalDocumentation externalDocs() {
        return new ExternalDocumentation()
                .description("User Service Documentation")
                .url("https://xmor.com/docs/user-service");
    }

    private List<Server> apiServers(ServletContext servletContext) {
        String contextPath = servletContext.getContextPath();
        String localUrl = String.format("http://localhost:%d%s", port, contextPath);
        String prodUrl = "https://xmor.com/user-service";

        return List.of(
                new Server().url(localUrl).description("Local Dev Server"),
                new Server().url(prodUrl).description("Production Server")
        );
    }
}