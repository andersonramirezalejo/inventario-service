package com.example.Inventario.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfig {

    @Value("${producto-service.api.key}")
    private String productsApiKeyForFeign;

    private static final String API_KEY_AUTH_HEADER = "X-API-KEY";

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // Añadir la API Key al encabezado de la solicitud de Feign
                template.header(API_KEY_AUTH_HEADER, productsApiKeyForFeign);
                System.out.println("FeignClient: Añadiendo encabezado " + API_KEY_AUTH_HEADER + " con valor: " + productsApiKeyForFeign);
            }
        };
    }
}