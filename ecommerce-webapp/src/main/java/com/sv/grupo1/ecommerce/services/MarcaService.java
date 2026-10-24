package com.sv.grupo1.ecommerce.services;

import com.sv.grupo1.ecommerce.dto.MarcaDTO;

import java.util.List;

public interface MarcaService {

    List<MarcaDTO> listarTodasConConteo();

    MarcaDTO obtenerDtoPorId(Integer idMarca);

    void guardarMarca(MarcaDTO dto);

    void cambiarEstado(Integer idMarca);

    void eliminarMarca(Integer idMarca);
}
