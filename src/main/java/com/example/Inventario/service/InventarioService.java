package com.example.Inventario.service;

import com.example.Inventario.client.ProductoFeignClient;
import com.example.Inventario.dto.InventarioResponseDto;
import com.example.Inventario.dto.ProductoDto;
import com.example.Inventario.model.Inventario;
import com.example.Inventario.repository.InventarioRepository;
import com.example.Inventario.event.InventarioActualizadoEvent; // Asegúrate de importar esto
import com.example.Inventario.exception.InventarioNotFoundException; // Importa la nueva excepción
import com.example.Inventario.exception.ProductoNotFoundException; // Importa la nueva excepción
import com.example.Inventario.dto.CompraRequest;
import com.example.Inventario.dto.CompraResponse;
import com.example.Inventario.exception.StockNotAvailableException; // Importa la nueva excepción
import jakarta.transaction.Transactional; // Importa Transactional

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class InventarioService {

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private ProductoFeignClient productoFeignClient;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public InventarioResponseDto getInventarioByProductoId(Long productoId) {
        // 1. Buscar inventario localmente
        Inventario inventario = inventarioRepository.findById(productoId)
                .orElseThrow(() -> new InventarioNotFoundException("Inventario no encontrado para el producto con ID: " + productoId));

        // 2. Llamar al microservicio de productos
        ProductoDto producto = productoFeignClient.getProductoById(productoId)
                .orElseThrow(() -> new ProductoNotFoundException("Producto no encontrado en el servicio de productos con ID: " + productoId));

        // 3. Combinar y devolver la respuesta
        return new InventarioResponseDto(
                inventario.getProductoId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                inventario.getCantidad()
        );
    }

    public Inventario updateInventario(Long productoId, Integer nuevaCantidad) {
        // Verificar si el producto existe en el servicio de productos antes de actualizar el inventario
        productoFeignClient.getProductoById(productoId)
                .orElseThrow(() -> new ProductoNotFoundException("No se puede actualizar el inventario: Producto no encontrado en el servicio de productos con ID: " + productoId));

        Inventario inventario = inventarioRepository.findById(productoId)
                .orElseThrow(() -> new InventarioNotFoundException("Inventario no encontrado para actualizar el producto con ID: " + productoId + ". Por favor, inicialícelo primero."));

        int oldCantidad = inventario.getCantidad();
        inventario.setCantidad(nuevaCantidad);
        Inventario updatedInventario = inventarioRepository.save(inventario);

        // Emitir evento de cambio de inventario
        eventPublisher.publishEvent(new InventarioActualizadoEvent(this, productoId, oldCantidad, nuevaCantidad));

        return updatedInventario;
    }

    public Inventario inicializarInventario(Long productoId, Integer cantidadInicial) {
        // Opcional: Verificar que el producto exista antes de inicializar el inventario
        productoFeignClient.getProductoById(productoId)
                .orElseThrow(() -> new ProductoNotFoundException("No se puede inicializar el inventario: Producto no encontrado en el servicio de productos con ID: " + productoId));

        // Si ya existe, no creamos uno nuevo, sino que lo actualizamos (o lanzamos un error si no queremos re-inicializar)
        Inventario inventario = inventarioRepository.findById(productoId)
                .orElse(new Inventario(productoId, 0));

        inventario.setCantidad(cantidadInicial);
        return inventarioRepository.save(inventario);
    }

    @Transactional
    public CompraResponse realizarCompra(CompraRequest compraRequest) {
        Long productoId = compraRequest.getProductoId();
        Integer cantidadComprada = compraRequest.getCantidad();

        // 1. Obtener información del producto (necesaria para el precio y validación)
        ProductoDto producto = productoFeignClient.getProductoById(productoId)
                .orElseThrow(() -> new ProductoNotFoundException("Producto no encontrado en el servicio de productos con ID: " + productoId));

        // 2. Verificar disponibilidad de inventario
        Inventario inventario = inventarioRepository.findById(productoId)
                .orElseThrow(() -> new InventarioNotFoundException("Inventario no inicializado para el producto con ID: " + productoId));

        if (inventario.getCantidad() < cantidadComprada) {
            throw new StockNotAvailableException("No hay suficiente stock para el producto " + producto.getNombre() +
                    ". Stock disponible: " + inventario.getCantidad() +
                    ", Cantidad solicitada: " + cantidadComprada);
        }

        // 3. Actualizar la cantidad disponible (disminuir stock)
        int oldCantidad = inventario.getCantidad();
        inventario.setCantidad(oldCantidad - cantidadComprada);
        inventarioRepository.save(inventario);

        // 4. Emitir evento de cambio de inventario (compra exitosa)
        eventPublisher.publishEvent(new InventarioActualizadoEvent(this, productoId, oldCantidad, inventario.getCantidad()));

        // 5. Retornar la información de la compra realizada
        Double totalPagar = producto.getPrecio() * cantidadComprada;
        return new CompraResponse(
                productoId,
                producto.getNombre(),
                producto.getPrecio(),
                cantidadComprada,
                totalPagar,
                LocalDateTime.now(),
                "Compra realizada exitosamente."
        );
    }
}