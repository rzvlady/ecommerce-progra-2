package com.sv.grupo1.ecommerce.services;

import com.sv.grupo1.ecommerce.dto.CategoriaDTO;
import com.sv.grupo1.ecommerce.entities.catalogo.Categoria;

import java.util.List;

public interface CategoriaService {

    List<CategoriaDTO> listarTodasConNivel();

    List<CategoriaDTO> listarCandidatasPadre();

    CategoriaDTO obtenerDtoPorId(Integer idCategoria);

    int calcularNivel(Categoria categoria);

    void guardarCategoria(CategoriaDTO dto);

    void eliminarCategoria(Integer idCategoria);
}
