package com.sv.grupo1.ecommerce.dao;

import com.sv.grupo1.ecommerce.entities.core.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Integer> {
    Optional<Pago> findFirstByPedido_IdPedidoOrderByIdPagoDesc(Integer idPedido);
}
