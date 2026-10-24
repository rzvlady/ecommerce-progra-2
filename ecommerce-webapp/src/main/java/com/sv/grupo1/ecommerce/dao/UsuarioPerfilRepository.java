package com.sv.grupo1.ecommerce.dao;

import com.sv.grupo1.ecommerce.entities.seguridad.UsuarioPerfil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioPerfilRepository extends JpaRepository<UsuarioPerfil, Integer> {

    Optional<UsuarioPerfil> findByUsuarioIdUsuario(Integer idUsuario);

    boolean existsByDuiIgnoreCase(String dui);

    boolean existsByDuiIgnoreCaseAndUsuarioIdUsuarioNot(String dui, Integer idUsuario);
}
