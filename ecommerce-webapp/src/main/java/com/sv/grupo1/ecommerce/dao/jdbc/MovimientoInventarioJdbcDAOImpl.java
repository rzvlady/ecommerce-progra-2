package com.sv.grupo1.ecommerce.dao.jdbc;

import com.sv.grupo1.ecommerce.entities.core.MovimientoInventario;
import com.sv.grupo1.ecommerce.entities.core.Producto;
import com.sv.grupo1.ecommerce.exceptions.DatabasePersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de persistencia con JDBC puro para movimientos de inventario.
 * Demuestra el uso explícito de:
 * - DataSource para adquisición de conexiones administradas por el pool.
 * - Connection, PreparedStatement y ResultSet.
 * - Métodos executeQuery() y executeUpdate().
 * - Try-with-resources para el cierre automático y determinista de recursos.
 * - Manejo robusto de excepciones convirtiendo SQLException a DatabasePersistenceException.
 */
@Repository
public class MovimientoInventarioJdbcDAOImpl implements MovimientoInventarioJdbcDAO {

    private static final Logger log = LoggerFactory.getLogger(MovimientoInventarioJdbcDAOImpl.class);

    private final DataSource dataSource;

    public MovimientoInventarioJdbcDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public MovimientoInventario insertar(MovimientoInventario movimiento) {
        if (movimiento == null) {
            throw new IllegalArgumentException("El movimiento no puede ser nulo");
        }
        if (movimiento.getProducto() == null || movimiento.getProducto().getIdProducto() == null) {
            throw new IllegalArgumentException("El producto asociado al movimiento es requerido");
        }

        final String sql = "INSERT INTO core.movimientos_inventario " +
                "(id_producto, tipo_movimiento, cantidad, motivo, id_pedido, fecha_movimiento) " +
                "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, movimiento.getProducto().getIdProducto());
            ps.setString(2, movimiento.getTipoMovimiento());
            ps.setInt(3, movimiento.getCantidad());
            ps.setString(4, movimiento.getMotivo());

            if (movimiento.getIdPedido() != null) {
                ps.setInt(5, movimiento.getIdPedido());
            } else {
                ps.setNull(5, Types.INTEGER);
            }

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas == 0) {
                throw new DatabasePersistenceException("La inserción del movimiento falló, ninguna fila fue afectada.");
            }

            try (ResultSet rsClaves = ps.getGeneratedKeys()) {
                if (rsClaves.next()) {
                    movimiento.setIdMovimiento(rsClaves.getInt(1));
                } else {
                    throw new DatabasePersistenceException("La inserción falló, no se pudo obtener el ID generado.");
                }
            }

            movimiento.setFechaMovimiento(OffsetDateTime.now());
            log.info("Movimiento de inventario persistido con ID: {}", movimiento.getIdMovimiento());
            return movimiento;

        } catch (SQLException e) {
            log.error("Error al insertar movimiento de inventario vía JDBC puro: {}", e.getMessage(), e);
            throw new DatabasePersistenceException("Error al insertar movimiento de inventario en base de datos", e);
        }
    }

    @Override
    public Optional<MovimientoInventario> buscarPorId(Integer idMovimiento) {
        if (idMovimiento == null) {
            return Optional.empty();
        }

        final String sql = "SELECT m.id_movimiento, m.id_producto, m.tipo_movimiento, " +
                "m.cantidad, m.motivo, m.fecha_movimiento, m.id_pedido, " +
                "p.nombre_producto, p.codigo_sku " +
                "FROM core.movimientos_inventario m " +
                "INNER JOIN catalogo.productos p ON m.id_producto = p.id_producto " +
                "WHERE m.id_movimiento = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idMovimiento);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearMovimiento(rs));
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            log.error("Error al consultar movimiento con ID {} vía JDBC: {}", idMovimiento, e.getMessage(), e);
            throw new DatabasePersistenceException("Error al consultar movimiento de inventario con ID: " + idMovimiento, e);
        }
    }

    @Override
    public List<MovimientoInventario> listarTodos() {
        final String sql = "SELECT m.id_movimiento, m.id_producto, m.tipo_movimiento, " +
                "m.cantidad, m.motivo, m.fecha_movimiento, m.id_pedido, " +
                "p.nombre_producto, p.codigo_sku " +
                "FROM core.movimientos_inventario m " +
                "INNER JOIN catalogo.productos p ON m.id_producto = p.id_producto " +
                "ORDER BY m.fecha_movimiento DESC, m.id_movimiento DESC";

        List<MovimientoInventario> lista = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearMovimiento(rs));
            }

            return lista;

        } catch (SQLException e) {
            log.error("Error al listar movimientos de inventario vía JDBC: {}", e.getMessage(), e);
            throw new DatabasePersistenceException("Error al listar movimientos de inventario", e);
        }
    }

    @Override
    public List<MovimientoInventario> listarPorProducto(Integer idProducto) {
        final String sql = "SELECT m.id_movimiento, m.id_producto, m.tipo_movimiento, " +
                "m.cantidad, m.motivo, m.fecha_movimiento, m.id_pedido, " +
                "p.nombre_producto, p.codigo_sku " +
                "FROM core.movimientos_inventario m " +
                "INNER JOIN catalogo.productos p ON m.id_producto = p.id_producto " +
                "WHERE m.id_producto = ? " +
                "ORDER BY m.fecha_movimiento DESC, m.id_movimiento DESC";

        List<MovimientoInventario> lista = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idProducto);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearMovimiento(rs));
                }
            }

            return lista;

        } catch (SQLException e) {
            log.error("Error al consultar movimientos por producto {} vía JDBC: {}", idProducto, e.getMessage(), e);
            throw new DatabasePersistenceException("Error al consultar movimientos para el producto: " + idProducto, e);
        }
    }

    @Override
    public boolean actualizarMotivoYCantidad(Integer idMovimiento, String nuevoMotivo, Integer nuevaCantidad) {
        if (idMovimiento == null) {
            return false;
        }

        final String sql = "UPDATE core.movimientos_inventario " +
                "SET motivo = ?, cantidad = ? " +
                "WHERE id_movimiento = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nuevoMotivo);
            ps.setInt(2, nuevaCantidad);
            ps.setInt(3, idMovimiento);

            int filasActualizadas = ps.executeUpdate();
            return filasActualizadas > 0;

        } catch (SQLException e) {
            log.error("Error al actualizar movimiento {} vía JDBC: {}", idMovimiento, e.getMessage(), e);
            throw new DatabasePersistenceException("Error al actualizar movimiento de inventario: " + idMovimiento, e);
        }
    }

    @Override
    public boolean eliminar(Integer idMovimiento) {
        if (idMovimiento == null) {
            return false;
        }

        final String sql = "DELETE FROM core.movimientos_inventario WHERE id_movimiento = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idMovimiento);

            int filasEliminadas = ps.executeUpdate();
            return filasEliminadas > 0;

        } catch (SQLException e) {
            log.error("Error al eliminar movimiento {} vía JDBC: {}", idMovimiento, e.getMessage(), e);
            throw new DatabasePersistenceException("Error al eliminar movimiento de inventario con ID: " + idMovimiento, e);
        }
    }

    @Override
    public long contarPorTipo(String tipoMovimiento) {
        final String sql = "SELECT COUNT(*) AS total FROM core.movimientos_inventario WHERE tipo_movimiento = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tipoMovimiento);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("total");
                }
            }
            return 0L;

        } catch (SQLException e) {
            log.error("Error al contar movimientos tipo {} vía JDBC: {}", tipoMovimiento, e.getMessage(), e);
            throw new DatabasePersistenceException("Error al contar movimientos por tipo: " + tipoMovimiento, e);
        }
    }

    /**
     * Mapea una fila del ResultSet a la entidad MovimientoInventario.
     */
    private MovimientoInventario mapearMovimiento(ResultSet rs) throws SQLException {
        MovimientoInventario mov = new MovimientoInventario();
        mov.setIdMovimiento(rs.getInt("id_movimiento"));

        Producto prod = new Producto();
        prod.setIdProducto(rs.getInt("id_producto"));
        prod.setNombreProducto(rs.getString("nombre_producto"));
        prod.setCodigoSku(rs.getString("codigo_sku"));
        mov.setProducto(prod);

        mov.setTipoMovimiento(rs.getString("tipo_movimiento"));
        mov.setCantidad(rs.getInt("cantidad"));
        mov.setMotivo(rs.getString("motivo"));

        Timestamp ts = rs.getTimestamp("fecha_movimiento");
        if (ts != null) {
            mov.setFechaMovimiento(ts.toInstant().atOffset(java.time.ZoneOffset.UTC));
        }

        int idPedido = rs.getInt("id_pedido");
        if (!rs.wasNull()) {
            mov.setIdPedido(idPedido);
        }

        return mov;
    }
}
