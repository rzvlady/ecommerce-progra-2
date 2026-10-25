package com.sv.grupo1.ecommerce.controllers;

import com.sv.grupo1.ecommerce.carrito.CarritoResumen;
import com.sv.grupo1.ecommerce.carrito.CarritoSesionService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CarritoApiControllerTest {

    private final CarritoSesionService carritoService = mock(CarritoSesionService.class);
    private final CarritoApiController controller = new CarritoApiController(carritoService);

    @Test
    void debeAgregarProductoExitosamente() {
        CarritoResumen resumen = new CarritoResumen(
                BigDecimal.valueOf(50.00), BigDecimal.ZERO, BigDecimal.valueOf(50.00), 2
        );
        when(carritoService.resumen()).thenReturn(resumen);

        ResponseEntity<?> response = controller.agregar(42, 2);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof Map);
        Map<?, ?> map = (Map<?, ?>) response.getBody();
        assertEquals(true, map.get("ok"));
        assertEquals(2, map.get("carritoCantidad"));

        verify(carritoService).agregar(42, 2);
    }

    @Test
    void debeRetornarBadRequestCuandoStockInsuficiente() {
        doThrow(new IllegalArgumentException("No hay suficiente stock disponible."))
                .when(carritoService).agregar(42, 999);

        ResponseEntity<?> response = controller.agregar(42, 999);

        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof Map);
        Map<?, ?> map = (Map<?, ?>) response.getBody();
        assertEquals(false, map.get("ok"));
        assertEquals("No hay suficiente stock disponible.", map.get("mensaje"));
    }

    @Test
    void debeRetornarNotFoundCuandoProductoNoExiste() {
        doThrow(new NoSuchElementException("El producto no existe."))
                .when(carritoService).agregar(999, 1);

        ResponseEntity<?> response = controller.agregar(999, 1);

        assertNotNull(response);
        assertEquals(404, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof Map);
        Map<?, ?> map = (Map<?, ?>) response.getBody();
        assertEquals(false, map.get("ok"));
        assertEquals("El producto no existe.", map.get("mensaje"));
    }
}
