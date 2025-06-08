package com.yamar.gatewayservice;

import com.yamar.gatewayservice.openapi.config.ApiDocsDiscoveryProperties;
import com.yamar.gatewayservice.openapi.config.CustomGatewayApiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        ApiDocsDiscoveryProperties.class,
        CustomGatewayApiProperties.class
})
public class GatewayServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }
}