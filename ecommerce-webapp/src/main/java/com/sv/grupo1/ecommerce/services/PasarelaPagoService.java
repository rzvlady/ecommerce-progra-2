package com.sv.grupo1.ecommerce.services;

import com.sv.grupo1.ecommerce.dto.RespuestaPagoPasarelaDTO;
import com.sv.grupo1.ecommerce.dto.SolicitudPagoPasarelaDTO;

public interface PasarelaPagoService {

    RespuestaPagoPasarelaDTO procesarPago(SolicitudPagoPasarelaDTO solicitud, String idempotencyKey);
}
