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
    private Integer quantity;
    private BigDecimal pricePerUnit;
}

