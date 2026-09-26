package com.sv.grupo1.ecommerce.dao;

import com.sv.grupo1.ecommerce.entities.seguridad.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    boolean existsByCorreoInicioSesionIgnoreCase(String correoInicioSesion);
}
