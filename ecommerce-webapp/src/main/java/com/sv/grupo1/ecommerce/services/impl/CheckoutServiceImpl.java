package com.sv.grupo1.ecommerce.services.impl;

import com.sv.grupo1.ecommerce.carrito.CarritoItem;
import com.sv.grupo1.ecommerce.carrito.CarritoSesionService;
import com.sv.grupo1.ecommerce.dao.DetallePedidoRepository;
import com.sv.grupo1.ecommerce.dao.MovimientoInventarioRepository;
import com.sv.grupo1.ecommerce.dao.MunicipioRepository;
import com.sv.grupo1.ecommerce.dao.PagoRepository;
import com.sv.grupo1.ecommerce.dao.PedidoRepository;
import com.sv.grupo1.ecommerce.dao.ProductoRepository;
import com.sv.grupo1.ecommerce.dao.UsuarioDireccionRepository;
import com.sv.grupo1.ecommerce.dao.UsuarioRepository;
import com.sv.grupo1.ecommerce.dto.CheckoutDTO;
import com.sv.grupo1.ecommerce.dto.ItemCompraDTO;
import com.sv.grupo1.ecommerce.dto.RespuestaPagoPasarelaDTO;
import com.sv.grupo1.ecommerce.dto.ResultadoCompraDTO;
import com.sv.grupo1.ecommerce.dto.ResumenCompraDTO;
import com.sv.grupo1.ecommerce.dto.SolicitudPagoPasarelaDTO;
import com.sv.grupo1.ecommerce.entities.catalogo.Municipio;
import com.sv.grupo1.ecommerce.entities.core.DetallePedido;
import com.sv.grupo1.ecommerce.entities.core.MovimientoInventario;
import com.sv.grupo1.ecommerce.entities.core.Pago;
import com.sv.grupo1.ecommerce.entities.core.Pedido;
import com.sv.grupo1.ecommerce.entities.core.Producto;
import com.sv.grupo1.ecommerce.entities.seguridad.Usuario;
import com.sv.grupo1.ecommerce.entities.seguridad.UsuarioDireccion;
import com.sv.grupo1.ecommerce.enums.EstadoEstandarGlobal;
import com.sv.grupo1.ecommerce.enums.EstadoPasarela;
import com.sv.grupo1.ecommerce.enums.EstadoPedido;
import com.sv.grupo1.ecommerce.enums.EstadoTransaccion;
import com.sv.grupo1.ecommerce.services.CheckoutService;
import com.sv.grupo1.ecommerce.services.PasarelaPagoService;
import com.sv.grupo1.ecommerce.utils.Constantes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CheckoutServiceImpl implements CheckoutService {

    private static final Logger LOG = LoggerFactory.getLogger(CheckoutServiceImpl.class);
    private static final String MONEDA = "USD";
    private static final String PREFIJO_ORDEN = "PED-";
    private static final BigDecimal MONTO_MINIMO_PAGO = new BigDecimal("0.50");

    private final UsuarioRepository usuarioRepository;
    private final UsuarioDireccionRepository usuarioDireccionRepository;
    private final MunicipioRepository municipioRepository;
    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final PagoRepository pagoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final CarritoSesionService carritoService;
    private final PasarelaPagoService pasarelaPagoService;

    public CheckoutServiceImpl(
            UsuarioRepository usuarioRepository,
            UsuarioDireccionRepository usuarioDireccionRepository,
            MunicipioRepository municipioRepository,
            ProductoRepository productoRepository,
            PedidoRepository pedidoRepository,
            DetallePedidoRepository detallePedidoRepository,
            PagoRepository pagoRepository,
            MovimientoInventarioRepository movimientoInventarioRepository,
            CarritoSesionService carritoService,
            PasarelaPagoService pasarelaPagoService) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioDireccionRepository = usuarioDireccionRepository;
        this.municipioRepository = municipioRepository;
        this.productoRepository = productoRepository;
        this.pedidoRepository = pedidoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.pagoRepository = pagoRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
        this.carritoService = carritoService;
        this.pasarelaPagoService = pasarelaPagoService;
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenCompraDTO obtenerResumenCarrito(String correo) {
        buscarUsuario(correo);
        List<DetallePedido> detalles = construirDetallesDesdeCarrito();

        ResumenCompraDTO resumen = new ResumenCompraDTO();
        resumen.setItems(convertirAItems(detalles));
        resumen.setTotal(calcularTotal(detalles));
        return resumen;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioDireccion> listarDirecciones(String correo) {
        Usuario usuario = buscarUsuario(correo);
        return usuarioDireccionRepository.findByUsuarioIdConMunicipio(usuario.getIdUsuario());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Municipio> listarMunicipios() {
        return municipioRepository.findAllByOrderByNombreMunicipioAsc();
    }

    @Override
    @Transactional
    public ResultadoCompraDTO finalizarCompra(String correo, CheckoutDTO dto) {
        LOG.info("finalizarCompra() => Iniciando");
        Usuario usuario = buscarUsuario(correo);

        List<DetallePedido> detalles = construirDetallesDesdeCarrito();
        if (detalles.isEmpty()) {
            throw new IllegalArgumentException("Tu carrito está vacío.");
        }
        validarDisponibilidad(detalles);

        BigDecimal total = calcularTotal(detalles);
        validarMontoMinimo(total);
        validarTotalMostrado(dto, total);

        UsuarioDireccion direccion = resolverDireccion(usuario, dto);
        Pedido pedido = crearPedido(usuario, direccion, total, detalles);

        String referenciaOrden = PREFIJO_ORDEN + pedido.getIdPedido();
        RespuestaPagoPasarelaDTO respuesta = pasarelaPagoService.procesarPago(
                construirSolicitud(referenciaOrden, total, dto), referenciaOrden);
        registrarPago(pedido, total, dto, respuesta);

        boolean aprobado = EstadoPasarela.APPROVED.equals(respuesta.getStatus());
        if (aprobado) {
            confirmarPedido(pedido, detalles);
            carritoService.vaciar();
        } else {
            anularPedido(pedido);
        }

        ResultadoCompraDTO resultado = new ResultadoCompraDTO();
        resultado.setAprobado(aprobado);
        resultado.setIdPedido(pedido.getIdPedido());
        resultado.setMensaje(construirMensaje(pedido, respuesta));
        LOG.info("finalizarCompra() => Completado, pedido: {}, aprobado: {}", pedido.getIdPedido(), aprobado);
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public ResumenCompraDTO obtenerConfirmacion(String correo, Integer idPedido) {
        Usuario usuario = buscarUsuario(correo);
        Pedido pedido = pedidoRepository.findById(idPedido)
                .orElseThrow(() -> new IllegalArgumentException("El pedido no existe."));

        if (!pedido.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
            throw new IllegalArgumentException("El pedido no pertenece a tu cuenta.");
        }
        if (!pedido.getEstadoPedido().equals(EstadoPedido.CONFIRMADO.getCodigoEstado())) {
            throw new IllegalArgumentException("El pedido no fue confirmado.");
        }

        List<DetallePedido> detalles = detallePedidoRepository.findByPedido_IdPedido(idPedido);
        UsuarioDireccion direccion = pedido.getDireccion();

        ResumenCompraDTO resumen = new ResumenCompraDTO();
        resumen.setIdPedido(pedido.getIdPedido());
        resumen.setItems(convertirAItems(detalles));
        resumen.setTotal(pedido.getTotal());
        resumen.setDireccionEntrega(direccion.getDireccionLinea1() + ", " + direccion.getMunicipio().getNombreMunicipio());
        resumen.setReferenciaPago(pagoRepository.findFirstByPedido_IdPedidoOrderByIdPagoDesc(idPedido)
                .map(Pago::getReferenciaPasarela)
                .orElse(null));
        return resumen;
    }

    private Usuario buscarUsuario(String correo) {
        return usuarioRepository.findByCorreoInicioSesionIgnoreCase(correo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
    }

    private List<DetallePedido> construirDetallesDesdeCarrito() {
        List<DetallePedido> detalles = new ArrayList<>();
        for (CarritoItem itemCarrito : carritoService.items()) {
            Producto producto = productoRepository.findById(itemCarrito.getIdProducto())
                    .orElseThrow(() -> new IllegalArgumentException("Un producto de tu carrito ya no existe."));

            DetallePedido detalle = new DetallePedido();
            detalle.setProducto(producto);
            detalle.setCantidad(itemCarrito.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecioVenta());
            detalle.setSubtotal(producto.getPrecioVenta().multiply(BigDecimal.valueOf(itemCarrito.getCantidad())));
            detalles.add(detalle);
        }
        return detalles;
    }

    private void validarDisponibilidad(List<DetallePedido> detalles) {
        for (DetallePedido detalle : detalles) {
            Producto producto = detalle.getProducto();
            if (!producto.getEstado().equals(EstadoEstandarGlobal.ACTIVO.getCodigoEstado())) {
                throw new IllegalArgumentException(
                        "El producto '" + producto.getNombreProducto() + "' ya no está disponible.");
            }
            int stockActual = producto.getStockDisponible() != null ? producto.getStockDisponible() : 0;
            if (stockActual < detalle.getCantidad()) {
                throw new IllegalArgumentException(
                        "Stock insuficiente para '" + producto.getNombreProducto()
                                + "'. Disponible: " + stockActual + " unidades.");
            }
        }
    }

    private UsuarioDireccion resolverDireccion(Usuario usuario, CheckoutDTO dto) {
        if (dto.getIdDireccion() != null) {
            UsuarioDireccion direccion = usuarioDireccionRepository.findById(dto.getIdDireccion())
                    .orElseThrow(() -> new IllegalArgumentException("La dirección seleccionada no existe."));
            if (!direccion.getUsuario().getIdUsuario().equals(usuario.getIdUsuario())) {
                throw new IllegalArgumentException("La dirección seleccionada no pertenece a tu cuenta.");
            }
            return direccion;
        }

        if (dto.getDireccionLinea1() == null || dto.getDireccionLinea1().isBlank() || dto.getIdMunicipio() == null) {
            throw new IllegalArgumentException("Selecciona una dirección de entrega o ingresa una nueva.");
        }
        String direccionLinea1 = dto.getDireccionLinea1().trim();
        Optional<UsuarioDireccion> direccionExistente = usuarioDireccionRepository
                .findFirstByUsuarioIdUsuarioAndMunicipioIdMunicipioAndDireccionLinea1IgnoreCase(
                        usuario.getIdUsuario(), dto.getIdMunicipio(), direccionLinea1);
        if (direccionExistente.isPresent()) {
            return direccionExistente.get();
        }

        Municipio municipio = municipioRepository.findById(dto.getIdMunicipio())
                .orElseThrow(() -> new IllegalArgumentException("El municipio seleccionado no existe."));

        UsuarioDireccion nueva = new UsuarioDireccion();
        nueva.setUsuario(usuario);
        nueva.setMunicipio(municipio);
        nueva.setDireccionLinea1(direccionLinea1);
        nueva.setCodigoPostal(dto.getCodigoPostal() != null && !dto.getCodigoPostal().isBlank()
                ? dto.getCodigoPostal().trim()
                : null);
        nueva.setTipoDireccion(Constantes.TIPO_DIRECCION_CASA);
        return usuarioDireccionRepository.save(nueva);
    }

    private Pedido crearPedido(Usuario usuario, UsuarioDireccion direccion, BigDecimal total, List<DetallePedido> detalles) {
        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setDireccion(direccion);
        pedido.setTotal(total);
        Pedido guardado = pedidoRepository.save(pedido);

        for (DetallePedido detalle : detalles) {
            detalle.setPedido(guardado);
        }
        detallePedidoRepository.saveAll(detalles);
        return guardado;
    }

    private SolicitudPagoPasarelaDTO construirSolicitud(String referenciaOrden, BigDecimal total, CheckoutDTO dto) {
        SolicitudPagoPasarelaDTO solicitud = new SolicitudPagoPasarelaDTO();
        solicitud.setMerchantOrderId(referenciaOrden);
        solicitud.setAmount(total);
        solicitud.setCurrency(MONEDA);
        solicitud.setCardNumber(dto.getNumeroTarjeta());
        solicitud.setCardHolderName(dto.getNombreTitular().trim());
        solicitud.setCardExpirationDate(dto.getFechaExpiracion());
        solicitud.setCvv(dto.getCvv());
        return solicitud;
    }

    private void registrarPago(Pedido pedido, BigDecimal total, CheckoutDTO dto, RespuestaPagoPasarelaDTO respuesta) {
        String numeroTarjeta = dto.getNumeroTarjeta();
        EstadoTransaccion estado = convertirAEstadoTransaccion(respuesta.getStatus());

        Pago pago = new Pago();
        pago.setPedido(pedido);
        pago.setMetodoPago("TARJETA **** " + numeroTarjeta.substring(numeroTarjeta.length() - 4));
        pago.setMonto(total);
        pago.setEstadoTransaccion(estado.getCodigoEstado());
        pago.setReferenciaPasarela(respuesta.getTransactionId());
        if (EstadoTransaccion.EXITOSO.equals(estado)) {
            pago.setFechaPago(OffsetDateTime.now());
        }
        pagoRepository.save(pago);
    }

    private EstadoTransaccion convertirAEstadoTransaccion(EstadoPasarela estadoPasarela) {
        return switch (estadoPasarela) {
            case APPROVED -> EstadoTransaccion.EXITOSO;
            case PENDING -> EstadoTransaccion.PENDIENTE;
            case DECLINED, ERROR -> EstadoTransaccion.RECHAZADO;
        };
    }

    private void confirmarPedido(Pedido pedido, List<DetallePedido> detalles) {
        pedido.setEstadoPedido(EstadoPedido.CONFIRMADO.getCodigoEstado());
        pedido.setFechaActualizacion(OffsetDateTime.now());
        pedidoRepository.save(pedido);

        for (DetallePedido detalle : detalles) {
            Producto producto = detalle.getProducto();
            producto.setStockDisponible(producto.getStockDisponible() - detalle.getCantidad());
            productoRepository.save(producto);

            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setProducto(producto);
            movimiento.setTipoMovimiento("SALIDA");
            movimiento.setCantidad(detalle.getCantidad());
            movimiento.setMotivo("VENTA - Pedido #" + pedido.getIdPedido());
            movimiento.setPedido(pedido);
            movimientoInventarioRepository.save(movimiento);
        }
    }

    private void anularPedido(Pedido pedido) {
        pedido.setEstadoPedido(EstadoPedido.ANULADO.getCodigoEstado());
        pedido.setFechaActualizacion(OffsetDateTime.now());
        pedidoRepository.save(pedido);
    }

    private String construirMensaje(Pedido pedido, RespuestaPagoPasarelaDTO respuesta) {
        if (EstadoPasarela.APPROVED.equals(respuesta.getStatus())) {
            return "¡Pago aprobado! Tu pedido #" + pedido.getIdPedido() + " fue confirmado.";
        }
        if (EstadoPasarela.PENDING.equals(respuesta.getStatus())) {
            return "Tu pago quedó en validación y el pedido no se pudo confirmar. Intenta nuevamente.";
        }
        return respuesta.getResponseMessage() != null
                ? respuesta.getResponseMessage()
                : "El pago no pudo ser procesado.";
    }

    private void validarMontoMinimo(BigDecimal total) {
        if (total.compareTo(MONTO_MINIMO_PAGO) < 0) {
            throw new IllegalArgumentException(
                    "El monto mínimo para pagar con tarjeta es $" + formatearMonto(MONTO_MINIMO_PAGO) + ".");
        }
    }

    private void validarTotalMostrado(CheckoutDTO dto, BigDecimal total) {
        if (dto.getTotalMostrado() != null && dto.getTotalMostrado().compareTo(total) != 0) {
            throw new IllegalStateException(
                    "El total de tu compra cambió de $" + formatearMonto(dto.getTotalMostrado())
                            + " a $" + formatearMonto(total) + ". Revisa el resumen antes de pagar.");
        }
    }

    private String formatearMonto(BigDecimal monto) {
        return monto.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private BigDecimal calcularTotal(List<DetallePedido> detalles) {
        return detalles.stream()
                .map(DetallePedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<ItemCompraDTO> convertirAItems(List<DetallePedido> detalles) {
        List<ItemCompraDTO> items = new ArrayList<>();
        for (DetallePedido detalle : detalles) {
            ItemCompraDTO item = new ItemCompraDTO();
            item.setNombreProducto(detalle.getProducto().getNombreProducto());
            item.setCodigoSku(detalle.getProducto().getCodigoSku());
            item.setCantidad(detalle.getCantidad());
            item.setPrecioUnitario(detalle.getPrecioUnitario());
            item.setSubtotal(detalle.getSubtotal());
            items.add(item);
        }
        return items;
    }
}
