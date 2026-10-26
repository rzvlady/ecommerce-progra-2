package com.sv.grupo1.backoffice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DespachoPedidoDTO {

    @NotNull(message = "El identificador del pedido es obligatorio")
    private Integer idPedido;

    @NotBlank(message = "Debe indicar la empresa de transporte o paquetería")
    private String empresaTransporte;

    @NotBlank(message = "El número de guía o seguimiento es obligatorio")
    private String numeroSeguimiento;

    private String notasEnvio;

    public DespachoPedidoDTO() {}

    public DespachoPedidoDTO(Integer idPedido, String empresaTransporte, String numeroSeguimiento, String notasEnvio) {
        this.idPedido = idPedido;
        this.empresaTransporte = empresaTransporte;
        this.numeroSeguimiento = numeroSeguimiento;
        this.notasEnvio = notasEnvio;
    }

    public Integer getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Integer idPedido) {
        this.idPedido = idPedido;
    }

    public String getEmpresaTransporte() {
        return empresaTransporte;
    }

    public void setEmpresaTransporte(String empresaTransporte) {
        this.empresaTransporte = empresaTransporte;
    }

    public String getNumeroSeguimiento() {
        return numeroSeguimiento;
    }

    public void setNumeroSeguimiento(String numeroSeguimiento) {
        this.numeroSeguimiento = numeroSeguimiento;
    }

    public String getNotasEnvio() {
        return notasEnvio;
    }

    public void setNotasEnvio(String notasEnvio) {
        this.notasEnvio = notasEnvio;
    }
}
