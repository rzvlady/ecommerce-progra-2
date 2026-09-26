package com.sv.grupo1.ecommerce.services;

import com.sv.grupo1.ecommerce.dao.UsuarioRepository;
import com.sv.grupo1.ecommerce.entities.seguridad.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UsuarioServiceTest {

    @Test
    void noDebeRegistrarCorreoRepetido() {

        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);

        when(usuarioRepository.existsByCorreoInicioSesionIgnoreCase("ivan@gmail.com"))
                .thenReturn(true);

        UsuarioService usuarioService = new UsuarioService(usuarioRepository);

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.registrarUsuario("ivan@gmail.com", "123456")
        );

        assertEquals(
                "El correo electrónico ya está registrado.",
                error.getMessage()
        );

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void debeProtegerContrasenaAntesDeGuardar() {

        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);

        when(usuarioRepository.existsByCorreoInicioSesionIgnoreCase("ivan@gmail.com"))
                .thenReturn(false);

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        UsuarioService usuarioService = new UsuarioService(usuarioRepository);

        Usuario usuario = usuarioService.registrarUsuario(
                "IVAN@gmail.com",
                "123456"
        );

        assertEquals("ivan@gmail.com", usuario.getCorreoInicioSesion());

        assertNotEquals(
                "123456",
                usuario.getContrasenia()
        );

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        assertTrue(
                encoder.matches("123456", usuario.getContrasenia())
        );
    }
}
