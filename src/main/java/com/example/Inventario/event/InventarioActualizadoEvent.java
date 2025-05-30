package com.example.Inventario.event;

import org.springframework.context.ApplicationEvent;

/**
 * InventarioActualizadoEvent representa un evento que indica que el inventario de un producto ha sido actualizado.
 * <p>
 * Este evento puede ser publicado cuando la cantidad de un producto cambia en el inventario.
 */
public class InventarioActualizadoEvent extends ApplicationEvent {
    private Long productoId;
    private int oldCantidad;
    private int newCantidad;

    /**
     * Crea un nuevo evento de actualización de inventario.
     *
     * @param source      el objeto fuente que publica el evento
     * @param productoId  identificador del producto actualizado
     * @param oldCantidad cantidad anterior del producto en inventario
     * @param newCantidad nueva cantidad del producto en inventario
     */
    public InventarioActualizadoEvent(Object source, Long productoId, int oldCantidad, int newCantidad) {
        super(source);
        this.productoId = productoId;
        this.oldCantidad = oldCantidad;
        this.newCantidad = newCantidad;
    }

    /**
     * Obtiene el identificador del producto.
     *
     * @return el ID del producto
     */
    public Long getProductoId() {
        return productoId;
    }

    /**
     * Obtiene la cantidad anterior del producto en inventario.
     *
     * @return la cantidad anterior
     */
    public int getOldCantidad() {
        return oldCantidad;
    }

    /**
     * Obtiene la nueva cantidad del producto en inventario.
     *
     * @return la nueva cantidad
     */
    public int getNewCantidad() {
        return newCantidad;
    }

    /**
     * Devuelve una representación en cadena del evento.
     *
     * @return una cadena con los detalles del evento
     */
    @Override
    public String toString() {
        return "InventarioActualizadoEvent{" +
                "productoId=" + productoId +
                ", oldCantidad=" + oldCantidad +
                ", newCantidad=" + newCantidad +
                '}';
    }
}