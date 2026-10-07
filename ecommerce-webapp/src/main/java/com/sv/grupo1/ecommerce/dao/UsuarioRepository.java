package com.sv.grupo1.ecommerce.dao;

import com.sv.grupo1.ecommerce.entities.seguridad.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer>{
    Optional<Usuario> findByCorreoInicioSesion(String correoInicioSesion);
    //Optional<Usuario> findByTokenReinicio(String tokenReinicio);
	boolean existsByCorreoInicioSesionIgnoreCase(String correoInicioSesion);
    Optional<Usuario> findByCorreoInicioSesionIgnoreCase(String correoInicioSesion);
}