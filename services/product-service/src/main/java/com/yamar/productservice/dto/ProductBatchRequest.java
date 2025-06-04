package com.yamar.productservice.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProductBatchRequest {
    @NotEmpty(message = "The list of IDs cannot be empty")
    private List<String> productIds;
}
