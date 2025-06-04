package com.yamar.orderservice.client.product;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class ProductBatchRequest {
    @NotEmpty(message = "The list of IDs cannot be empty")
    private List<String> productIds;
}