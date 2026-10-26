package com.sv.grupo1.ecommerce.exceptions;

import java.sql.SQLException;

/**
 * Excepción para errores de persistencia en base de datos.
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
