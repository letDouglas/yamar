package com.yamar.orderservice.client.user;

public record AddressResponse(
        Long id,
        String street,
        String city,
        String zipCode,
        String country,
        AddressType type
) {}