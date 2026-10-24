package com.sv.grupo1.ecommerce.entities.seguridad;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;

@Entity
@Table(schema = "seguridad", name = "usuarios_roles")
public class UsuarioRol {

    @EmbeddedId
    private UsuarioRolId id = new UsuarioRolId();

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idUsuario")
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idRol")
    @JoinColumn(name = "id_rol", nullable = false)
    private Rol rol;

    @Column(name = "fecha_asignacion", insertable = false, updatable = false)
    private OffsetDateTime fechaAsignacion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "auditoria", columnDefinition = "jsonb")
    private Map<String, Object> auditoria;

    public UsuarioRol() {
        /* Constructor Vacio */
    }

    public UsuarioRol(Usuario usuario, Rol rol) {
        this.usuario = usuario;
        this.rol = rol;
        this.id = new UsuarioRolId(
            usuario != null ? usuario.getIdUsuario() : null,
            rol != null ? rol.getIdRol() : null
        );
    }

    public UsuarioRolId getId() {
        return id;
    }

    public void setId(UsuarioRolId id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public OffsetDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(OffsetDateTime fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }

    public Map<String, Object> getAuditoria() {
        return auditoria;
    }

    public void setAuditoria(Map<String, Object> auditoria) {
        this.auditoria = auditoria;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsuarioRol that = (UsuarioRol) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
