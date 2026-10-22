package com.sv.grupo1.ecommerce.dao;

import com.sv.grupo1.ecommerce.entities.catalogo.Marca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MarcaRepository extends JpaRepository<Marca, Integer> {
    List<Marca> findAllByOrderByNombreMarcaAsc();
}
