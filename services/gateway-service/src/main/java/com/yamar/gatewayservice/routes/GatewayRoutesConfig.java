package com.yamar.gatewayservice.routes;

import com.yamar.gatewayservice.config.ServiceConfigProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;

@Configuration
@RequiredArgsConstructor
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
        return route(routeId)
                .route(RequestPredicates.path(service.getPath()),
                        HandlerFunctions.http(service.getUrl()))
                .build();
    }
}