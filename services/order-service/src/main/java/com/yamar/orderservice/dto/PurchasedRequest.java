package com.yamar.orderservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchasedRequest {
    @NotNull(message = "Product is mandatory")
    private String productId;
    @Positive(message = "Quantity is mandatory")
    private Integer quantity;
}
