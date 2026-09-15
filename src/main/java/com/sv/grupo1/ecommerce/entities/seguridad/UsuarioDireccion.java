package com.sv.grupo1.ecommerce.entities.seguridad;

import com.sv.grupo1.ecommerce.entities.catalogo.Municipio;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios_direccion", schema = "seguridad")
public class UsuarioDireccion {

    @Id
    @Column(name = "id_direccion")
    private Integer idDireccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_municipio", nullable = false)
    private Municipio municipio;

    @Column(name = "direccion_linea_1", nullable = false)
    private String direccionLinea1;

    @Column(name = "codigo_postal")
    private Integer codigoPostal;

    @Column(name = "tipo_direccion", length = 1)
    private Character tipoDireccion;

    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "json")
    private String metadata;

    public UsuarioDireccion() {}

    // Getters y Setters
    public Integer getIdDireccion() { return idDireccion; }
    public void setIdDireccion(Integer idDireccion) { this.idDireccion = idDireccion; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Municipio getMunicipio() { return municipio; }
    public void setMunicipio(Municipio municipio) { this.municipio = municipio; }
    public String getDireccionLinea1() { return direccionLinea1; }
    public void setDireccionLinea1(String direccionLinea1) { this.direccionLinea1 = direccionLinea1; }
    public Integer getCodigoPostal() { return codigoPostal; }
    public void setCodigoPostal(Integer codigoPostal) { this.codigoPostal = codigoPostal; }
    public Character getTipoDireccion() { return tipoDireccion; }
    public void setTipoDireccion(Character tipoDireccion) { this.tipoDireccion = tipoDireccion; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
}
