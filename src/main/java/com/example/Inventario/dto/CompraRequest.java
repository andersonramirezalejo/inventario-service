package com.example.Inventario.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request DTO para realizar una compra de un producto.")
public class CompraRequest {
    @Schema(description = "ID del producto a comprar.", example = "1", required = true)
    private Long productoId;

    @Schema(description = "Cantidad del producto a comprar.", example = "5", required = true)
    private Integer cantidad;
}