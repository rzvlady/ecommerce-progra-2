package com.sv.grupo1.backoffice.service;

import com.sv.grupo1.backoffice.dto.UsuarioRolAsignacionDTO;

import java.util.List;

public interface BackofficeUsuarioRolService {

    List<UsuarioRolAsignacionDTO> listarUsuariosConRol();

    UsuarioRolAsignacionDTO obtenerPorIdUsuario(Integer idUsuario);

    void cambiarRolUsuario(UsuarioRolAsignacionDTO dto, String correoAdminAutenticado);
}
