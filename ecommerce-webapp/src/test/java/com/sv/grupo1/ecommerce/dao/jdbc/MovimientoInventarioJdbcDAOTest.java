package com.sv.grupo1.ecommerce.dao.jdbc;

import com.sv.grupo1.ecommerce.entities.core.MovimientoInventario;
import com.sv.grupo1.ecommerce.entities.core.Producto;
import com.sv.grupo1.ecommerce.exceptions.DatabasePersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovimientoInventarioJdbcDAOTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private MovimientoInventarioJdbcDAOImpl jdbcDAO;

    @BeforeEach
    void setUp() {
        jdbcDAO = new MovimientoInventarioJdbcDAOImpl(dataSource);
    }

    @Test
    @DisplayName("Debe insertar un movimiento exitosamente y asignar ID generado")
    void testInsertarExitoso() throws SQLException {
        // Arrange
        Producto prod = new Producto();
        prod.setIdProducto(101);

        MovimientoInventario mov = new MovimientoInventario();
        mov.setProducto(prod);
        mov.setTipoMovimiento("ENTRADA");
        mov.setCantidad(15);
        mov.setMotivo("Reposición de stock");

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(42);

        // Act
        MovimientoInventario resultado = jdbcDAO.insertar(mov);

        // Assert
        assertNotNull(resultado);
        assertEquals(42, resultado.getIdMovimiento());
        assertEquals("ENTRADA", resultado.getTipoMovimiento());
        verify(preparedStatement).setInt(1, 101);
        verify(preparedStatement).setString(2, "ENTRADA");
        verify(preparedStatement).setInt(3, 15);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    @DisplayName("Debe lanzar DatabasePersistenceException si ocurre SQLException al insertar")
    void testInsertarErrorSQLException() throws SQLException {
        Producto prod = new Producto();
        prod.setIdProducto(101);
        MovimientoInventario mov = new MovimientoInventario();
        mov.setProducto(prod);
        mov.setTipoMovimiento("ENTRADA");
        mov.setCantidad(5);
        mov.setMotivo("Compra inicial");

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString(), anyInt())).thenThrow(new SQLException("Conexión rechazada"));

        assertThrows(DatabasePersistenceException.class, () -> jdbcDAO.insertar(mov));
    }

    @Test
    @DisplayName("Debe buscar un movimiento por ID exitosamente")
    void testBuscarPorIdExitoso() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("id_movimiento")).thenReturn(99);
        when(resultSet.getInt("id_producto")).thenReturn(5);
        when(resultSet.getString("nombre_producto")).thenReturn("Laptop Pro");
        when(resultSet.getString("codigo_sku")).thenReturn("LAP-001");
        when(resultSet.getString("tipo_movimiento")).thenReturn("SALIDA");
        when(resultSet.getInt("cantidad")).thenReturn(2);
        when(resultSet.getString("motivo")).thenReturn("Venta orden #10");
        when(resultSet.getTimestamp("fecha_movimiento")).thenReturn(new Timestamp(System.currentTimeMillis()));

        Optional<MovimientoInventario> resultado = jdbcDAO.buscarPorId(99);

        assertTrue(resultado.isPresent());
        assertEquals(99, resultado.get().getIdMovimiento());
        assertEquals("Laptop Pro", resultado.get().getProducto().getNombreProducto());
        assertEquals(2, resultado.get().getCantidad());
    }

    @Test
    @DisplayName("Debe actualizar motivo y cantidad correctamente")
    void testActualizarMotivoYCantidad() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean actualizado = jdbcDAO.actualizarMotivoYCantidad(15, "Ajuste por conteo físico", 8);

        assertTrue(actualizado);
        verify(preparedStatement).setString(1, "Ajuste por conteo físico");
        verify(preparedStatement).setInt(2, 8);
        verify(preparedStatement).setInt(3, 15);
    }

    @Test
    @DisplayName("Debe eliminar un movimiento por ID")
    void testEliminar() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        boolean eliminado = jdbcDAO.eliminar(20);

        assertTrue(eliminado);
        verify(preparedStatement).setInt(1, 20);
    }

    @Test
    @DisplayName("Debe retornar false si el ID a eliminar no existe")
    void testEliminarInexistente() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        boolean eliminado = jdbcDAO.eliminar(9999);

        assertFalse(eliminado);
    }

    @Test
    @DisplayName("Debe contar movimientos por tipo correctamente")
    void testContarPorTipo() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong("total")).thenReturn(14L);

        long total = jdbcDAO.contarPorTipo("SALIDA");

        assertEquals(14L, total);
        verify(preparedStatement).setString(1, "SALIDA");
    }
}
