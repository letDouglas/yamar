package com.yamar.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yamar.orderservice.model.PaymentMethod;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record OrderResponse(
        Long id,
        String customerId,
        String orderNumber,
        BigDecimal totalAmount,
        PaymentMethod paymentMethod
) {
}
