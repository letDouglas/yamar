package com.yamar.gatewayservice.openapi.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenApiModifier {
    private final ObjectMapper objectMapper;

    public String modifyServersUrl(String openApiJson, String newServerUrl, String serverDescription) {
        try {
            JsonNode rootNode = objectMapper.readTree(openApiJson);
            if (rootNode.has("servers")) {
                ((ObjectNode) rootNode).putArray("servers")
                        .addObject()
                        .put("url", newServerUrl)
                        .put("description", serverDescription);
            }
            return objectMapper.writeValueAsString(rootNode);
        } catch (Exception e) {
            log.error("Error modifying OpenAPI servers field: {}", e.getMessage(), e);
            return openApiJson;
        }
    }
}