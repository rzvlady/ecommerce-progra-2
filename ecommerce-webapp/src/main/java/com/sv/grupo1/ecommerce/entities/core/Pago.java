package com.sv.grupo1.ecommerce.entities.core;

import com.sv.grupo1.ecommerce.enums.EstadoTransaccion;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "pagos", schema = "core")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Integer idPago;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pedido", nullable = false)
    private Pedido pedido;

    @Column(name = "metodo_pago", nullable = false)
    private String metodoPago;

    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "estado_transaccion", nullable = false, length = 1)
    private Character estadoTransaccion = EstadoTransaccion.PENDIENTE.getCodigoEstado();

    @Column(name = "referencia_pasarela")
    private String referenciaPasarela;

    @Column(name = "fecha_pago")
    private OffsetDateTime fechaPago;

    public Pago() {}

    // Getters y Setters
    public Integer getIdPago() { return idPago; }
    public void setIdPago(Integer idPago) { this.idPago = idPago; }
    public Pedido getPedido() { return pedido; }
    public void setPedido(Pedido pedido) { this.pedido = pedido; }
    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public Character getEstadoTransaccion() { return estadoTransaccion; }
    public void setEstadoTransaccion(Character estadoTransaccion) { this.estadoTransaccion = estadoTransaccion; }
    public String getReferenciaPasarela() { return referenciaPasarela; }
    public void setReferenciaPasarela(String referenciaPasarela) { this.referenciaPasarela = referenciaPasarela; }
    public OffsetDateTime getFechaPago() { return fechaPago; }
    public void setFechaPago(OffsetDateTime fechaPago) { this.fechaPago = fechaPago; }
}
