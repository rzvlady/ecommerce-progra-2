package com.sv.grupo1.ecommerce.dao;

import com.sv.grupo1.ecommerce.entities.core.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
    List<Categoria> findAllByOrderByNombreCategoriaAsc();
}
