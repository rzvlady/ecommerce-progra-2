package com.sv.grupo1.backoffice.service;

import com.sv.grupo1.backoffice.dto.UsuarioRolAsignacionDTO;
import com.sv.grupo1.backoffice.repository.RolRepository;
import com.sv.grupo1.backoffice.repository.UsuarioRolRepository;
import com.sv.grupo1.backoffice.service.impl.BackofficeUsuarioRolServiceImpl;
import com.sv.grupo1.ecommerce.entities.seguridad.Rol;
import com.sv.grupo1.ecommerce.entities.seguridad.Usuario;
import com.sv.grupo1.ecommerce.entities.seguridad.UsuarioRol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BackofficeUsuarioRolServiceTest {

    private UsuarioRolRepository usuarioRolRepository;
    private RolRepository rolRepository;
    private BackofficeUsuarioRolServiceImpl service;

    @BeforeEach
    void setUp() {
        usuarioRolRepository = mock(UsuarioRolRepository.class);
        rolRepository = mock(RolRepository.class);
        service = new BackofficeUsuarioRolServiceImpl(usuarioRolRepository, rolRepository);
    }

    @Test
    void cambiarRolReemplazaRolAnteriorYRegistraAuditoriaJsonb() {
        Rol rolCliente = new Rol();
        rolCliente.setIdRol(3);
        rolCliente.setNombre("ROLE_CLIENTE");

        Rol rolVendedor = new Rol();
        rolVendedor.setIdRol(2);
        rolVendedor.setNombre("ROLE_VENDEDOR");

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(15);
        usuario.setCorreoInicioSesion("usuario@tienda.com");

        UsuarioRol asignacionActual = new UsuarioRol(usuario, rolCliente);
        usuario.setRoles(List.of(asignacionActual));

        when(usuarioRolRepository.buscarUsuarioConRolesPorId(15)).thenReturn(Optional.of(usuario));
        when(rolRepository.findById(2)).thenReturn(Optional.of(rolVendedor));

        UsuarioRolAsignacionDTO dto = new UsuarioRolAsignacionDTO();
        dto.setIdUsuario(15);
        dto.setIdRol(2);
        dto.setMotivo("Promoción a ejecutivo de ventas");

        service.cambiarRolUsuario(dto, "admin@tienda.com");

        verify(usuarioRolRepository, times(1)).eliminarRolesPorIdUsuario(15);
        verify(usuarioRolRepository, times(1)).flush();

        ArgumentCaptor<UsuarioRol> captor = ArgumentCaptor.forClass(UsuarioRol.class);
        verify(usuarioRolRepository, times(1)).save(captor.capture());

        UsuarioRol guardado = captor.getValue();
        assertEquals(2, guardado.getRol().getIdRol());
        assertNotNull(guardado.getAuditoria());
        assertEquals("cambio_rol_backoffice", guardado.getAuditoria().get("operacion"));
        assertEquals("admin@tienda.com", guardado.getAuditoria().get("asignado_por"));
        assertEquals("ROLE_CLIENTE", guardado.getAuditoria().get("rol_anterior"));
        assertEquals("ROLE_VENDEDOR", guardado.getAuditoria().get("rol_nuevo"));
        assertEquals("Promoción a ejecutivo de ventas", guardado.getAuditoria().get("motivo"));
        assertNotNull(guardado.getAuditoria().get("fecha_modificacion"));
    }

    @Test
    void bloqueaAutorevocacionDeRolAdminEnSesionActiva() {
        Rol rolAdmin = new Rol();
        rolAdmin.setIdRol(1);
        rolAdmin.setNombre("ROLE_ADMIN");

        Rol rolCliente = new Rol();
        rolCliente.setIdRol(3);
        rolCliente.setNombre("ROLE_CLIENTE");

        Usuario adminEnSesion = new Usuario();
        adminEnSesion.setIdUsuario(1);
        adminEnSesion.setCorreoInicioSesion("admin@tienda.com");
        adminEnSesion.setRoles(List.of(new UsuarioRol(adminEnSesion, rolAdmin)));

        when(usuarioRolRepository.buscarUsuarioConRolesPorId(1)).thenReturn(Optional.of(adminEnSesion));
        when(rolRepository.findById(3)).thenReturn(Optional.of(rolCliente));

        UsuarioRolAsignacionDTO dto = new UsuarioRolAsignacionDTO();
        dto.setIdUsuario(1);
        dto.setIdRol(3);
        dto.setMotivo("Prueba de cambio propio");

        assertThrows(IllegalStateException.class, () -> service.cambiarRolUsuario(dto, "admin@tienda.com"));
        verify(usuarioRolRepository, never()).eliminarRolesPorIdUsuario(anyInt());
    }

    @Test
    void bloqueaAsignarElMismoRolQueYaPoseeElUsuario() {
        Rol rolCliente = new Rol();
        rolCliente.setIdRol(3);
        rolCliente.setNombre("ROLE_CLIENTE");

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(20);
        usuario.setCorreoInicioSesion("cliente@tienda.com");
        usuario.setRoles(List.of(new UsuarioRol(usuario, rolCliente)));

        when(usuarioRolRepository.buscarUsuarioConRolesPorId(20)).thenReturn(Optional.of(usuario));
        when(rolRepository.findById(3)).thenReturn(Optional.of(rolCliente));

        UsuarioRolAsignacionDTO dto = new UsuarioRolAsignacionDTO();
        dto.setIdUsuario(20);
        dto.setIdRol(3);
        dto.setMotivo("Reasignar mismo rol");

        assertThrows(IllegalArgumentException.class, () -> service.cambiarRolUsuario(dto, "admin@tienda.com"));
        verify(usuarioRolRepository, never()).eliminarRolesPorIdUsuario(anyInt());
    }
}
