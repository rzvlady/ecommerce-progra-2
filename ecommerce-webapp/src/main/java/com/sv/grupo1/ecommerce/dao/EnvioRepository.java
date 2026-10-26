package com.sv.grupo1.ecommerce.dao;

import com.sv.grupo1.ecommerce.entities.core.Envio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EnvioRepository extends JpaRepository<Envio, Integer> {
    Optional<Envio> findByPedido_IdPedido(Integer idPedido);
}
