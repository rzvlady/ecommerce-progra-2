package com.sv.grupo1.ecommerce.dao;

import com.sv.grupo1.ecommerce.entities.seguridad.UsuarioContacto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioContactoRepository extends JpaRepository<UsuarioContacto, Integer> {

    List<UsuarioContacto> findByUsuarioIdUsuario(Integer idUsuario);

    List<UsuarioContacto> findByUsuarioIdUsuarioAndTipoContacto(Integer idUsuario, Character tipoContacto);

    Optional<UsuarioContacto> findFirstByUsuarioIdUsuarioAndTipoContactoAndPrincipalTrue(Integer idUsuario, Character tipoContacto);
}
