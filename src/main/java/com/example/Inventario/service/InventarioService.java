package com.example.Inventario.service;

import com.example.Inventario.client.ProductoFeignClient;
import com.example.Inventario.dto.InventarioResponseDto;
import com.example.Inventario.dto.ProductoDto;
import com.example.Inventario.model.Inventario;
import com.example.Inventario.repository.InventarioRepository;
import com.example.Inventario.event.InventarioActualizadoEvent;
import com.example.Inventario.exception.InventarioNotFoundException;
import com.example.Inventario.exception.ProductoNotFoundException;
import com.example.Inventario.dto.CompraRequest;
import com.example.Inventario.dto.CompraResponse;
import com.example.Inventario.exception.StockNotAvailableException;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

/**
 * InventarioService gestiona la lógica de negocio relacionada con el inventario de productos.
 * <p>
 * Permite consultar, actualizar, inicializar inventarios y realizar compras, integrándose con el microservicio de productos.
 */
@Service
public class InventarioService {

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private ProductoFeignClient productoFeignClient;

    @Autowired
    private ApplicationEventPublisher eventPublisher;


    /**
     * Obtiene la información de inventario y producto para un producto dado.
     *
     * @param productoId identificador del producto
     * @return DTO con información combinada de inventario y producto
     * @throws InventarioNotFoundException si el inventario no existe
     * @throws ProductoNotFoundException si el producto no existe en el microservicio de productos
     */
    public InventarioResponseDto getInventarioByProductoId(Long productoId) {
        Inventario inventario = inventarioRepository.findById(productoId)
                .orElseThrow(() -> new InventarioNotFoundException("Inventario no encontrado para el producto con ID: " + productoId));

        ProductoDto producto = productoFeignClient.getProductoById(productoId)
                .orElseThrow(() -> new ProductoNotFoundException("Producto no encontrado en el servicio de productos con ID: " + productoId));

        return new InventarioResponseDto(
                inventario.getProductoId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                inventario.getCantidad()
        );
    }


    /**
     * Actualiza la cantidad de inventario para un producto existente.
     *
     * @param productoId    identificador del producto
     * @param nuevaCantidad nueva cantidad a establecer en el inventario
     * @return el inventario actualizado
     * @throws ProductoNotFoundException si el producto no existe en el microservicio de productos
     * @throws InventarioNotFoundException si el inventario no existe
     */
    public Inventario updateInventario(Long productoId, Integer nuevaCantidad) {
        productoFeignClient.getProductoById(productoId)
                .orElseThrow(() -> new ProductoNotFoundException("No se puede actualizar el inventario: Producto no encontrado en el servicio de productos con ID: " + productoId));

        Inventario inventario = inventarioRepository.findById(productoId)
                .orElseThrow(() -> new InventarioNotFoundException("Inventario no encontrado para actualizar el producto con ID: " + productoId + ". Por favor, inicialícelo primero."));

        int oldCantidad = inventario.getCantidad();
        inventario.setCantidad(nuevaCantidad);
        Inventario updatedInventario = inventarioRepository.save(inventario);

        eventPublisher.publishEvent(new InventarioActualizadoEvent(this, productoId, oldCantidad, nuevaCantidad));

        return updatedInventario;
    }


    /**
     * Inicializa el inventario para un producto.
     *
     * @param productoId      identificador del producto
     * @param cantidadInicial cantidad inicial a establecer
     * @return el inventario inicializado o actualizado
     * @throws ProductoNotFoundException si el producto no existe en el microservicio de productos
     */
    public Inventario inicializarInventario(Long productoId, Integer cantidadInicial) {
        productoFeignClient.getProductoById(productoId)
                .orElseThrow(() -> new ProductoNotFoundException("No se puede inicializar el inventario: Producto no encontrado en el servicio de productos con ID: " + productoId));

        Inventario inventario = inventarioRepository.findById(productoId)
                .orElse(new Inventario(productoId, 0));

        inventario.setCantidad(cantidadInicial);
        return inventarioRepository.save(inventario);
    }


    /**
     * Realiza una compra de un producto, actualizando el inventario y emitiendo un evento.
     *
     * @param compraRequest objeto con los datos de la compra (productoId y cantidad)
     * @return respuesta con los detalles de la compra realizada
     * @throws ProductoNotFoundException si el producto no existe en el microservicio de productos
     * @throws InventarioNotFoundException si el inventario no está inicializado para el producto
     * @throws StockNotAvailableException si no hay suficiente stock disponible
     */
    @Transactional
    public CompraResponse realizarCompra(CompraRequest compraRequest) {
        Long productoId = compraRequest.getProductoId();
        Integer cantidadComprada = compraRequest.getCantidad();

        ProductoDto producto = productoFeignClient.getProductoById(productoId)
                .orElseThrow(() -> new ProductoNotFoundException("Producto no encontrado en el servicio de productos con ID: " + productoId));

        Inventario inventario = inventarioRepository.findById(productoId)
                .orElseThrow(() -> new InventarioNotFoundException("Inventario no inicializado para el producto con ID: " + productoId));

        if (inventario.getCantidad() < cantidadComprada) {
            throw new StockNotAvailableException("No hay suficiente stock para el producto " + producto.getNombre() +
                    ". Stock disponible: " + inventario.getCantidad() +
                    ", Cantidad solicitada: " + cantidadComprada);
        }

        int oldCantidad = inventario.getCantidad();
        inventario.setCantidad(oldCantidad - cantidadComprada);
        inventarioRepository.save(inventario);

        eventPublisher.publishEvent(new InventarioActualizadoEvent(this, productoId, oldCantidad, inventario.getCantidad()));

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