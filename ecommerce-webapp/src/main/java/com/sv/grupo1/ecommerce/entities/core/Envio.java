package com.sv.grupo1.ecommerce.entities.core;

import java.io.Serializable;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "envios", schema = "core")
public class Envio implements Serializable{
    private static final long serialVersionUID=1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_envio")
    private Integer idEnvio;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido", nullable = false, unique = true)
    private Pedido pedido;

    @Column(name = "empresa_transporte")
    private String empresaTransporte;

    @Column(name = "numero_seguimiento")
    private String numeroSeguimiento;

    @Column(name = "estado_envio", nullable = false)
    private String estadoEnvio;

    @Column(name = "fecha_despacho")
    private OffsetDateTime fechaDespacho;

    @Column(name = "fecha_entrega")
    private OffsetDateTime fechaEntrega;

    public Envio() {}

    // Getters y Setters
    public Integer getIdEnvio() { return idEnvio; }
    public void setIdEnvio(Integer idEnvio) { this.idEnvio = idEnvio; }
    public Pedido getPedido() { return pedido; }
    public void setPedido(Pedido pedido) { this.pedido = pedido; }
    public String getEmpresaTransporte() { return empresaTransporte; }
    public void setEmpresaTransporte(String empresaTransporte) { this.empresaTransporte = empresaTransporte; }
    public String getNumeroSeguimiento() { return numeroSeguimiento; }
    public void setNumeroSeguimiento(String numeroSeguimiento) { this.numeroSeguimiento = numeroSeguimiento; }
    public String getEstadoEnvio() { return estadoEnvio; }
    public void setEstadoEnvio(String estadoEnvio) { this.estadoEnvio = estadoEnvio; }
    public OffsetDateTime getFechaDespacho() { return fechaDespacho; }
    public void setFechaDespacho(OffsetDateTime fechaDespacho) { this.fechaDespacho = fechaDespacho; }
    public OffsetDateTime getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(OffsetDateTime fechaEntrega) { this.fechaEntrega = fechaEntrega; }
}