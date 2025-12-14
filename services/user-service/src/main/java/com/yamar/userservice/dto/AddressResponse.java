package com.yamar.userservice.dto;

import com.yamar.userservice.model.AddressType;

public record AddressResponse(
        Long id,
        String street,
        String city,
        String zipCode,
        String country,
        AddressType type
) {}