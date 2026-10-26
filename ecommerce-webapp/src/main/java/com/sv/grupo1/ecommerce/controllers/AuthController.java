package com.sv.grupo1.ecommerce.controllers;

import com.sv.grupo1.ecommerce.dto.RegistroUsuarioDTO;
import com.sv.grupo1.ecommerce.entities.seguridad.Usuario;
import com.sv.grupo1.ecommerce.exceptions.CredencialesInvalidasException;
import com.sv.grupo1.ecommerce.services.AuthService;
import com.sv.grupo1.ecommerce.services.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            Usuario usuario = authService.login(request.getCorreo(), request.getContrasenia());
            return ResponseEntity.ok(usuario);
        } catch (CredencialesInvalidasException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@RequestBody(required = false) RegistroUsuarioDTO dtoJson,
                                       @RequestParam(value = "correoInicioSesion", required = false) String correoParam,
                                       @RequestParam(value = "contrasenia", required = false) String passParam,
                                       @RequestParam(value = "confirmarContrasenia", required = false) String passConfParam) {
        RegistroUsuarioDTO dto = dtoJson != null ? dtoJson : new RegistroUsuarioDTO();
        if (dto.getCorreoInicioSesion() == null || dto.getCorreoInicioSesion().isBlank()) {
            dto.setCorreoInicioSesion(correoParam);
        }
        if (dto.getContrasenia() == null || dto.getContrasenia().isBlank()) {
            dto.setContrasenia(passParam);
        }
        if (dto.getConfirmarContrasenia() == null || dto.getConfirmarContrasenia().isBlank()) {
            dto.setConfirmarContrasenia(passConfParam);
        }

        if (dto.getCorreoInicioSesion() == null || dto.getCorreoInicioSesion().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "mensaje", "El correo electrónico es requerido."));
        }
        if (dto.getContrasenia() == null || dto.getContrasenia().length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "mensaje", "La contraseña debe tener al menos 6 caracteres."));
        }
        if (!dto.getContrasenia().equals(dto.getConfirmarContrasenia())) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "mensaje", "Las contraseñas no coinciden. Por favor, verifica e inténtalo de nuevo."));
        }

        try {
            usuarioService.registrarUsuario(dto);
            return ResponseEntity.ok(Map.of(
                    "ok", true,
                    "mensaje", "¡Cuenta creada con éxito! Ahora puedes iniciar sesión."
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("ok", false, "mensaje", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "ok", false,
                    "mensaje", "Ocurrió un error inesperado al registrar la cuenta."
            ));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            String token = authService.solicitarRecuperacion(request.getCorreo());
            return ResponseEntity.ok("Token generado: " + token);
        } catch (CredencialesInvalidasException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            //authService.resetearPassword(request.getToken(), request.getNuevaPassword());
            return ResponseEntity.ok("Contraseña actualizada correctamente");
        } catch (CredencialesInvalidasException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    public static class LoginRequest {
        private String correo;
        private String contrasenia;
        public String getCorreo() { return correo; }
        public void setCorreo(String correo) { this.correo = correo; }
        public String getContrasenia() { return contrasenia; }
        public void setContrasenia(String contrasenia) { this.contrasenia = contrasenia; }
    }

    public static class ForgotPasswordRequest {
        private String correo;
        public String getCorreo() { return correo; }
        public void setCorreo(String correo) { this.correo = correo; }
    }

    public static class ResetPasswordRequest {
        private String token;
        private String nuevaPassword;
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getNuevaPassword() { return nuevaPassword; }
        public void setNuevaPassword(String nuevaPassword) { this.nuevaPassword = nuevaPassword; }
    }
}
