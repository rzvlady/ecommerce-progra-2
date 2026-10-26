package com.sv.grupo1.ecommerce.services;

import com.sv.grupo1.ecommerce.dto.RolDTO;

import java.util.List;

public interface RolService {

    List<RolDTO> listarTodos();

    RolDTO obtenerDtoPorId(Integer idRol);

    String normalizarNombreRol(String nombre);

    boolean esRolSistema(String nombreRol);

    String guardarRol(RolDTO dto);

    void eliminarRol(Integer idRol);
}
