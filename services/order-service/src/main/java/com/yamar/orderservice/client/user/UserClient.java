package com.yamar.orderservice.client.user;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "user-service",
        url = "${application.config.user-service.url}"
)
public interface UserClient {

    @GetMapping("/me")
    UserResponse getCurrentUser();
}