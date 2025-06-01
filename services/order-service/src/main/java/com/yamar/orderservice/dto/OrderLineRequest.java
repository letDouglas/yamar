package com.yamar.orderservice.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderLineRequest {
    private String productId;
    private double quantity;
    private BigDecimal pricePerUnit;
}

