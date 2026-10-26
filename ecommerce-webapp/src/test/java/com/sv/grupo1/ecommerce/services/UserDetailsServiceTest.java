package com.sv.grupo1.ecommerce.services;

import com.sv.grupo1.ecommerce.dao.UsuarioRepository;
import com.sv.grupo1.ecommerce.entities.seguridad.Rol;
import com.sv.grupo1.ecommerce.entities.seguridad.Usuario;
import com.sv.grupo1.ecommerce.entities.seguridad.UsuarioRol;
import com.sv.grupo1.ecommerce.services.impl.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserDetailsServiceTest {

    private static final String CORREO = "admin@example.com";

    private UsuarioRepository usuarioRepository;
    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        userDetailsService = new UserDetailsServiceImpl(usuarioRepository);
    }

    @Test
    void debeCargarElUsuarioActivoConTodosSusRoles() {
        Usuario usuario = crearUsuario('A', "ROLE_ADMIN", "ROLE_CLIENTE");
        when(usuarioRepository.findByCorreoInicioSesionIgnoreCase(CORREO)).thenReturn(Optional.of(usuario));

        UserDetails detalles = userDetailsService.loadUserByUsername(CORREO);

        assertEquals(CORREO, detalles.getUsername());
        assertEquals("hash-guardado", detalles.getPassword());
        assertTrue(detalles.isEnabled());
        Set<String> roles = detalles.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        assertEquals(Set.of("ROLE_ADMIN", "ROLE_CLIENTE"), roles);
    }

    @Test
    void debeDeshabilitarLaCuentaDeUnUsuarioInactivo() {
        Usuario usuario = crearUsuario('I', "ROLE_CLIENTE");
        when(usuarioRepository.findByCorreoInicioSesionIgnoreCase(CORREO)).thenReturn(Optional.of(usuario));

        UserDetails detalles = userDetailsService.loadUserByUsername(CORREO);

        assertFalse(detalles.isEnabled());
    }

    @Test
    void noDebeCargarUnUsuarioQueNoExiste() {
        when(usuarioRepository.findByCorreoInicioSesionIgnoreCase(CORREO)).thenReturn(Optional.empty());

        UsernameNotFoundException error = assertThrows(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(CORREO)
        );

        assertEquals("Usuario no encontrado", error.getMessage());
    }

    private Usuario crearUsuario(char estado, String... nombresRoles) {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setCorreoInicioSesion(CORREO);
        usuario.setContrasenia("hash-guardado");
        usuario.setEstado(estado);

        List<UsuarioRol> roles = new ArrayList<>();
        int idRol = 1;
        for (String nombreRol : nombresRoles) {
            Rol rol = new Rol();
            rol.setIdRol(idRol++);
            rol.setNombre(nombreRol);
            roles.add(new UsuarioRol(usuario, rol));
        }
        usuario.setRoles(roles);
        return usuario;
    }
}
