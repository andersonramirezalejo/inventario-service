package com.example.Inventario.service;

import com.example.Inventario.client.ProductoFeignClient;
import com.example.Inventario.dto.CompraRequest;
import com.example.Inventario.dto.CompraResponse; 
import com.example.Inventario.dto.InventarioResponseDto;
import com.example.Inventario.dto.ProductoDto;
import com.example.Inventario.exception.InventarioNotFoundException;
import com.example.Inventario.exception.ProductoNotFoundException;
import com.example.Inventario.exception.StockNotAvailableException;
import com.example.Inventario.model.Inventario;
import com.example.Inventario.repository.InventarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private ProductoFeignClient productoFeignClient;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private InventarioService inventarioService;

    private Inventario inventario;
    private ProductoDto productoDto;
    private CompraRequest compraRequest;
    private InventarioResponseDto inventarioResponseDto; // Nuevo: Para la respuesta esperada

    @BeforeEach
    void setUp() {
        inventario = new Inventario();
        inventario.setProductoId(1L);
        inventario.setCantidad(10);

        productoDto = new ProductoDto();
        productoDto.setId(1L);
        productoDto.setNombre("Producto de prueba");
        productoDto.setDescripcion("Descripción de prueba"); // Añadido
        productoDto.setPrecio(100.0);

        compraRequest = new CompraRequest();
        compraRequest.setProductoId(1L);
        compraRequest.setCantidad(5);

        // Inicializar el DTO de respuesta esperado
        inventarioResponseDto = new InventarioResponseDto(
                inventario.getProductoId(),
                productoDto.getNombre(),
                productoDto.getDescripcion(),
                productoDto.getPrecio(),
                inventario.getCantidad()
        );
    }

    @Test
    @DisplayName("Debe obtener el inventario por ID de producto y devolver un DTO")
    void givenProductoId_whenGetInventarioByProductoId_thenReturnInventarioResponseDto() {
        // Configurar el mock del repositorio para devolver el objeto Inventario
        when(inventarioRepository.findById(anyLong())).thenReturn(Optional.of(inventario));
        // Configurar el mock del cliente Feign para devolver el ProductoDto
        when(productoFeignClient.getProductoById(anyLong())).thenReturn(Optional.of(productoDto));

        // ¡Aquí está el cambio! El tipo de 'result' ahora es InventarioResponseDto
        InventarioResponseDto result = inventarioService.getInventarioByProductoId(1L);

        assertNotNull(result);
        assertEquals(inventarioResponseDto.getProductoId(), result.getProductoId());
        assertEquals(inventarioResponseDto.getNombreProducto(), result.getNombreProducto());
        assertEquals(inventarioResponseDto.getCantidadDisponible(), result.getCantidadDisponible());
        assertEquals(inventarioResponseDto.getPrecioProducto(), result.getPrecioProducto());
        assertEquals(inventarioResponseDto.getDescripcionProducto(), result.getDescripcionProducto());


        verify(inventarioRepository, times(1)).findById(1L);
        verify(productoFeignClient, times(1)).getProductoById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción si no se encuentra el inventario")
    void givenNonExistingProductoId_whenGetInventarioByProductoId_thenThrowInventarioNotFoundException() {
        when(inventarioRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(InventarioNotFoundException.class, () -> inventarioService.getInventarioByProductoId(1L));
        verify(inventarioRepository, times(1)).findById(1L);
        verify(productoFeignClient, never()).getProductoById(anyLong()); // No se debe llamar al cliente de productos
    }

    @Test
    @DisplayName("Debe lanzar excepción si el producto no se encuentra en el servicio de productos")
    void givenInventarioExistsButProductoNotFound_whenGetInventarioByProductoId_thenThrowProductoNotFoundException() {
        when(inventarioRepository.findById(anyLong())).thenReturn(Optional.of(inventario));
        when(productoFeignClient.getProductoById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ProductoNotFoundException.class, () -> inventarioService.getInventarioByProductoId(1L));
        verify(inventarioRepository, times(1)).findById(1L);
        verify(productoFeignClient, times(1)).getProductoById(1L);
    }

    @Test
    @DisplayName("Debe realizar una compra exitosamente y actualizar el stock")
    void givenCompraRequest_whenRealizarCompra_thenReturnCompraResponseAndReduceStock() {
        // Given
        when(productoFeignClient.getProductoById(anyLong())).thenReturn(Optional.of(productoDto));
        when(inventarioRepository.findById(anyLong())).thenReturn(Optional.of(inventario));
        // Simula que save devuelve el inventario con la cantidad actualizada
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(invocation -> {
            Inventario savedInventario = invocation.getArgument(0);
            return savedInventario; // Devuelve el mismo objeto que se pasó, con la cantidad modificada
        });

        // When
        var compraResponse = inventarioService.realizarCompra(compraRequest);

        // Then
        assertNotNull(compraResponse);
        assertEquals(productoDto.getId(), compraResponse.getProductoId());
        assertEquals(productoDto.getNombre(), compraResponse.getNombreProducto());
        assertEquals(compraRequest.getCantidad(), compraResponse.getCantidadComprada());
        assertEquals(productoDto.getPrecio() * compraRequest.getCantidad(), compraResponse.getTotalPagar());

        // Verifica que el stock en el objeto 'inventario' mockeado se redujo
        assertEquals(5, inventario.getCantidad()); // 10 - 5 = 5

        // Verifica que el método save del repositorio fue llamado una vez
        verify(inventarioRepository, times(1)).save(inventario); // Se pasó el objeto 'inventario' modificado
        // Verifica que el evento se publicó
        verify(eventPublisher, times(1)).publishEvent(any()); // O un matcher más específico para el evento
    }

    @Test
    @DisplayName("Debe lanzar StockNotAvailableException si no hay suficiente stock para la compra")
    void givenCompraRequestWithInsufficientStock_whenRealizarCompra_thenThrowStockNotAvailableException() {
        // Given
        compraRequest.setCantidad(15); // Intentar comprar 15, cuando solo hay 10
        when(productoFeignClient.getProductoById(anyLong())).thenReturn(Optional.of(productoDto));
        when(inventarioRepository.findById(anyLong())).thenReturn(Optional.of(inventario));

        // When & Then
        StockNotAvailableException thrown = assertThrows(StockNotAvailableException.class, () -> {
            inventarioService.realizarCompra(compraRequest);
        });

        assertEquals("No hay suficiente stock para el producto Producto de prueba. Stock disponible: 10, Cantidad solicitada: 15", thrown.getMessage());

        // Verificar que el stock no se modificó ni se guardó en el repositorio
        verify(inventarioRepository, never()).save(any(Inventario.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Debe lanzar ProductoNotFoundException si el producto no existe al realizar compra")
    void givenCompraRequest_whenProductoNotFound_thenThrowProductoNotFoundException() {
        // Given
        when(productoFeignClient.getProductoById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProductoNotFoundException.class, () -> inventarioService.realizarCompra(compraRequest));

        // Verificar que no se intentó obtener inventario ni guardar
        verify(inventarioRepository, never()).findById(anyLong());
        verify(inventarioRepository, never()).save(any(Inventario.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Debe lanzar InventarioNotFoundException si el inventario no está inicializado al realizar compra")
    void givenProductoExistsButInventarioNotFound_whenRealizarCompra_thenThrowInventarioNotFoundException() {
        // Given
        when(productoFeignClient.getProductoById(anyLong())).thenReturn(Optional.of(productoDto));
        when(inventarioRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(InventarioNotFoundException.class, () -> inventarioService.realizarCompra(compraRequest));

        // Verificar que no se intentó guardar ni publicar evento
        verify(inventarioRepository, never()).save(any(Inventario.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Debe obtener el inventario por ID de producto y devolver un DTO con info de producto")
    void givenProductoId_whenGetInventarioByProductoId_thenReturnInventarioResponseDtoWithProductInfo() {
        // Configurar el mock del repositorio para devolver el objeto Inventario
        when(inventarioRepository.findById(anyLong())).thenReturn(Optional.of(inventario));

        // Configurar el mock del cliente Feign para devolver el ProductoDto
        when(productoFeignClient.getProductoById(anyLong())).thenReturn(Optional.of(productoDto));

        InventarioResponseDto result = inventarioService.getInventarioByProductoId(1L);

        assertNotNull(result);
        assertEquals(inventarioResponseDto.getProductoId(), result.getProductoId());
        assertEquals(inventarioResponseDto.getNombreProducto(), result.getNombreProducto());
        assertEquals(inventarioResponseDto.getCantidadDisponible(), result.getCantidadDisponible());
        assertEquals(inventarioResponseDto.getPrecioProducto(), result.getPrecioProducto());
        assertEquals(inventarioResponseDto.getDescripcionProducto(), result.getDescripcionProducto());

        // Verificar que el método findById del repositorio fue llamado una vez con el ID correcto
        verify(inventarioRepository, times(1)).findById(1L);
        // Verificar que el método getProductoById del cliente Feign fue llamado una vez con el ID correcto
        verify(productoFeignClient, times(1)).getProductoById(1L);
    }

    @Test
    @DisplayName("Debe lanzar excepción si no se encuentra el inventario (y por tanto no se llama a productos)")
    void givenNonExistingInventario_whenGetInventarioByProductoId_thenThrowInventarioNotFoundExceptionAndNoProductCall() {
        // Configurar el mock del repositorio para devolver Optional.empty()
        when(inventarioRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Asegurarse de que se lanza la excepción InventarioNotFoundException
        assertThrows(InventarioNotFoundException.class, () -> inventarioService.getInventarioByProductoId(1L));

        // Verificar que el método findById del repositorio fue llamado
        verify(inventarioRepository, times(1)).findById(1L);
        // Verificar que el método getProductoById del cliente Feign NUNCA fue llamado
        verify(productoFeignClient, never()).getProductoById(anyLong());
    }

    @Test
    @DisplayName("Debe realizar una compra exitosamente, reducir stock y llamar a productos para info")
    void givenCompraRequest_whenRealizarCompra_thenReturnCompraResponseAndReduceStockAndCallProductService() {
        // Given
        when(productoFeignClient.getProductoById(anyLong())).thenReturn(Optional.of(productoDto));
        when(inventarioRepository.findById(anyLong())).thenReturn(Optional.of(inventario));
        // Simula que save devuelve el inventario con la cantidad actualizada
        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(invocation -> {
            Inventario savedInventario = invocation.getArgument(0);
            return savedInventario; // Devuelve el mismo objeto que se pasó, con la cantidad modificada
        });

        // When
        CompraResponse compraResponse = inventarioService.realizarCompra(compraRequest);

        // Then
        assertNotNull(compraResponse);
        assertEquals(productoDto.getId(), compraResponse.getProductoId());
        assertEquals(productoDto.getNombre(), compraResponse.getNombreProducto());
        assertEquals(compraRequest.getCantidad(), compraResponse.getCantidadComprada());
        assertEquals(productoDto.getPrecio() * compraRequest.getCantidad(), compraResponse.getTotalPagar());

        // Verificar que el stock en el objeto 'inventario' mockeado se redujo
        assertEquals(5, inventario.getCantidad()); // 10 - 5 = 5

        // Verificar que el método getProductoById del cliente Feign fue llamado una vez
        verify(productoFeignClient, times(1)).getProductoById(productoDto.getId());
        // Verificar que el método save del repositorio fue llamado una vez
        verify(inventarioRepository, times(1)).save(inventario);
        // Verificar que el evento se publicó
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    @DisplayName("Debe lanzar ProductoNotFoundException si el producto no existe al realizar compra")
    void givenCompraRequest_whenProductoNotFoundOnRealizarCompra_thenThrowProductoNotFoundException() {
        // Given: El cliente Feign devuelve Optional.empty() para el producto
        when(productoFeignClient.getProductoById(anyLong())).thenReturn(Optional.empty());

        // When & Then: Se espera que se lance ProductoNotFoundException
        assertThrows(ProductoNotFoundException.class, () -> inventarioService.realizarCompra(compraRequest));

        // Verificar que el repositorio de inventario NUNCA fue consultado o modificado
        verify(inventarioRepository, never()).findById(anyLong());
        verify(inventarioRepository, never()).save(any(Inventario.class));
        verify(eventPublisher, never()).publishEvent(any());
        // Verificar que el cliente Feign fue llamado
        verify(productoFeignClient, times(1)).getProductoById(productoDto.getId());
    }

    @Test
    @DisplayName("Debe lanzar InventarioNotFoundException si no se encuentra el inventario local")
    void givenNonExistingInventarioId_whenGetInventarioByProductoId_thenThrowInventarioNotFoundException() {
        // Given: El repositorio devuelve Optional.empty()
        when(inventarioRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then: Verificar que se lanza la excepción correcta
        InventarioNotFoundException thrown = assertThrows(InventarioNotFoundException.class, () ->
                inventarioService.getInventarioByProductoId(99L) // Usamos un ID que no esperamos encontrar
        );

        // Verificar el mensaje de la excepción
        assertEquals("Inventario no encontrado para el producto con ID: 99", thrown.getMessage());
        // Verificar que solo se llamó al repositorio de inventario
        verify(inventarioRepository, times(1)).findById(99L);
        // Verificar que el cliente Feign NO fue llamado
        verify(productoFeignClient, never()).getProductoById(anyLong());
    }

    @Test
    @DisplayName("Debe lanzar ProductoNotFoundException si el inventario existe pero el producto no se encuentra en el servicio remoto")
    void givenInventarioExistsButProductNotFound_whenGetInventarioByProductoId_thenThrowProductoNotFoundException() {
        // Given: El repositorio de inventario devuelve un Inventario
        when(inventarioRepository.findById(anyLong())).thenReturn(Optional.of(inventario));
        // Given: El cliente Feign devuelve Optional.empty() para el producto
        when(productoFeignClient.getProductoById(anyLong())).thenReturn(Optional.empty());

        // When & Then: Verificar que se lanza la excepción correcta
        ProductoNotFoundException thrown = assertThrows(ProductoNotFoundException.class, () ->
                inventarioService.getInventarioByProductoId(1L)
        );

        // Verificar el mensaje de la excepción
        assertEquals("Producto no encontrado en el servicio de productos con ID: 1", thrown.getMessage());
        // Verificar que se llamó a ambos (repositorio de inventario y cliente Feign)
        verify(inventarioRepository, times(1)).findById(1L);
        verify(productoFeignClient, times(1)).getProductoById(1L);
    }

    @Test
    @DisplayName("Debe lanzar ProductoNotFoundException si el producto no existe al intentar realizar una compra")
    void givenNonExistingProductForPurchase_whenRealizarCompra_thenThrowProductoNotFoundException() {
        // Given: El cliente Feign devuelve Optional.empty() para el producto
        when(productoFeignClient.getProductoById(anyLong())).thenReturn(Optional.empty());

        // When & Then: Verificar que se lanza la excepción correcta
        ProductoNotFoundException thrown = assertThrows(ProductoNotFoundException.class, () ->
                inventarioService.realizarCompra(compraRequest)
        );

        // Verificar el mensaje de la excepción
        assertEquals("Producto no encontrado en el servicio de productos con ID: " + compraRequest.getProductoId(), thrown.getMessage());
        // Verificar que solo se llamó al cliente Feign y no se procedió con la lógica de inventario
        verify(productoFeignClient, times(1)).getProductoById(compraRequest.getProductoId());
        verify(inventarioRepository, never()).findById(anyLong());
        verify(inventarioRepository, never()).save(any(Inventario.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Debe lanzar InventarioNotFoundException si el inventario no está inicializado al intentar realizar una compra")
    void givenProductExistsButInventarioNotInitializedForPurchase_whenRealizarCompra_thenThrowInventarioNotFoundException() {
        // Given: El cliente Feign encuentra el producto
        when(productoFeignClient.getProductoById(anyLong())).thenReturn(Optional.of(productoDto));
        // Given: Pero el repositorio de inventario devuelve Optional.empty()
        when(inventarioRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When & Then: Verificar que se lanza la excepción correcta
        InventarioNotFoundException thrown = assertThrows(InventarioNotFoundException.class, () ->
                inventarioService.realizarCompra(compraRequest)
        );

        // Verificar el mensaje de la excepción
        assertEquals("Inventario no inicializado para el producto con ID: " + compraRequest.getProductoId(), thrown.getMessage());
        // Verificar que se llamó al cliente Feign y al repositorio de inventario, pero no se guardó ni se publicó evento
        verify(productoFeignClient, times(1)).getProductoById(compraRequest.getProductoId());
        verify(inventarioRepository, times(1)).findById(compraRequest.getProductoId());
        verify(inventarioRepository, never()).save(any(Inventario.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("Debe lanzar StockNotAvailableException si la cantidad solicitada excede el stock disponible al realizar una compra")
    void givenInsufficientStockForPurchase_whenRealizarCompra_thenThrowStockNotAvailableException() {
        // Given: Configurar la solicitud para exceder el stock actual (10)
        compraRequest.setCantidad(15);
        // Given: El cliente Feign encuentra el producto
        when(productoFeignClient.getProductoById(anyLong())).thenReturn(Optional.of(productoDto));
        // Given: El repositorio de inventario encuentra el inventario con 10 unidades
        when(inventarioRepository.findById(anyLong())).thenReturn(Optional.of(inventario));

        // When & Then: Verificar que se lanza la excepción correcta
        StockNotAvailableException thrown = assertThrows(StockNotAvailableException.class, () ->
                inventarioService.realizarCompra(compraRequest)
        );

        // Verificar el mensaje de la excepción
        assertEquals("No hay suficiente stock para el producto Producto de prueba. Stock disponible: 10, Cantidad solicitada: 15", thrown.getMessage());
        // Verificar que se llamó al cliente Feign y al repositorio de inventario, pero no se guardó ni se publicó evento
        verify(productoFeignClient, times(1)).getProductoById(compraRequest.getProductoId());
        verify(inventarioRepository, times(1)).findById(compraRequest.getProductoId());
        verify(inventarioRepository, never()).save(any(Inventario.class));
        verify(eventPublisher, never()).publishEvent(any());
    }
}