package com.yamar.orderservice.client.inventory;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "inventory-service", url = "${application.config.inventory-service.url}")
public interface InventoryClient {

    @PostMapping("/stock/check/batch")
    Boolean checkStockForProducts(@RequestBody @Valid BatchStockRequest request);
}


