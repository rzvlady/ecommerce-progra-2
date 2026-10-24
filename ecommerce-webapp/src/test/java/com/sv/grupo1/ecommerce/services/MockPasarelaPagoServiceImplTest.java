package com.sv.grupo1.ecommerce.services;

import com.sun.net.httpserver.HttpServer;
import com.sv.grupo1.ecommerce.dto.RespuestaPagoPasarelaDTO;
import com.sv.grupo1.ecommerce.dto.SolicitudPagoPasarelaDTO;
import com.sv.grupo1.ecommerce.enums.EstadoPasarela;
import com.sv.grupo1.ecommerce.services.impl.MockPasarelaPagoServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class MockPasarelaPagoServiceImplTest {

    private HttpServer servidor;
    private String idempotencyKeyRecibida;
    private String cuerpoRecibido;

    @AfterEach
    void detenerPasarela() {
        if (servidor != null) {
            servidor.stop(0);
        }
    }

    @Test
    void debeDevolverLaRespuestaAprobadaYEnviarLaLlaveDeIdempotencia() throws IOException {
        String url = iniciarPasarela(201,
                "{\"transactionId\":\"pay_123\",\"merchantOrderId\":\"PED-7\",\"status\":\"APPROVED\","
                        + "\"responseCode\":\"00\",\"responseMessage\":\"Transacción aprobada\"}");
        PasarelaPagoService pasarelaPagoService = new MockPasarelaPagoServiceImpl(url);

        RespuestaPagoPasarelaDTO respuesta = pasarelaPagoService.procesarPago(crearSolicitud(), "PED-7");

        assertEquals(EstadoPasarela.APPROVED, respuesta.getStatus());
        assertEquals("pay_123", respuesta.getTransactionId());
        assertEquals("PED-7", idempotencyKeyRecibida);
        assertTrue(cuerpoRecibido.contains("\"merchantOrderId\":\"PED-7\""));
        assertTrue(cuerpoRecibido.contains("\"cardNumber\":\"4111111111111234\""));
    }

    @Test
    void debeLeerElCuerpoCuandoLaPasarelaRechazaElPago() throws IOException {
        String url = iniciarPasarela(402,
                "{\"transactionId\":\"pay_456\",\"merchantOrderId\":\"PED-7\",\"status\":\"DECLINED\","
                        + "\"responseCode\":\"51\",\"responseMessage\":\"Fondos insuficientes\"}");
        PasarelaPagoService pasarelaPagoService = new MockPasarelaPagoServiceImpl(url);

        RespuestaPagoPasarelaDTO respuesta = pasarelaPagoService.procesarPago(crearSolicitud(), "PED-7");

        assertEquals(EstadoPasarela.DECLINED, respuesta.getStatus());
        assertEquals("Fondos insuficientes", respuesta.getResponseMessage());
    }

    @Test
    void debeDevolverErrorCuandoLaPasarelaNoEstaDisponible() throws IOException {
        PasarelaPagoService pasarelaPagoService = new MockPasarelaPagoServiceImpl(
                "http://localhost:" + obtenerPuertoLibre() + "/mock-gateway/v1/payments");

        RespuestaPagoPasarelaDTO respuesta = pasarelaPagoService.procesarPago(crearSolicitud(), "PED-7");

        assertEquals(EstadoPasarela.ERROR, respuesta.getStatus());
        assertEquals("99", respuesta.getResponseCode());
        assertEquals("PED-7", respuesta.getMerchantOrderId());
        assertEquals("1234", respuesta.getLastFourDigits());
        assertNotNull(respuesta.getResponseMessage());
    }

    @Test
    void debeDevolverErrorCuandoLaPasarelaRespondeSinEstado() throws IOException {
        String url = iniciarPasarela(200, "{}");
        PasarelaPagoService pasarelaPagoService = new MockPasarelaPagoServiceImpl(url);

        RespuestaPagoPasarelaDTO respuesta = pasarelaPagoService.procesarPago(crearSolicitud(), "PED-7");

        assertEquals(EstadoPasarela.ERROR, respuesta.getStatus());
        assertEquals("99", respuesta.getResponseCode());
    }

    private String iniciarPasarela(int codigoHttp, String cuerpoRespuesta) throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        servidor.createContext("/mock-gateway/v1/payments", intercambio -> {
            idempotencyKeyRecibida = intercambio.getRequestHeaders().getFirst("Idempotency-Key");
            cuerpoRecibido = new String(intercambio.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

            byte[] bytes = cuerpoRespuesta.getBytes(StandardCharsets.UTF_8);
            intercambio.getResponseHeaders().add("Content-Type", "application/json");
            intercambio.sendResponseHeaders(codigoHttp, bytes.length);
            try (OutputStream salida = intercambio.getResponseBody()) {
                salida.write(bytes);
            }
        });
        servidor.start();
        return "http://localhost:" + servidor.getAddress().getPort() + "/mock-gateway/v1/payments";
    }

    private int obtenerPuertoLibre() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private SolicitudPagoPasarelaDTO crearSolicitud() {
        SolicitudPagoPasarelaDTO solicitud = new SolicitudPagoPasarelaDTO();
        solicitud.setMerchantOrderId("PED-7");
        solicitud.setAmount(new BigDecimal("200.00"));
        solicitud.setCurrency("USD");
        solicitud.setCardNumber("4111111111111234");
        solicitud.setCardHolderName("Cliente Prueba");
        solicitud.setCardExpirationDate("12/29");
        solicitud.setCvv("123");
        return solicitud;
    }
}
