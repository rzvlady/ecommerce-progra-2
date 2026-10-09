package com.sv.grupo1.ecommerce.dao;

import com.sv.grupo1.ecommerce.entities.core.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetallePedidoRepository /*extends JpaRepository<DetallePedido, Integer>*/ {
    List<DetallePedido> findByPedido_IdPedido(Integer idPedido);
}