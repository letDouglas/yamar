package com.yamar.productservice.exception;

import java.util.List;

public class ProductsNotFoundException extends RuntimeException {
    private final List<String> missingIds;

    public ProductsNotFoundException(List<String> missingIds) {
        super("Products not found for IDs: " + String.join(", ", missingIds));
        this.missingIds = missingIds;
    }

    public List<String> getMissingIds() {
        return missingIds;
    }
}
