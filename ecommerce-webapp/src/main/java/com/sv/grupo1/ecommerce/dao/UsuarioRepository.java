package com.sv.grupo1.ecommerce.dao;

import com.sv.grupo1.ecommerce.entities.seguridad.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByCorreoInicioSesion(String correoInicioSesion);

    //Optional<Usuario> findByTokenReinicio(String tokenReinicio);

    boolean existsByCorreoInicioSesionIgnoreCase(String correoInicioSesion);

    @EntityGraph(attributePaths = {"roles", "roles.rol", "perfil"})
    Optional<Usuario> findByCorreoInicioSesionIgnoreCase(String correoInicioSesion);

    @Modifying
    @Query("UPDATE Usuario u SET u.ultimoInicioSesion = :fecha WHERE LOWER(u.correoInicioSesion) = LOWER(:correo)")
    int actualizarUltimoInicioSesionPorCorreo(@Param("correo") String correo, @Param("fecha") OffsetDateTime fecha);
}