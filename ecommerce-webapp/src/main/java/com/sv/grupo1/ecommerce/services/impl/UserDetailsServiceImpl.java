package com.sv.grupo1.ecommerce.services.impl;

import com.sv.grupo1.ecommerce.dao.UsuarioRepository;
import com.sv.grupo1.ecommerce.entities.seguridad.Usuario;
import com.sv.grupo1.ecommerce.enums.EstadoEstandarGlobal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/*
 * @author alexemestica
 * */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger LOG = LoggerFactory.getLogger(UserDetailsServiceImpl.class);
    private final UsuarioRepository usuarioRepository;

    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LOG.info("loadUserByUsername() =>  Iniciando");
        LOG.info("loadUserByUsername() =>  Correo: \"{}\"", username);

        LOG.info("loadUserByUsername() =>  Buscando Usuario...");
        Usuario usuario = usuarioRepository
            .findByCorreoInicioSesionIgnoreCase(username)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        LOG.info("loadUserByUsername() =>  Validando Estado Usuario...");
        boolean cuentaActiva = usuario.getEstado().equals(EstadoEstandarGlobal.ACTIVO.getCodigoEstado());

        LOG.info("loadUserByUsername() =>  Completado");
        return User.builder()
            .username(usuario.getCorreoInicioSesion())
            .password(usuario.getContrasenia())
            .roles("CLIENTE")
            .disabled(!cuentaActiva)
            .build();
    }

}
