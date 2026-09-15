package com.sv.grupo1.ecommerce.entities.seguridad;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios_contacto", schema = "seguridad")
public class UsuarioContacto {

    @Id
    @Column(name = "id_contacto")
    private Integer idContacto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "tipo_contacto")
    private Integer tipoContacto;

    @Column(name = "valor", nullable = false)
    private String valor;

    @Column(name = "principal")
    private Boolean principal;

    @Column(name = "verificado")
    private Boolean verificado;

    public UsuarioContacto() {}

    // Getters y Setters
    public Integer getIdContacto() { return idContacto; }
    public void setIdContacto(Integer idContacto) { this.idContacto = idContacto; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Integer getTipoContacto() { return tipoContacto; }
    public void setTipoContacto(Integer tipoContacto) { this.tipoContacto = tipoContacto; }
    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }
    public Boolean getPrincipal() { return principal; }
    public void setPrincipal(Boolean principal) { this.principal = principal; }
    public Boolean getVerificado() { return verificado; }
    public void setVerificado(Boolean verificado) { this.verificado = verificado; }
}
