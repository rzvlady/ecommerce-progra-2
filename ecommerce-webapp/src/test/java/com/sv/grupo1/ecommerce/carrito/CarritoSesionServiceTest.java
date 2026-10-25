package com.sv.grupo1.ecommerce.carrito;

import com.sv.grupo1.ecommerce.dao.ProductoRepository;
import com.sv.grupo1.ecommerce.entities.core.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CarritoSesionServiceTest {

    private ProductoRepository productoRepository;
    private CarritoSesion carrito;
    private CarritoSesionService carritoService;

    @BeforeEach
    void setUp() {
        productoRepository = mock(ProductoRepository.class);
        carrito = new CarritoSesion();
        carritoService = new CarritoSesionService(productoRepository, carrito, null,
                new BigDecimal("3.50"), new BigDecimal("50.00"));
    }

    @Test
    void debeAgregarUnProductoNuevoAlCarrito() {
        registrarProducto(crearProducto(10, 5, 'A'));

        carritoService.agregar(10, 2);

        CarritoItem item = carrito.get(10);
        assertNotNull(item);
        assertEquals(2, item.getCantidad());
        assertEquals("LAPTOP HP", item.getNombre());
        assertEquals(new BigDecimal("100.00"), item.getPrecioUnitario());
        assertEquals(new BigDecimal("200.00"), item.getSubtotal());
    }

    @Test
    void debeSumarLaCantidadCuandoElProductoYaEstaEnElCarrito() {
        registrarProducto(crearProducto(10, 5, 'A'));

        carritoService.agregar(10, 2);
        carritoService.agregar(10, 3);

        assertEquals(1, carrito.getItems().size());
        assertEquals(5, carrito.get(10).getCantidad());
    }

    @Test
    void noDebeAgregarUnaCantidadMenorAUno() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> carritoService.agregar(10, 0)
        );

        assertEquals("La cantidad debe ser al menos 1.", error.getMessage());
        verifyNoInteractions(productoRepository);
        assertTrue(carrito.estaVacio());
    }

    @Test
    void noDebeAgregarUnProductoQueNoExiste() {
        when(productoRepository.findByIdConRelaciones(99)).thenReturn(Optional.empty());
        when(productoRepository.findById(99)).thenReturn(Optional.empty());

        NoSuchElementException error = assertThrows(
                NoSuchElementException.class,
                () -> carritoService.agregar(99, 1)
        );

        assertEquals("El producto no existe.", error.getMessage());
    }

    @Test
    void noDebeAgregarUnProductoInactivo() {
        registrarProducto(crearProducto(10, 5, 'I'));

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> carritoService.agregar(10, 1)
        );

        assertEquals("\"LAPTOP HP\" ya no esta disponible.", error.getMessage());
        assertTrue(carrito.estaVacio());
    }

    @Test
    void noDebeAgregarUnProductoSinStock() {
        registrarProducto(crearProducto(10, 0, 'A'));

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> carritoService.agregar(10, 1)
        );

        assertEquals("\"LAPTOP HP\" no tiene stock disponible.", error.getMessage());
    }

    @Test
    void noDebeSuperarElStockAlSumarUnidadesQueYaEstanEnElCarrito() {
        registrarProducto(crearProducto(10, 5, 'A'));
        carritoService.agregar(10, 3);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> carritoService.agregar(10, 3)
        );

        assertEquals("Solo hay 5 unidad(es) de \"LAPTOP HP\".", error.getMessage());
        assertEquals(3, carrito.get(10).getCantidad());
    }

    @Test
    void noDebeAgregarUnProductoSinPrecioDeVenta() {
        Producto producto = crearProducto(10, 5, 'A');
        producto.setPrecioVenta(null);
        registrarProducto(producto);

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> carritoService.agregar(10, 1)
        );

        assertEquals("El producto no tiene precio de venta.", error.getMessage());
    }

    @Test
    void debeActualizarLaCantidadConElPrecioActualDelProducto() {
        Producto producto = crearProducto(10, 5, 'A');
        registrarProducto(producto);
        carritoService.agregar(10, 1);
        producto.setPrecioVenta(new BigDecimal("120.00"));

        carritoService.actualizar(10, 4);

        assertEquals(4, carrito.get(10).getCantidad());
        assertEquals(new BigDecimal("120.00"), carrito.get(10).getPrecioUnitario());
    }

    @Test
    void debeQuitarElProductoAlActualizarLaCantidadACero() {
        registrarProducto(crearProducto(10, 5, 'A'));
        carritoService.agregar(10, 2);

        carritoService.actualizar(10, 0);

        assertNull(carrito.get(10));
        assertTrue(carrito.estaVacio());
    }

    @Test
    void noDebeHacerNadaAlActualizarUnProductoQueNoEstaEnElCarrito() {
        carritoService.actualizar(10, 3);

        assertTrue(carrito.estaVacio());
        verifyNoInteractions(productoRepository);
    }

    @Test
    void noDebeActualizarAUnaCantidadMayorAlStock() {
        registrarProducto(crearProducto(10, 5, 'A'));
        carritoService.agregar(10, 2);

        assertThrows(IllegalArgumentException.class, () -> carritoService.actualizar(10, 6));

        assertEquals(2, carrito.get(10).getCantidad());
    }

    @Test
    void debeEliminarUnProductoYVaciarElCarrito() {
        registrarProducto(crearProducto(10, 5, 'A'));
        registrarProducto(crearProducto(20, 5, 'A'));
        carritoService.agregar(10, 1);
        carritoService.agregar(20, 1);

        carritoService.eliminar(10);
        assertNull(carrito.get(10));
        assertNotNull(carrito.get(20));

        carritoService.vaciar();
        assertTrue(carrito.estaVacio());
    }

    @Test
    void debeCalcularElResumenConSubtotalUnidadesYEnvioGratis() {
        Producto laptop = crearProducto(10, 5, 'A');
        Producto mouse = crearProducto(20, 10, 'A');
        mouse.setPrecioVenta(new BigDecimal("15.25"));
        registrarProducto(laptop);
        registrarProducto(mouse);
        carritoService.agregar(10, 2);
        carritoService.agregar(20, 3);

        CarritoResumen resumen = carritoService.resumen();

        assertEquals(new BigDecimal("245.75"), resumen.getSubtotal());
        assertEquals(new BigDecimal("0.00"), resumen.getEnvio());
        assertEquals(new BigDecimal("245.75"), resumen.getTotal());
        assertEquals(5, resumen.getCantidadTotal());
    }

    @Test
    void debeDevolverUnResumenEnCeroCuandoElCarritoEstaVacio() {
        CarritoResumen resumen = carritoService.resumen();

        assertEquals(new BigDecimal("0.00"), resumen.getSubtotal());
        assertEquals(new BigDecimal("0.00"), resumen.getTotal());
        assertEquals(0, resumen.getCantidadTotal());
    }

    @Test
    void debeDevolverUnaCopiaDeLosItemsDelCarrito() {
        registrarProducto(crearProducto(10, 5, 'A'));
        carritoService.agregar(10, 1);

        carritoService.items().clear();

        assertEquals(1, carrito.getItems().size());
    }

    private void registrarProducto(Producto producto) {
        when(productoRepository.findByIdConRelaciones(producto.getIdProducto())).thenReturn(Optional.of(producto));
    }

    private Producto crearProducto(Integer idProducto, int stock, char estado) {
        Producto producto = new Producto();
        producto.setIdProducto(idProducto);
        producto.setNombreProducto("LAPTOP HP");
        producto.setCodigoSku("ELC-COM-LAP-HP-000" + idProducto);
        producto.setPrecioVenta(new BigDecimal("100.00"));
        producto.setStockDisponible(stock);
        producto.setEstado(estado);
        return producto;
    }
}
