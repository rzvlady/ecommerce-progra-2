package com.sv.grupo1.ecommerce.services;

import com.sv.grupo1.ecommerce.dao.UsuarioRepository;
import com.sv.grupo1.ecommerce.entities.seguridad.Usuario;
import com.sv.grupo1.ecommerce.exceptions.CredencialesInvalidasException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    private static final String CORREO = "cliente@example.com";
    private static final String MENSAJE_CREDENCIALES = "Correo o contraseña incorrectos";

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void debeIniciarSesionYRegistrarLaFechaDelUltimoInicio() {
        Usuario usuario = crearUsuario();
        when(usuarioRepository.findByCorreoInicioSesion(CORREO)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("clave-correcta", "hash-guardado")).thenReturn(true);
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        Usuario resultado = authService.login(CORREO, "clave-correcta");

        assertSame(usuario, resultado);
        assertNotNull(usuario.getUltimoInicioSesion());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void noDebeIniciarSesionConUnCorreoQueNoExiste() {
        when(usuarioRepository.findByCorreoInicioSesion(CORREO)).thenReturn(Optional.empty());

        CredencialesInvalidasException error = assertThrows(
                CredencialesInvalidasException.class,
                () -> authService.login(CORREO, "cualquier-clave")
        );

        assertEquals(MENSAJE_CREDENCIALES, error.getMessage());
        verifyNoInteractions(passwordEncoder);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void noDebeIniciarSesionConUnaContraseniaIncorrectaYDebeUsarElMismoMensaje() {
        when(usuarioRepository.findByCorreoInicioSesion(CORREO)).thenReturn(Optional.of(crearUsuario()));
        when(passwordEncoder.matches("clave-incorrecta", "hash-guardado")).thenReturn(false);

        CredencialesInvalidasException error = assertThrows(
                CredencialesInvalidasException.class,
                () -> authService.login(CORREO, "clave-incorrecta")
        );

        assertEquals(MENSAJE_CREDENCIALES, error.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void debeGenerarUnTokenDeRecuperacionParaUnCorreoRegistrado() {
        Usuario usuario = crearUsuario();
        when(usuarioRepository.findByCorreoInicioSesion(CORREO)).thenReturn(Optional.of(usuario));

        String token = authService.solicitarRecuperacion(CORREO);

        assertNotNull(token);
        assertDoesNotThrow(() -> UUID.fromString(token));
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void noDebeGenerarTokenDeRecuperacionParaUnCorreoQueNoExiste() {
        when(usuarioRepository.findByCorreoInicioSesion(CORREO)).thenReturn(Optional.empty());

        CredencialesInvalidasException error = assertThrows(
                CredencialesInvalidasException.class,
                () -> authService.solicitarRecuperacion(CORREO)
        );

        assertEquals("No existe una cuenta con ese correo", error.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    private Usuario crearUsuario() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(1);
        usuario.setCorreoInicioSesion(CORREO);
        usuario.setContrasenia("hash-guardado");
        return usuario;
    }
}
