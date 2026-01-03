package com.supersonic.limitedstore.domain.order.infrastructure.client;


import java.util.UUID;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service")
public interface ProductClient {
    @GetMapping("/products/{productId}/exists")
    boolean exists(@PathVariable UUID productId);
}
