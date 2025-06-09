package com.yamar.gatewayservice.fallback;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/serviceUnavailable")
    public Mono<ResponseEntity<Map<String, String>>> serviceUnavailable() {
        Map<String, String> response = Map.of(
                "code", "SERVICE_UNAVAILABLE",
                "message", "The service is temporarily unavailable. Please try again later."
        );

        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }
}