package com.sv.grupo1.ecommerce.dao;

import com.sv.grupo1.ecommerce.entities.core.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    boolean existsByCodigoSku(String codigoSku);

    boolean existsByCodigoSkuAndIdProductoNot(String codigoSku, Integer idProducto);

    boolean existsByCategoria_IdCategoria(Integer idCategoria);

    long countByCategoria_IdCategoria(Integer idCategoria);

    @Query("SELECT p FROM Producto p " +
            "JOIN FETCH p.categoria c " +
            "LEFT JOIN FETCH c.categoriaPadre cp " +
            "LEFT JOIN FETCH cp.categoriaPadre " +
            "JOIN FETCH p.marca " +
            "ORDER BY p.fechaCreacion DESC, p.idProducto DESC")
    List<Producto> findAllConRelaciones();

    @Query("SELECT p FROM Producto p " +
            "JOIN FETCH p.categoria c " +
            "LEFT JOIN FETCH c.categoriaPadre cp " +
            "LEFT JOIN FETCH cp.categoriaPadre " +
            "JOIN FETCH p.marca " +
            "WHERE p.idProducto = :id")
    Optional<Producto> findByIdConRelaciones(@Param("id") Integer id);

    @Query("SELECT p FROM Producto p " +
            "JOIN FETCH p.categoria c " +
            "LEFT JOIN FETCH c.categoriaPadre cp " +
            "LEFT JOIN FETCH cp.categoriaPadre " +
            "JOIN FETCH p.marca " +
            "WHERE p.stockMinimo IS NOT NULL AND p.stockDisponible <= p.stockMinimo")
    List<Producto> findProductosConStockBajo();

    @Query("SELECT COALESCE(MAX(p.idProducto), 0) FROM Producto p")
    Integer obtenerMaxIdProducto();
}
