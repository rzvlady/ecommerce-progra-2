package com.sv.grupo1.ecommerce.dao;

import com.sv.grupo1.ecommerce.entities.core.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Integer> {

    @Query("SELECT m FROM MovimientoInventario m " +
            "JOIN FETCH m.producto p " +
            "JOIN FETCH p.categoria c " +
            "LEFT JOIN FETCH c.categoriaPadre cp " +
            "LEFT JOIN FETCH cp.categoriaPadre " +
            "JOIN FETCH p.marca " +
            "ORDER BY m.fechaMovimiento DESC, m.idMovimiento DESC")
    List<MovimientoInventario> findAllConProducto();

    @Query("SELECT m FROM MovimientoInventario m " +
            "JOIN FETCH m.producto p " +
            "JOIN FETCH p.categoria c " +
            "LEFT JOIN FETCH c.categoriaPadre cp " +
            "LEFT JOIN FETCH cp.categoriaPadre " +
            "JOIN FETCH p.marca " +
            "WHERE p.idProducto = :idProducto " +
            "ORDER BY m.fechaMovimiento DESC, m.idMovimiento DESC")
    List<MovimientoInventario> findByIdProductoConRelaciones(@Param("idProducto") Integer idProducto);

    long countByTipoMovimiento(String tipoMovimiento);
}
