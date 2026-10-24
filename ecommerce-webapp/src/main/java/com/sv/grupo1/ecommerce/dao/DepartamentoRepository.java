package com.sv.grupo1.ecommerce.dao;

import com.sv.grupo1.ecommerce.entities.catalogo.Departamento;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartamentoRepository extends JpaRepository<Departamento, Integer> {

    @EntityGraph(attributePaths = {"pais"})
    List<Departamento> findAllByOrderByNombreDepartamentoAsc();
}
