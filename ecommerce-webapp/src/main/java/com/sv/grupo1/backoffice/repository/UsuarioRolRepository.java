package com.sv.grupo1.backoffice.repository;

import com.sv.grupo1.ecommerce.entities.seguridad.Usuario;
import com.sv.grupo1.ecommerce.entities.seguridad.UsuarioRol;
import com.sv.grupo1.ecommerce.entities.seguridad.UsuarioRolId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRolRepository extends JpaRepository<UsuarioRol, UsuarioRolId> {

    @Query("SELECT DISTINCT u FROM Usuario u LEFT JOIN FETCH u.perfil LEFT JOIN FETCH u.roles ur LEFT JOIN FETCH ur.rol ORDER BY u.idUsuario ASC")
    List<Usuario> listarUsuariosConPerfilYRoles();

    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.perfil LEFT JOIN FETCH u.roles ur LEFT JOIN FETCH ur.rol WHERE u.idUsuario = :idUsuario")
    Optional<Usuario> buscarUsuarioConRolesPorId(@Param("idUsuario") Integer idUsuario);

    @Modifying
    @Query("DELETE FROM UsuarioRol ur WHERE ur.id.idUsuario = :idUsuario")
    void eliminarRolesPorIdUsuario(@Param("idUsuario") Integer idUsuario);
}
