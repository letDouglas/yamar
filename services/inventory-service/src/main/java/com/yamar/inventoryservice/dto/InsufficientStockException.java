package com.yamar.inventoryservice.dto;

import java.util.List;

public class InsufficientStockException extends RuntimeException {
    private final List<String> insufficientStockIds;

    public InsufficientStockException(List<String> insufficientStockIds) {
        super("Insufficient stock");
        this.insufficientStockIds = insufficientStockIds;
    }

    public List<String> getInsufficientStockIds() {
        return insufficientStockIds;
    }
}
