package com.yamar.inventoryservice.service;

import com.yamar.inventoryservice.dto.BatchStockRequest;
import com.yamar.inventoryservice.dto.InsufficientStockException;
import com.yamar.inventoryservice.dto.StockRequest;
import com.yamar.inventoryservice.exception.ProductNotFoundException;
import com.yamar.inventoryservice.model.Inventory;
import com.yamar.inventoryservice.repository.InventoryRepository;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class InventoryService {

    private final InventoryRepository repository;

    public Boolean isInStock(StockRequest request) {
        Inventory inventory = repository.findByProductId(request.getProductId())
                .orElseThrow(() -> {
                    log.error("Product not found with ID: {}", request.getProductId());
                    return new ProductNotFoundException("Product not found with id: " + request.getProductId());
                });

        log.info("Checking stock for product ID: {}. Requested quantity: {}, Available quantity: {}", request.getProductId(), request.getQuantity(), inventory.getQuantity());
        return inventory.getQuantity() >= request.getQuantity();
    }

    public Boolean areInStock(@Valid BatchStockRequest request) {
        List<String> insufficientProducts = new ArrayList<>();

        for (StockRequest stock : request.getItems()) {
            Inventory inventory = repository.findByProductId(stock.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + stock.getProductId()));

            if (inventory.getQuantity() < stock.getQuantity()) {
                insufficientProducts.add(stock.getProductId());
            }
        }

        if (!insufficientProducts.isEmpty()) {
            throw new InsufficientStockException(insufficientProducts);
        }

        return true;
    }
}
