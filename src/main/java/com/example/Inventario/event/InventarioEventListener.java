package com.example.Inventario.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * InventarioEventListener escucha y maneja eventos relacionados con el inventario.
 * <p>
 * Permite realizar acciones cuando se publica un evento de actualización de inventario.
 */
@Component
public class InventarioEventListener {

    /**
     * Maneja el evento InventarioActualizadoEvent.
     * <p>
     * Este método se ejecuta automáticamente cuando se publica un evento de actualización de inventario.
     *
     * @param event el evento de inventario actualizado recibido
     */
    @EventListener
    public void handleInventarioActualizadoEvent(InventarioActualizadoEvent event) {
        System.out.println("EVENTO DE INVENTARIO: Inventario actualizado para Producto ID: " + event.getProductoId() +
                " de " + event.getOldCantidad() + " a " + event.getNewCantidad());
    }
}