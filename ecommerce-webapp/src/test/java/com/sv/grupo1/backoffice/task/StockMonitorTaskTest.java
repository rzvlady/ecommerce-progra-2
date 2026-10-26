package com.sv.grupo1.backoffice.task;

import com.sv.grupo1.backoffice.task.impl.StockMonitorTaskImpl;
import com.sv.grupo1.ecommerce.dao.ProductoRepository;
import com.sv.grupo1.ecommerce.entities.core.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class StockMonitorTaskTest {

    private ProductoRepository productoRepository;
    private StockMonitorTaskImpl stockMonitorTask;

    @BeforeEach
    void setUp() {
        productoRepository = mock(ProductoRepository.class);
        stockMonitorTask = new StockMonitorTaskImpl(productoRepository);
    }

    @Test
    void debeIniciarSinAlertasAntesDelPrimerEscaneo() {
        assertTrue(stockMonitorTask.getProductosBajoStockAlertados().isEmpty());
        verifyNoInteractions(productoRepository);
    }

    @Test
    void debePublicarLosProductosConStockBajoDelEscaneo() {
        Producto iphone = crearProducto("IPHONE 15", 2, 5);
        Producto xperia = crearProducto("SONY XPERIA 10", 0, 4);
        when(productoRepository.findProductosConStockBajo()).thenReturn(List.of(iphone, xperia));

        stockMonitorTask.escanearInventario();

        assertEquals(List.of(iphone, xperia), stockMonitorTask.getProductosBajoStockAlertados());
    }

    @Test
    void debePublicarUnaListaQueNoSePuedeModificar() {
        when(productoRepository.findProductosConStockBajo()).thenReturn(List.of(crearProducto("IPHONE 15", 2, 5)));

        stockMonitorTask.escanearInventario();
        List<Producto> alertas = stockMonitorTask.getProductosBajoStockAlertados();

        assertThrows(UnsupportedOperationException.class, () -> alertas.add(crearProducto("OTRO", 1, 5)));
        assertThrows(UnsupportedOperationException.class, alertas::clear);
    }

    @Test
    void noDebeVerseAfectadaLaListaPublicadaSiCambiaElResultadoOriginal() {
        List<Producto> resultadoConsulta = new ArrayList<>();
        resultadoConsulta.add(crearProducto("IPHONE 15", 2, 5));
        when(productoRepository.findProductosConStockBajo()).thenReturn(resultadoConsulta);

        stockMonitorTask.escanearInventario();
        resultadoConsulta.add(crearProducto("SONY XPERIA 10", 0, 4));

        assertEquals(1, stockMonitorTask.getProductosBajoStockAlertados().size());
    }

    @Test
    void debeReemplazarLasAlertasAnterioresEnCadaEscaneo() {
        when(productoRepository.findProductosConStockBajo())
                .thenReturn(List.of(crearProducto("IPHONE 15", 2, 5)))
                .thenReturn(List.of());

        stockMonitorTask.escanearInventario();
        assertEquals(1, stockMonitorTask.getProductosBajoStockAlertados().size());

        stockMonitorTask.escanearInventario();
        assertTrue(stockMonitorTask.getProductosBajoStockAlertados().isEmpty());
    }

    @Test
    void debePermitirLeerLasAlertasDesdeVariosHilosMientrasSeEscanea() throws Exception {
        when(productoRepository.findProductosConStockBajo()).thenReturn(List.of(crearProducto("IPHONE 15", 2, 5)));
        ExecutorService hilos = Executors.newFixedThreadPool(4);
        CountDownLatch inicio = new CountDownLatch(1);
        List<Future<Integer>> lecturas = new ArrayList<>();

        try {
            for (int i = 0; i < 4; i++) {
                lecturas.add(hilos.submit(() -> {
                    inicio.await();
                    stockMonitorTask.escanearInventario();
                    return stockMonitorTask.getProductosBajoStockAlertados().size();
                }));
            }
            inicio.countDown();

            for (Future<Integer> lectura : lecturas) {
                assertEquals(1, lectura.get(2, TimeUnit.SECONDS));
            }
        } finally {
            hilos.shutdownNow();
        }
        verify(productoRepository, times(4)).findProductosConStockBajo();
    }

    private Producto crearProducto(String nombre, int stock, int stockMinimo) {
        Producto producto = new Producto();
        producto.setNombreProducto(nombre);
        producto.setCodigoSku("SKU-" + nombre.hashCode());
        producto.setStockDisponible(stock);
        producto.setStockMinimo(stockMinimo);
        return producto;
    }
}
