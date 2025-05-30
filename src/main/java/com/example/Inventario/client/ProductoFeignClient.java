package com.example.Inventario.client;

import com.example.Inventario.config.FeignClientConfig;
import com.example.Inventario.dto.ProductoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@FeignClient(name = "productos-service", url = "http://productos-service:8080", configuration = FeignClientConfig.class)
public interface ProductoFeignClient {

    @GetMapping("/api/productos/{id}")
    Optional<ProductoDto> getProductoById(@PathVariable Long id);
}