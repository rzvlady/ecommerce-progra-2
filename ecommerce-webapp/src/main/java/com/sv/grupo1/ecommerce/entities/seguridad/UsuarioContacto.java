package com.sv.grupo1.ecommerce.entities.seguridad;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "usuarios_contacto", schema = "seguridad")
@SQLDelete(sql = "UPDATE seguridad.usuarios_contacto SET metadata = COALESCE(metadata, '{}'::jsonb) || '{\"eliminado\": true}'::jsonb WHERE id_contacto = ?")
@SQLRestriction("COALESCE(metadata->>'eliminado', 'false') = 'false'")
public class UsuarioContacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contacto")
    private Integer idContacto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "tipo_contacto", length = 1)
    private Character tipoContacto;

    @Column(name = "valor", nullable = false)
    private String valor;

    @Column(name = "principal", nullable = false, insertable = false, updatable = true)
    private Boolean principal = true;

    @Column(name = "verificado")
    private Boolean verificado;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    public UsuarioContacto() {
        /* Constructor Vacio */
    }

    public Integer getIdContacto() {
        return idContacto;
    }

    public void setIdContacto(Integer idContacto) {
        this.idContacto = idContacto;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Character getTipoContacto() {
        return tipoContacto;
    }

    public void setTipoContacto(Character tipoContacto) {
        this.tipoContacto = tipoContacto;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public Boolean getPrincipal() {
        return principal;
    }

    public void setPrincipal(Boolean principal) {
        this.principal = principal;
    }

    public Boolean getVerificado() {
        return verificado;
    }

    public void setVerificado(Boolean verificado) {
        this.verificado = verificado;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public void setEstado(String estado) {
        if (this.metadata == null) this.metadata = new java.util.HashMap<>();
        this.metadata.put("estado", estado);
    }

    public String getEstado() {
        if (this.metadata == null) return null;
        return (String) this.metadata.get("estado");
    }
}
