package com.yamar.gatewayservice.openapi.dto;

import java.util.List;

public record ApiDocsConfigResponse(
        String configUrl,
        List<SwaggerUrl> urls
) {
    public record SwaggerUrl(
            String name,
            String url
    ) {
    }
}