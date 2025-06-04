package com.yamar.orderservice.client.inventory;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "inventory-service", url = "${application.config.inventory-service.url}")
public interface InventoryClient {

    @PostMapping("/check-stock")
    Boolean isInStock(StockRequest request);
}


