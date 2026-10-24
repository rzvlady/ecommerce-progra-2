package com.sv.grupo1.ecommerce.dao;

import com.sv.grupo1.ecommerce.entities.seguridad.UsuarioDireccion;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UsuarioDireccionRepository extends JpaRepository<UsuarioDireccion, Integer> {

    @EntityGraph(attributePaths = {"municipio", "municipio.departamento", "municipio.departamento.pais"})
    List<UsuarioDireccion> findByUsuarioIdUsuarioOrderByIdDireccionAsc(Integer idUsuario);
}
