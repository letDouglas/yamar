package com.yamar.orderservice.client.user;

import java.util.List;

public record UserResponse(
        String id,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        List<AddressResponse> addresses
) {}