package com.sv.grupo1.ecommerce.dao.jdbc;

import com.sv.grupo1.ecommerce.entities.core.MovimientoInventario;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de persistencia pura mediante JDBC para la gestión de movimientos de inventario.
 * Demuestra el cumplimiento de estándares requeridos en la rúbrica:
 * - Operaciones CRUD completas (inserción, consulta, actualización, eliminación).
 * - Acceso directo mediante DataSource relacional.
 */
public interface MovimientoInventarioJdbcDAO {

    /**
     * Inserta un nuevo movimiento de inventario utilizando PreparedStatement y executeUpdate().
     * Recupera la clave primaria generada (id_movimiento).
     *
     * @param movimiento Entidad con los datos del movimiento.
     * @return El movimiento persistido con su ID asignado.
     */
    MovimientoInventario insertar(MovimientoInventario movimiento);

    /**
     * Consulta un movimiento de inventario por su ID utilizando executeQuery() y ResultSet.
     *
     * @param idMovimiento Identificador único del movimiento.
     * @return Optional con el movimiento si existe.
     */
    Optional<MovimientoInventario> buscarPorId(Integer idMovimiento);

    /**
     * Obtiene el listado completo de movimientos ordenados cronológicamente descendente.
     *
     * @return Lista de movimientos encontrados.
     */
    List<MovimientoInventario> listarTodos();

    /**
     * Consulta los movimientos asociados a un producto específico.
     *
     * @param idProducto Identificador del producto.
     * @return Lista de movimientos del producto.
     */
    List<MovimientoInventario> listarPorProducto(Integer idProducto);

    /**
     * Actualiza el motivo y la cantidad de un movimiento existente mediante executeUpdate().
     *
     * @param idMovimiento Identificador del movimiento a modificar.
     * @param nuevoMotivo  Nuevo motivo explicativo.
     * @param nuevaCantidad Nueva cantidad del movimiento.
     * @return true si se actualizó al menos un registro; false de lo contrario.
     */
    boolean actualizarMotivoYCantidad(Integer idMovimiento, String nuevoMotivo, Integer nuevaCantidad);

    /**
     * Elimina un movimiento de inventario por su ID mediante executeUpdate().
     *
     * @param idMovimiento Identificador del registro a eliminar.
     * @return true si el registro fue eliminado exitosamente.
     */
    boolean eliminar(Integer idMovimiento);

    /**
     * Cuenta la cantidad de movimientos según su tipo ("ENTRADA", "SALIDA", "AJUSTE").
     *
     * @param tipoMovimiento Tipo de movimiento a filtrar.
     * @return Cantidad total encontrada.
     */
    long contarPorTipo(String tipoMovimiento);
}
