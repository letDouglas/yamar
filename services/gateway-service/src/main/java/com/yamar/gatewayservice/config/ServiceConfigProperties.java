package com.yamar.gatewayservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "application.config")
public class ServiceConfigProperties {

    private Map<String, ServiceRoute> services = new HashMap<>();

    @Data
    public static class ServiceRoute {
        private String url;
        private String path;
    }
}