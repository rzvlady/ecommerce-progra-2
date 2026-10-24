package com.sv.grupo1.ecommerce.services;

import com.sv.grupo1.ecommerce.dto.CheckoutDTO;
import com.sv.grupo1.ecommerce.dto.ResultadoCompraDTO;
import com.sv.grupo1.ecommerce.dto.ResumenCompraDTO;
import com.sv.grupo1.ecommerce.entities.catalogo.Municipio;
import com.sv.grupo1.ecommerce.entities.seguridad.UsuarioDireccion;

import java.util.List;

public interface CheckoutService {

    ResumenCompraDTO obtenerResumenCarrito(String correo);

    List<UsuarioDireccion> listarDirecciones(String correo);

    List<Municipio> listarMunicipios();

    ResultadoCompraDTO finalizarCompra(String correo, CheckoutDTO dto);

    ResumenCompraDTO obtenerConfirmacion(String correo, Integer idPedido);
}
