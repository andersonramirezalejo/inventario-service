package com.example.Inventario.controller;

import com.example.Inventario.dto.InventarioResponseDto;
import com.example.Inventario.model.Inventario;
import com.example.Inventario.service.InventarioService;
import com.example.Inventario.dto.CompraRequest;
import com.example.Inventario.dto.CompraResponse;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventario")
@Tag(name = "Inventario", description = "Operaciones de consulta y actualización de inventario")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    @Operation(summary = "Obtener el inventario por ID de producto", description = "Recupera la información de inventario de un producto específico, incluyendo detalles del producto desde el servicio de Productos.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Inventario recuperado exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = InventarioResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Inventario o producto no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{productoId}")
    public ResponseEntity<InventarioResponseDto> getInventarioByProductoId(@PathVariable Long productoId) {
        InventarioResponseDto inventarioDto = inventarioService.getInventarioByProductoId(productoId);
        return new ResponseEntity<>(inventarioDto, HttpStatus.OK);
    }

    @PostMapping("/inicializar")
    public ResponseEntity<Inventario> inicializarInventario(@RequestParam Long productoId, @RequestParam Integer cantidadInicial) {
        Inventario inventario = inventarioService.inicializarInventario(productoId, cantidadInicial);
        return new ResponseEntity<>(inventario, HttpStatus.CREATED);
    }

    @PutMapping("/{productoId}/cantidad")
    public ResponseEntity<Inventario> updateCantidadInventario(@PathVariable Long productoId, @RequestParam Integer cantidad) {
        Inventario updatedInventario = inventarioService.updateInventario(productoId, cantidad);
        return new ResponseEntity<>(updatedInventario, HttpStatus.OK);
    }

    @Operation(summary = "Realizar una compra de un producto",
            description = "Procesa una solicitud de compra, reduciendo el stock del producto si hay suficiente disponibilidad.")
    @Tag(name = "Compras")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Compra realizada exitosamente",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CompraResponse.class))),
            @ApiResponse(responseCode = "400", description = "Stock insuficiente",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "Producto o inventario no encontrado",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/comprar")
    public ResponseEntity<CompraResponse> comprarProducto(@RequestBody CompraRequest compraRequest) {
        CompraResponse compraResponse = inventarioService.realizarCompra(compraRequest);
        return new ResponseEntity<>(compraResponse, HttpStatus.OK);
    }
}