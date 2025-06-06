package com.yamar.orderservice.dto;

import java.math.BigDecimal;

public record OrderLineResponse(
        Long id,
        Integer quantity,
        String productId,
        BigDecimal pricePerUnit,
        BigDecimal subTotal
) {
}
