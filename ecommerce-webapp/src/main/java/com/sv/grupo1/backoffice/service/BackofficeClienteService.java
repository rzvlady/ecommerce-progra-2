package com.sv.grupo1.backoffice.service;

import com.sv.grupo1.backoffice.dto.ClienteBackofficeDTO;
import com.sv.grupo1.backoffice.dto.ClienteRegistroBackofficeDTO;

import java.util.List;
import java.util.Map;

public interface BackofficeClienteService {

    List<ClienteBackofficeDTO> listarClientes(String busqueda, String filtroEstado);

    ClienteBackofficeDTO obtenerDetalleCliente(Integer idUsuario);

    ClienteRegistroBackofficeDTO obtenerDtoRegistroPorId(Integer idUsuario);

    void registrarCliente(ClienteRegistroBackofficeDTO dto, String usuarioAuditor);

    void actualizarCliente(Integer idUsuario, ClienteRegistroBackofficeDTO dto, String usuarioAuditor);

    void alternarEstado(Integer idUsuario, String usuarioAuditor);

    void cambiarEstado(Integer idUsuario, Character nuevoEstado, String motivo, String usuarioAuditor);

    void eliminarCliente(Integer idUsuario, String usuarioAuditor);

    void guardarNotaInterna(Integer idUsuario, String nota, String usuarioAuditor);

    Map<String, Long> obtenerMetricasDashboard();
}
