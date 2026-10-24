package com.sv.grupo1.ecommerce.dao;

import com.sv.grupo1.ecommerce.entities.catalogo.Municipio;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MunicipioRepository extends JpaRepository<Municipio, Integer> {

    @EntityGraph(attributePaths = {"departamento"})
    List<Municipio> findAllByOrderByNombreMunicipioAsc();

    @EntityGraph(attributePaths = {"departamento"})
    List<Municipio> findByDepartamentoIdDepartamentoOrderByNombreMunicipioAsc(Integer idDepartamento);
}
