package com.sv.grupo1.ecommerce.services.impl;

import com.sv.grupo1.ecommerce.dto.RespuestaPagoPasarelaDTO;
import com.sv.grupo1.ecommerce.dto.SolicitudPagoPasarelaDTO;
import com.sv.grupo1.ecommerce.enums.EstadoPasarela;
import com.sv.grupo1.ecommerce.services.PasarelaPagoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;

@Service
public class MockPasarelaPagoServiceImpl implements PasarelaPagoService {

    private static final Logger LOG = LoggerFactory.getLogger(MockPasarelaPagoServiceImpl.class);
    private static final String CODIGO_ERROR_CONEXION = "99";
    private static final String MENSAJE_ERROR_CONEXION = "No se pudo comunicar con la pasarela de pago. Intente nuevamente.";

    private final RestClient restClient;

    public MockPasarelaPagoServiceImpl(@Value("${app.pasarela.url}") String urlPasarela) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        this.restClient = RestClient.builder()
                .baseUrl(urlPasarela)
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public RespuestaPagoPasarelaDTO procesarPago(SolicitudPagoPasarelaDTO solicitud, String idempotencyKey) {
        LOG.info("procesarPago() => Iniciando, orden: {}", solicitud.getMerchantOrderId());
        try {
            RespuestaPagoPasarelaDTO respuesta = restClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Idempotency-Key", idempotencyKey)
                    .body(solicitud)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> { })
                    .body(RespuestaPagoPasarelaDTO.class);

            if (respuesta == null || respuesta.getStatus() == null) {
                LOG.warn("procesarPago() => Respuesta vacia de la pasarela, orden: {}", solicitud.getMerchantOrderId());
                return crearRespuestaError(solicitud);
            }
            LOG.info("procesarPago() => Completado, orden: {}, estado: {}",
                    solicitud.getMerchantOrderId(), respuesta.getStatus());
            return respuesta;
        } catch (RestClientException e) {
            LOG.error("procesarPago() => Fallo la comunicacion con la pasarela, orden: {}: {}",
                    solicitud.getMerchantOrderId(), e.getMessage());
            return crearRespuestaError(solicitud);
        }
    }

    private RespuestaPagoPasarelaDTO crearRespuestaError(SolicitudPagoPasarelaDTO solicitud) {
        String numeroTarjeta = solicitud.getCardNumber();

        RespuestaPagoPasarelaDTO respuesta = new RespuestaPagoPasarelaDTO();
        respuesta.setMerchantOrderId(solicitud.getMerchantOrderId());
        respuesta.setStatus(EstadoPasarela.ERROR);
        respuesta.setResponseCode(CODIGO_ERROR_CONEXION);
        respuesta.setResponseMessage(MENSAJE_ERROR_CONEXION);
        respuesta.setAmount(solicitud.getAmount());
        respuesta.setCurrency(solicitud.getCurrency());
        respuesta.setProcessedAt(Instant.now());
        if (numeroTarjeta != null && numeroTarjeta.length() >= 4) {
            respuesta.setLastFourDigits(numeroTarjeta.substring(numeroTarjeta.length() - 4));
        }
        return respuesta;
    }
}
