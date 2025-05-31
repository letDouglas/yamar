package com.yamar.productservice.dto;

import com.yamar.productservice.model.Category;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Builder
@Getter
@Setter
public class ProductUpdateRequest {

    @NotBlank(message = "Product ID is required")
    private String id;

    private String name;
    private String description;
    private Category category;
    private List<String> images;
    private BigDecimal price;
    private String originCountry;
}
