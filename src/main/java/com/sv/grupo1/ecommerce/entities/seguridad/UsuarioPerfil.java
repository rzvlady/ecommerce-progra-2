package com.sv.grupo1.ecommerce.entities.seguridad;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "usuarios_perfil", schema = "seguridad")
public class UsuarioPerfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_perfil")
    private Integer idPerfil;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "nombres", nullable = false)
    private String nombres;

    @Column(name = "apellidos", nullable = false)
    private String apellidos;

    @Column(name = "dui", unique = true)
    private String dui;

    @Column(name = "genero", length = 1)
    private Character genero;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    public UsuarioPerfil() {}

    // Getters y Setters
    public Integer getIdPerfil() { return idPerfil; }
    public void setIdPerfil(Integer idPerfil) { this.idPerfil = idPerfil; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getDui() { return dui; }
    public void setDui(String dui) { this.dui = dui; }
    public Character getGenero() { return genero; }
    public void setGenero(Character genero) { this.genero = genero; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
}