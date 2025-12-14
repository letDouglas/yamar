package com.yamar.userservice.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UserResponse(
        String id,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        List<AddressResponse> addresses,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}