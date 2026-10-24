package com.sv.grupo1.backoffice.service.impl;

import com.sv.grupo1.backoffice.dto.ClienteBackofficeDTO;
import com.sv.grupo1.backoffice.dto.ClienteDireccionResumenDTO;
import com.sv.grupo1.backoffice.dto.ClientePedidoResumenDTO;
import com.sv.grupo1.backoffice.dto.ClienteRegistroBackofficeDTO;
import com.sv.grupo1.backoffice.repository.RolRepository;
import com.sv.grupo1.backoffice.service.BackofficeClienteService;
import com.sv.grupo1.ecommerce.dao.DepartamentoRepository;
import com.sv.grupo1.ecommerce.dao.MunicipioRepository;
import com.sv.grupo1.ecommerce.dao.UsuarioContactoRepository;
import com.sv.grupo1.ecommerce.dao.UsuarioDireccionRepository;
import com.sv.grupo1.ecommerce.dao.UsuarioPerfilRepository;
import com.sv.grupo1.ecommerce.dao.UsuarioRepository;
import com.sv.grupo1.ecommerce.entities.catalogo.Municipio;
import com.sv.grupo1.ecommerce.entities.seguridad.Rol;
import com.sv.grupo1.ecommerce.entities.seguridad.Usuario;
import com.sv.grupo1.ecommerce.entities.seguridad.UsuarioContacto;
import com.sv.grupo1.ecommerce.entities.seguridad.UsuarioDireccion;
import com.sv.grupo1.ecommerce.entities.seguridad.UsuarioPerfil;
import com.sv.grupo1.ecommerce.entities.seguridad.UsuarioRol;
import com.sv.grupo1.ecommerce.enums.TipoContactoEnum;
import com.sv.grupo1.ecommerce.enums.TipoDireccionEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BackofficeClienteServiceImpl implements BackofficeClienteService {

    private static final Logger LOG = LoggerFactory.getLogger(BackofficeClienteServiceImpl.class);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final UsuarioRepository usuarioRepository;
    private final UsuarioPerfilRepository usuarioPerfilRepository;
    private final UsuarioContactoRepository usuarioContactoRepository;
    private final UsuarioDireccionRepository usuarioDireccionRepository;
    private final MunicipioRepository municipioRepository;
    private final DepartamentoRepository departamentoRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public BackofficeClienteServiceImpl(
            UsuarioRepository usuarioRepository,
            UsuarioPerfilRepository usuarioPerfilRepository,
            UsuarioContactoRepository usuarioContactoRepository,
            UsuarioDireccionRepository usuarioDireccionRepository,
            MunicipioRepository municipioRepository,
            DepartamentoRepository departamentoRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioPerfilRepository = usuarioPerfilRepository;
        this.usuarioContactoRepository = usuarioContactoRepository;
        this.usuarioDireccionRepository = usuarioDireccionRepository;
        this.municipioRepository = municipioRepository;
        this.departamentoRepository = departamentoRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteBackofficeDTO> listarClientes(String busqueda, String filtroEstado) {
        LOG.info("Listando clientes. Busqueda: '{}', Filtro Estado: '{}'", busqueda, filtroEstado);
        List<Usuario> usuarios = usuarioRepository.findAllByOrderByIdUsuarioDesc();

        String busquedaNorm = busqueda != null ? busqueda.trim().toLowerCase() : "";
        String estadoNorm = filtroEstado != null ? filtroEstado.trim().toUpperCase() : "";

        return usuarios.stream()
                .filter(u -> esCliente(u))
                .map(this::mapearAClienteDTO)
                .filter(c -> {
                    if (!estadoNorm.isEmpty() && !"TODOS".equals(estadoNorm)) {
                        String st = c.getEstado() != null ? String.valueOf(c.getEstado()).toUpperCase() : "I";
                        if (!estadoNorm.equals(st)) {
                            return false;
                        }
                    }
                    if (!busquedaNorm.isEmpty()) {
                        boolean coincideNombre = c.getNombreCompleto() != null && c.getNombreCompleto().toLowerCase().contains(busquedaNorm);
                        boolean coincideCorreo = c.getCorreo() != null && c.getCorreo().toLowerCase().contains(busquedaNorm);
                        boolean coincideDui = c.getDui() != null && c.getDui().toLowerCase().contains(busquedaNorm);
                        boolean coincideTelefono = c.getTelefono() != null && c.getTelefono().toLowerCase().contains(busquedaNorm);
                        return coincideNombre || coincideCorreo || coincideDui || coincideTelefono;
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteBackofficeDTO obtenerDetalleCliente(Integer idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + idUsuario));
        return mapearAClienteDTO(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteRegistroBackofficeDTO obtenerDtoRegistroPorId(Integer idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + idUsuario));

        ClienteRegistroBackofficeDTO dto = new ClienteRegistroBackofficeDTO();
        dto.setIdUsuario(usuario.getIdUsuario());
        dto.setCorreo(usuario.getCorreoInicioSesion());
        dto.setEstado(usuario.getEstado());

        UsuarioPerfil perfil = usuario.getPerfil() != null ? usuario.getPerfil()
                : usuarioPerfilRepository.findByUsuarioIdUsuario(idUsuario).orElse(null);

        if (perfil != null) {
            dto.setNombres(perfil.getNombres());
            dto.setApellidos(perfil.getApellidos());
            dto.setDui(perfil.getDui());
            dto.setGenero(perfil.getGenero());
            dto.setFechaNacimiento(perfil.getFechaNacimiento());
        }

        List<UsuarioContacto> contactos = usuarioContactoRepository.findByUsuarioIdUsuario(idUsuario);
        contactos.stream()
                .filter(c -> Character.valueOf(TipoContactoEnum.MOVIL.getCodigo()).equals(c.getTipoContacto()))
                .findFirst()
                .ifPresent(tel -> {
                    dto.setTelefono(tel.getValor());
                    if (tel.getMetadata() != null && tel.getMetadata().containsKey("tiene_whatsapp")) {
                        dto.setTieneWhatsapp(Boolean.TRUE.equals(tel.getMetadata().get("tiene_whatsapp")));
                    }
                });

        List<UsuarioDireccion> direcciones = usuarioDireccionRepository.findByUsuarioIdUsuarioOrderByIdDireccionAsc(idUsuario);
        if (!direcciones.isEmpty()) {
            UsuarioDireccion dir = direcciones.get(0);
            dto.setDireccionLinea1(dir.getDireccionLinea1());
            dto.setTipoDireccion(dir.getTipoDireccion());
            if (dir.getMunicipio() != null) {
                dto.setIdMunicipio(dir.getMunicipio().getIdMunicipio());
                if (dir.getMunicipio().getDepartamento() != null) {
                    dto.setIdDepartamento(dir.getMunicipio().getDepartamento().getIdDepartamento());
                }
            }
            if (dir.getMetadata() != null && dir.getMetadata().containsKey("punto_referencia")) {
                dto.setPuntoReferencia((String) dir.getMetadata().get("punto_referencia"));
            }
        }

        if (usuario.getMetadata() != null && usuario.getMetadata().containsKey("notas_internas")) {
            dto.setNotasInternas((String) usuario.getMetadata().get("notas_internas"));
        }

        return dto;
    }

    @Override
    @Transactional
    public void registrarCliente(ClienteRegistroBackofficeDTO dto, String usuarioAuditor) {
        String correoNorm = dto.getCorreo().trim();
        if (usuarioRepository.existsByCorreoInicioSesionIgnoreCase(correoNorm)) {
            throw new IllegalArgumentException("El correo '" + correoNorm + "' ya se encuentra registrado.");
        }

        if (dto.getDui() != null && !dto.getDui().isBlank()) {
            String duiNorm = normalizarDui(dto.getDui());
            if (usuarioPerfilRepository.existsByDuiIgnoreCase(duiNorm)) {
                throw new IllegalArgumentException("El DUI '" + duiNorm + "' ya está registrado con otro cliente.");
            }
            dto.setDui(duiNorm);
        }

        Usuario usuario = new Usuario();
        usuario.setCorreoInicioSesion(correoNorm);
        
        // Asignar contraseña inicial suministrada o una contraseña temporal segura
        String passRaw = (dto.getContrasenia() != null && !dto.getContrasenia().isBlank())
                ? dto.getContrasenia().trim()
                : "Cliente" + UUID.randomUUID().toString().substring(0, 6) + "*";
        usuario.setContrasenia(passwordEncoder.encode(passRaw));
        usuario.setEstado(dto.getEstado() != null ? dto.getEstado() : 'A');

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("origen", "BACKOFFICE_ADMIN");
        metadata.put("creado_por", usuarioAuditor);
        metadata.put("fecha_creacion", OffsetDateTime.now().toString());
        if (dto.getNotasInternas() != null && !dto.getNotasInternas().isBlank()) {
            metadata.put("notas_internas", dto.getNotasInternas().trim());
        }
        usuario.setMetadata(metadata);

        Rol rolCliente = rolRepository.findByNombre("ROLE_CLIENTE")
                .orElseGet(() -> {
                    Rol r = new Rol();
                    r.setNombre("ROLE_CLIENTE");
                    return rolRepository.save(r);
                });

        UsuarioRol usuarioRol = new UsuarioRol(usuario, rolCliente);
        Map<String, Object> auditoriaRol = new HashMap<>();
        auditoriaRol.put("operacion", "alta_backoffice");
        auditoriaRol.put("asignado_por", usuarioAuditor);
        usuarioRol.setAuditoria(auditoriaRol);

        List<UsuarioRol> roles = new ArrayList<>();
        roles.add(usuarioRol);
        usuario.setRoles(roles);

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        // Perfil personal
        UsuarioPerfil perfil = new UsuarioPerfil();
        perfil.setUsuario(usuarioGuardado);
        perfil.setNombres(dto.getNombres().trim());
        perfil.setApellidos(dto.getApellidos().trim());
        perfil.setDui(dto.getDui() != null && !dto.getDui().isBlank() ? normalizarDui(dto.getDui()) : "00000000-0");
        perfil.setGenero(dto.getGenero() != null ? dto.getGenero() : 'O');
        perfil.setFechaNacimiento(dto.getFechaNacimiento());
        usuarioPerfilRepository.save(perfil);

        // Contacto Correo
        UsuarioContacto contactoCorreo = new UsuarioContacto();
        contactoCorreo.setUsuario(usuarioGuardado);
        contactoCorreo.setTipoContacto(TipoContactoEnum.CORREO.getCodigo());
        contactoCorreo.setValor(correoNorm);
        contactoCorreo.setPrincipal(true);
        contactoCorreo.setVerificado(true);
        usuarioContactoRepository.save(contactoCorreo);

        // Contacto Teléfono
        if (dto.getTelefono() != null && !dto.getTelefono().isBlank()) {
            UsuarioContacto contactoTel = new UsuarioContacto();
            contactoTel.setUsuario(usuarioGuardado);
            contactoTel.setTipoContacto(TipoContactoEnum.MOVIL.getCodigo());
            contactoTel.setValor(dto.getTelefono().trim());
            contactoTel.setPrincipal(true);
            contactoTel.setVerificado(false);
            Map<String, Object> metaTel = new HashMap<>();
            metaTel.put("tiene_whatsapp", Boolean.TRUE.equals(dto.getTieneWhatsapp()));
            contactoTel.setMetadata(metaTel);
            usuarioContactoRepository.save(contactoTel);
        }

        // Dirección si fue proporcionada
        if (dto.getIdMunicipio() != null && dto.getDireccionLinea1() != null && !dto.getDireccionLinea1().isBlank()) {
            Municipio mun = municipioRepository.findById(dto.getIdMunicipio()).orElse(null);
            if (mun != null) {
                UsuarioDireccion dir = new UsuarioDireccion();
                dir.setUsuario(usuarioGuardado);
                dir.setMunicipio(mun);
                dir.setDireccionLinea1(dto.getDireccionLinea1().trim());
                dir.setTipoDireccion(dto.getTipoDireccion() != null ? dto.getTipoDireccion() : 'C');
                Map<String, Object> metaDir = new HashMap<>();
                metaDir.put("principal", true);
                if (dto.getPuntoReferencia() != null && !dto.getPuntoReferencia().isBlank()) {
                    metaDir.put("punto_referencia", dto.getPuntoReferencia().trim());
                }
                dir.setMetadata(metaDir);
                usuarioDireccionRepository.save(dir);
            }
        }

        LOG.info("Cliente registrado exitosamente desde backoffice con ID: {}", usuarioGuardado.getIdUsuario());
    }

    @Override
    @Transactional
    public void actualizarCliente(Integer idUsuario, ClienteRegistroBackofficeDTO dto, String usuarioAuditor) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + idUsuario));

        // Actualizar estado del usuario
        if (dto.getEstado() != null) {
            usuario.setEstado(dto.getEstado());
        }

        // Notas internas y auditoría en metadata
        Map<String, Object> meta = usuario.getMetadata() != null ? new HashMap<>(usuario.getMetadata()) : new HashMap<>();
        meta.put("modificado_por", usuarioAuditor);
        meta.put("fecha_modificacion", OffsetDateTime.now().toString());
        if (dto.getNotasInternas() != null) {
            meta.put("notas_internas", dto.getNotasInternas().trim());
        }
        usuario.setMetadata(meta);
        usuarioRepository.save(usuario);

        // Actualizar perfil
        UsuarioPerfil perfil = usuario.getPerfil() != null ? usuario.getPerfil()
                : usuarioPerfilRepository.findByUsuarioIdUsuario(idUsuario).orElse(null);
        if (perfil == null) {
            perfil = new UsuarioPerfil();
            perfil.setUsuario(usuario);
        }

        if (dto.getDui() != null && !dto.getDui().isBlank()) {
            String duiNorm = normalizarDui(dto.getDui());
            if (usuarioPerfilRepository.existsByDuiIgnoreCaseAndUsuarioIdUsuarioNot(duiNorm, idUsuario)) {
                throw new IllegalArgumentException("El DUI '" + duiNorm + "' ya pertenece a otro cliente.");
            }
            perfil.setDui(duiNorm);
        }

        perfil.setNombres(dto.getNombres().trim());
        perfil.setApellidos(dto.getApellidos().trim());
        if (dto.getGenero() != null) perfil.setGenero(dto.getGenero());
        if (dto.getFechaNacimiento() != null) perfil.setFechaNacimiento(dto.getFechaNacimiento());
        usuarioPerfilRepository.save(perfil);

        // Actualizar teléfono
        List<UsuarioContacto> contactos = usuarioContactoRepository.findByUsuarioIdUsuario(idUsuario);
        UsuarioContacto contactoTel = contactos.stream()
                .filter(c -> Character.valueOf(TipoContactoEnum.MOVIL.getCodigo()).equals(c.getTipoContacto()))
                .findFirst()
                .orElse(null);

        if (dto.getTelefono() != null && !dto.getTelefono().isBlank()) {
            if (contactoTel == null) {
                contactoTel = new UsuarioContacto();
                contactoTel.setUsuario(usuario);
                contactoTel.setTipoContacto(TipoContactoEnum.MOVIL.getCodigo());
                contactoTel.setPrincipal(true);
            }
            contactoTel.setValor(dto.getTelefono().trim());
            Map<String, Object> metaTel = contactoTel.getMetadata() != null ? new HashMap<>(contactoTel.getMetadata()) : new HashMap<>();
            metaTel.put("tiene_whatsapp", Boolean.TRUE.equals(dto.getTieneWhatsapp()));
            contactoTel.setMetadata(metaTel);
            usuarioContactoRepository.save(contactoTel);
        }

        // Actualizar o crear dirección
        if (dto.getIdMunicipio() != null && dto.getDireccionLinea1() != null && !dto.getDireccionLinea1().isBlank()) {
            List<UsuarioDireccion> direcciones = usuarioDireccionRepository.findByUsuarioIdUsuarioOrderByIdDireccionAsc(idUsuario);
            UsuarioDireccion dir = !direcciones.isEmpty() ? direcciones.get(0) : new UsuarioDireccion();
            Municipio mun = municipioRepository.findById(dto.getIdMunicipio()).orElse(null);

            if (mun != null) {
                dir.setUsuario(usuario);
                dir.setMunicipio(mun);
                dir.setDireccionLinea1(dto.getDireccionLinea1().trim());
                if (dto.getTipoDireccion() != null) dir.setTipoDireccion(dto.getTipoDireccion());
                Map<String, Object> metaDir = dir.getMetadata() != null ? new HashMap<>(dir.getMetadata()) : new HashMap<>();
                metaDir.put("principal", true);
                if (dto.getPuntoReferencia() != null) metaDir.put("punto_referencia", dto.getPuntoReferencia().trim());
                dir.setMetadata(metaDir);
                usuarioDireccionRepository.save(dir);
            }
        }

        LOG.info("Cliente ID {} actualizado por {}", idUsuario, usuarioAuditor);
    }

    @Override
    @Transactional
    public void alternarEstado(Integer idUsuario, String usuarioAuditor) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + idUsuario));
        char nuevo = (usuario.getEstado() != null && (usuario.getEstado() == 'A' || usuario.getEstado() == 'a')) ? 'I' : 'A';
        cambiarEstado(idUsuario, nuevo, "Cambio rápido de estado desde listado", usuarioAuditor);
    }

    @Override
    @Transactional
    public void cambiarEstado(Integer idUsuario, Character nuevoEstado, String motivo, String usuarioAuditor) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + idUsuario));
        usuario.setEstado(nuevoEstado);

        Map<String, Object> meta = usuario.getMetadata() != null ? new HashMap<>(usuario.getMetadata()) : new HashMap<>();
        meta.put("ultimo_cambio_estado", Map.of(
                "estado_anterior", String.valueOf(usuario.getEstado()),
                "nuevo_estado", String.valueOf(nuevoEstado),
                "motivo", motivo != null ? motivo : "Sin motivo especificado",
                "cambiado_por", usuarioAuditor,
                "fecha", OffsetDateTime.now().toString()
        ));
        usuario.setMetadata(meta);
        usuarioRepository.save(usuario);

        LOG.info("Estado del cliente ID {} modificado a {} por {}", idUsuario, nuevoEstado, usuarioAuditor);
    }

    @Override
    @Transactional
    public void eliminarCliente(Integer idUsuario, String usuarioAuditor) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + idUsuario));

        Map<String, Object> meta = usuario.getMetadata() != null ? new HashMap<>(usuario.getMetadata()) : new HashMap<>();
        meta.put("eliminado_por", usuarioAuditor);
        meta.put("fecha_eliminacion", OffsetDateTime.now().toString());
        usuario.setMetadata(meta);
        usuarioRepository.save(usuario);

        usuarioRepository.deleteById(idUsuario);
        LOG.info("Baja lógica aplicada al cliente ID {} por {}", idUsuario, usuarioAuditor);
    }

    @Override
    @Transactional
    public void guardarNotaInterna(Integer idUsuario, String nota, String usuarioAuditor) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + idUsuario));

        Map<String, Object> meta = usuario.getMetadata() != null ? new HashMap<>(usuario.getMetadata()) : new HashMap<>();
        meta.put("notas_internas", nota != null ? nota.trim() : "");
        meta.put("nota_actualizada_por", usuarioAuditor);
        meta.put("fecha_nota", OffsetDateTime.now().toString());
        usuario.setMetadata(meta);
        usuarioRepository.save(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> obtenerMetricasDashboard() {
        List<Usuario> clientes = usuarioRepository.findAll().stream()
                .filter(this::esCliente)
                .collect(Collectors.toList());

        long total = clientes.size();
        long activos = clientes.stream()
                .filter(c -> c.getEstado() != null && (c.getEstado() == 'A' || c.getEstado() == 'a'))
                .count();
        long inactivos = total - activos;

        OffsetDateTime inicioMes = OffsetDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        long nuevosMes = clientes.stream()
                .filter(c -> c.getFechaCreacion() != null && c.getFechaCreacion().isAfter(inicioMes))
                .count();

        Map<String, Long> metricas = new HashMap<>();
        metricas.put("total", total);
        metricas.put("activos", activos);
        metricas.put("inactivos", inactivos);
        metricas.put("nuevosMes", nuevosMes);
        return metricas;
    }

    private boolean esCliente(Usuario u) {
        if (u.getRoles() == null || u.getRoles().isEmpty()) {
            return true; // Por defecto es cliente si no tiene rol explícito de admin
        }
        return u.getRoles().stream().anyMatch(r -> r.getRol() != null && "ROLE_CLIENTE".equalsIgnoreCase(r.getRol().getNombre()))
                || u.getRoles().stream().noneMatch(r -> r.getRol() != null && "ROLE_ADMIN".equalsIgnoreCase(r.getRol().getNombre()));
    }

    private ClienteBackofficeDTO mapearAClienteDTO(Usuario usuario) {
        ClienteBackofficeDTO dto = new ClienteBackofficeDTO();
        dto.setIdUsuario(usuario.getIdUsuario());
        dto.setCorreo(usuario.getCorreoInicioSesion());
        dto.setEstado(usuario.getEstado());

        if (usuario.getFechaCreacion() != null) {
            dto.setFechaRegistro(usuario.getFechaCreacion().format(FORMATO_FECHA));
        } else {
            dto.setFechaRegistro("N/D");
        }

        if (usuario.getUltimoInicioSesion() != null) {
            dto.setUltimoInicioSesion(usuario.getUltimoInicioSesion().format(FORMATO_FECHA));
        } else {
            dto.setUltimoInicioSesion("Nunca");
        }

        // Perfil
        UsuarioPerfil perfil = usuario.getPerfil() != null ? usuario.getPerfil()
                : usuarioPerfilRepository.findByUsuarioIdUsuario(usuario.getIdUsuario()).orElse(null);

        if (perfil != null) {
            dto.setNombres(perfil.getNombres());
            dto.setApellidos(perfil.getApellidos());
            dto.setDui(perfil.getDui());
            dto.setGenero(perfil.getGenero());
            if (perfil.getFechaNacimiento() != null) {
                dto.setFechaNacimiento(perfil.getFechaNacimiento().toString());
            }
        }

        // Contactos
        List<UsuarioContacto> contactos = usuarioContactoRepository.findByUsuarioIdUsuario(usuario.getIdUsuario());
        contactos.stream()
                .filter(c -> Character.valueOf(TipoContactoEnum.MOVIL.getCodigo()).equals(c.getTipoContacto()))
                .findFirst()
                .ifPresent(tel -> {
                    dto.setTelefono(tel.getValor());
                    if (tel.getMetadata() != null && tel.getMetadata().containsKey("tiene_whatsapp")) {
                        dto.setTieneWhatsapp(Boolean.TRUE.equals(tel.getMetadata().get("tiene_whatsapp")));
                    }
                });

        // Direcciones
        List<UsuarioDireccion> direcciones = usuarioDireccionRepository.findByUsuarioIdUsuarioOrderByIdDireccionAsc(usuario.getIdUsuario());
        List<ClienteDireccionResumenDTO> dirsDto = new ArrayList<>();
        for (UsuarioDireccion d : direcciones) {
            ClienteDireccionResumenDTO dirDto = new ClienteDireccionResumenDTO();
            dirDto.setIdDireccion(d.getIdDireccion());
            dirDto.setDireccion(d.getDireccionLinea1());
            dirDto.setCodigoPostal(d.getCodigoPostal());
            dirDto.setTipo(d.getTipoDireccion() != null ? String.valueOf(d.getTipoDireccion()) : "C");
            if (d.getMunicipio() != null) {
                dirDto.setMunicipio(d.getMunicipio().getNombreMunicipio());
                if (d.getMunicipio().getDepartamento() != null) {
                    dirDto.setDepartamento(d.getMunicipio().getDepartamento().getNombreDepartamento());
                }
            }
            if (d.getMetadata() != null) {
                dirDto.setPuntoReferencia((String) d.getMetadata().get("punto_referencia"));
                dirDto.setPrincipal(Boolean.TRUE.equals(d.getMetadata().get("principal")));
            }
            dirsDto.add(dirDto);
        }
        dto.setDirecciones(dirsDto);

        if (!dirsDto.isEmpty()) {
            ClienteDireccionResumenDTO dirP = dirsDto.stream().filter(d -> Boolean.TRUE.equals(d.getPrincipal())).findFirst().orElse(dirsDto.get(0));
            dto.setDireccionPrincipal(dirP.getDireccion());
            dto.setMunicipio(dirP.getMunicipio());
            dto.setDepartamento(dirP.getDepartamento());
        }

        // Notas internas
        if (usuario.getMetadata() != null && usuario.getMetadata().containsKey("notas_internas")) {
            dto.setNotasInternas((String) usuario.getMetadata().get("notas_internas"));
        }

        // Mapear historial de pedidos vinculados
        asociarPedidosEjemplo(dto);

        return dto;
    }

    private void asociarPedidosEjemplo(ClienteBackofficeDTO dto) {
        // Enlazar con pedidos de prueba del ecosistema del backoffice
        List<ClientePedidoResumenDTO> lista = new ArrayList<>();
        String correo = dto.getCorreo() != null ? dto.getCorreo().toLowerCase() : "";

        if (correo.contains("carlos.mendoza")) {
            lista.add(new ClientePedidoResumenDTO(1081, "ORD-2026-1081", "2026-10-23 15:42", "PAGADO", "TARJETA_CREDITO", new BigDecimal("180.00"), 3));
        } else if (correo.contains("sofia.reyes")) {
            lista.add(new ClientePedidoResumenDTO(1080, "ORD-2026-1080", "2026-10-23 11:15", "DESPACHADO", "TARJETA_DEBITO", new BigDecimal("354.50"), 1));
        } else if (correo.contains("mlopez") || correo.contains("mauricio")) {
            lista.add(new ClientePedidoResumenDTO(1079, "ORD-2026-1079", "2026-10-23 09:30", "PENDIENTE", "TRANSFERENCIA", new BigDecimal("68.49"), 1));
        } else if (correo.contains("b.pena") || correo.contains("beatriz")) {
            lista.add(new ClientePedidoResumenDTO(1078, "ORD-2026-1078", "2026-10-22 14:10", "ENTREGADO", "TARJETA_CREDITO", new BigDecimal("495.00"), 1));
        } else if (correo.contains("jorge.castro")) {
            lista.add(new ClientePedidoResumenDTO(1075, "ORD-2026-1075", "2026-10-21 18:22", "ANULADO", "TARJETA_CREDITO", new BigDecimal("93.90"), 1));
        }

        dto.setPedidos(lista);
        dto.setTotalPedidos(lista.size());
        BigDecimal total = lista.stream()
                .filter(p -> !"ANULADO".equalsIgnoreCase(p.getEstadoPedido()))
                .map(ClientePedidoResumenDTO::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTotalGastado(total);
    }

    private String normalizarDui(String dui) {
        if (dui == null) return null;
        String soloDigitos = dui.replaceAll("\\D", "");
        if (soloDigitos.length() == 9) {
            return soloDigitos.substring(0, 8) + "-" + soloDigitos.substring(8);
        }
        return dui.trim();
    }
}
