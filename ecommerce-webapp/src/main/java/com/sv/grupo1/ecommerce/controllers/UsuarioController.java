package com.sv.grupo1.ecommerce.controllers;

import com.sv.grupo1.ecommerce.dao.DepartamentoRepository;
import com.sv.grupo1.ecommerce.dao.MunicipioRepository;
import com.sv.grupo1.ecommerce.dao.PagoRepository;
import com.sv.grupo1.ecommerce.dao.PedidoRepository;
import com.sv.grupo1.ecommerce.dao.UsuarioRepository;
import com.sv.grupo1.ecommerce.dto.OrdenEcommerceDTO;
import com.sv.grupo1.ecommerce.dto.UsuarioPerfilCompletoDTO;
import com.sv.grupo1.ecommerce.entities.core.Pago;
import com.sv.grupo1.ecommerce.entities.core.Pedido;
import com.sv.grupo1.ecommerce.entities.seguridad.Usuario;
import com.sv.grupo1.ecommerce.enums.EstadoPedido;
import com.sv.grupo1.ecommerce.enums.GeneroEnum;
import com.sv.grupo1.ecommerce.enums.TipoContactoEnum;
import com.sv.grupo1.ecommerce.enums.TipoDireccionEnum;
import com.sv.grupo1.ecommerce.services.UsuarioService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/mi-perfil")
public class UsuarioController {

    private static final Logger LOG = LoggerFactory.getLogger(UsuarioController.class);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final UsuarioService usuarioService;
    private final DepartamentoRepository departamentoRepository;
    private final MunicipioRepository municipioRepository;
    private final PedidoRepository pedidoRepository;
    private final PagoRepository pagoRepository;
    private final UsuarioRepository usuarioRepository;

    public UsuarioController(
            UsuarioService usuarioService,
            DepartamentoRepository departamentoRepository,
            MunicipioRepository municipioRepository,
            PedidoRepository pedidoRepository,
            PagoRepository pagoRepository,
            UsuarioRepository usuarioRepository) {
        this.usuarioService = usuarioService;
        this.departamentoRepository = departamentoRepository;
        this.municipioRepository = municipioRepository;
        this.pedidoRepository = pedidoRepository;
        this.pagoRepository = pagoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public String mostrarMiPerfil(
            @RequestParam(name = "tab", required = false, defaultValue = "perfil") String tab,
            Authentication authentication,
            Model model) {
        String correo = authentication.getName();
        LOG.info("mostrarMiPerfil() => Cargando perfil del usuario '{}', tab: '{}'", correo, tab);

        UsuarioPerfilCompletoDTO perfilDTO = usuarioService.obtenerPerfilCompletoPorCorreo(correo);
        model.addAttribute("perfilDTO", perfilDTO);
        model.addAttribute("activeTab", tab);
        cargarCatalogosFormulario(model);
        cargarOrdenesUsuario(correo, model);

        return "ecommerce/pages/perfil/mi-perfil";
    }

    @PostMapping
    public String guardarMiPerfil(
            Authentication authentication,
            @Valid @ModelAttribute("perfilDTO") UsuarioPerfilCompletoDTO perfilDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        String correo = authentication.getName();
        LOG.info("guardarMiPerfil() => Procesando actualización para '{}'", correo);

        if (bindingResult.hasErrors()) {
            LOG.warn("guardarMiPerfil() => Errores de validación encontrados en el formulario de '{}'", correo);
            restaurarCamposSoloLectura(correo, perfilDTO);
            model.addAttribute("mensajeError", "Por favor revisa y corrige los campos marcados en el formulario.");
            model.addAttribute("activeTab", "perfil");
            cargarCatalogosFormulario(model);
            cargarOrdenesUsuario(correo, model);
            return "ecommerce/pages/perfil/mi-perfil";
        }

        try {
            usuarioService.guardarPerfilCompleto(correo, perfilDTO, "MI_PERFIL_ECOMMERCE");
            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "Tus datos de perfil, contacto y dirección se han guardado correctamente."
            );
            return "redirect:/mi-perfil?tab=perfil";
        } catch (IllegalArgumentException ex) {
            LOG.warn("guardarMiPerfil() => Error de negocio al guardar perfil de '{}': {}", correo, ex.getMessage());
            restaurarCamposSoloLectura(correo, perfilDTO);
            model.addAttribute("mensajeError", ex.getMessage());
            model.addAttribute("activeTab", "perfil");
            cargarCatalogosFormulario(model);
            cargarOrdenesUsuario(correo, model);
            return "ecommerce/pages/perfil/mi-perfil";
        }
    }

    private void cargarOrdenesUsuario(String correo, Model model) {
        List<OrdenEcommerceDTO> listaOrdenes = new ArrayList<>();
        try {
            Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreoInicioSesionIgnoreCase(correo);
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                List<Pedido> pedidos = pedidoRepository.findByUsuario_IdUsuarioOrderByIdPedidoDesc(usuario.getIdUsuario());
                for (Pedido p : pedidos) {
                    OrdenEcommerceDTO ord = new OrdenEcommerceDTO();
                    ord.setIdPedido(p.getIdPedido());
                    ord.setCodigoOrden("#" + p.getIdPedido());
                    ord.setFecha(p.getFechaCreacion() != null ? p.getFechaCreacion().format(FORMATO_FECHA) : "Reciente");
                    ord.setTipoEntrega("Envío a domicilio");
                    ord.setTotal(p.getTotal());

                    Optional<Pago> pagoOpt = pagoRepository.findFirstByPedido_IdPedidoOrderByIdPagoDesc(p.getIdPedido());
                    ord.setMetodoPago(pagoOpt.map(Pago::getMetodoPago).orElse("Tarjeta"));

                    char est = p.getEstadoPedido() != null ? p.getEstadoPedido() : 'P';
                    if (est == EstadoPedido.CONFIRMADO.getCodigoEstado()) {
                        ord.setEstado("Confirmado");
                        ord.setEstadoClass("badge bg-success-subtle text-success border border-success-subtle");
                        ord.setConfirmed(true);
                    } else if (est == EstadoPedido.ENVIADO.getCodigoEstado()) {
                        ord.setEstado("Enviado");
                        ord.setEstadoClass("badge bg-info-subtle text-info border border-info-subtle");
                        ord.setConfirmed(true);
                    } else if (est == EstadoPedido.FINALIZADO.getCodigoEstado()) {
                        ord.setEstado("Entregado");
                        ord.setEstadoClass("badge bg-primary-subtle text-primary border border-primary-subtle");
                        ord.setConfirmed(true);
                    } else if (est == EstadoPedido.ANULADO.getCodigoEstado()) {
                        ord.setEstado("Anulado");
                        ord.setEstadoClass("badge bg-danger-subtle text-danger border border-danger-subtle");
                        ord.setConfirmed(false);
                    } else {
                        ord.setEstado("Pendiente");
                        ord.setEstadoClass("badge bg-warning-subtle text-dark border border-warning-subtle");
                        ord.setConfirmed(false);
                    }
                    listaOrdenes.add(ord);
                }
            }
        } catch (Exception e) {
            LOG.error("Error al cargar órdenes del usuario {}: {}", correo, e.getMessage());
        }
        model.addAttribute("ordenes", listaOrdenes);
    }

    private void restaurarCamposSoloLectura(String correo, UsuarioPerfilCompletoDTO perfilDTO) {
        UsuarioPerfilCompletoDTO actual = usuarioService.obtenerPerfilCompletoPorCorreo(correo);
        perfilDTO.setCorreoPrincipal(actual.getCorreoPrincipal());
        perfilDTO.setCorreoVerificado(actual.getCorreoVerificado());
        perfilDTO.setPerfilCompletado(actual.isPerfilCompletado());
        perfilDTO.setUltimaActualizacion(actual.getUltimaActualizacion());
    }

    @GetMapping("/municipios")
    @ResponseBody
    public List<Map<String, Object>> listarMunicipiosPorDepartamento(
            @RequestParam("idDepartamento") Integer idDepartamento) {
        return municipioRepository.findByDepartamentoIdDepartamentoOrderByNombreMunicipioAsc(idDepartamento)
                .stream()
                .map(m -> Map.<String, Object>of(
                        "idMunicipio", m.getIdMunicipio(),
                        "nombreMunicipio", m.getNombreMunicipio(),
                        "idDepartamento", m.getDepartamento().getIdDepartamento()
                ))
                .toList();
    }

    private void cargarCatalogosFormulario(Model model) {
        model.addAttribute("generos", GeneroEnum.values());
        model.addAttribute("tiposDireccion", TipoDireccionEnum.values());
        model.addAttribute("tiposContacto", TipoContactoEnum.values());
        model.addAttribute("departamentos", departamentoRepository.findAllByOrderByNombreDepartamentoAsc());
        model.addAttribute("municipios", municipioRepository.findAllByOrderByNombreMunicipioAsc());
    }
}
