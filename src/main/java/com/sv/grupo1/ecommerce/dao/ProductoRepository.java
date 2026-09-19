package com.sv.grupo1.ecommerce.dao;

import com.sv.grupo1.ecommerce.entities.core.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {
    boolean existsByCodigoSku(String codigoSku);

    @Query("SELECT p FROM Producto p JOIN FETCH p.categoria JOIN FETCH p.marca " +
            "WHERE p.stockMinimo IS NOT NULL AND p.stockDisponible <= p.stockMinimo")
    List<Producto> findProductosConStockBajo();
}