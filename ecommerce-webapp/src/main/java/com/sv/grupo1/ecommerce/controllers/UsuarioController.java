package com.sv.grupo1.ecommerce.controllers;

import com.sv.grupo1.ecommerce.dao.DepartamentoRepository;
import com.sv.grupo1.ecommerce.dao.MunicipioRepository;
import com.sv.grupo1.ecommerce.dto.UsuarioPerfilCompletoDTO;
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

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/mi-perfil")
public class UsuarioController {

    private static final Logger LOG = LoggerFactory.getLogger(UsuarioController.class);

    private final UsuarioService usuarioService;
    private final DepartamentoRepository departamentoRepository;
    private final MunicipioRepository municipioRepository;

    public UsuarioController(
            UsuarioService usuarioService,
            DepartamentoRepository departamentoRepository,
            MunicipioRepository municipioRepository) {
        this.usuarioService = usuarioService;
        this.departamentoRepository = departamentoRepository;
        this.municipioRepository = municipioRepository;
    }

    @GetMapping
    public String mostrarMiPerfil(Authentication authentication, Model model) {
        String correo = authentication.getName();
        LOG.info("mostrarMiPerfil() => Cargando perfil del usuario '{}'", correo);

        UsuarioPerfilCompletoDTO perfilDTO = usuarioService.obtenerPerfilCompletoPorCorreo(correo);
        model.addAttribute("perfilDTO", perfilDTO);
        cargarCatalogosFormulario(model);

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
            cargarCatalogosFormulario(model);
            return "ecommerce/pages/perfil/mi-perfil";
        }

        try {
            usuarioService.guardarPerfilCompleto(correo, perfilDTO, "MI_PERFIL_ECOMMERCE");
            redirectAttributes.addFlashAttribute(
                    "mensajeExito",
                    "Tus datos de perfil, contacto y dirección se han guardado correctamente."
            );
            return "redirect:/mi-perfil";
        } catch (IllegalArgumentException ex) {
            LOG.warn("guardarMiPerfil() => Error de negocio al guardar perfil de '{}': {}", correo, ex.getMessage());
            restaurarCamposSoloLectura(correo, perfilDTO);
            model.addAttribute("mensajeError", ex.getMessage());
            cargarCatalogosFormulario(model);
            return "ecommerce/pages/perfil/mi-perfil";
        }
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
