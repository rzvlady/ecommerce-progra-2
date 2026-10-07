package com.sv.grupo1.ecommerce.services.impl;

import com.sv.grupo1.ecommerce.dao.UsuarioRepository;
import com.sv.grupo1.ecommerce.dto.RegistroUsuarioDTO;
import com.sv.grupo1.ecommerce.entities.seguridad.Usuario;
import com.sv.grupo1.ecommerce.entities.seguridad.UsuarioContacto;
import com.sv.grupo1.ecommerce.enums.EstadoEstandarGlobal;
import com.sv.grupo1.ecommerce.services.UsuarioService;
import com.sv.grupo1.ecommerce.utils.Constantes;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/*
 * @author ivandiaz-007
 * */
@Service
public class UsuarioServiceImpl implements UsuarioService {

    private static final Logger LOG = LoggerFactory.getLogger(UsuarioServiceImpl.class);
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder encoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder encoder) {
        this.usuarioRepository = usuarioRepository;
        this.encoder = encoder;
    }

    @Transactional
    public Usuario registrarUsuario(RegistroUsuarioDTO dto) {
        LOG.info("registrarUsuario() => Iniciando");
        String correoNormalizado = dto.getCorreoInicioSesion().trim();
        if (usuarioRepository.existsByCorreoInicioSesionIgnoreCase(correoNormalizado)) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado.");
        }
        dto.setCorreoInicioSesion(correoNormalizado);
        LOG.info("registrarUsuario() => Mapeando...");
        Usuario usuario = mapperToUsuario(dto);
        LOG.info("registrarUsuario() => Completado");
        return usuarioRepository.save(usuario);
    }

    public Usuario mapperToUsuario(RegistroUsuarioDTO dto) {
        EstadoEstandarGlobal estandarGlobal = EstadoEstandarGlobal.ACTIVO;

        Usuario usuario = new Usuario();
        usuario.setCorreoInicioSesion(dto.getCorreoInicioSesion());
        usuario.setContrasenia(this.encoder.encode(dto.getContrasenia().trim()));
        usuario.setEstado(estandarGlobal.getCodigoEstado());

        UsuarioContacto  usuarioContacto = new UsuarioContacto();
        usuarioContacto.setUsuario(usuario);
        usuarioContacto.setEstado(String.valueOf(estandarGlobal.getCodigoEstado()));
        usuarioContacto.setVerificado(false);
        usuarioContacto.setValor(dto.getCorreoInicioSesion());
        usuarioContacto.setTipoContacto(Constantes.TIPO_CONTACTO_CORREO);

        if (usuario.getContactos() == null) {
            usuario.setContactos(new ArrayList<>());
        }
        usuario.getContactos().add(usuarioContacto);

        return usuario;
    }
}
