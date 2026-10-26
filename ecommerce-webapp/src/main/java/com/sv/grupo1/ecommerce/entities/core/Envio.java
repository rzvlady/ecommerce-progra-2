package com.sv.grupo1.ecommerce.entities.core;

import java.io.Serializable;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "envios", schema = "core")
public class Envio implements Serializable {
    private static final long serialVersionUID = 1L;

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

    @Column(name = "costo_envio", precision = 10, scale = 2)
    private BigDecimal costoEnvio;

    @Column(name = "estado_envio", nullable = false, length = 1)
    private Character estadoEnvio = 'P';

    @Column(name = "fecha_despacho")
    private OffsetDateTime fechaDespacho;

    @Column(name = "fecha_entrega")
    private OffsetDateTime fechaEntrega;

    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private OffsetDateTime fechaCreacion;

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
    public BigDecimal getCostoEnvio() { return costoEnvio; }
    public void setCostoEnvio(BigDecimal costoEnvio) { this.costoEnvio = costoEnvio; }
    
    public Character getEstadoEnvio() { return estadoEnvio; }
    public void setEstadoEnvio(Character estadoEnvio) { this.estadoEnvio = estadoEnvio; }

    public void setEstadoEnvio(String estado) {
        if (estado == null || estado.isEmpty()) {
            this.estadoEnvio = 'P';
        } else if (estado.length() == 1) {
            this.estadoEnvio = estado.charAt(0);
        } else {
            switch (estado.toUpperCase()) {
                case "DESPACHADO":
                case "EN_TRANSITO":
                case "ENVIADO":
                    this.estadoEnvio = 'T';
                    break;
                case "ENTREGADO":
                case "FINALIZADO":
                    this.estadoEnvio = 'E';
                    break;
                case "DEVUELTO":
                    this.estadoEnvio = 'D';
                    break;
                case "LISTO":
                    this.estadoEnvio = 'L';
                    break;
                case "PREPARANDO":
                case "PENDIENTE":
                default:
                    this.estadoEnvio = 'P';
                    break;
            }
        }
    }

    public OffsetDateTime getFechaDespacho() { return fechaDespacho; }
    public void setFechaDespacho(OffsetDateTime fechaDespacho) { this.fechaDespacho = fechaDespacho; }
    public OffsetDateTime getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(OffsetDateTime fechaEntrega) { this.fechaEntrega = fechaEntrega; }
    public OffsetDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(OffsetDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
