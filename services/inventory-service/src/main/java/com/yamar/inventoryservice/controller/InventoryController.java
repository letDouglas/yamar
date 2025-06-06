package com.yamar.inventoryservice.controller;

import com.yamar.inventoryservice.dto.BatchStockRequest;
import com.yamar.inventoryservice.dto.StockRequest;
import com.yamar.inventoryservice.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService service;

    @PostMapping("/stock/check")
    public ResponseEntity<Boolean> checkStockForProduct(
            @RequestBody StockRequest request) {
        return ResponseEntity.ok(service.isInStock(request));
    }

    @PostMapping("/stock/check/batch")
    public ResponseEntity<Boolean> checkStockForProducts(
            @Valid @RequestBody BatchStockRequest request) {
        return ResponseEntity.ok(service.areInStock(request));
    }
}
