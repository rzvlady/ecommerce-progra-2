package com.sv.grupo1.ecommerce.services;

import com.sv.grupo1.backoffice.repository.RolRepository;
import com.sv.grupo1.ecommerce.dto.RolDTO;
import com.sv.grupo1.ecommerce.entities.seguridad.Rol;
import com.sv.grupo1.ecommerce.services.impl.RolServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RolServiceTest {

    private RolRepository rolRepository;
    private RolServiceImpl rolService;

    @BeforeEach
    void setUp() {
        rolRepository = mock(RolRepository.class);
        rolService = new RolServiceImpl(rolRepository);
    }

    @Test
    void normalizaMinusculasYAnteponePrefijoRole() {
        assertEquals("ROLE_AUDITORIA", rolService.normalizarNombreRol("auditoria"));
    }

    @Test
    void noDuplicaPrefijoSiYaVieneConRole() {
        assertEquals("ROLE_EJEMPLO", rolService.normalizarNombreRol("ROLE_EJEMPLO"));
        assertEquals("ROLE_EJEMPLO", rolService.normalizarNombreRol("role_ejemplo"));
    }

    @Test
    void reemplazaAcentosYEliminaCaracteresEspeciales() {
        assertEquals("ROLE_AUDITORIA", rolService.normalizarNombreRol("auditoría"));
        assertEquals("ROLE_GESTION_LOGISTICA_1", rolService.normalizarNombreRol("¡Gestión & Logística #1!"));
        assertEquals("ROLE_SUPERVISOR_DE_CAJA", rolService.normalizarNombreRol("role_supervisor-de-caja@$%"));
    }

    @Test
    void lanzaExcepcionSiSoloContieneCaracteresEspecialesOPrefijoVacio() {
        assertThrows(IllegalArgumentException.class, () -> rolService.normalizarNombreRol("@#$%&*"));
        assertThrows(IllegalArgumentException.class, () -> rolService.normalizarNombreRol("ROLE_"));
        assertThrows(IllegalArgumentException.class, () -> rolService.normalizarNombreRol("role_@#$"));
    }

    @Test
    void guardarNuevoRolNormalizaNombreYPersiste() {
        RolDTO dto = new RolDTO();
        dto.setNombre("auditoría interna!");
        dto.setDescripcion("Rol para revisión de inventario");

        when(rolRepository.findByNombreIgnorandoEliminado("ROLE_AUDITORIA_INTERNA")).thenReturn(Optional.empty());

        String nombreGuardado = rolService.guardarRol(dto);

        assertEquals("ROLE_AUDITORIA_INTERNA", nombreGuardado);
        ArgumentCaptor<Rol> captor = ArgumentCaptor.forClass(Rol.class);
        verify(rolRepository).save(captor.capture());
        assertEquals("ROLE_AUDITORIA_INTERNA", captor.getValue().getNombre());
        assertEquals("Rol para revisión de inventario", captor.getValue().getDescripcion());
        assertFalse(captor.getValue().getEliminado());
    }

    @Test
    void reactivaRolSiYaExistiaConEliminadoLogico() {
        Rol rolEliminado = new Rol();
        rolEliminado.setIdRol(8);
        rolEliminado.setNombre("ROLE_AUDITORIA");
        rolEliminado.setEliminado(true);

        when(rolRepository.findByNombreIgnorandoEliminado("ROLE_AUDITORIA")).thenReturn(Optional.of(rolEliminado));

        RolDTO dto = new RolDTO();
        dto.setNombre("auditoria");
        dto.setDescripcion("Descripción reactivada");

        String resultado = rolService.guardarRol(dto);

        assertEquals("ROLE_AUDITORIA", resultado);
        verify(rolRepository, times(1)).reactivarRol(8, "Descripción reactivada");
        verify(rolRepository, never()).save(any());
    }

    @Test
    void bloqueaEliminacionDeRolSistemaORolConUsuarios() {
        Rol rolAdmin = new Rol();
        rolAdmin.setIdRol(1);
        rolAdmin.setNombre("ROLE_ADMIN");

        when(rolRepository.findById(1)).thenReturn(Optional.of(rolAdmin));

        assertThrows(IllegalStateException.class, () -> rolService.eliminarRol(1));

        Rol rolCustomConUsuarios = new Rol();
        rolCustomConUsuarios.setIdRol(5);
        rolCustomConUsuarios.setNombre("ROLE_AUDITORIA");

        when(rolRepository.findById(5)).thenReturn(Optional.of(rolCustomConUsuarios));
        when(rolRepository.contarUsuariosPorRol(5)).thenReturn(3L);

        assertThrows(IllegalStateException.class, () -> rolService.eliminarRol(5));
        verify(rolRepository, never()).delete(any());
    }
}
