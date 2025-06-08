package com.yamar.gatewayservice.openapi.controller;

import com.yamar.gatewayservice.openapi.config.ApiDocsDiscoveryProperties;
import com.yamar.gatewayservice.openapi.config.CustomGatewayApiProperties;
import com.yamar.gatewayservice.openapi.dto.ApiDocsConfigResponse;
import com.yamar.gatewayservice.openapi.service.OpenApiModifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ApiDocsController {

    private final ApiDocsDiscoveryProperties apiDocsDiscoveryProperties;
    private final CustomGatewayApiProperties apiConfig;
    private final WebClient.Builder webClientBuilder;
    private final OpenApiModifier openApiModifier;

    @GetMapping("/v3/api-docs/swagger-config")
    public ResponseEntity<ApiDocsConfigResponse> swaggerConfig() {
        List<ApiDocsConfigResponse.SwaggerUrl> urls = apiDocsDiscoveryProperties.getServices().stream()
                .map(service -> new ApiDocsConfigResponse.SwaggerUrl(
                        service.getName(),
                        apiConfig.getBasePath() + "/api-docs?service=" + service.getName().replaceAll("\\s+", "-").toLowerCase()
                ))
                .toList();
        return ResponseEntity.ok(new ApiDocsConfigResponse("/v3/api-docs/swagger-config", urls));
    }

    @GetMapping("${api-config.base-path}/api-docs")
    public Mono<ResponseEntity<String>> apiDocs(
            @RequestParam("service") String serviceName,
            ServerHttpRequest request) {

        Optional<ApiDocsDiscoveryProperties.ServiceInfo> serviceInfo = apiDocsDiscoveryProperties.getServices().stream()
                .filter(s -> s.getName().replaceAll("\\s+", "-").equalsIgnoreCase(serviceName))
                .findFirst();

        return serviceInfo
                .map(service -> fetchAndModifyDocs(service, request))
                .orElse(Mono.just(ResponseEntity.notFound().build()));
    }

    private Mono<ResponseEntity<String>> fetchAndModifyDocs(ApiDocsDiscoveryProperties.ServiceInfo service, ServerHttpRequest request) {
        String docsUrl = buildDocsUrl(service);

        URI originalUri = request.getURI();
        String gatewayUrl = UriComponentsBuilder.fromUri(originalUri)
                .replacePath(apiConfig.getBasePath())
                .replaceQuery(null)
                .build(true)
                .toUriString();

        String serverDescription = apiConfig.getServerDescription();

        log.debug("Fetching docs from: {} to be served at: {}", docsUrl, gatewayUrl);

        return webClientBuilder.build()
                .get()
                .uri(docsUrl)
                .retrieve()
                .bodyToMono(String.class)
                .map(docs -> openApiModifier.modifyServersUrl(docs, gatewayUrl, serverDescription))
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    log.error("Error fetching docs from {}: {}", docsUrl, e.getMessage());
                    return Mono.just(ResponseEntity.notFound().build());
                });
    }

    private String buildDocsUrl(ApiDocsDiscoveryProperties.ServiceInfo service) {
        return service.getServiceUrl() + service.getContextPath() + "/api-docs";
    }
}
