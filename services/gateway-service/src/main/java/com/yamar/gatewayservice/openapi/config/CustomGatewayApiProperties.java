package com.yamar.gatewayservice.openapi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "api-config")
@Data
public class CustomGatewayApiProperties {
    private String basePath;
    private String serverDescription;
}