package com.example.Inventario.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompraResponse {
    private Long productoId;
    private String nombreProducto;
    private Double precioUnitario;
    private Integer cantidadComprada;
    private Double totalPagar;
    private LocalDateTime fechaCompra;
    private String mensaje;
}