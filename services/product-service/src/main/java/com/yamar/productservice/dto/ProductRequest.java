package com.yamar.productservice.dto;

import com.yamar.productservice.model.Category;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
public class ProductRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be at most 100 characters")
    private String name;

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description must be at most 1000 characters")
    private String description;

    @NotNull(message = "Category is required")
    private Category category;

    @NotNull(message = "Images list is required")
    @Size(min = 1, message = "At least one image URL is required")
    private List<@NotBlank(message = "Image URL cannot be blank") String> images;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
    private BigDecimal price;

    @NotBlank(message = "Origin country is required")
    @Size(max = 100, message = "Origin country must be at most 100 characters")
    private String originCountry;
}
