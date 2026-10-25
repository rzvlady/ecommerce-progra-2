package com.sv.grupo1.ecommerce.services;

import com.sv.grupo1.backoffice.service.ProductoImagenStorageService;
import com.sv.grupo1.ecommerce.dao.CategoriaRepository;
import com.sv.grupo1.ecommerce.dao.MarcaRepository;
import com.sv.grupo1.ecommerce.dao.ProductoRepository;
import com.sv.grupo1.ecommerce.dto.SugerenciaBusquedaDTO;
import com.sv.grupo1.ecommerce.entities.catalogo.Categoria;
import com.sv.grupo1.ecommerce.entities.catalogo.Marca;
import com.sv.grupo1.ecommerce.entities.core.Producto;
import com.sv.grupo1.ecommerce.services.impl.ProductoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductoServiceSugerenciasTest {

    private final ProductoRepository productoRepository = mock(ProductoRepository.class);
    private final CategoriaRepository categoriaRepository = mock(CategoriaRepository.class);
    private final MarcaRepository marcaRepository = mock(MarcaRepository.class);
    private final ProductoImagenStorageService imagenStorageService = mock(ProductoImagenStorageService.class);

    private ProductoServiceImpl productoService;

    @BeforeEach
    void setup() {
        productoService = new ProductoServiceImpl(
                productoRepository, categoriaRepository, marcaRepository, imagenStorageService);
    }

    @Test
    void debeBuscarSugerenciasPorPrefijo() {
        Categoria cat1 = new Categoria();
        cat1.setIdCategoria(1);
        cat1.setNombreCategoria("Smartphones");
        cat1.setEstado('A');

        Categoria cat2 = new Categoria();
        cat2.setIdCategoria(2);
        cat2.setNombreCategoria("Computación");
        cat2.setEstado('A');

        when(categoriaRepository.findAllByOrderByNombreCategoriaAsc()).thenReturn(List.of(cat1, cat2));

        Marca marca1 = new Marca();
        marca1.setIdMarca(10);
        marca1.setNombreMarca("Samsung");

        Marca marca2 = new Marca();
        marca2.setIdMarca(20);
        marca2.setNombreMarca("Apple");

        when(marcaRepository.findAllByOrderByNombreMarcaAsc()).thenReturn(List.of(marca1, marca2));

        Producto prod1 = new Producto();
        prod1.setIdProducto(101);
        prod1.setCodigoSku("SAM-S23-01");
        prod1.setNombreProducto("Samsung Galaxy S23 Ultra");
        prod1.setPrecioVenta(BigDecimal.valueOf(1199.99));
        prod1.setEstado('A');
        prod1.setCategoria(cat1);
        prod1.setMarca(marca1);

        Producto prod2 = new Producto();
        prod2.setIdProducto(102);
        prod2.setCodigoSku("APP-IPH-15");
        prod2.setNombreProducto("iPhone 15 Pro");
        prod2.setPrecioVenta(BigDecimal.valueOf(999.99));
        prod2.setEstado('A');
        prod2.setCategoria(cat1);
        prod2.setMarca(marca2);

        when(productoRepository.findAllConRelaciones()).thenReturn(List.of(prod1, prod2));

        // Buscar con la letra 's'
        SugerenciaBusquedaDTO resultado = productoService.buscarSugerencias("s");

        assertNotNull(resultado);
        assertTrue(resultado.getTotalResultados() > 0);

        // Categoría 'Smartphones' empieza con 's'
        assertTrue(resultado.getCategorias().stream().anyMatch(c -> c.getNombreCategoria().equals("Smartphones")));

        // Marca 'Samsung' empieza con 's'
        assertTrue(resultado.getMarcas().stream().anyMatch(m -> m.getNombreMarca().equals("Samsung")));

        // Producto 'Samsung Galaxy S23 Ultra' empieza con 's'
        assertTrue(resultado.getProductos().stream().anyMatch(p -> p.getNombreProducto().contains("Samsung")));
    }

    @Test
    void debeRetornarVacioSiTerminoEsNullOBlanco() {
        SugerenciaBusquedaDTO res1 = productoService.buscarSugerencias(null);
        assertNotNull(res1);
        assertEquals(0, res1.getTotalResultados());

        SugerenciaBusquedaDTO res2 = productoService.buscarSugerencias("   ");
        assertNotNull(res2);
        assertEquals(0, res2.getTotalResultados());
    }

    @Test
    void debeConstruirArbolCategoriasJerarquico() {
        Categoria electronica = new Categoria();
        electronica.setIdCategoria(1);
        electronica.setNombreCategoria("Electrónica");
        electronica.setEstado('A');

        Categoria computacion = new Categoria();
        computacion.setIdCategoria(2);
        computacion.setNombreCategoria("Computación");
        computacion.setEstado('A');
        computacion.setCategoriaPadre(electronica);

        Categoria accesorios = new Categoria();
        accesorios.setIdCategoria(3);
        accesorios.setNombreCategoria("Accesorios");
        accesorios.setEstado('A');

        when(categoriaRepository.findAllConJerarquia()).thenReturn(List.of(electronica, computacion, accesorios));

        List<com.sv.grupo1.ecommerce.dto.CategoriaNodoDTO> arbol = productoService.obtenerArbolCategorias();

        assertNotNull(arbol);
        assertEquals(2, arbol.size()); // electronica and accesorios

        com.sv.grupo1.ecommerce.dto.CategoriaNodoDTO nodoElectronica = arbol.stream()
                .filter(c -> c.getIdCategoria().equals(1))
                .findFirst()
                .orElse(null);
        assertNotNull(nodoElectronica);
        assertTrue(nodoElectronica.isTieneSubcategorias());
        assertEquals(1, nodoElectronica.getSubcategorias().size());
        assertEquals("Computación", nodoElectronica.getSubcategorias().get(0).getNombreCategoria());

        com.sv.grupo1.ecommerce.dto.CategoriaNodoDTO nodoAccesorios = arbol.stream()
                .filter(c -> c.getIdCategoria().equals(3))
                .findFirst()
                .orElse(null);
        assertNotNull(nodoAccesorios);
        assertFalse(nodoAccesorios.isTieneSubcategorias());
    }
}
