package com.sv.grupo1.ecommerce.services;

import com.sv.grupo1.ecommerce.dao.CategoriaRepository;
import com.sv.grupo1.ecommerce.dao.ProductoRepository;
import com.sv.grupo1.ecommerce.dto.CategoriaDTO;
import com.sv.grupo1.ecommerce.entities.catalogo.Categoria;
import com.sv.grupo1.ecommerce.services.impl.CategoriaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CategoriaServiceTest {

    private CategoriaRepository categoriaRepository;
    private ProductoRepository productoRepository;
    private CategoriaServiceImpl categoriaService;

    @BeforeEach
    void setUp() {
        categoriaRepository = mock(CategoriaRepository.class);
        productoRepository = mock(ProductoRepository.class);
        categoriaService = new CategoriaServiceImpl(categoriaRepository, productoRepository);
    }

    @Test
    void debeCalcularElNivelSegunLaCantidadDePadres() {
        Categoria electronica = crearCategoria(1, "Electrónica", null);
        Categoria computacion = crearCategoria(2, "Computación", electronica);
        Categoria laptops = crearCategoria(3, "Laptops", computacion);

        assertEquals(0, categoriaService.calcularNivel(null));
        assertEquals(1, categoriaService.calcularNivel(electronica));
        assertEquals(2, categoriaService.calcularNivel(computacion));
        assertEquals(3, categoriaService.calcularNivel(laptops));
    }

    @Test
    void debeTerminarElCalculoDeNivelAunqueExistaUnCiclo() {
        Categoria a = crearCategoria(1, "A", null);
        Categoria b = crearCategoria(2, "B", a);
        a.setCategoriaPadre(b);

        int nivel = assertTimeoutPreemptively(Duration.ofSeconds(1), () -> categoriaService.calcularNivel(a));

        assertEquals(2, nivel);
    }

    @Test
    void debeRegistrarUnaCategoriaRaizActiva() {
        CategoriaDTO dto = crearDto(null, "  Hogar  ", null);
        when(categoriaRepository.existsByNombreCategoriaIgnoreCase("Hogar")).thenReturn(false);

        categoriaService.guardarCategoria(dto);

        ArgumentCaptor<Categoria> captor = ArgumentCaptor.forClass(Categoria.class);
        verify(categoriaRepository).save(captor.capture());
        assertEquals("Hogar", captor.getValue().getNombreCategoria());
        assertNull(captor.getValue().getCategoriaPadre());
        assertEquals('A', captor.getValue().getEstado());
    }

    @Test
    void debeRegistrarUnaSubcategoriaBajoUnPadreDeNivelDos() {
        Categoria electronica = crearCategoria(1, "Electrónica", null);
        Categoria computacion = crearCategoria(2, "Computación", electronica);
        when(categoriaRepository.existsByNombreCategoriaIgnoreCase("Tablets")).thenReturn(false);
        when(categoriaRepository.findByIdConJerarquia(2)).thenReturn(Optional.of(computacion));

        categoriaService.guardarCategoria(crearDto(null, "Tablets", 2));

        ArgumentCaptor<Categoria> captor = ArgumentCaptor.forClass(Categoria.class);
        verify(categoriaRepository).save(captor.capture());
        assertSame(computacion, captor.getValue().getCategoriaPadre());
    }

    @Test
    void noDebeRegistrarUnaCategoriaSinNombre() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> categoriaService.guardarCategoria(crearDto(null, " ", null))
        );

        assertEquals("El nombre de la categoría es obligatorio.", error.getMessage());
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void noDebeRegistrarUnaCategoriaConUnNombreQueYaExiste() {
        when(categoriaRepository.existsByNombreCategoriaIgnoreCase("Hogar")).thenReturn(true);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> categoriaService.guardarCategoria(crearDto(null, "Hogar", null))
        );

        assertEquals("Ya existe una categoría registrada con el nombre 'Hogar'.", error.getMessage());
    }

    @Test
    void noDebePermitirQueUnaCategoriaSeaSuPropioPadre() {
        when(categoriaRepository.findByIdConJerarquia(2)).thenReturn(Optional.of(crearCategoria(2, "Computación", null)));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> categoriaService.guardarCategoria(crearDto(2, "Computación", 2))
        );

        assertEquals("Una categoría no puede asignarse a sí misma como categoría padre.", error.getMessage());
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void noDebeAsignarComoPadreAUnaCategoriaDescendiente() {
        Categoria electronica = crearCategoria(1, "Electrónica", null);
        Categoria computacion = crearCategoria(2, "Computación", electronica);
        when(categoriaRepository.findByIdConJerarquia(1)).thenReturn(Optional.of(electronica));
        when(categoriaRepository.findByIdConJerarquia(2)).thenReturn(Optional.of(computacion));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> categoriaService.guardarCategoria(crearDto(1, "Electrónica", 2))
        );

        assertTrue(error.getMessage().startsWith("Referencia circular detectada"));
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void noDebeCrearUnCuartoNivelDeCategorias() {
        Categoria electronica = crearCategoria(1, "Electrónica", null);
        Categoria computacion = crearCategoria(2, "Computación", electronica);
        Categoria laptops = crearCategoria(3, "Laptops", computacion);
        when(categoriaRepository.existsByNombreCategoriaIgnoreCase("Gamer")).thenReturn(false);
        when(categoriaRepository.findByIdConJerarquia(3)).thenReturn(Optional.of(laptops));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> categoriaService.guardarCategoria(crearDto(null, "Gamer", 3))
        );

        assertTrue(error.getMessage().contains("El límite máximo permitido es de 3 niveles"));
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void noDebeMoverUnaCategoriaSiSusHijasSuperanElTercerNivel() {
        Categoria electronica = crearCategoria(1, "Electrónica", null);
        Categoria computacion = crearCategoria(2, "Computación", electronica);
        Categoria ropa = crearCategoria(5, "Ropa", null);
        Categoria camisas = crearCategoria(6, "Camisas", ropa);
        when(categoriaRepository.findByIdConJerarquia(5)).thenReturn(Optional.of(ropa));
        when(categoriaRepository.findByIdConJerarquia(2)).thenReturn(Optional.of(computacion));
        when(categoriaRepository.findAllConJerarquia()).thenReturn(List.of(electronica, computacion, ropa, camisas));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> categoriaService.guardarCategoria(crearDto(5, "Ropa", 2))
        );

        assertTrue(error.getMessage().startsWith("No se puede mover esta categoría al Nivel 3"));
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void noDebeEliminarUnaCategoriaConSubcategorias() {
        when(categoriaRepository.findById(1)).thenReturn(Optional.of(crearCategoria(1, "Electrónica", null)));
        when(categoriaRepository.countByCategoriaPadre_IdCategoria(1)).thenReturn(2L);

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> categoriaService.eliminarCategoria(1)
        );

        assertTrue(error.getMessage().contains("tiene 2 subcategoría(s) dependiente(s)"));
        verify(categoriaRepository, never()).delete(any());
    }

    @Test
    void noDebeEliminarUnaCategoriaConProductos() {
        when(categoriaRepository.findById(3)).thenReturn(Optional.of(crearCategoria(3, "Laptops", null)));
        when(categoriaRepository.countByCategoriaPadre_IdCategoria(3)).thenReturn(0L);
        when(productoRepository.countByCategoria_IdCategoria(3)).thenReturn(7L);

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> categoriaService.eliminarCategoria(3)
        );

        assertTrue(error.getMessage().contains("tiene 7 producto(s) asociado(s)"));
        verify(categoriaRepository, never()).delete(any());
    }

    @Test
    void debeEliminarUnaCategoriaSinDependencias() {
        Categoria categoria = crearCategoria(3, "Laptops", null);
        when(categoriaRepository.findById(3)).thenReturn(Optional.of(categoria));
        when(categoriaRepository.countByCategoriaPadre_IdCategoria(3)).thenReturn(0L);
        when(productoRepository.countByCategoria_IdCategoria(3)).thenReturn(0L);

        categoriaService.eliminarCategoria(3);

        verify(categoriaRepository).delete(categoria);
        verify(categoriaRepository).flush();
    }

    private CategoriaDTO crearDto(Integer idCategoria, String nombre, Integer idCategoriaPadre) {
        CategoriaDTO dto = new CategoriaDTO();
        dto.setIdCategoria(idCategoria);
        dto.setNombreCategoria(nombre);
        dto.setIdCategoriaPadre(idCategoriaPadre);
        return dto;
    }

    private Categoria crearCategoria(Integer idCategoria, String nombre, Categoria padre) {
        Categoria categoria = new Categoria();
        categoria.setIdCategoria(idCategoria);
        categoria.setNombreCategoria(nombre);
        categoria.setCategoriaPadre(padre);
        return categoria;
    }
}
