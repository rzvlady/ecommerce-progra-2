package com.sv.grupo1.backoffice.controller;

import com.sv.grupo1.backoffice.dto.DespachoPedidoDTO;
import com.sv.grupo1.backoffice.dto.ItemPedidoDTO;
import com.sv.grupo1.backoffice.dto.PedidoBackofficeDTO;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(path = "/backoffice/pedidos")
public class BackofficePedidoController {

    private static final Logger LOG = LoggerFactory.getLogger(BackofficePedidoController.class);

    @GetMapping
    public String vistaPedidos(
            @RequestParam(name = "estado", required = false, defaultValue = "TODOS") String filtroEstado,
            Model model) {
        LOG.info("Cargando boceto de la vista de gestión de pedidos en backoffice. Filtro: {}", filtroEstado);

        List<PedidoBackofficeDTO> pedidos = obtenerPedidosEjemplo();

        long totalPedidos = pedidos.size();
        long pendientesPago = pedidos.stream().filter(p -> "PENDIENTE".equalsIgnoreCase(p.getEstadoPedido())).count();
        long listosDespacho = pedidos.stream().filter(p -> "PAGADO".equalsIgnoreCase(p.getEstadoPedido()) || "EN_PREPARACION".equalsIgnoreCase(p.getEstadoPedido())).count();
        long enTransito = pedidos.stream().filter(p -> "DESPACHADO".equalsIgnoreCase(p.getEstadoPedido())).count();
        long entregados = pedidos.stream().filter(p -> "ENTREGADO".equalsIgnoreCase(p.getEstadoPedido())).count();

        BigDecimal montoTotalFacturado = pedidos.stream()
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
        model.addAttribute("montoTotalFacturado", montoTotalFacturado);
        model.addAttribute("pedidosJson", serializarPedidosAJson(pedidos));

        if (!model.containsAttribute("despachoDTO")) {
            model.addAttribute("despachoDTO", new DespachoPedidoDTO());
        }

        return "backoffice/pedidos/lista";
    }

    @PostMapping("/despachar")
    public String registrarDespacho(
            @Valid @ModelAttribute("despachoDTO") DespachoPedidoDTO despachoDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al registrar despacho: verifique los datos ingresados.");
            return "redirect:/backoffice/pedidos";
        }

        LOG.info("Simulando despacho de pedido #{} con transporte {} y guía {}",
                despachoDTO.getIdPedido(), despachoDTO.getEmpresaTransporte(), despachoDTO.getNumeroSeguimiento());

        redirectAttributes.addFlashAttribute("mensajeExito",
                "¡Pedido #" + despachoDTO.getIdPedido() + " despachado con éxito! Guía: "
                        + despachoDTO.getNumeroSeguimiento() + " (" + despachoDTO.getEmpresaTransporte() + ").");

        return "redirect:/backoffice/pedidos";
    }

    @PostMapping("/cambiar-estado")
    public String cambiarEstadoPedido(
            @RequestParam("idPedido") Integer idPedido,
            @RequestParam("nuevoEstado") String nuevoEstado,
            RedirectAttributes redirectAttributes) {

        LOG.info("Cambiando estado de pedido #{} a {}", idPedido, nuevoEstado);

        redirectAttributes.addFlashAttribute("mensajeExito",
                "El estado del pedido #" + idPedido + " ha sido actualizado a " + nuevoEstado + ".");

        return "redirect:/backoffice/pedidos";
    }

    /**
     * Datos de prueba representativos para el boceto visual.
     */
    private List<PedidoBackofficeDTO> obtenerPedidosEjemplo() {
        List<PedidoBackofficeDTO> lista = new ArrayList<>();

        // Pedido 1: Pagado / Listo para despacho
        PedidoBackofficeDTO p1 = new PedidoBackofficeDTO();
        p1.setIdPedido(1081);
        p1.setCodigoPedido("ORD-2026-1081");
        p1.setFechaCreacion("2026-10-23 15:42");
        p1.setClienteNombre("Carlos Mendoza");
        p1.setClienteEmail("carlos.mendoza@email.com");
        p1.setClienteTelefono("+503 7845-1234");
        p1.setDireccionEntrega("Colonia Escalón, Calle El Mirador #452");
        p1.setDepartamento("San Salvador");
        p1.setMunicipio("San Salvador Centro");
        p1.setEstadoPedido("PAGADO");
        p1.setEstadoPago("APROBADO");
        p1.setMetodoPago("TARJETA_CREDITO");
        p1.setReferenciaPago("TXN-VISA-994182");
        p1.setSubtotal(new BigDecimal("185.00"));
        p1.setDescuento(new BigDecimal("10.00"));
        p1.setCostoEnvio(new BigDecimal("5.00"));
        p1.setTotal(new BigDecimal("180.00"));
        p1.setNotas("Entregar preferiblemente por la tarde");
        List<ItemPedidoDTO> items1 = new ArrayList<>();
        items1.add(new ItemPedidoDTO("SKU-AUD-001", "Auriculares Inalámbricos Pro Noise Cancelling", "Electrónica", "/backoffice/img/sample-headphones.png", new BigDecimal("120.00"), 1, new BigDecimal("120.00")));
        items1.add(new ItemPedidoDTO("SKU-ACC-004", "Mouse Ergonómico Bluetooth Recargable", "Accesorios", "/backoffice/img/sample-mouse.png", new BigDecimal("32.50"), 2, new BigDecimal("65.00")));
        p1.setItems(items1);
        lista.add(p1);

        // Pedido 2: En tránsito / Despachado
        PedidoBackofficeDTO p2 = new PedidoBackofficeDTO();
        p2.setIdPedido(1080);
        p2.setCodigoPedido("ORD-2026-1080");
        p2.setFechaCreacion("2026-10-23 11:15");
        p2.setClienteNombre("Sofía Vanessa Reyes");
        p2.setClienteEmail("sofia.reyes@gmail.com");
        p2.setClienteTelefono("+503 7122-8899");
        p2.setDireccionEntrega("Residencial Santa Teresa, Senda 4, Polígono B #12");
        p2.setDepartamento("La Libertad");
        p2.setMunicipio("Santa Tecla");
        p2.setEstadoPedido("DESPACHADO");
        p2.setEstadoPago("APROBADO");
        p2.setMetodoPago("TARJETA_DEBITO");
        p2.setReferenciaPago("TXN-MASTERCARD-44219");
        p2.setSubtotal(new BigDecimal("350.00"));
        p2.setDescuento(BigDecimal.ZERO);
        p2.setCostoEnvio(new BigDecimal("4.50"));
        p2.setTotal(new BigDecimal("354.50"));
        p2.setEmpresaTransporte("DHL Express");
        p2.setNumeroSeguimiento("TRK-DHL-992381");
        p2.setFechaDespacho("2026-10-23 14:00");
        List<ItemPedidoDTO> items2 = new ArrayList<>();
        items2.add(new ItemPedidoDTO("SKU-GAM-002", "Monitor Gamer 27\" IPS 165Hz QHD", "Electrónica", "", new BigDecimal("350.00"), 1, new BigDecimal("350.00")));
        p2.setItems(items2);
        lista.add(p2);

        // Pedido 3: Pendiente de pago
        PedidoBackofficeDTO p3 = new PedidoBackofficeDTO();
        p3.setIdPedido(1079);
        p3.setCodigoPedido("ORD-2026-1079");
        p3.setFechaCreacion("2026-10-23 09:30");
        p3.setClienteNombre("Mauricio Alejandro López");
        p3.setClienteEmail("mlopez@outlook.com");
        p3.setClienteTelefono("+503 7988-1122");
        p3.setDireccionEntrega("Condominio Las Vistas, Apto 402");
        p3.setDepartamento("San Salvador");
        p3.setMunicipio("Antiguo Cuscatlán");
        p3.setEstadoPedido("PENDIENTE");
        p3.setEstadoPago("PENDIENTE");
        p3.setMetodoPago("TRANSFERENCIA");
        p3.setReferenciaPago("TRANSF-PEND-771");
        p3.setSubtotal(new BigDecimal("64.99"));
        p3.setDescuento(BigDecimal.ZERO);
        p3.setCostoEnvio(new BigDecimal("3.50"));
        p3.setTotal(new BigDecimal("68.49"));
        List<ItemPedidoDTO> items3 = new ArrayList<>();
        items3.add(new ItemPedidoDTO("SKU-HOG-015", "Cafetera de Goteo Programable 12 Tazas", "Hogar", "", new BigDecimal("64.99"), 1, new BigDecimal("64.99")));
        p3.setItems(items3);
        lista.add(p3);

        // Pedido 4: Entregado
        PedidoBackofficeDTO p4 = new PedidoBackofficeDTO();
        p4.setIdPedido(1078);
        p4.setCodigoPedido("ORD-2026-1078");
        p4.setFechaCreacion("2026-10-22 14:10");
        p4.setClienteNombre("Beatriz Elizabeth Peña");
        p4.setClienteEmail("b.pena@empresa.com");
        p4.setClienteTelefono("+503 7654-3210");
        p4.setDireccionEntrega("Boulevard Los Próceres, Edificio Torre Roble, Nivel 7");
        p4.setDepartamento("San Salvador");
        p4.setMunicipio("San Salvador");
        p4.setEstadoPedido("ENTREGADO");
        p4.setEstadoPago("APROBADO");
        p4.setMetodoPago("TARJETA_CREDITO");
        p4.setReferenciaPago("TXN-VISA-882190");
        p4.setSubtotal(new BigDecimal("520.00"));
        p4.setDescuento(new BigDecimal("25.00"));
        p4.setCostoEnvio(BigDecimal.ZERO);
        p4.setTotal(new BigDecimal("495.00"));
        p4.setEmpresaTransporte("Cargo Express");
        p4.setNumeroSeguimiento("CRG-8871239");
        p4.setFechaDespacho("2026-10-22 16:30");
        p4.setFechaEntrega("2026-10-23 10:45");
        List<ItemPedidoDTO> items4 = new ArrayList<>();
        items4.add(new ItemPedidoDTO("SKU-LAP-001", "Laptop Ultrabook 14\" Core i7 16GB 512GB SSD", "Computación", "", new BigDecimal("520.00"), 1, new BigDecimal("520.00")));
        p4.setItems(items4);
        lista.add(p4);

        // Pedido 5: Anulado
        PedidoBackofficeDTO p5 = new PedidoBackofficeDTO();
        p5.setIdPedido(1075);
        p5.setCodigoPedido("ORD-2026-1075");
        p5.setFechaCreacion("2026-10-21 18:22");
        p5.setClienteNombre("Jorge Ernesto Castro");
        p5.setClienteEmail("jorge.castro@gmail.com");
        p5.setClienteTelefono("+503 7001-2233");
        p5.setDireccionEntrega("Colonia San Benito, Pasaje 3, Casa #8");
        p5.setDepartamento("San Salvador");
        p5.setMunicipio("San Salvador");
        p5.setEstadoPedido("ANULADO");
        p5.setEstadoPago("RECHAZADO");
        p5.setMetodoPago("TARJETA_CREDITO");
        p5.setReferenciaPago("TXN-DECLINED-02");
        p5.setSubtotal(new BigDecimal("89.90"));
        p5.setDescuento(BigDecimal.ZERO);
        p5.setCostoEnvio(new BigDecimal("4.00"));
        p5.setTotal(new BigDecimal("93.90"));
        p5.setNotas("Transacción rechazada por el banco emisor");
        List<ItemPedidoDTO> items5 = new ArrayList<>();
        items5.add(new ItemPedidoDTO("SKU-ACC-010", "Teclado Mecánico RGB Switch Brown", "Accesorios", "", new BigDecimal("89.90"), 1, new BigDecimal("89.90")));
        p5.setItems(items5);
        lista.add(p5);

        return lista;
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
