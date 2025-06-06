package com.yamar.productservice.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .externalDocs(externalDocs())
                .servers(apiServers());
    }

    private Info apiInfo() {
        return new Info()
                .title("Product Service API")
                .description("RESTful microservice to manage products in the catalog")
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
                .description("Product Service Documentation")
                .url("https://xmor.com/docs/product-service");
    }

    private List<Server> apiServers() {
        return List.of(
                new Server().url("http://localhost:8041").description("Local Dev Server"),
                new Server().url("https://xmor.com/product-service").description("Production Server")
        );
    }
}

