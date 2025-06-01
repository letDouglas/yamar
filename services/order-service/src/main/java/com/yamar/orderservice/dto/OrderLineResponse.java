package com.yamar.orderservice.dto;

import java.math.BigDecimal;

public record OrderLineResponse(
        Long id,
        double quantity,
        String productId,
        BigDecimal pricePerUnit,
        BigDecimal subTotal
) {
}
