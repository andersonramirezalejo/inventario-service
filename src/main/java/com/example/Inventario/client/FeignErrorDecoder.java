package com.example.Inventario.client;

import com.example.Inventario.exception.ProductoNotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class FeignErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultErrorDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 404) {
            return new ProductoNotFoundException("Producto no encontrado en el servicio de productos remoto.");
        }

        return defaultErrorDecoder.decode(methodKey, response);
    }
}