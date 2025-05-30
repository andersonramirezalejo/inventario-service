package com.example.Inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventarioResponseDto {
    private Long productoId;
    private String nombreProducto;
    private String descripcionProducto;
    private Double precioProducto;
    private Integer cantidadDisponible;
}