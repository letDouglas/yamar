package com.yamar.productservice.exception;

import java.util.List;

public class ProductsNotFoundException extends RuntimeException {
    private final List<String> missingIds;

    public ProductsNotFoundException(List<String> missingIds) {
        super("Some products were not found");
        this.missingIds = missingIds;
    }

    public List<String> getMissingIds() {
        return missingIds;
    }
}

