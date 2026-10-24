package com.sv.grupo1.backoffice.controller;

import com.sv.grupo1.backoffice.dto.ClienteBackofficeDTO;
import com.sv.grupo1.backoffice.dto.ClienteRegistroBackofficeDTO;
import com.sv.grupo1.backoffice.service.BackofficeClienteService;
import com.sv.grupo1.ecommerce.dao.DepartamentoRepository;
import com.sv.grupo1.ecommerce.dao.MunicipioRepository;
import com.sv.grupo1.ecommerce.enums.GeneroEnum;
import com.sv.grupo1.ecommerce.enums.TipoDireccionEnum;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(path = {"/backoffice/clientes", "/admin/clientes"})
@PreAuthorize("hasRole('ADMIN')")
public class BackofficeClienteController {

    private static final Logger LOG = LoggerFactory.getLogger(BackofficeClienteController.class);

    private final BackofficeClienteService clienteService;
    private final DepartamentoRepository departamentoRepository;
    private final MunicipioRepository municipioRepository;

    public BackofficeClienteController(
            BackofficeClienteService clienteService,
            DepartamentoRepository departamentoRepository,
            MunicipioRepository municipioRepository) {
        this.clienteService = clienteService;
        this.departamentoRepository = departamentoRepository;
        this.municipioRepository = municipioRepository;
    }

    @GetMapping
    public String listarClientes(
            @RequestParam(name = "busqueda", required = false) String busqueda,
            @RequestParam(name = "estado", required = false, defaultValue = "TODOS") String filtroEstado,
            Model model) {

        LOG.info("Cargando vista de gestión de clientes. Busqueda: '{}', Estado: '{}'", busqueda, filtroEstado);

        List<ClienteBackofficeDTO> clientes = clienteService.listarClientes(busqueda, filtroEstado);
        Map<String, Long> metricas = clienteService.obtenerMetricasDashboard();

        model.addAttribute("clientes", clientes);
        model.addAttribute("busqueda", busqueda != null ? busqueda : "");
        model.addAttribute("filtroEstado", filtroEstado);

        model.addAttribute("totalClientes", metricas.getOrDefault("total", 0L));
        model.addAttribute("clientesActivos", metricas.getOrDefault("activos", 0L));
        model.addAttribute("clientesInactivos", metricas.getOrDefault("inactivos", 0L));
        model.addAttribute("clientesNuevosMes", metricas.getOrDefault("nuevosMes", 0L));

        cargarCatalogos(model);

        if (!model.containsAttribute("clienteRegistroDTO")) {
            model.addAttribute("clienteRegistroDTO", new ClienteRegistroBackofficeDTO());
        }
        if (!model.containsAttribute("modoEdicion")) {
            model.addAttribute("modoEdicion", false);
        }
        if (!model.containsAttribute("abrirModalFormulario")) {
            model.addAttribute("abrirModalFormulario", false);
        }

        return "backoffice/clientes/lista";
    }

    @GetMapping("/{id}/detalle")
    @ResponseBody
    public ResponseEntity<ClienteBackofficeDTO> obtenerDetalleCliente(@PathVariable("id") Integer idUsuario) {
        try {
            ClienteBackofficeDTO detalle = clienteService.obtenerDetalleCliente(idUsuario);
            return ResponseEntity.ok(detalle);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/editar-dto")
    @ResponseBody
    public ResponseEntity<ClienteRegistroBackofficeDTO> obtenerDtoEdicion(@PathVariable("id") Integer idUsuario) {
        try {
            ClienteRegistroBackofficeDTO dto = clienteService.obtenerDtoRegistroPorId(idUsuario);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/guardar")
    public String guardarCliente(
            @Valid @ModelAttribute("clienteRegistroDTO") ClienteRegistroBackofficeDTO dto,
            BindingResult result,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        String auditor = authentication != null ? authentication.getName() : "ADMIN_SISTEMA";
        boolean esEdicion = dto.getIdUsuario() != null;

        if (result.hasErrors()) {
            LOG.warn("Errores de validación en formulario de cliente: {}", result.getAllErrors());
            cargarModeloVistaPrincipal(model);
            model.addAttribute("modoEdicion", esEdicion);
            model.addAttribute("abrirModalFormulario", true);
            return "backoffice/clientes/lista";
        }

        try {
            if (esEdicion) {
                clienteService.actualizarCliente(dto.getIdUsuario(), dto, auditor);
                redirectAttributes.addFlashAttribute("mensajeExito",
                        "Cliente '" + dto.getNombres() + " " + dto.getApellidos() + "' actualizado correctamente.");
            } else {
                clienteService.registrarCliente(dto, auditor);
                redirectAttributes.addFlashAttribute("mensajeExito",
                        "Cliente '" + dto.getNombres() + " " + dto.getApellidos() + "' registrado exitosamente.");
            }
            return "redirect:/backoffice/clientes";
        } catch (IllegalArgumentException e) {
            LOG.warn("Error de negocio al guardar cliente: {}", e.getMessage());
            cargarModeloVistaPrincipal(model);
            model.addAttribute("mensajeErrorModal", e.getMessage());
            model.addAttribute("modoEdicion", esEdicion);
            model.addAttribute("abrirModalFormulario", true);
            return "backoffice/clientes/lista";
        }
    }

    @PostMapping("/alternar-estado")
    public String alternarEstado(
            @RequestParam("idUsuario") Integer idUsuario,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String auditor = authentication != null ? authentication.getName() : "ADMIN_SISTEMA";
        try {
            clienteService.alternarEstado(idUsuario, auditor);
            redirectAttributes.addFlashAttribute("mensajeExito", "Estado del cliente actualizado correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al cambiar estado: " + e.getMessage());
        }
        return "redirect:/backoffice/clientes";
    }

    @PostMapping("/cambiar-estado")
    public String cambiarEstado(
            @RequestParam("idUsuario") Integer idUsuario,
            @RequestParam("nuevoEstado") Character nuevoEstado,
            @RequestParam(name = "motivo", required = false) String motivo,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String auditor = authentication != null ? authentication.getName() : "ADMIN_SISTEMA";
        try {
            clienteService.cambiarEstado(idUsuario, nuevoEstado, motivo, auditor);
            redirectAttributes.addFlashAttribute("mensajeExito", "El estado del cliente fue actualizado a '" + nuevoEstado + "'.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al actualizar estado: " + e.getMessage());
        }
        return "redirect:/backoffice/clientes";
    }

    @PostMapping("/eliminar")
    public String eliminarCliente(
            @RequestParam("idUsuario") Integer idUsuario,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String auditor = authentication != null ? authentication.getName() : "ADMIN_SISTEMA";
        try {
            clienteService.eliminarCliente(idUsuario, auditor);
            redirectAttributes.addFlashAttribute("mensajeExito", "El cliente ha sido dado de baja correctamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al eliminar cliente: " + e.getMessage());
        }
        return "redirect:/backoffice/clientes";
    }

    @PostMapping("/guardar-nota")
    public String guardarNota(
            @RequestParam("idUsuario") Integer idUsuario,
            @RequestParam("nota") String nota,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        String auditor = authentication != null ? authentication.getName() : "ADMIN_SISTEMA";
        try {
            clienteService.guardarNotaInterna(idUsuario, nota, auditor);
            redirectAttributes.addFlashAttribute("mensajeExito", "Nota interna guardada con éxito.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Error al guardar nota: " + e.getMessage());
        }
        return "redirect:/backoffice/clientes";
    }

    private void cargarModeloVistaPrincipal(Model model) {
        model.addAttribute("clientes", clienteService.listarClientes(null, "TODOS"));
        Map<String, Long> metricas = clienteService.obtenerMetricasDashboard();
        model.addAttribute("totalClientes", metricas.getOrDefault("total", 0L));
        model.addAttribute("clientesActivos", metricas.getOrDefault("activos", 0L));
        model.addAttribute("clientesInactivos", metricas.getOrDefault("inactivos", 0L));
        model.addAttribute("clientesNuevosMes", metricas.getOrDefault("nuevosMes", 0L));
        model.addAttribute("filtroEstado", "TODOS");
        cargarCatalogos(model);
    }

    private void cargarCatalogos(Model model) {
        model.addAttribute("departamentos", departamentoRepository.findAllByOrderByNombreDepartamentoAsc());
        model.addAttribute("municipios", municipioRepository.findAllByOrderByNombreMunicipioAsc());
        model.addAttribute("generos", GeneroEnum.values());
        model.addAttribute("tiposDireccion", TipoDireccionEnum.values());
    }
}
