package com.yamar.orderservice.client.product;

import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = "product-service",
        url = "${application.config.product-service.url}"
)
public interface ProductClient {

    @GetMapping("/batch")
    List<ProductResponse> getProductsByIds(@RequestBody @Valid ProductBatchRequest request);
}
