package com.sv.grupo1.backoffice.controller;

import com.sv.grupo1.backoffice.dto.DespachoPedidoDTO;
import com.sv.grupo1.backoffice.dto.ItemPedidoDTO;
import com.sv.grupo1.backoffice.dto.PedidoBackofficeDTO;
import com.sv.grupo1.backoffice.service.ProductoImagenStorageService;
import com.sv.grupo1.ecommerce.dao.*;
import com.sv.grupo1.ecommerce.entities.core.*;
import com.sv.grupo1.ecommerce.entities.seguridad.Usuario;
import com.sv.grupo1.ecommerce.entities.seguridad.UsuarioContacto;
import com.sv.grupo1.ecommerce.entities.seguridad.UsuarioDireccion;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(path = "/backoffice/pedidos")
@PreAuthorize("hasRole('ADMIN')")
public class BackofficePedidoController {

    private static final Logger LOG = LoggerFactory.getLogger(BackofficePedidoController.class);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final PagoRepository pagoRepository;
    private final EnvioRepository envioRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final ProductoImagenStorageService imagenStorageService;

    public BackofficePedidoController(
            PedidoRepository pedidoRepository,
            DetallePedidoRepository detallePedidoRepository,
            PagoRepository pagoRepository,
            EnvioRepository envioRepository,
            ProductoRepository productoRepository,
            MovimientoInventarioRepository movimientoRepository,
            ProductoImagenStorageService imagenStorageService) {
        this.pedidoRepository = pedidoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.pagoRepository = pagoRepository;
        this.envioRepository = envioRepository;
        this.productoRepository = productoRepository;
        this.movimientoRepository = movimientoRepository;
        this.imagenStorageService = imagenStorageService;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public String vistaPedidos(
            @RequestParam(name = "estado", required = false, defaultValue = "TODOS") String filtroEstado,
            Model model) {
        LOG.info("Cargando vista de gestión de pedidos en backoffice desde Supabase. Filtro: {}", filtroEstado);

        List<Pedido> pedidosEntidad = pedidoRepository.findAllConRelaciones();
        List<PedidoBackofficeDTO> todosLosPedidos = pedidosEntidad.stream()
                .map(this::convertirAPedidoBackofficeDTO)
                .toList();

        List<PedidoBackofficeDTO> pedidos = "TODOS".equalsIgnoreCase(filtroEstado)
                ? todosLosPedidos
                : todosLosPedidos.stream()
                    .filter(p -> filtroEstado.equalsIgnoreCase(p.getEstadoPedido()))
                    .toList();

        long totalPedidos = todosLosPedidos.size();
        long pendientesPago = todosLosPedidos.stream().filter(p -> "PENDIENTE".equalsIgnoreCase(p.getEstadoPedido())).count();
        long listosDespacho = todosLosPedidos.stream().filter(p -> "PAGADO".equalsIgnoreCase(p.getEstadoPedido()) || "EN_PREPARACION".equalsIgnoreCase(p.getEstadoPedido())).count();
        long enTransito = todosLosPedidos.stream().filter(p -> "DESPACHADO".equalsIgnoreCase(p.getEstadoPedido())).count();
        long entregados = todosLosPedidos.stream().filter(p -> "ENTREGADO".equalsIgnoreCase(p.getEstadoPedido())).count();
        long anulados = todosLosPedidos.stream().filter(p -> "ANULADO".equalsIgnoreCase(p.getEstadoPedido())).count();

        BigDecimal montoTotalFacturado = todosLosPedidos.stream()
                .filter(p -> !"ANULADO".equalsIgnoreCase(p.getEstadoPedido()))
                .map(PedidoBackofficeDTO::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("pedidos", pedidos);
        model.addAttribute("filtroEstado", filtroEstado);
        model.addAttribute("totalPedidos", totalPedidos);
        model.addAttribute("pendientesPago", pendientesPago);
        model.addAttribute("listosDespacho", listosDespacho);
        model.addAttribute("enTransito", enTransito);
        model.addAttribute("entregados", entregados);
        model.addAttribute("anulados", anulados);
        model.addAttribute("montoTotalFacturado", montoTotalFacturado);
        model.addAttribute("pedidosJson", serializarPedidosAJson(todosLosPedidos));

        if (!model.containsAttribute("despachoDTO")) {
            model.addAttribute("despachoDTO", new DespachoPedidoDTO());
        }

        return "backoffice/pedidos/lista";
    }

    @PostMapping("/despachar")
    @Transactional
    public String registrarDespacho(
            @Valid @ModelAttribute("despachoDTO") DespachoPedidoDTO despachoDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al registrar despacho: verifique los datos ingresados.");
            return "redirect:/backoffice/pedidos";
        }

        LOG.info("Registrando despacho real para pedido #{} con transporte {} y guía {}",
                despachoDTO.getIdPedido(), despachoDTO.getEmpresaTransporte(), despachoDTO.getNumeroSeguimiento());

        Pedido pedido = pedidoRepository.findById(despachoDTO.getIdPedido()).orElse(null);
        if (pedido == null) {
            redirectAttributes.addFlashAttribute("mensajeError", "No se encontró el pedido #" + despachoDTO.getIdPedido());
            return "redirect:/backoffice/pedidos";
        }

        // Actualizar estado del pedido a 'E' (Enviado / Despachado)
        pedido.setEstadoPedido('E');
        pedido.setFechaActualizacion(OffsetDateTime.now());
        pedidoRepository.save(pedido);

        // Crear o actualizar registro en core.envios
        Envio envio = envioRepository.findByPedido_IdPedido(pedido.getIdPedido())
                .orElseGet(() -> {
                    Envio e = new Envio();
                    e.setPedido(pedido);
                    return e;
                });

        envio.setEmpresaTransporte(despachoDTO.getEmpresaTransporte());
        envio.setNumeroSeguimiento(despachoDTO.getNumeroSeguimiento());
        envio.setEstadoEnvio('T'); // 'T' = En Tránsito
        envio.setFechaDespacho(OffsetDateTime.now());
        envioRepository.save(envio);

        redirectAttributes.addFlashAttribute("mensajeExito",
                "¡Pedido #" + despachoDTO.getIdPedido() + " despachado con éxito! Guía registrada: "
                        + despachoDTO.getNumeroSeguimiento() + " (" + despachoDTO.getEmpresaTransporte() + ").");

        return "redirect:/backoffice/pedidos";
    }

    @PostMapping("/cambiar-estado")
    @Transactional
    public String cambiarEstadoPedido(
            @RequestParam("idPedido") Integer idPedido,
            @RequestParam("nuevoEstado") String nuevoEstado,
            RedirectAttributes redirectAttributes) {

        LOG.info("Cambiando estado de pedido #{} a {}", idPedido, nuevoEstado);

        Pedido pedido = pedidoRepository.findById(idPedido).orElse(null);
        if (pedido == null) {
            redirectAttributes.addFlashAttribute("mensajeError", "No se encontró el pedido #" + idPedido);
            return "redirect:/backoffice/pedidos";
        }

        char anteriorEstado = pedido.getEstadoPedido() != null ? pedido.getEstadoPedido() : 'P';
        char targetEstado;
        String nombreEstado;

        switch (nuevoEstado.toUpperCase()) {
            case "PAGADO":
            case "CONFIRMADO":
            case "C":
                targetEstado = 'C';
                nombreEstado = "Pagado (Listo para despacho)";
                break;
            case "DESPACHADO":
            case "ENVIADO":
            case "E":
                targetEstado = 'E';
                nombreEstado = "Despachado (En Tránsito)";
                break;
            case "ENTREGADO":
            case "FINALIZADO":
            case "F":
                targetEstado = 'F';
                nombreEstado = "Entregado";
                envioRepository.findByPedido_IdPedido(pedido.getIdPedido()).ifPresent(envio -> {
                    envio.setEstadoEnvio('E'); // 'E' = Entregado
                    envio.setFechaEntrega(OffsetDateTime.now());
                    envioRepository.save(envio);
                });
                break;
            case "ANULADO":
            case "A":
                targetEstado = 'A';
                nombreEstado = "Anulado";
                // Si el pedido estaba confirmado ('C' o 'E'), reponer stock y asentar en Kardex
                if (anteriorEstado == 'C' || anteriorEstado == 'E') {
                    List<DetallePedido> detalles = detallePedidoRepository.findByPedido_IdPedido(pedido.getIdPedido());
                    for (DetallePedido det : detalles) {
                        Producto prod = det.getProducto();
                        if (prod != null) {
                            prod.setStockDisponible((prod.getStockDisponible() != null ? prod.getStockDisponible() : 0) + det.getCantidad());
                            productoRepository.save(prod);

                            MovimientoInventario mov = new MovimientoInventario();
                            mov.setProducto(prod);
                            mov.setTipoMovimiento("ENTRADA");
                            mov.setCantidad(det.getCantidad());
                            mov.setMotivo("DEVOLUCION - Cancelación Pedido #" + pedido.getIdPedido());
                            mov.setPedido(pedido);
                            movimientoRepository.save(mov);
                        }
                    }
                }
                break;
            default:
                targetEstado = 'P';
                nombreEstado = "Pendiente";
                break;
        }

        pedido.setEstadoPedido(targetEstado);
        pedido.setFechaActualizacion(OffsetDateTime.now());
        pedidoRepository.save(pedido);

        redirectAttributes.addFlashAttribute("mensajeExito",
                "El estado del pedido #" + idPedido + " ha sido actualizado a " + nombreEstado + ".");

        return "redirect:/backoffice/pedidos";
    }

    private PedidoBackofficeDTO convertirAPedidoBackofficeDTO(Pedido pedido) {
        PedidoBackofficeDTO dto = new PedidoBackofficeDTO();
        dto.setIdPedido(pedido.getIdPedido());
        dto.setCodigoPedido("ORD-2026-" + String.format("%04d", pedido.getIdPedido()));

        if (pedido.getFechaCreacion() != null) {
            dto.setFechaCreacion(pedido.getFechaCreacion().format(FORMATO_FECHA));
        } else {
            dto.setFechaCreacion("Reciente");
        }

        // Cliente
        Usuario usuario = pedido.getUsuario();
        String nombreCliente = "Usuario #" + (usuario != null ? usuario.getIdUsuario() : "");
        String emailCliente = (usuario != null && usuario.getCorreoInicioSesion() != null) ? usuario.getCorreoInicioSesion() : "";
        String telefonoCliente = "Sin teléfono registrado";

        if (usuario != null) {
            if (usuario.getPerfil() != null) {
                String n = usuario.getPerfil().getNombres();
                String a = usuario.getPerfil().getApellidos();
                if ((n != null && !n.isBlank()) || (a != null && !a.isBlank())) {
                    nombreCliente = ((n != null ? n : "") + " " + (a != null ? a : "")).trim();
                }
            }
            if (usuario.getContactos() != null && !usuario.getContactos().isEmpty()) {
                // 1. Priorizar tipo de contacto MOVIL / CELULAR ('M', 'T')
                for (UsuarioContacto c : usuario.getContactos()) {
                    if (c.getTipoContacto() != null && (Character.toUpperCase(c.getTipoContacto()) == 'M' || Character.toUpperCase(c.getTipoContacto()) == 'T')
                            && c.getValor() != null && !c.getValor().isBlank()) {
                        telefonoCliente = c.getValor().trim();
                        break;
                    }
                }
                // 2. Si no encontró explícito 'M', buscar cualquier contacto que no sea email (no contenga '@')
                if ("Sin teléfono registrado".equals(telefonoCliente)) {
                    for (UsuarioContacto c : usuario.getContactos()) {
                        if (c.getValor() != null && !c.getValor().isBlank() && !c.getValor().contains("@")) {
                            telefonoCliente = c.getValor().trim();
                            break;
                        }
                    }
                }
            }
        }
        dto.setClienteNombre(nombreCliente);
        dto.setClienteEmail(emailCliente);
        dto.setClienteTelefono(telefonoCliente);

        // Dirección y Punto de Referencia
        UsuarioDireccion direccion = pedido.getDireccion();
        if (direccion != null) {
            dto.setDireccionEntrega(direccion.getDireccionLinea1());
            if (direccion.getMunicipio() != null) {
                dto.setMunicipio(direccion.getMunicipio().getNombreMunicipio());
                if (direccion.getMunicipio().getDepartamento() != null) {
                    dto.setDepartamento(direccion.getMunicipio().getDepartamento().getNombreDepartamento());
                } else {
                    dto.setDepartamento("El Salvador");
                }
            } else {
                dto.setMunicipio("San Salvador");
                dto.setDepartamento("San Salvador");
            }
            if (direccion.getMetadata() != null && direccion.getMetadata().get("punto_referencia") != null) {
                String ref = String.valueOf(direccion.getMetadata().get("punto_referencia")).trim();
                if (!ref.isBlank()) {
                    dto.setNotas(ref);
                }
            }
        } else {
            dto.setDireccionEntrega("Dirección estándar registrada");
            dto.setMunicipio("San Salvador");
            dto.setDepartamento("San Salvador");
        }

        // Estado del pedido
        char estado = pedido.getEstadoPedido() != null ? pedido.getEstadoPedido() : 'P';
        String estadoStr;
        switch (estado) {
            case 'C':
                estadoStr = "PAGADO";
                break;
            case 'E':
                estadoStr = "DESPACHADO";
                break;
            case 'F':
                estadoStr = "ENTREGADO";
                break;
            case 'A':
                estadoStr = "ANULADO";
                break;
            case 'P':
            default:
                estadoStr = "PENDIENTE";
                break;
        }
        dto.setEstadoPedido(estadoStr);

        // Pago
        Optional<Pago> pagoOpt = pagoRepository.findFirstByPedido_IdPedidoOrderByIdPagoDesc(pedido.getIdPedido());
        if (pagoOpt.isPresent()) {
            Pago pago = pagoOpt.get();
            dto.setMetodoPago(pago.getMetodoPago() != null ? pago.getMetodoPago() : "TARJETA_CREDITO");
            dto.setReferenciaPago(pago.getReferenciaPasarela() != null ? pago.getReferenciaPasarela() : "TXN-" + pago.getIdPago());

            char ep = pago.getEstadoTransaccion() != null ? pago.getEstadoTransaccion() : 'P';
            if (ep == 'E' || ep == 'A') {
                dto.setEstadoPago("APROBADO");
            } else if (ep == 'R') {
                dto.setEstadoPago("RECHAZADO");
            } else {
                dto.setEstadoPago("PENDIENTE");
            }
        } else {
            if (estado == 'C' || estado == 'E' || estado == 'F') {
                dto.setMetodoPago("TARJETA_CREDITO");
                dto.setReferenciaPago("TXN-ORD-" + pedido.getIdPedido());
                dto.setEstadoPago("APROBADO");
            } else if (estado == 'A') {
                dto.setMetodoPago("TARJETA_CREDITO");
                dto.setReferenciaPago("DECLINED");
                dto.setEstadoPago("RECHAZADO");
            } else {
                dto.setMetodoPago("PENDIENTE");
                dto.setReferenciaPago("PENDIENTE");
                dto.setEstadoPago("PENDIENTE");
            }
        }

        // Envío
        Optional<Envio> envioOpt = envioRepository.findByPedido_IdPedido(pedido.getIdPedido());
        BigDecimal costoEnvio = BigDecimal.ZERO;
        if (envioOpt.isPresent()) {
            Envio envio = envioOpt.get();
            dto.setEmpresaTransporte(envio.getEmpresaTransporte());
            dto.setNumeroSeguimiento(envio.getNumeroSeguimiento());
            if (envio.getCostoEnvio() != null) {
                costoEnvio = envio.getCostoEnvio();
            }
            if (envio.getFechaDespacho() != null) {
                dto.setFechaDespacho(envio.getFechaDespacho().format(FORMATO_FECHA));
            }
            if (envio.getFechaEntrega() != null) {
                dto.setFechaEntrega(envio.getFechaEntrega().format(FORMATO_FECHA));
            }
        }
        dto.setCostoEnvio(costoEnvio);

        // Financiero
        BigDecimal total = pedido.getTotal() != null ? pedido.getTotal() : BigDecimal.ZERO;
        BigDecimal descuento = pedido.getDescuento() != null ? pedido.getDescuento() : BigDecimal.ZERO;
        dto.setTotal(total);
        dto.setDescuento(descuento);
        dto.setSubtotal(total.add(descuento).subtract(costoEnvio).max(BigDecimal.ZERO));

        // Items
        List<DetallePedido> detalles = detallePedidoRepository.findByPedido_IdPedido(pedido.getIdPedido());
        List<ItemPedidoDTO> itemsDto = new ArrayList<>();
        for (DetallePedido det : detalles) {
            Producto prod = det.getProducto();
            if (prod != null) {
                imagenStorageService.cargarImagenesEnProducto(prod);
                String sku = prod.getCodigoSku();
                String nombreProd = prod.getNombreProducto();
                String cat = prod.getCategoria() != null ? prod.getCategoria().getNombreCategoria() : "General";
                String img = prod.getImagenPrincipalUrl() != null ? prod.getImagenPrincipalUrl() : "";
                BigDecimal precioUnit = det.getPrecioUnitario() != null ? det.getPrecioUnitario() : BigDecimal.ZERO;
                int cant = det.getCantidad() != null ? det.getCantidad() : 1;
                BigDecimal sub = det.getSubtotal() != null ? det.getSubtotal() : precioUnit.multiply(BigDecimal.valueOf(cant));
                itemsDto.add(new ItemPedidoDTO(sku, nombreProd, cat, img, precioUnit, cant, sub));
            }
        }
        dto.setItems(itemsDto);

        return dto;
    }

    private String serializarPedidosAJson(List<PedidoBackofficeDTO> pedidos) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < pedidos.size(); i++) {
            PedidoBackofficeDTO p = pedidos.get(i);
            sb.append("{");
            sb.append("\"idPedido\":").append(p.getIdPedido()).append(",");
            sb.append("\"codigoPedido\":\"").append(escapeJson(p.getCodigoPedido())).append("\",");
            sb.append("\"fechaCreacion\":\"").append(escapeJson(p.getFechaCreacion())).append("\",");
            sb.append("\"clienteNombre\":\"").append(escapeJson(p.getClienteNombre())).append("\",");
            sb.append("\"clienteEmail\":\"").append(escapeJson(p.getClienteEmail())).append("\",");
            sb.append("\"clienteTelefono\":\"").append(escapeJson(p.getClienteTelefono())).append("\",");
            sb.append("\"direccionEntrega\":\"").append(escapeJson(p.getDireccionEntrega())).append("\",");
            sb.append("\"departamento\":\"").append(escapeJson(p.getDepartamento())).append("\",");
            sb.append("\"municipio\":\"").append(escapeJson(p.getMunicipio())).append("\",");
            sb.append("\"estadoPedido\":\"").append(escapeJson(p.getEstadoPedido())).append("\",");
            sb.append("\"estadoPago\":\"").append(escapeJson(p.getEstadoPago())).append("\",");
            sb.append("\"metodoPago\":\"").append(escapeJson(p.getMetodoPago())).append("\",");
            sb.append("\"referenciaPago\":\"").append(escapeJson(p.getReferenciaPago())).append("\",");
            sb.append("\"subtotal\":").append(p.getSubtotal() != null ? p.getSubtotal() : 0).append(",");
            sb.append("\"descuento\":").append(p.getDescuento() != null ? p.getDescuento() : 0).append(",");
            sb.append("\"costoEnvio\":").append(p.getCostoEnvio() != null ? p.getCostoEnvio() : 0).append(",");
            sb.append("\"total\":").append(p.getTotal() != null ? p.getTotal() : 0).append(",");
            sb.append("\"empresaTransporte\":\"").append(escapeJson(p.getEmpresaTransporte())).append("\",");
            sb.append("\"numeroSeguimiento\":\"").append(escapeJson(p.getNumeroSeguimiento())).append("\",");
            sb.append("\"notas\":\"").append(escapeJson(p.getNotas())).append("\",");
            sb.append("\"cantidadTotalArticulos\":").append(p.getCantidadTotalArticulos()).append(",");
            sb.append("\"items\":[");
            if (p.getItems() != null) {
                for (int j = 0; j < p.getItems().size(); j++) {
                    ItemPedidoDTO it = p.getItems().get(j);
                    sb.append("{");
                    sb.append("\"sku\":\"").append(escapeJson(it.getSku())).append("\",");
                    sb.append("\"nombreProducto\":\"").append(escapeJson(it.getNombreProducto())).append("\",");
                    sb.append("\"precioUnitario\":").append(it.getPrecioUnitario() != null ? it.getPrecioUnitario() : 0).append(",");
                    sb.append("\"cantidad\":").append(it.getCantidad() != null ? it.getCantidad() : 0).append(",");
                    sb.append("\"subtotal\":").append(it.getSubtotal() != null ? it.getSubtotal() : 0);
                    sb.append("}");
                    if (j < p.getItems().size() - 1) sb.append(",");
                }
            }
            sb.append("]}");
            if (i < pedidos.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\b", "\\b")
                  .replace("\f", "\\f")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
