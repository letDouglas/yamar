package com.yamar.orderservice.config;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Configuration
@Slf4j
public class FeignClientAuthConfig {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_TOKEN_TYPE = "Bearer";

    @Bean
    public RequestInterceptor requestTokenBearerInterceptor() {
        return requestTemplate -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                String token = jwtAuth.getToken().getTokenValue();
                requestTemplate.header(AUTHORIZATION_HEADER, BEARER_TOKEN_TYPE + " " + token);
                log.debug("Feign Interceptor: JWT token forwarded to {}", requestTemplate.url());
            } else if (authentication == null) {
                log.warn("Feign Interceptor: No authentication found for request to {}", requestTemplate.url());
            } else {
                log.warn("Feign Interceptor: Authentication type {} is not JWT for request to {}",
                        authentication.getClass().getSimpleName(), requestTemplate.url());
            }
        };
    }
}
