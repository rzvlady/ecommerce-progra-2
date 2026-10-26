package com.sv.grupo1.backoffice.service;

import com.sv.grupo1.backoffice.dto.MovimientoInventarioDTO;
import com.sv.grupo1.ecommerce.entities.catalogo.Marca;
import com.sv.grupo1.ecommerce.entities.catalogo.Categoria;
import com.sv.grupo1.ecommerce.entities.core.MovimientoInventario;
import com.sv.grupo1.ecommerce.entities.core.Producto;

import java.util.List;

public interface BackofficeInventarioService {

    List<Producto> listarProductos();

    List<MovimientoInventario> listarMovimientos();

    List<Producto> obtenerAlertasMonitor();

    void ejecutarEscaneoMonitor();

    Producto registrarMovimiento(MovimientoInventarioDTO dto);

    Producto actualizarStockMinimo(Integer idProducto, Integer nuevoStockMinimo);

    List<Categoria> listarCategorias();

    List<Marca> listarMarcas();
}
