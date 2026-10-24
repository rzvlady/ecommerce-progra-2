package com.sv.grupo1.ecommerce.dao;

import com.sv.grupo1.ecommerce.entities.catalogo.Municipio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MunicipioRepository extends JpaRepository<Municipio, Integer> {
    List<Municipio> findAllByOrderByNombreMunicipioAsc();
}
