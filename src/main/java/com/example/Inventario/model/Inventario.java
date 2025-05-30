package com.example.Inventario.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "inventarios")
public class Inventario {

    @Id
    @Schema(description = "ID de inventario es el mismo que el ID del producto")
    private Long productoId;

    @Column(nullable = false)
    @Schema(description = "Cantidad de inventario del producto", example = "50")
    private Integer cantidad;
}