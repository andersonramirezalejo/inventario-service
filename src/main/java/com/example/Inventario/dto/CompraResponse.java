package com.example.Inventario.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response DTO para la compra de un producto.")
public class CompraResponse {
    
    @Schema(description = "ID del producto comprado.", example = "1", required = true)
    private Long productoId;

    @Schema(description = "Nombre del producto comprado.", example = "Laptop", required = true)
    private String nombreProducto;

    @Schema(description = "Precio unitario del producto.", example = "1500000.00", required = true)
    private Double precioUnitario;

    @Schema(description = "Cantidad comprada del producto.", example = "2", required = true)
    private Integer cantidadComprada;

    @Schema(description = "Total a pagar por la compra.", example = "1000000.00", required = true)
    private Double totalPagar;

    @Schema(description = "Fecha y hora de la compra.", example = "2023-10-01T10:15:30", required = true)
    private LocalDateTime fechaCompra;

    @Schema(description = "Mensaje de confirmación de la compra.", example = "Compra realizada con éxito", required = true)
    private String mensaje;
}