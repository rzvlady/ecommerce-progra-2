package com.sv.grupo1.ecommerce.services;

import com.sv.grupo1.ecommerce.dao.UsuarioRepository;
import com.sv.grupo1.ecommerce.entities.seguridad.Usuario;
import com.sv.grupo1.ecommerce.exceptions.CredencialesInvalidasException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Usuario login(String correo, String contrasenia) {
        Usuario usuario = usuarioRepository.findByCorreoInicioSesion(correo)
                .orElseThrow(() -> new CredencialesInvalidasException("Correo o contraseña incorrectos"));

        if (!passwordEncoder.matches(contrasenia, usuario.getContrasenia())) {
            throw new CredencialesInvalidasException("Correo o contraseña incorrectos");
        }

        return usuario;
    }

    public String solicitarRecuperacion(String correo) {
        Usuario usuario = usuarioRepository.findByCorreoInicioSesion(correo)
                .orElseThrow(() -> new CredencialesInvalidasException("No existe una cuenta con ese correo"));

        String token = UUID.randomUUID().toString();
        //usuario.setTokenReinicio(token);
        //usuario.setExpiracionToken(LocalDateTime.now().plusMinutes(15));
        usuarioRepository.save(usuario);

        return token;
    }

    /*public void resetearPassword(String token, String nuevaPassword) {
        Usuario usuario = usuarioRepository.findByTokenReinicio(token)
                .orElseThrow(() -> new CredencialesInvalidasException("Token inválido"));

        if (usuario.getExpiracionToken().isBefore(LocalDateTime.now())) {
            throw new CredencialesInvalidasException("El token ha expirado");
        }

        usuario.setContrasenia(passwordEncoder.encode(nuevaPassword));
        usuario.setTokenReinicio(null);
        usuario.setExpiracionToken(null);
        usuarioRepository.save(usuario);
    }*/

    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }
}