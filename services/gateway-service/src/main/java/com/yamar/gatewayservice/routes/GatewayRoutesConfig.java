package com.yamar.gatewayservice.routes;

import com.yamar.gatewayservice.config.ServiceConfigProperties;
import com.yamar.gatewayservice.exception.GatewayConfigurationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class GatewayRoutesConfig {

    private final ServiceConfigProperties serviceConfig;

    @Bean
    public RouterFunction<ServerResponse> productServiceRoute() {
        return createServiceRoute("product", "product_service");
    }

    @Bean
    public RouterFunction<ServerResponse> orderServiceRoute() {
        return createServiceRoute("order", "order_service");
    }

    @Bean
    public RouterFunction<ServerResponse> inventoryServiceRoute() {
        return createServiceRoute("inventory", "inventory_service");
    }

    private RouterFunction<ServerResponse> createServiceRoute(String serviceKey, String routeId) {
        ServiceConfigProperties.ServiceRoute service = serviceConfig.getServices().get(serviceKey);

        if (service == null || service.getUrl() == null || service.getUrl().isBlank() ||
                service.getPath() == null || service.getPath().isBlank()) {
            String errorMessage = String.format(
                    "Configuration for service key '%s' (routeId '%s') is incomplete or missing. Check URL and Path.", serviceKey, routeId
            );
            log.error("GATEWAY_CONFIG_ERROR: {}", errorMessage);
            throw new GatewayConfigurationException(errorMessage);
        }

        log.info("GATEWAY_CONFIG_INFO: Creating route '{}'. Path: '{}' -> URL: '{}'",
                routeId, service.getPath(), service.getUrl());

        return route(routeId)
                .route(RequestPredicates.path(service.getPath()),
                        HandlerFunctions.http(service.getUrl()))
                .build();
    }
}
