package com.example.Inventario.client;

import com.example.Inventario.config.FeignClientConfig;
import com.example.Inventario.dto.ProductoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

/**
 * ProductoFeignClient es un cliente Feign para comunicarse con el servicio remoto de productos.
 */
@FeignClient(name = "productos-service", url = "http://productos-service:8080", configuration = FeignClientConfig.class)
public interface ProductoFeignClient {

     /**
     * Obtiene un producto por su identificador desde el servicio remoto de productos.
     *
     * @param id identificador único del producto
     * @return un Optional que contiene el ProductoDto si existe, o vacío si no se encuentra
     */
    @GetMapping("/api/productos/{id}")
    Optional<ProductoDto> getProductoById(@PathVariable Long id);
}