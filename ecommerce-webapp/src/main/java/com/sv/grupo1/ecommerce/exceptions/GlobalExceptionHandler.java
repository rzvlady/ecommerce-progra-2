package com.sv.grupo1.ecommerce.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.sql.SQLException;

/**
 * Manejador global de excepciones para la aplicación web.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DatabasePersistenceException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String manejarDatabasePersistenceException(DatabasePersistenceException ex, Model model) {
        log.error("Fallo de persistencia JDBC puro [SQLState: {}, ErrorCode: {}]: {}",
                ex.getSqlState(), ex.getErrorCode(), ex.getMessage(), ex);

        model.addAttribute("tituloError", "Servicio temporalmente no disponible");
        model.addAttribute("mensajeUsuario", "Estamos experimentando dificultades para conectar con el servicio de base de datos. Por favor, intenta de nuevo en unos minutos.");
        model.addAttribute("codigoEstado", 503);
        model.addAttribute("tipoError", "Error de Persistencia Relacional");
        return "error";
    }

    @ExceptionHandler({SQLException.class, DataAccessException.class})
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public String manejarErroresBaseDatos(Exception ex, Model model) {
        log.error("Excepción crítica de base de datos interceptada: {}", ex.getMessage(), ex);

        model.addAttribute("tituloError", "Error en el sistema de almacenamiento");
        model.addAttribute("mensajeUsuario", "No se pudo completar la operación solicitada debido a una indisponibilidad temporal de la base de datos.");
        model.addAttribute("codigoEstado", 503);
        model.addAttribute("tipoError", "Indisponibilidad de Datos");
        return "error";
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String manejarCredencialesInvalidas(CredencialesInvalidasException ex, Model model) {
        log.warn("Intento de autenticación rechazado: {}", ex.getMessage());

        model.addAttribute("tituloError", "Credenciales incorrectas");
        model.addAttribute("mensajeUsuario", ex.getMessage());
        model.addAttribute("codigoEstado", 401);
        model.addAttribute("tipoError", "Autenticación");
        return "error";
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String manejarRecursoNoEncontrado(org.springframework.web.servlet.resource.NoResourceFoundException ex, Model model) {
        log.warn("Recurso estático o ruta no encontrada: {}", ex.getResourcePath());
        model.addAttribute("tituloError", "Recurso no encontrado");
        model.addAttribute("mensajeUsuario", "El recurso o archivo solicitado no se encuentra en el servidor.");
        model.addAttribute("codigoEstado", 404);
        model.addAttribute("tipoError", "Recurso No Encontrado");
        return "error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String manejarExcepcionGeneral(Exception ex, Model model) {
        log.error("Excepción no controlada capturada por GlobalExceptionHandler: {}", ex.getMessage(), ex);

        model.addAttribute("tituloError", "Ha ocurrido un error inesperado");
        model.addAttribute("mensajeUsuario", "Ocurrió un problema interno al procesar tu solicitud. Nuestro equipo técnico ha sido notificado.");
        model.addAttribute("codigoEstado", 500);
        model.addAttribute("tipoError", "Error Interno del Servidor");
        return "error";
    }
}
