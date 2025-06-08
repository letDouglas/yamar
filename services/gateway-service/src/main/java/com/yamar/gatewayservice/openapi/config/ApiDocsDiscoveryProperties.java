package com.yamar.gatewayservice.openapi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@ConfigurationProperties(prefix = "api-docs-discovery")
@Data
public class ApiDocsDiscoveryProperties {
    private List<ServiceInfo> services;

    @Data
    public static class ServiceInfo {
        private String name;
        private String url;
        private String serviceUrl;
        private String contextPath;
    }
}