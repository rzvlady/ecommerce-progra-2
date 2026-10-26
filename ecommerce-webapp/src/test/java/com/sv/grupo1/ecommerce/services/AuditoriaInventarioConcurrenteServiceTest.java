package com.sv.grupo1.ecommerce.services;

import com.sv.grupo1.ecommerce.dto.ResultadoAuditoriaConcurrente;
import com.sv.grupo1.ecommerce.services.impl.AuditoriaInventarioConcurrenteServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditoriaInventarioConcurrenteServiceTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement psCategorias;

    @Mock
    private PreparedStatement psProductos;

    @Mock
    private ResultSet rsCategorias;

    @Mock
    private ResultSet rsProductos;

    private AuditoriaInventarioConcurrenteServiceImpl service;

    @BeforeEach
    void setUp() throws SQLException {
        service = new AuditoriaInventarioConcurrenteServiceImpl(dataSource);
    }

    @Test
    @DisplayName("Debe ejecutar auditoría multihilo y recolectar métricas de concurrencia")
    void testEjecutarAuditoriaConcurrenteExitoso() throws SQLException {
        // Arrange
        when(dataSource.getConnection()).thenReturn(connection);

        // Mock para consulta de categorías
        when(connection.prepareStatement(contains("catalogo.categorias"))).thenReturn(psCategorias);
        when(psCategorias.executeQuery()).thenReturn(rsCategorias);
        when(rsCategorias.next()).thenReturn(true, true, true, false);
        when(rsCategorias.getInt("id_categoria")).thenReturn(1, 2, 3);
        when(rsCategorias.getString("nombre_categoria")).thenReturn("Tecnología", "Calzado", "Hogar");

        // Mock para consulta de productos por categoría
        when(connection.prepareStatement(contains("core.productos"))).thenReturn(psProductos);
        when(psProductos.executeQuery()).thenReturn(rsProductos);
        when(rsProductos.next()).thenReturn(true, false);
        when(rsProductos.getInt("stock_disponible")).thenReturn(5);
        when(rsProductos.getInt("stock_minimo")).thenReturn(2);

        // Act
        ResultadoAuditoriaConcurrente resultado = service.ejecutarAuditoriaConcurrente(3);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isExito());
        assertEquals(3, resultado.getTotalCategoriasAuditadas());
        assertFalse(resultado.getHilosUtilizados().isEmpty());
        assertTrue(resultado.getDetallesPorCategoria().stream().allMatch(d -> d.getHiloEjecutor().startsWith("audit-worker-thread-")));
        assertTrue(resultado.getTiempoTotalMs() >= 0);
        assertNotNull(resultado.getMensaje());
    }

    @Test
    @DisplayName("Debe manejar gracefully cuando no hay categorías para auditar")
    void testEjecutarAuditoriaSinCategorias() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(contains("catalogo.categorias"))).thenReturn(psCategorias);
        when(psCategorias.executeQuery()).thenReturn(rsCategorias);
        when(rsCategorias.next()).thenReturn(false);

        ResultadoAuditoriaConcurrente resultado = service.ejecutarAuditoriaConcurrente(2);

        assertNotNull(resultado);
        assertTrue(resultado.isExito());
        assertEquals(0, resultado.getTotalCategoriasAuditadas());
        assertTrue(resultado.getMensaje().contains("No se encontraron categorías"));
    }
}
