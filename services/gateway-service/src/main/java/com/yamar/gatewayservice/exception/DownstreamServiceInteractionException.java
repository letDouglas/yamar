package com.yamar.gatewayservice.exception;

public class DownstreamServiceInteractionException extends RuntimeException {
    public DownstreamServiceInteractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
