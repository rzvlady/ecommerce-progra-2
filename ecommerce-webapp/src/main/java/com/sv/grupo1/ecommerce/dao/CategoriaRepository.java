package com.sv.grupo1.ecommerce.dao;

import com.sv.grupo1.ecommerce.entities.catalogo.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    List<Categoria> findAllByOrderByNombreCategoriaAsc();

    @Query("SELECT c FROM Categoria c " +
           "LEFT JOIN FETCH c.categoriaPadre p " +
           "LEFT JOIN FETCH p.categoriaPadre " +
           "ORDER BY c.nombreCategoria ASC")
    List<Categoria> findAllConJerarquia();

    @Query("SELECT c FROM Categoria c " +
           "LEFT JOIN FETCH c.categoriaPadre p " +
           "LEFT JOIN FETCH p.categoriaPadre " +
           "WHERE c.idCategoria = :id")
    Optional<Categoria> findByIdConJerarquia(@Param("id") Integer id);

    boolean existsByCategoriaPadre_IdCategoria(Integer idCategoriaPadre);

    long countByCategoriaPadre_IdCategoria(Integer idCategoriaPadre);

    boolean existsByNombreCategoriaIgnoreCase(String nombreCategoria);

    boolean existsByNombreCategoriaIgnoreCaseAndIdCategoriaNot(String nombreCategoria, Integer idCategoria);
}
