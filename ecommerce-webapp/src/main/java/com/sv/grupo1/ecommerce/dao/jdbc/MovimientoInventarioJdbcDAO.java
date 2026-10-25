package com.sv.grupo1.ecommerce.dao.jdbc;

import com.sv.grupo1.ecommerce.entities.core.MovimientoInventario;

import java.util.List;
import java.util.Optional;

/**
 * DAO para la persistencia de movimientos de inventario mediante JDBC.
 */
public interface MovimientoInventarioJdbcDAO {

    MovimientoInventario insertar(MovimientoInventario movimiento);

    Optional<MovimientoInventario> buscarPorId(Integer idMovimiento);

    List<MovimientoInventario> listarTodos();

    List<MovimientoInventario> listarPorProducto(Integer idProducto);

    boolean actualizarMotivoYCantidad(Integer idMovimiento, String nuevoMotivo, Integer nuevaCantidad);

    boolean eliminar(Integer idMovimiento);

    long contarPorTipo(String tipoMovimiento);
}
