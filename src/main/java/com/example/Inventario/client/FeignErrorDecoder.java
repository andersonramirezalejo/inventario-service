package com.example.Inventario.client;

import com.example.Inventario.exception.ProductoNotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;

/**
 * FeignErrorDecoder es una implementación personalizada de {@link ErrorDecoder}
 * para manejar errores específicos al consumir servicios remotos con Feign.
 * <p>
 * Permite lanzar excepciones personalizadas según el código de estado HTTP recibido.
 */
public class FeignErrorDecoder implements ErrorDecoder {
    
    private final ErrorDecoder defaultErrorDecoder = new Default();

    /**
     * Decodifica la respuesta de error de Feign y lanza una excepción personalizada si corresponde.
     *
     * @param methodKey clave del método Feign que produjo el error
     * @param response  respuesta HTTP recibida
     * @return una excepción personalizada o la excepción por defecto
     */
    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 404) {
            return new ProductoNotFoundException("Producto no encontrado en el servicio de productos remoto.");
        }

        return defaultErrorDecoder.decode(methodKey, response);
    }
}