package com.example.Inventario.event;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class InventarioEventListener {

    @EventListener
    public void handleInventarioActualizadoEvent(InventarioActualizadoEvent event) {
        System.out.println("EVENTO DE INVENTARIO: Inventario actualizado para Producto ID: " + event.getProductoId() +
                " de " + event.getOldCantidad() + " a " + event.getNewCantidad());
    }
}