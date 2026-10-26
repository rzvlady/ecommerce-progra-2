package com.sv.grupo1.ecommerce.controllers;

import com.sv.grupo1.ecommerce.dto.SugerenciaBusquedaDTO;
import com.sv.grupo1.ecommerce.services.ProductoService;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BusquedaApiControllerTest {

    private final ProductoService productoService = mock(ProductoService.class);
    private final BusquedaApiController controller = new BusquedaApiController(productoService);

    @Test
    void debeRetornarSugerenciasExitosamente() {
        SugerenciaBusquedaDTO dtoMock = new SugerenciaBusquedaDTO();
        dtoMock.setCategorias(List.of(
                new SugerenciaBusquedaDTO.SugerenciaCategoriaDTO(1, "Smartphones", "/productos?idCategoria=1")
        ));
        dtoMock.setMarcas(List.of(
                new SugerenciaBusquedaDTO.SugerenciaMarcaDTO(1, "Samsung", "/productos?idMarca=1")
        ));
        dtoMock.setProductos(List.of(
                new SugerenciaBusquedaDTO.SugerenciaProductoDTO(
                        10, "SKU-S23", "Samsung Galaxy S23", BigDecimal.valueOf(899.99),
                        "img.png", "Smartphones", "Samsung", "/productos/10"
                )
        ));
        dtoMock.setTotalResultados(3);

        when(productoService.buscarSugerencias("sam")).thenReturn(dtoMock);

        ResponseEntity<SugerenciaBusquedaDTO> response = controller.sugerencias("sam");

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getCategorias().size());
        assertEquals("Smartphones", response.getBody().getCategorias().get(0).getNombreCategoria());
        assertEquals(1, response.getBody().getMarcas().size());
        assertEquals("Samsung", response.getBody().getMarcas().get(0).getNombreMarca());
        assertEquals(1, response.getBody().getProductos().size());
        assertEquals("Samsung Galaxy S23", response.getBody().getProductos().get(0).getNombreProducto());
    }
}
