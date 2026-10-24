package com.sv.grupo1.ecommerce.services.impl;

import com.sv.grupo1.backoffice.repository.RolRepository;
import com.sv.grupo1.ecommerce.dao.MunicipioRepository;
import com.sv.grupo1.ecommerce.dao.UsuarioContactoRepository;
import com.sv.grupo1.ecommerce.dao.UsuarioDireccionRepository;
import com.sv.grupo1.ecommerce.dao.UsuarioPerfilRepository;
import com.sv.grupo1.ecommerce.dao.UsuarioRepository;
import com.sv.grupo1.ecommerce.dto.RegistroUsuarioDTO;
import com.sv.grupo1.ecommerce.dto.UsuarioPerfilCompletoDTO;
import com.sv.grupo1.ecommerce.entities.catalogo.Municipio;
import com.sv.grupo1.ecommerce.entities.seguridad.Rol;
import com.sv.grupo1.ecommerce.entities.seguridad.Usuario;
import com.sv.grupo1.ecommerce.entities.seguridad.UsuarioContacto;
import com.sv.grupo1.ecommerce.entities.seguridad.UsuarioDireccion;
import com.sv.grupo1.ecommerce.entities.seguridad.UsuarioPerfil;
import com.sv.grupo1.ecommerce.entities.seguridad.UsuarioRol;
import com.sv.grupo1.ecommerce.enums.EstadoEstandarGlobal;
import com.sv.grupo1.ecommerce.enums.GeneroEnum;
import com.sv.grupo1.ecommerce.enums.TipoContactoEnum;
import com.sv.grupo1.ecommerce.enums.TipoDireccionEnum;
import com.sv.grupo1.ecommerce.services.UsuarioService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * @author ivandiaz-007
 * */
@Service
public class UsuarioServiceImpl implements UsuarioService {

    private static final Logger LOG = LoggerFactory.getLogger(UsuarioServiceImpl.class);

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioPerfilRepository usuarioPerfilRepository;
    private final UsuarioContactoRepository usuarioContactoRepository;
    private final UsuarioDireccionRepository usuarioDireccionRepository;
    private final MunicipioRepository municipioRepository;
    private final PasswordEncoder encoder;

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            UsuarioPerfilRepository usuarioPerfilRepository,
            UsuarioContactoRepository usuarioContactoRepository,
            UsuarioDireccionRepository usuarioDireccionRepository,
            MunicipioRepository municipioRepository,
            PasswordEncoder encoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.usuarioPerfilRepository = usuarioPerfilRepository;
        this.usuarioContactoRepository = usuarioContactoRepository;
        this.usuarioDireccionRepository = usuarioDireccionRepository;
        this.municipioRepository = municipioRepository;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public Usuario registrarUsuario(RegistroUsuarioDTO dto) {
        LOG.info("registrarUsuario() => Iniciando");
        String correoNormalizado = dto.getCorreoInicioSesion().trim();
        if (usuarioRepository.existsByCorreoInicioSesionIgnoreCase(correoNormalizado)) {
            throw new IllegalArgumentException("El correo electrónico ya está registrado.");
        }
        dto.setCorreoInicioSesion(correoNormalizado);
        LOG.info("registrarUsuario() => Mapeando...");
        Usuario usuario = mapperToUsuario(dto);

        Rol rolCliente = this.rolRepository.findByNombre("ROLE_CLIENTE")
                .orElseThrow(() -> new IllegalStateException("Error Crítico: El rol ROLE_CLIENTE no existe en el sistema."));

        UsuarioRol usuarioRol = new UsuarioRol();
        usuarioRol.setUsuario(usuario);
        usuarioRol.setRol(rolCliente);

        Map<String, Object> auditoriaRol = new HashMap<>();
        auditoriaRol.put("operacion", "asignacion_rol");
        auditoriaRol.put("motivo", "registro_usuario_web");
        auditoriaRol.put("rol", rolCliente.getNombre());
        auditoriaRol.put("fecha_asignacion", OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        usuarioRol.setAuditoria(auditoriaRol);

        if (usuario.getRoles() == null) {
            usuario.setRoles(new ArrayList<>());
        }

        usuario.getRoles().add(usuarioRol);

        LOG.info("registrarUsuario() => Completado");
        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional
    public void registrarUltimoInicioSesion(String correoInicioSesion) {
        if (correoInicioSesion == null || correoInicioSesion.isBlank()) {
            return;
        }
        OffsetDateTime ahora = OffsetDateTime.now();
        int filas = usuarioRepository.actualizarUltimoInicioSesionPorCorreo(correoInicioSesion.trim(), ahora);
        LOG.info("registrarUltimoInicioSesion() => Fecha de último inicio de sesión actualizada para '{}' (filas={})",
                correoInicioSesion, filas);
    }

    public Usuario mapperToUsuario(RegistroUsuarioDTO dto) {
        EstadoEstandarGlobal estandarGlobal = EstadoEstandarGlobal.ACTIVO;
        String timestampAhora = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        Usuario usuario = new Usuario();
        usuario.setCorreoInicioSesion(dto.getCorreoInicioSesion());
        usuario.setContrasenia(this.encoder.encode(dto.getContrasenia().trim()));
        usuario.setEstado(estandarGlobal.getCodigoEstado());

        Map<String, Object> metadataUsuario = new HashMap<>();
        metadataUsuario.put("origen", "REGISTRO_WEB");
        metadataUsuario.put("tipo_usuario", "cliente");
        metadataUsuario.put("fecha_registro", timestampAhora);
        usuario.setMetadata(metadataUsuario);

        UsuarioContacto usuarioContacto = new UsuarioContacto();
        usuarioContacto.setUsuario(usuario);
        usuarioContacto.setPrincipal(true);
        usuarioContacto.setVerificado(false);
        usuarioContacto.setValor(dto.getCorreoInicioSesion());
        usuarioContacto.setTipoContacto(TipoContactoEnum.CORREO.getCodigo());

        Map<String, Object> metadataContacto = new HashMap<>();
        metadataContacto.put("estado", String.valueOf(estandarGlobal.getCodigoEstado()));
        metadataContacto.put("origen", "REGISTRO_WEB");
        metadataContacto.put("creado_por", dto.getCorreoInicioSesion());
        metadataContacto.put("fecha_creacion", timestampAhora);
        usuarioContacto.setMetadata(metadataContacto);

        if (usuario.getContactos() == null) {
            usuario.setContactos(new ArrayList<>());
        }
        usuario.getContactos().add(usuarioContacto);

        return usuario;
    }

    @Override
    @Transactional
    public UsuarioPerfilCompletoDTO obtenerPerfilCompletoPorCorreo(String correoInicioSesion) {
        Usuario usuario = usuarioRepository.findByCorreoInicioSesionIgnoreCase(correoInicioSesion)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el usuario autenticado: " + correoInicioSesion));
        return construirPerfilCompletoDTO(usuario);
    }

    @Override
    @Transactional
    public UsuarioPerfilCompletoDTO obtenerPerfilCompletoPorIdUsuario(Integer idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el usuario con ID: " + idUsuario));
        return construirPerfilCompletoDTO(usuario);
    }

    private UsuarioPerfilCompletoDTO construirPerfilCompletoDTO(Usuario usuario) {
        Integer idUsuario = usuario.getIdUsuario();
        UsuarioPerfilCompletoDTO dto = new UsuarioPerfilCompletoDTO();
        dto.setIdUsuario(idUsuario);
        dto.setCorreoPrincipal(usuario.getCorreoInicioSesion());
        dto.setCorreoVerificado(false);

        // 1. Cargar Perfil (1:1) - Aprovecha usuario.getPerfil() si ya vino cargado en el grafo
        UsuarioPerfil perfilExistente = usuario.getPerfil() != null
                ? usuario.getPerfil()
                : usuarioPerfilRepository.findByUsuarioIdUsuario(idUsuario).orElse(null);

        if (perfilExistente != null) {
            dto.setIdPerfil(perfilExistente.getIdPerfil());
            dto.setNombres(perfilExistente.getNombres());
            dto.setApellidos(perfilExistente.getApellidos());
            dto.setDui(perfilExistente.getDui());
            dto.setGenero(perfilExistente.getGenero());
            dto.setFechaNacimiento(perfilExistente.getFechaNacimiento());
        }

        // 2. Cargar Contactos (Correo y Móvil)
        List<UsuarioContacto> contactos = usuarioContactoRepository.findByUsuarioIdUsuario(idUsuario);

        UsuarioContacto contactoCorreo = contactos.stream()
                .filter(c -> Character.valueOf(TipoContactoEnum.CORREO.getCodigo()).equals(c.getTipoContacto()))
                .sorted((a, b) -> Boolean.compare(Boolean.TRUE.equals(b.getPrincipal()), Boolean.TRUE.equals(a.getPrincipal())))
                .findFirst()
                .orElse(null);

        if (contactoCorreo != null) {
            dto.setIdContactoCorreo(contactoCorreo.getIdContacto());
            dto.setCorreoPrincipal(contactoCorreo.getValor());
            dto.setCorreoVerificado(Boolean.TRUE.equals(contactoCorreo.getVerificado()));
        }

        UsuarioContacto contactoMovil = contactos.stream()
                .filter(c -> Character.valueOf(TipoContactoEnum.MOVIL.getCodigo()).equals(c.getTipoContacto()))
                .sorted((a, b) -> Boolean.compare(Boolean.TRUE.equals(b.getPrincipal()), Boolean.TRUE.equals(a.getPrincipal())))
                .findFirst()
                .orElse(null);

        if (contactoMovil != null) {
            dto.setIdContactoMovil(contactoMovil.getIdContacto());
            dto.setTelefonoMovil(contactoMovil.getValor());
            Map<String, Object> metaMovil = contactoMovil.getMetadata();
            if (metaMovil != null) {
                Object whatsappObj = metaMovil.get("tiene_whatsapp");
                if (whatsappObj instanceof Boolean b) {
                    dto.setTieneWhatsapp(b);
                } else if (whatsappObj != null) {
                    dto.setTieneWhatsapp(Boolean.parseBoolean(String.valueOf(whatsappObj)));
                }
                if (metaMovil.get("fecha_actualizacion") != null) {
                    dto.setUltimaActualizacion(String.valueOf(metaMovil.get("fecha_actualizacion")));
                }
            }
        }

        // 3. Cargar Dirección Principal
        List<UsuarioDireccion> direcciones = usuarioDireccionRepository.findByUsuarioIdUsuarioOrderByIdDireccionAsc(idUsuario);
        if (!direcciones.isEmpty()) {
            UsuarioDireccion direccion = direcciones.stream()
                    .filter(d -> d.getMetadata() != null && Boolean.TRUE.equals(d.getMetadata().get("principal")))
                    .findFirst()
                    .orElse(direcciones.get(0));

            dto.setIdDireccion(direccion.getIdDireccion());
            dto.setDireccionLinea1(direccion.getDireccionLinea1());
            dto.setCodigoPostal(direccion.getCodigoPostal());
            dto.setTipoDireccion(direccion.getTipoDireccion());

            if (direccion.getMunicipio() != null) {
                dto.setIdMunicipio(direccion.getMunicipio().getIdMunicipio());
                if (direccion.getMunicipio().getDepartamento() != null) {
                    dto.setIdDepartamento(direccion.getMunicipio().getDepartamento().getIdDepartamento());
                }
            }

            Map<String, Object> metaDir = direccion.getMetadata();
            if (metaDir != null) {
                if (metaDir.get("punto_referencia") != null) {
                    dto.setPuntoReferencia(String.valueOf(metaDir.get("punto_referencia")));
                }
                if (metaDir.get("fecha_actualizacion") != null) {
                    dto.setUltimaActualizacion(String.valueOf(metaDir.get("fecha_actualizacion")));
                }
            }
        } else {
            // Valor por defecto amigable si aún no tiene dirección registrada
            dto.setTipoDireccion(TipoDireccionEnum.CASA.getCodigo());
        }

        boolean completado = dto.getIdPerfil() != null
                && dto.getIdContactoMovil() != null
                && dto.getIdDireccion() != null;
        dto.setPerfilCompletado(completado);

        return dto;
    }

    @Override
    @Transactional
    public void guardarPerfilCompleto(String correoAutenticado, UsuarioPerfilCompletoDTO dto, String origen) {
        Usuario usuario = usuarioRepository.findByCorreoInicioSesionIgnoreCase(correoAutenticado)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el usuario autenticado: " + correoAutenticado));
        guardarPerfilInterno(usuario, dto, usuario.getCorreoInicioSesion(), origen);
    }

    @Override
    @Transactional
    public void guardarPerfilCompletoPorIdUsuario(Integer idUsuario, UsuarioPerfilCompletoDTO dto, String usuarioAuditor, String origen) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el usuario con ID: " + idUsuario));
        guardarPerfilInterno(usuario, dto, usuarioAuditor, origen);
    }

    private void guardarPerfilInterno(Usuario usuario, UsuarioPerfilCompletoDTO dto, String usuarioAuditor, String origen) {
        Integer idUsuario = usuario.getIdUsuario();
        String timestampAhora = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        String origenAuditoria = (origen != null && !origen.isBlank()) ? origen : "MI_PERFIL_ECOMMERCE";

        // 1. Validar Enums y Unicidad de DUI
        if (GeneroEnum.fromCodigo(dto.getGenero()) == null) {
            throw new IllegalArgumentException("El género seleccionado no es válido.");
        }
        if (TipoDireccionEnum.fromCodigo(dto.getTipoDireccion()) == null) {
            throw new IllegalArgumentException("El tipo de dirección seleccionado no es válido.");
        }

        String duiNormalizado = normalizarDui(dto.getDui());
        if (usuarioPerfilRepository.existsByDuiIgnoreCaseAndUsuarioIdUsuarioNot(duiNormalizado, idUsuario)) {
            throw new IllegalArgumentException("El número de DUI '" + duiNormalizado + "' ya se encuentra registrado en otra cuenta.");
        }

        // 2. Guardar o Actualizar UsuarioPerfil (1:1) - Reutiliza usuario.getPerfil() si ya está cargado
        UsuarioPerfil perfil = usuario.getPerfil() != null
                ? usuario.getPerfil()
                : usuarioPerfilRepository.findByUsuarioIdUsuario(idUsuario)
                        .orElseGet(() -> {
                            UsuarioPerfil nuevo = new UsuarioPerfil();
                            nuevo.setUsuario(usuario);
                            return nuevo;
                        });

        perfil.setNombres(dto.getNombres().trim());
        perfil.setApellidos(dto.getApellidos().trim());
        perfil.setDui(duiNormalizado);
        perfil.setGenero(Character.toUpperCase(dto.getGenero()));
        perfil.setFechaNacimiento(dto.getFechaNacimiento());
        usuarioPerfilRepository.save(perfil);

        // 3. Garantizar unicidad de principal=true por tipo de contacto y guardar Contactos
        sincronizarContactoCorreoPrincipal(usuario, usuarioAuditor, origenAuditoria, timestampAhora);
        sincronizarContactoMovilPrincipal(usuario, dto, usuarioAuditor, origenAuditoria, timestampAhora);

        // 4. Guardar o Actualizar UsuarioDireccion Principal
        sincronizarDireccionPrincipal(usuario, dto, usuarioAuditor, origenAuditoria, timestampAhora);

        LOG.info("guardarPerfilInterno() => Perfil, Contacto y Dirección actualizados exitosamente para usuario ID={}", idUsuario);
    }

    private void sincronizarContactoCorreoPrincipal(Usuario usuario, String usuarioAuditor, String origen, String timestampAhora) {
        Integer idUsuario = usuario.getIdUsuario();
        char codigoCorreo = TipoContactoEnum.CORREO.getCodigo();

        List<UsuarioContacto> contactosCorreo = usuarioContactoRepository
                .findByUsuarioIdUsuarioAndTipoContacto(idUsuario, codigoCorreo);

        if (contactosCorreo.isEmpty()) {
            // Si por alguna razón no existía fila de correo en usuarios_contacto, se crea como principal
            UsuarioContacto nuevoCorreo = new UsuarioContacto();
            nuevoCorreo.setUsuario(usuario);
            nuevoCorreo.setTipoContacto(codigoCorreo);
            nuevoCorreo.setValor(usuario.getCorreoInicioSesion());
            nuevoCorreo.setPrincipal(true);
            nuevoCorreo.setVerificado(false);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("estado", String.valueOf(EstadoEstandarGlobal.ACTIVO.getCodigoEstado()));
            metadata.put("origen", origen);
            metadata.put("creado_por", usuarioAuditor);
            metadata.put("fecha_creacion", timestampAhora);
            nuevoCorreo.setMetadata(metadata);

            usuarioContactoRepository.save(nuevoCorreo);
            return;
        }

        // Asegurar que SOLO UNO tenga principal = true
        UsuarioContacto principalSeleccionado = contactosCorreo.stream()
                .filter(c -> Boolean.TRUE.equals(c.getPrincipal()))
                .findFirst()
                .orElse(contactosCorreo.get(0));

        for (UsuarioContacto c : contactosCorreo) {
            boolean debeSerPrincipal = c.getIdContacto().equals(principalSeleccionado.getIdContacto());
            if (!Boolean.valueOf(debeSerPrincipal).equals(c.getPrincipal())) {
                c.setPrincipal(debeSerPrincipal);
                usuarioContactoRepository.save(c);
            }
        }
    }

    private void sincronizarContactoMovilPrincipal(
            Usuario usuario,
            UsuarioPerfilCompletoDTO dto,
            String usuarioAuditor,
            String origen,
            String timestampAhora) {

        Integer idUsuario = usuario.getIdUsuario();
        char codigoMovil = TipoContactoEnum.MOVIL.getCodigo();
        String movilFormateado = normalizarTelefono(dto.getTelefonoMovil());

        List<UsuarioContacto> contactosMovil = usuarioContactoRepository
                .findByUsuarioIdUsuarioAndTipoContacto(idUsuario, codigoMovil);

        UsuarioContacto movilPrincipal = contactosMovil.stream()
                .filter(c -> Boolean.TRUE.equals(c.getPrincipal()))
                .findFirst()
                .orElse(contactosMovil.isEmpty() ? null : contactosMovil.get(0));

        // Regla de negocio: ningún otro contacto móvil puede quedar con principal = true
        for (UsuarioContacto otro : contactosMovil) {
            if (movilPrincipal == null || !otro.getIdContacto().equals(movilPrincipal.getIdContacto())) {
                if (Boolean.TRUE.equals(otro.getPrincipal())) {
                    otro.setPrincipal(false);
                    usuarioContactoRepository.save(otro);
                }
            }
        }

        boolean esNuevo = (movilPrincipal == null);
        if (esNuevo) {
            movilPrincipal = new UsuarioContacto();
            movilPrincipal.setUsuario(usuario);
            movilPrincipal.setTipoContacto(codigoMovil);
            movilPrincipal.setVerificado(false);
        }

        movilPrincipal.setValor(movilFormateado);
        movilPrincipal.setPrincipal(true);

        Map<String, Object> metadata = movilPrincipal.getMetadata() != null
                ? new HashMap<>(movilPrincipal.getMetadata())
                : new HashMap<>();

        metadata.put("estado", String.valueOf(EstadoEstandarGlobal.ACTIVO.getCodigoEstado()));
        metadata.put("codigo_pais", "+503");
        metadata.put("tiene_whatsapp", Boolean.TRUE.equals(dto.getTieneWhatsapp()));

        if (esNuevo || !metadata.containsKey("fecha_creacion")) {
            metadata.put("creado_por", usuarioAuditor);
            metadata.put("fecha_creacion", timestampAhora);
            metadata.put("origen_creacion", origen);
        }
        metadata.put("modificado_por", usuarioAuditor);
        metadata.put("fecha_actualizacion", timestampAhora);
        metadata.put("origen_ultima_edicion", origen);

        movilPrincipal.setMetadata(metadata);
        usuarioContactoRepository.save(movilPrincipal);
    }

    private void sincronizarDireccionPrincipal(
            Usuario usuario,
            UsuarioPerfilCompletoDTO dto,
            String usuarioAuditor,
            String origen,
            String timestampAhora) {

        Integer idUsuario = usuario.getIdUsuario();

        Municipio municipio = municipioRepository.findById(dto.getIdMunicipio())
                .orElseThrow(() -> new IllegalArgumentException("El municipio seleccionado no existe."));

        if (dto.getIdDepartamento() != null
                && municipio.getDepartamento() != null
                && !municipio.getDepartamento().getIdDepartamento().equals(dto.getIdDepartamento())) {
            throw new IllegalArgumentException("El municipio seleccionado no pertenece al departamento indicado.");
        }

        List<UsuarioDireccion> direcciones = usuarioDireccionRepository
                .findByUsuarioIdUsuarioOrderByIdDireccionAsc(idUsuario);

        UsuarioDireccion direccionPrincipal = null;
        if (dto.getIdDireccion() != null) {
            direccionPrincipal = direcciones.stream()
                    .filter(d -> d.getIdDireccion().equals(dto.getIdDireccion()))
                    .findFirst()
                    .orElse(null);
        }
        if (direccionPrincipal == null && !direcciones.isEmpty()) {
            direccionPrincipal = direcciones.stream()
                    .filter(d -> d.getMetadata() != null && Boolean.TRUE.equals(d.getMetadata().get("principal")))
                    .findFirst()
                    .orElse(direcciones.get(0));
        }

        // Desmarcar principal en otras direcciones si existieran
        for (UsuarioDireccion otra : direcciones) {
            if (direccionPrincipal == null || !otra.getIdDireccion().equals(direccionPrincipal.getIdDireccion())) {
                if (otra.getMetadata() != null && Boolean.TRUE.equals(otra.getMetadata().get("principal"))) {
                    Map<String, Object> metaOtra = new HashMap<>(otra.getMetadata());
                    metaOtra.put("principal", false);
                    otra.setMetadata(metaOtra);
                    usuarioDireccionRepository.save(otra);
                }
            }
        }

        boolean esNueva = (direccionPrincipal == null);
        if (esNueva) {
            direccionPrincipal = new UsuarioDireccion();
            direccionPrincipal.setUsuario(usuario);
        }

        direccionPrincipal.setMunicipio(municipio);
        direccionPrincipal.setTipoDireccion(Character.toUpperCase(dto.getTipoDireccion()));
        direccionPrincipal.setDireccionLinea1(dto.getDireccionLinea1().trim());
        direccionPrincipal.setCodigoPostal(
                dto.getCodigoPostal() != null && !dto.getCodigoPostal().isBlank()
                        ? dto.getCodigoPostal().trim()
                        : null
        );

        Map<String, Object> metadata = direccionPrincipal.getMetadata() != null
                ? new HashMap<>(direccionPrincipal.getMetadata())
                : new HashMap<>();

        metadata.put("estado", String.valueOf(EstadoEstandarGlobal.ACTIVO.getCodigoEstado()));
        metadata.put("principal", true);
        if (dto.getPuntoReferencia() != null && !dto.getPuntoReferencia().isBlank()) {
            metadata.put("punto_referencia", dto.getPuntoReferencia().trim());
        } else {
            metadata.remove("punto_referencia");
        }

        if (esNueva || !metadata.containsKey("fecha_creacion")) {
            metadata.put("creado_por", usuarioAuditor);
            metadata.put("fecha_creacion", timestampAhora);
            metadata.put("origen_creacion", origen);
        }
        metadata.put("modificado_por", usuarioAuditor);
        metadata.put("fecha_actualizacion", timestampAhora);
        metadata.put("origen_ultima_edicion", origen);

        direccionPrincipal.setMetadata(metadata);
        usuarioDireccionRepository.save(direccionPrincipal);
    }

    private String normalizarDui(String dui) {
        if (dui == null) {
            return "";
        }
        String limpio = dui.trim().replaceAll("[^0-9]", "");
        if (limpio.length() == 9) {
            return limpio.substring(0, 8) + "-" + limpio.substring(8);
        }
        return dui.trim();
    }

    private String normalizarTelefono(String telefono) {
        if (telefono == null) {
            return "";
        }
        String limpio = telefono.trim().replaceAll("[^0-9]", "");
        if (limpio.length() == 8) {
            return limpio.substring(0, 4) + "-" + limpio.substring(4);
        }
        return telefono.trim();
    }
}
