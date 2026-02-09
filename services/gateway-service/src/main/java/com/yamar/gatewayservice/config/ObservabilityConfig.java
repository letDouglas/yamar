package com.yamar.gatewayservice.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Hooks;

@Configuration
public class ObservabilityConfig {

    @PostConstruct
    public void init() {
        Hooks.enableAutomaticContextPropagation();
    }
}