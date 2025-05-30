package com.example.Inventario.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para representar un producto en el sistema de inventario.")
public class ProductoDto {

    @Schema(description = "ID único del producto.", example = "1", required = true)
    private Long id;

    @Schema(description = "Nombre del producto.", example = "Laptop", required = true)
    private String nombre;

    @Schema(description = "Descripción del producto.", example = "Laptop Dell XPS 13", required = true)
    private String descripcion;

    @Schema(description = "Precio del producto.", example = "1500000.00", required = true)
    private Double precio;
}