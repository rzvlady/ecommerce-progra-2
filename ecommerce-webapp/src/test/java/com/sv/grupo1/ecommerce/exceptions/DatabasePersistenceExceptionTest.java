package com.sv.grupo1.ecommerce.exceptions;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class DatabasePersistenceExceptionTest {

    @Test
    void debeConservarLaCausaElSqlStateYElCodigoDeError() {
        SQLException causa = new SQLException("Connection refused", "08001", 0);

        DatabasePersistenceException excepcion =
                new DatabasePersistenceException("Error al insertar movimiento de inventario", causa);

        assertEquals("Error al insertar movimiento de inventario", excepcion.getMessage());
        assertSame(causa, excepcion.getCause());
        assertEquals("08001", excepcion.getSqlState());
        assertEquals(0, excepcion.getErrorCode());
    }

    @Test
    void debeLeerElSqlStateAunqueLaCausaLlegueComoThrowable() {
        Throwable causa = new SQLException("duplicate key value", "23505", 7);

        DatabasePersistenceException excepcion = new DatabasePersistenceException("Error al guardar", causa);

        assertEquals("23505", excepcion.getSqlState());
        assertEquals(7, excepcion.getErrorCode());
    }

    @Test
    void debeDejarElSqlStateVacioCuandoLaCausaNoEsDeBaseDeDatos() {
        IllegalStateException causa = new IllegalStateException("Fallo inesperado");

        DatabasePersistenceException excepcion = new DatabasePersistenceException("Error al guardar", causa);

        assertSame(causa, excepcion.getCause());
        assertNull(excepcion.getSqlState());
        assertEquals(0, excepcion.getErrorCode());
    }

    @Test
    void debeCrearseSoloConMensajeSinCausa() {
        DatabasePersistenceException excepcion =
                new DatabasePersistenceException("La inserción del movimiento falló, ninguna fila fue afectada.");

        assertNull(excepcion.getCause());
        assertNull(excepcion.getSqlState());
        assertEquals(0, excepcion.getErrorCode());
    }
}
