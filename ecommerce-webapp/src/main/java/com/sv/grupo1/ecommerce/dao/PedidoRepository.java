package com.sv.grupo1.ecommerce.dao;

import com.sv.grupo1.ecommerce.entities.core.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    List<Pedido> findByUsuario_IdUsuarioOrderByIdPedidoDesc(Integer idUsuario);

    @Query("SELECT DISTINCT p FROM Pedido p " +
            "JOIN FETCH p.usuario u " +
            "LEFT JOIN FETCH u.perfil " +
            "LEFT JOIN FETCH p.direccion d " +
            "LEFT JOIN FETCH d.municipio m " +
            "LEFT JOIN FETCH m.departamento " +
            "ORDER BY p.idPedido DESC")
    List<Pedido> findAllConRelaciones();

    long countByEstadoPedido(Character estadoPedido);

    @Query("SELECT COALESCE(SUM(p.total), 0) FROM Pedido p " +
            "WHERE p.estadoPedido IN ('C', 'E', 'F') " +
            "AND p.fechaCreacion >= :inicioDia")
    BigDecimal calcularVentasDesde(@Param("inicioDia") OffsetDateTime inicioDia);

    @Query("SELECT COALESCE(SUM(p.total), 0) FROM Pedido p WHERE p.estadoPedido IN ('C', 'E', 'F')")
    BigDecimal calcularTotalVentasAprobadas();
}
