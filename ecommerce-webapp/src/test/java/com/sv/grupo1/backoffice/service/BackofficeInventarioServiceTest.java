package com.sv.grupo1.backoffice.service;

import com.sv.grupo1.backoffice.dto.MovimientoInventarioDTO;
import com.sv.grupo1.backoffice.service.impl.BackofficeInventarioServiceImpl;
import com.sv.grupo1.backoffice.task.StockMonitorTask;
import com.sv.grupo1.ecommerce.dao.CategoriaRepository;
import com.sv.grupo1.ecommerce.dao.MarcaRepository;
import com.sv.grupo1.ecommerce.dao.MovimientoInventarioRepository;
import com.sv.grupo1.ecommerce.dao.ProductoRepository;
import com.sv.grupo1.ecommerce.entities.core.MovimientoInventario;
import com.sv.grupo1.ecommerce.entities.core.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BackofficeInventarioServiceTest {

    private ProductoRepository productoRepository;
    private MovimientoInventarioRepository movimientoRepository;
    private StockMonitorTask stockMonitorTask;
    private ProductoImagenStorageService imagenStorageService;
    private BackofficeInventarioServiceImpl service;

    @BeforeEach
    void setUp() {
        productoRepository = mock(ProductoRepository.class);
        movimientoRepository = mock(MovimientoInventarioRepository.class);
        stockMonitorTask = mock(StockMonitorTask.class);
        imagenStorageService = mock(ProductoImagenStorageService.class);
        service = new BackofficeInventarioServiceImpl(
                productoRepository,
                movimientoRepository,
                mock(CategoriaRepository.class),
                mock(MarcaRepository.class),
                stockMonitorTask,
                imagenStorageService
        );
    }

    @Test
    void debeRegistrarUnaEntradaSumandoStockYGuardandoElMovimiento() {
        Producto producto = crearProducto(10);
        when(productoRepository.findByIdConRelaciones(1)).thenReturn(Optional.of(producto));

        Producto resultado = service.registrarMovimiento(crearDto(" entrada ", 5, "compra_proveedor", " Factura 123 "));

        assertEquals(15, resultado.getStockDisponible());
        verify(productoRepository).save(producto);

        ArgumentCaptor<MovimientoInventario> captor = ArgumentCaptor.forClass(MovimientoInventario.class);
        verify(movimientoRepository).save(captor.capture());
        assertEquals("ENTRADA", captor.getValue().getTipoMovimiento());
        assertEquals(5, captor.getValue().getCantidad());
        assertEquals("COMPRA_PROVEEDOR - Factura 123", captor.getValue().getMotivo());
        assertSame(producto, captor.getValue().getProducto());
        verify(stockMonitorTask).escanearInventario();
    }

    @Test
    void debeRegistrarUnaSalidaRestandoStock() {
        Producto producto = crearProducto(10);
        when(productoRepository.findByIdConRelaciones(1)).thenReturn(Optional.of(producto));

        Producto resultado = service.registrarMovimiento(crearDto("SALIDA", 4, "merma", null));

        assertEquals(6, resultado.getStockDisponible());
    }

    @Test
    void debeUsarAjusteComoMotivoCuandoNoSeIndicaUno() {
        when(productoRepository.findByIdConRelaciones(1)).thenReturn(Optional.of(crearProducto(10)));

        service.registrarMovimiento(crearDto("ENTRADA", 1, "  ", null));

        ArgumentCaptor<MovimientoInventario> captor = ArgumentCaptor.forClass(MovimientoInventario.class);
        verify(movimientoRepository).save(captor.capture());
        assertEquals("AJUSTE", captor.getValue().getMotivo());
    }

    @Test
    void noDebeRegistrarUnaSalidaMayorAlStockDisponible() {
        Producto producto = crearProducto(3);
        when(productoRepository.findByIdConRelaciones(1)).thenReturn(Optional.of(producto));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.registrarMovimiento(crearDto("SALIDA", 5, "merma", null))
        );

        assertTrue(error.getMessage().contains("solo tiene 3 unidades disponibles"));
        assertEquals(3, producto.getStockDisponible());
        verify(productoRepository, never()).save(any());
        verify(movimientoRepository, never()).save(any());
    }

    @Test
    void noDebeRegistrarUnTipoDeMovimientoDesconocido() {
        when(productoRepository.findByIdConRelaciones(1)).thenReturn(Optional.of(crearProducto(10)));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.registrarMovimiento(crearDto("AJUSTE", 5, "conteo", null))
        );

        assertEquals("El tipo de movimiento debe ser ENTRADA o SALIDA.", error.getMessage());
        verify(movimientoRepository, never()).save(any());
    }

    @Test
    void noDebeRegistrarUnMovimientoConCantidadCeroOSinCantidad() {
        when(productoRepository.findByIdConRelaciones(1)).thenReturn(Optional.of(crearProducto(10)));

        IllegalArgumentException conCero = assertThrows(
                IllegalArgumentException.class,
                () -> service.registrarMovimiento(crearDto("ENTRADA", 0, "compra", null))
        );
        IllegalArgumentException sinCantidad = assertThrows(
                IllegalArgumentException.class,
                () -> service.registrarMovimiento(crearDto("ENTRADA", null, "compra", null))
        );

        assertEquals("La cantidad del movimiento debe ser mayor a 0.", conCero.getMessage());
        assertEquals("La cantidad del movimiento debe ser mayor a 0.", sinCantidad.getMessage());
        verify(productoRepository, never()).save(any());
    }

    @Test
    void noDebeRegistrarUnMovimientoDeUnProductoQueNoExiste() {
        when(productoRepository.findByIdConRelaciones(1)).thenReturn(Optional.empty());

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.registrarMovimiento(crearDto("ENTRADA", 1, "compra", null))
        );

        assertEquals("No se encontró el producto seleccionado.", error.getMessage());
    }

    @Test
    void debeActualizarElStockMinimoYRefrescarElMonitor() {
        Producto producto = crearProducto(10);
        when(productoRepository.findByIdConRelaciones(1)).thenReturn(Optional.of(producto));

        service.actualizarStockMinimo(1, 8);

        assertEquals(8, producto.getStockMinimo());
        verify(productoRepository).save(producto);
        verify(stockMonitorTask).escanearInventario();
    }

    @Test
    void noDebeAceptarUnStockMinimoNegativo() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.actualizarStockMinimo(1, -1)
        );

        assertEquals("El stock mínimo no puede ser negativo.", error.getMessage());
        verifyNoInteractions(productoRepository);
    }

    @Test
    void debeEscanearElInventarioCuandoElMonitorAunNoTieneAlertas() {
        Producto critico = crearProducto(1);
        when(stockMonitorTask.getProductosBajoStockAlertados()).thenReturn(List.of()).thenReturn(List.of(critico));

        List<Producto> alertas = service.obtenerAlertasMonitor();

        assertEquals(List.of(critico), alertas);
        verify(stockMonitorTask).escanearInventario();
    }

    @Test
    void noDebeEscanearDeNuevoCuandoElMonitorYaTieneAlertas() {
        Producto critico = crearProducto(1);
        when(stockMonitorTask.getProductosBajoStockAlertados()).thenReturn(List.of(critico));

        List<Producto> alertas = service.obtenerAlertasMonitor();

        assertEquals(1, alertas.size());
        verify(stockMonitorTask, never()).escanearInventario();
    }

    private MovimientoInventarioDTO crearDto(String tipo, Integer cantidad, String motivo, String observacion) {
        MovimientoInventarioDTO dto = new MovimientoInventarioDTO();
        dto.setIdProducto(1);
        dto.setTipoMovimiento(tipo);
        dto.setCantidad(cantidad);
        dto.setMotivo(motivo);
        dto.setObservacion(observacion);
        return dto;
    }

    private Producto crearProducto(int stock) {
        Producto producto = new Producto();
        producto.setIdProducto(1);
        producto.setNombreProducto("LAPTOP HP");
        producto.setCodigoSku("ELC-COM-LAP-HP-0001");
        producto.setStockDisponible(stock);
        producto.setStockMinimo(5);
        return producto;
    }
}
