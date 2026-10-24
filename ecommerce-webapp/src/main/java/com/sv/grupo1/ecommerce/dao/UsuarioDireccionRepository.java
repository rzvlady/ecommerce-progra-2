package com.sv.grupo1.ecommerce.dao;

import com.sv.grupo1.ecommerce.entities.seguridad.UsuarioDireccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UsuarioDireccionRepository extends JpaRepository<UsuarioDireccion, Integer> {

    @Query("SELECT d FROM UsuarioDireccion d " +
            "JOIN FETCH d.municipio " +
            "WHERE d.usuario.idUsuario = :idUsuario " +
            "ORDER BY d.idDireccion")
    List<UsuarioDireccion> findByUsuarioIdConMunicipio(@Param("idUsuario") Integer idUsuario);
}
