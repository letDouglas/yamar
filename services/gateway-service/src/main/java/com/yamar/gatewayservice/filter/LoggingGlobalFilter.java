package com.yamar.gatewayservice.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("INCOMING REQUEST: {} {}",
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI().getPath());

        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    var statusCode = exchange.getResponse().getStatusCode();
                    log.info("OUTGOING RESPONSE: {} {} - Status: {}",
                            exchange.getRequest().getMethod(),
                            exchange.getRequest().getURI().getPath(),
                            statusCode != null ? statusCode.value() : "unknown");
                }));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}