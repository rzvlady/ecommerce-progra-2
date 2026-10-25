package com.sv.grupo1.ecommerce.exceptions;

import java.sql.SQLException;

/**
 * Excepción personalizada para encapsular y gestionar errores de acceso
 * y persistencia a nivel de base de datos relacional mediante JDBC puro.
 * Permite desacoplar las capas superiores de detalles específicos de bajo nivel (SQLException).
 */
public class DatabasePersistenceException extends RuntimeException {

    private final String sqlState;
    private final int errorCode;

    public DatabasePersistenceException(String message) {
        super(message);
        this.sqlState = null;
        this.errorCode = 0;
    }

    public DatabasePersistenceException(String message, Throwable cause) {
        super(message, cause);
        if (cause instanceof SQLException sqlEx) {
            this.sqlState = sqlEx.getSQLState();
            this.errorCode = sqlEx.getErrorCode();
        } else {
            this.sqlState = null;
            this.errorCode = 0;
        }
    }

    public DatabasePersistenceException(String message, SQLException cause) {
        super(message, cause);
        this.sqlState = cause.getSQLState();
        this.errorCode = cause.getErrorCode();
    }

    public String getSqlState() {
        return sqlState;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
