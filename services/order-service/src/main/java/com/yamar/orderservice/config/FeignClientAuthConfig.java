package com.yamar.orderservice.config;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;

@Configuration
@Slf4j
public class FeignClientAuthConfig {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_TOKEN_TYPE = "Bearer";

    @Bean
    public RequestInterceptor requestTokenBearerInterceptor() {
        return requestTemplate -> extractJwt()
                .map(Jwt::getTokenValue)
                .ifPresentOrElse(
                        token -> {
                            log.debug("Feign Interceptor: Adding Authorization header for request to: {}", requestTemplate.url());
                            requestTemplate.header(AUTHORIZATION_HEADER, String.format("%s %s", BEARER_TOKEN_TYPE, token));
                        },
                        () -> {
                            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                            if (auth == null) {
                                log.warn("Feign Interceptor: No authentication found in SecurityContext for: {}", requestTemplate.url());
                            } else {
                                log.warn("Feign Interceptor: Authentication is not JwtAuthenticationToken ({}) for: {}", auth.getClass().getName(), requestTemplate.url());
                            }
                        }
                );
    }

    private Optional<Jwt> extractJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            return Optional.ofNullable(jwtAuth.getToken());
        }
        return Optional.empty();
    }
}
