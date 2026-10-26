package com.sv.grupo1.ecommerce.services;

import com.sv.grupo1.ecommerce.dao.UsuarioRepository;
import com.sv.grupo1.ecommerce.entities.seguridad.Usuario;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public Usuario registrarUsuario(String correo, String contrasenia) {

        String correoNormalizado = correo.trim().toLowerCase();

        if (usuarioRepository.existsByCorreoInicioSesionIgnoreCase(correoNormalizado)) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado.");
        }

        Usuario usuario = new Usuario();

        usuario.setCorreoInicioSesion(correoNormalizado);
        usuario.setContrasenia(passwordEncoder.encode(contrasenia));
        usuario.setEliminado(false);
        usuario.setEstado('A');

        return usuarioRepository.save(usuario);
    }
}
