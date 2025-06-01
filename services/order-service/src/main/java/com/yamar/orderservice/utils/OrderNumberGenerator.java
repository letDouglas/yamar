package com.yamar.orderservice.utils;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OrderNumberGenerator {

    public String generate() {
        return "ORD-" + UUID.randomUUID();
    }
}
