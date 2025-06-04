package com.yamar.orderservice.client.product;

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
