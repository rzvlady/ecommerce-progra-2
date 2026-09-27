package com.sv.grupo1.ecommerce.entities.seguridad;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios", schema = "seguridad")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "correo_inicio_sesion", nullable = false, unique = true)
    private String correoInicioSesion;

    @Column(name = "contrasenia", nullable = false)
    private String contrasenia;

    @Column(name = "eliminado")
    private Boolean eliminado;

    @Column(name = "estado", nullable = false, length = 1)
    private Character estado;

    @Column(name = "ultimo_inicio_sesion")
    private LocalDateTime ultimoInicioSesion;

    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "json")
    private String metadata;

    public Usuario() {}

    // Getters y Setters
    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
    public String getCorreoInicioSesion() { return correoInicioSesion; }
    public void setCorreoInicioSesion(String correoInicioSesion) { this.correoInicioSesion = correoInicioSesion; }
    public String getContrasenia() { return contrasenia; }
    public void setContrasenia(String contrasenia) { this.contrasenia = contrasenia; }
    public Boolean getEliminado() { return eliminado; }
    public void setEliminado(Boolean eliminado) { this.eliminado = eliminado; }
    public Character getEstado() { return estado; }
    public void setEstado(Character estado) { this.estado = estado; }
    public LocalDateTime getUltimoInicioSesion() { return ultimoInicioSesion; }
    public void setUltimoInicioSesion(LocalDateTime ultimoInicioSesion) { this.ultimoInicioSesion = ultimoInicioSesion; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
}
