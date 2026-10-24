package com.sv.grupo1.ecommerce.services;

import com.sv.grupo1.ecommerce.dto.RegistroUsuarioDTO;
import com.sv.grupo1.ecommerce.dto.UsuarioPerfilCompletoDTO;
import com.sv.grupo1.ecommerce.entities.seguridad.Usuario;

/*
 * @author alexemestica
 * */
public interface UsuarioService {

    Usuario registrarUsuario(RegistroUsuarioDTO dto);

    void registrarUltimoInicioSesion(String correoInicioSesion);

    UsuarioPerfilCompletoDTO obtenerPerfilCompletoPorCorreo(String correoInicioSesion);

    UsuarioPerfilCompletoDTO obtenerPerfilCompletoPorIdUsuario(Integer idUsuario);

    void guardarPerfilCompleto(String correoAutenticado, UsuarioPerfilCompletoDTO dto, String origen);

    void guardarPerfilCompletoPorIdUsuario(Integer idUsuario, UsuarioPerfilCompletoDTO dto, String usuarioAuditor, String origen);

}
