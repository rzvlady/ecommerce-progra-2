package com.sv.grupo1.ecommerce.gateway.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * @author alexemestica
 * */
@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String idempotencyKey = request.getHeader("Idempotency-Key");
        String traceId = (idempotencyKey != null && !idempotencyKey.isBlank())
                ? idempotencyKey
                : UUID.randomUUID().toString().substring(0, 8);

        long startTime = System.currentTimeMillis();
        MDC.put("traceId", traceId);
        response.setHeader("X-Trace-Id", traceId);

        try {
            LOGGER.info("--> Entrando peticion: {} {}", request.getMethod(), request.getRequestURI());
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            LOGGER.info("<-- Respuesta enviada: HTTP {} ({} ms)", response.getStatus(), duration);
            // CRÍTICO: Limpiar siempre el MDC al terminar para no contaminar el hilo de Tomcat
            MDC.clear();
        }
    }
}
