package com.example.Inventario.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class InventarioNotFoundException extends RuntimeException {

    public InventarioNotFoundException(String message) {
        super(message);
    }
}