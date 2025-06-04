package com.yamar.inventoryservice.service;

import com.yamar.inventoryservice.dto.StockRequest;
import com.yamar.inventoryservice.exception.ProductNotFoundException;
import com.yamar.inventoryservice.model.Inventory;
import com.yamar.inventoryservice.repository.InventoryRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class InventoryService {

    private final InventoryRepository repository;

    public Boolean isInStock(StockRequest request) {
        Inventory inventory = repository.findByProductId(request.getProductId())
                .orElseThrow(() -> {
                    log.error("Product not found with ID: {}", request.getProductId());
                    return new ProductNotFoundException("Product not found");
                });

        log.info("Checking stock for product ID: {}. Requested quantity: {}, Available quantity: {}", request.getProductId(), request.getQuantity(), inventory.getQuantity());
        return inventory.getQuantity() >= request.getQuantity();
    }
}
