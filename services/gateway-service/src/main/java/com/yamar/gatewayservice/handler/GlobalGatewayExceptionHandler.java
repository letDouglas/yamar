package com.yamar.gatewayservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.ResourceAccessException;

@ControllerAdvice
@Slf4j
public class GlobalGatewayExceptionHandler {

    @ExceptionHandler(ResourceAccessException.class)
    public void translateAndHandleResourceAccessException(ResourceAccessException ex, HttpServletRequest request) {
        String errorMessage = String.format(
                "Failed to interact with a downstream service for request %s %s. Original error: %s",
                request.getMethod(), request.getRequestURI(), ex.getMessage()
        );

        log.warn("GATEWAY_RUNTIME_WARNING: Translating ResourceAccessException to DownstreamServiceInteractionException. Details: {}", errorMessage, ex.getCause() != null ? ex.getCause() : ex);

        throw new DownstreamServiceInteractionException(errorMessage, ex);
    }

    @ExceptionHandler(DownstreamServiceInteractionException.class)
    public ResponseEntity<String> handleDownstreamServiceInteraction(DownstreamServiceInteractionException ex, HttpServletRequest request) {
        log.error("GATEWAY_RUNTIME_ERROR: DownstreamServiceInteractionException for request {} {}. Message: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage(), ex.getCause());

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("GATEWAY_ERROR: Could not communicate with a backend service. (Cause: " + ex.getCause().getMessage() + ")");
    }

    @ExceptionHandler(GatewayConfigurationException.class)
    public ResponseEntity<String> handleGatewayConfigurationAtRuntime(GatewayConfigurationException ex, HttpServletRequest request) {
        log.error("GATEWAY_CONFIG_ERROR_RUNTIME: Critical configuration issue detected for request {} {}. Message: {}",
                request.getMethod(), request.getRequestURI(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("GATEWAY_ERROR: Critical gateway configuration error. " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex, HttpServletRequest request) {
        log.error("GATEWAY_UNEXPECTED_ERROR: An unexpected error occurred for request {} {}. Type: {}, Message: {}",
                request.getMethod(), request.getRequestURI(), ex.getClass().getSimpleName(), ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("GATEWAY_ERROR: An unexpected internal error occurred.");
    }
}
