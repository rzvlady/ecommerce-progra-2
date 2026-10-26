package com.sv.grupo1.backoffice.repository;

import com.sv.grupo1.ecommerce.entities.seguridad.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Integer> {

    Optional<Rol> findByNombre(String nombre);

    List<Rol> findAllByOrderByIdRolAsc();

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdRolNot(String nombre, Integer idRol);

    @Query(value = "SELECT * FROM seguridad.roles WHERE UPPER(nombre) = UPPER(:nombre) LIMIT 1", nativeQuery = true)
    Optional<Rol> findByNombreIgnorandoEliminado(@Param("nombre") String nombre);

    @Modifying
    @Query(value = "UPDATE seguridad.roles SET eliminado = false, descripcion = :descripcion WHERE id_rol = :idRol", nativeQuery = true)
    void reactivarRol(@Param("idRol") Integer idRol, @Param("descripcion") String descripcion);

    @Query(value = "SELECT COUNT(*) FROM seguridad.usuarios_roles WHERE id_rol = :idRol", nativeQuery = true)
    long contarUsuariosPorRol(@Param("idRol") Integer idRol);
}
