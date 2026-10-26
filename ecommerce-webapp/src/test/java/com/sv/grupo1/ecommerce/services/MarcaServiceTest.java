package com.sv.grupo1.ecommerce.services;

import com.sv.grupo1.ecommerce.dao.MarcaRepository;
import com.sv.grupo1.ecommerce.dao.ProductoRepository;
import com.sv.grupo1.ecommerce.dto.MarcaDTO;
import com.sv.grupo1.ecommerce.entities.catalogo.Marca;
import com.sv.grupo1.ecommerce.services.impl.MarcaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MarcaServiceTest {

    private MarcaRepository marcaRepository;
    private ProductoRepository productoRepository;
    private MarcaServiceImpl marcaService;

    @BeforeEach
    void setUp() {
        marcaRepository = mock(MarcaRepository.class);
        productoRepository = mock(ProductoRepository.class);
        marcaService = new MarcaServiceImpl(marcaRepository, productoRepository);
    }

    @Test
    void debeRegistrarUnaMarcaNuevaActivaConElNombreSinEspacios() {
        MarcaDTO dto = crearDto(null, "  Samsung  ");
        dto.setDescripcion("   ");
        when(marcaRepository.existsByNombreMarcaIgnoreCase("Samsung")).thenReturn(false);

        marcaService.guardarMarca(dto);

        ArgumentCaptor<Marca> captor = ArgumentCaptor.forClass(Marca.class);
        verify(marcaRepository).save(captor.capture());
        assertEquals("Samsung", captor.getValue().getNombreMarca());
        assertNull(captor.getValue().getDescripcion());
        assertEquals('A', captor.getValue().getEstado());
    }

    @Test
    void noDebeRegistrarUnaMarcaSinNombre() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> marcaService.guardarMarca(crearDto(null, "   "))
        );

        assertEquals("El nombre de la marca es obligatorio.", error.getMessage());
        verify(marcaRepository, never()).save(any());
    }

    @Test
    void noDebeRegistrarUnaMarcaConUnNombreQueYaExiste() {
        when(marcaRepository.existsByNombreMarcaIgnoreCase("Samsung")).thenReturn(true);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> marcaService.guardarMarca(crearDto(null, "Samsung"))
        );

        assertEquals("Ya existe una marca registrada con el nombre 'Samsung'.", error.getMessage());
        verify(marcaRepository, never()).save(any());
    }

    @Test
    void noDebeEditarUnaMarcaConElNombreDeOtra() {
        when(marcaRepository.findById(5)).thenReturn(Optional.of(crearMarca(5, "Sony", 'A')));
        when(marcaRepository.existsByNombreMarcaIgnoreCaseAndIdMarcaNot("Samsung", 5)).thenReturn(true);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> marcaService.guardarMarca(crearDto(5, "Samsung"))
        );

        assertEquals("Ya existe otra marca registrada con el nombre 'Samsung'.", error.getMessage());
        verify(marcaRepository, never()).save(any());
    }

    @Test
    void debeEditarUnaMarcaConservandoSuEstado() {
        Marca marca = crearMarca(5, "Sony", 'I');
        when(marcaRepository.findById(5)).thenReturn(Optional.of(marca));
        when(marcaRepository.existsByNombreMarcaIgnoreCaseAndIdMarcaNot("Sony Electronics", 5)).thenReturn(false);

        marcaService.guardarMarca(crearDto(5, "Sony Electronics"));

        verify(marcaRepository).save(marca);
        assertEquals("Sony Electronics", marca.getNombreMarca());
        assertEquals('I', marca.getEstado());
    }

    @Test
    void debeAlternarElEstadoDeLaMarca() {
        Marca marca = crearMarca(5, "Sony", 'A');
        when(marcaRepository.findById(5)).thenReturn(Optional.of(marca));

        marcaService.cambiarEstado(5);
        assertEquals('I', marca.getEstado());

        marcaService.cambiarEstado(5);
        assertEquals('A', marca.getEstado());
        verify(marcaRepository, times(2)).save(marca);
    }

    @Test
    void noDebeEliminarUnaMarcaConProductosVinculados() {
        when(marcaRepository.findById(5)).thenReturn(Optional.of(crearMarca(5, "Sony", 'A')));
        when(productoRepository.countByMarca_IdMarca(5)).thenReturn(3L);

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> marcaService.eliminarMarca(5)
        );

        assertTrue(error.getMessage().contains("tiene 3 producto(s) vinculado(s)"));
        verify(marcaRepository, never()).delete(any());
    }

    @Test
    void debeEliminarUnaMarcaSinProductos() {
        Marca marca = crearMarca(5, "Sony", 'A');
        when(marcaRepository.findById(5)).thenReturn(Optional.of(marca));
        when(productoRepository.countByMarca_IdMarca(5)).thenReturn(0L);

        marcaService.eliminarMarca(5);

        verify(marcaRepository).delete(marca);
        verify(marcaRepository).flush();
    }

    @Test
    void debeExplicarElErrorCuandoLaBaseRechazaLaEliminacion() {
        Marca marca = crearMarca(5, "Sony", 'A');
        when(marcaRepository.findById(5)).thenReturn(Optional.of(marca));
        when(productoRepository.countByMarca_IdMarca(5)).thenReturn(0L);
        doThrow(new DataIntegrityViolationException("fk_productos_to_marcas")).when(marcaRepository).flush();

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> marcaService.eliminarMarca(5)
        );

        assertEquals("No es posible eliminar la marca 'Sony' debido a registros asociados en el sistema.",
                error.getMessage());
    }

    @Test
    void noDebeBuscarUnaMarcaSinId() {
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> marcaService.obtenerDtoPorId(null)
        );

        assertEquals("El ID de la marca es obligatorio.", error.getMessage());
        verifyNoInteractions(marcaRepository);
    }

    @Test
    void debeListarLasMarcasConSuCantidadDeProductos() {
        when(marcaRepository.findAllByOrderByNombreMarcaAsc())
                .thenReturn(List.of(crearMarca(1, "Apple", 'A'), crearMarca(2, "Sony", 'I')));
        when(productoRepository.countByMarca_IdMarca(1)).thenReturn(4L);
        when(productoRepository.countByMarca_IdMarca(2)).thenReturn(0L);

        List<MarcaDTO> marcas = marcaService.listarTodasConConteo();

        assertEquals(2, marcas.size());
        assertEquals("Apple", marcas.get(0).getNombreMarca());
        assertEquals(4L, marcas.get(0).getCantidadProductos());
        assertEquals('I', marcas.get(1).getEstado());
    }

    private MarcaDTO crearDto(Integer idMarca, String nombre) {
        MarcaDTO dto = new MarcaDTO();
        dto.setIdMarca(idMarca);
        dto.setNombreMarca(nombre);
        return dto;
    }

    private Marca crearMarca(Integer idMarca, String nombre, char estado) {
        Marca marca = new Marca();
        marca.setIdMarca(idMarca);
        marca.setNombreMarca(nombre);
        marca.setEstado(estado);
        return marca;
    }
}
