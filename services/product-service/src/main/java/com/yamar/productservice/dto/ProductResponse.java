package com.yamar.productservice.dto;

import com.yamar.productservice.model.Category;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponse(
        String id,
        String name,
        String description,
        Category category,
        List<String> images,
        BigDecimal price,
        String originCountry
) {
}

