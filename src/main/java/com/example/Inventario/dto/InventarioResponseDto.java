package com.example.Inventario.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO para la información del inventario de un producto.")
public class InventarioResponseDto {

    @Schema(description = "ID del producto en el inventario.", example = "1", required = true)
    private Long productoId;

    @Schema(description = "Nombre del producto en el inventario.", example = "Laptop", required = true)
    private String nombreProducto;

    @Schema(description = "Descripción del producto en el inventario.", example = "Laptop Dell XPS 13", required = true)
    private String descripcionProducto;

    @Schema(description = "Precio del producto en el inventario.", example = "1500000.00", required = true)
    private Double precioProducto;

    @Schema(description = "Cantidad disponible del producto en el inventario.", example = "50", required = true)
    private Integer cantidadDisponible;
}