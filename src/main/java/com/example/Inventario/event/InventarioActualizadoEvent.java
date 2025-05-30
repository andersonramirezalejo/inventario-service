package com.example.Inventario.event;

import org.springframework.context.ApplicationEvent;

public class InventarioActualizadoEvent extends ApplicationEvent {
    private Long productoId;
    private int oldCantidad;
    private int newCantidad;

    public InventarioActualizadoEvent(Object source, Long productoId, int oldCantidad, int newCantidad) {
        super(source);
        this.productoId = productoId;
        this.oldCantidad = oldCantidad;
        this.newCantidad = newCantidad;
    }

    public Long getProductoId() {
        return productoId;
    }

    public int getOldCantidad() {
        return oldCantidad;
    }

    public int getNewCantidad() {
        return newCantidad;
    }

    @Override
    public String toString() {
        return "InventarioActualizadoEvent{" +
                "productoId=" + productoId +
                ", oldCantidad=" + oldCantidad +
                ", newCantidad=" + newCantidad +
                '}';
    }
}