package com.sv.grupo1.backoffice.service.impl;

import com.sv.grupo1.backoffice.dto.UsuarioRolAsignacionDTO;
import com.sv.grupo1.backoffice.repository.RolRepository;
import com.sv.grupo1.backoffice.repository.UsuarioRolRepository;
import com.sv.grupo1.backoffice.service.BackofficeUsuarioRolService;
import com.sv.grupo1.ecommerce.entities.seguridad.Rol;
import com.sv.grupo1.ecommerce.entities.seguridad.Usuario;
import com.sv.grupo1.ecommerce.entities.seguridad.UsuarioPerfil;
import com.sv.grupo1.ecommerce.entities.seguridad.UsuarioRol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BackofficeUsuarioRolServiceImpl implements BackofficeUsuarioRolService {

    private static final Logger LOG = LoggerFactory.getLogger(BackofficeUsuarioRolServiceImpl.class);

    private final UsuarioRolRepository usuarioRolRepository;
    private final RolRepository rolRepository;

    public BackofficeUsuarioRolServiceImpl(
            UsuarioRolRepository usuarioRolRepository,
            RolRepository rolRepository) {
        this.usuarioRolRepository = usuarioRolRepository;
        this.rolRepository = rolRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioRolAsignacionDTO> listarUsuariosConRol() {
        return usuarioRolRepository.listarUsuariosConPerfilYRoles()
                .stream()
                .map(this::convertirADto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioRolAsignacionDTO obtenerPorIdUsuario(Integer idUsuario) {
        if (idUsuario == null) {
            throw new IllegalArgumentException("El ID del usuario es obligatorio.");
        }
        Usuario usuario = usuarioRolRepository.buscarUsuarioConRolesPorId(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el usuario con ID: " + idUsuario));
        return convertirADto(usuario);
    }

    @Override
    @Transactional
    public void cambiarRolUsuario(UsuarioRolAsignacionDTO dto, String correoAdminAutenticado) {
        if (dto == null || dto.getIdUsuario() == null) {
            throw new IllegalArgumentException("El usuario a modificar es obligatorio.");
        }
        if (dto.getIdRol() == null) {
            throw new IllegalArgumentException("Debe seleccionar el nuevo rol a asignar.");
        }

        String motivoLimpio = dto.getMotivo() != null ? dto.getMotivo().trim() : "";
        if (motivoLimpio.isEmpty()) {
            throw new IllegalArgumentException("Debe especificar el motivo o justificación del cambio de rol.");
        }

        Usuario usuario = usuarioRolRepository.buscarUsuarioConRolesPorId(dto.getIdUsuario())
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró el usuario seleccionado (ID: " + dto.getIdUsuario() + ")."
                ));

        Rol nuevoRol = rolRepository.findById(dto.getIdRol())
                .orElseThrow(() -> new IllegalArgumentException(
                        "El rol seleccionado no existe o fue eliminado."
                ));

        UsuarioRol asignacionActual = obtenerAsignacionPrincipal(usuario);
        Integer idRolActual = (asignacionActual != null && asignacionActual.getRol() != null)
                ? asignacionActual.getRol().getIdRol()
                : null;
        String nombreRolAnterior = (asignacionActual != null && asignacionActual.getRol() != null)
                ? asignacionActual.getRol().getNombre()
                : "SIN_ROL";

        // Validar que no esté seleccionando el mismo rol que ya tiene
        if (idRolActual != null && idRolActual.equals(nuevoRol.getIdRol())) {
            throw new IllegalArgumentException(
                    "El usuario '" + usuario.getCorreoInicioSesion() + "' ya tiene asignado el rol '"
                            + nuevoRol.getNombre() + "'. Seleccione un rol diferente."
            );
        }

        // Regla de autoprotección (Punto 3): El administrador en sesión no puede quitarse ROLE_ADMIN a sí mismo
        if (correoAdminAutenticado != null
                && usuario.getCorreoInicioSesion().equalsIgnoreCase(correoAdminAutenticado.trim())
                && !"ROLE_ADMIN".equalsIgnoreCase(nuevoRol.getNombre())) {
            throw new IllegalStateException(
                    "Por seguridad, no puedes revocar tu propio rol de Administrador desde tu sesión activa."
            );
        }

        // Proteger que el sistema nunca se quede sin al menos un ROLE_ADMIN
        if ("ROLE_ADMIN".equalsIgnoreCase(nombreRolAnterior)
                && !"ROLE_ADMIN".equalsIgnoreCase(nuevoRol.getNombre())
                && idRolActual != null) {
            long totalAdmins = rolRepository.contarUsuariosPorRol(idRolActual);
            if (totalAdmins <= 1) {
                throw new IllegalStateException(
                        "No se puede cambiar el rol porque este usuario es el único Administrador activo del sistema."
                );
            }
        }

        // Reemplazar rol anterior (Opción A: 1 rol por usuario)
        usuarioRolRepository.eliminarRolesPorIdUsuario(usuario.getIdUsuario());
        usuarioRolRepository.flush();

        String autorCambio = (correoAdminAutenticado != null && !correoAdminAutenticado.isBlank())
                ? correoAdminAutenticado.trim()
                : "ADMIN_BACKOFFICE";

        Map<String, Object> auditoria = new HashMap<>();
        auditoria.put("operacion", "cambio_rol_backoffice");
        auditoria.put("asignado_por", autorCambio);
        auditoria.put("rol_anterior", nombreRolAnterior);
        auditoria.put("rol_nuevo", nuevoRol.getNombre());
        auditoria.put("motivo", motivoLimpio);
        auditoria.put("fecha_modificacion", OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));

        UsuarioRol nuevaAsignacion = new UsuarioRol(usuario, nuevoRol);
        nuevaAsignacion.setAuditoria(auditoria);
        usuarioRolRepository.save(nuevaAsignacion);

        dto.setCorreoInicioSesion(usuario.getCorreoInicioSesion());
        dto.setNombreRolActual(nuevoRol.getNombre());

        LOG.info("Rol del usuario '{}' cambiado de '{}' a '{}' por '{}'. Motivo: {}",
                usuario.getCorreoInicioSesion(), nombreRolAnterior, nuevoRol.getNombre(), autorCambio, motivoLimpio);
    }

    private UsuarioRol obtenerAsignacionPrincipal(Usuario usuario) {
        if (usuario.getRoles() == null || usuario.getRoles().isEmpty()) {
            return null;
        }
        return usuario.getRoles().get(0);
    }

    private UsuarioRolAsignacionDTO convertirADto(Usuario usuario) {
        UsuarioRolAsignacionDTO dto = new UsuarioRolAsignacionDTO();
        dto.setIdUsuario(usuario.getIdUsuario());
        dto.setCorreoInicioSesion(usuario.getCorreoInicioSesion());
        dto.setEstadoUsuario(usuario.getEstado() != null ? usuario.getEstado() : 'A');
        dto.setUltimoInicioSesion(usuario.getUltimoInicioSesion());

        UsuarioPerfil perfil = usuario.getPerfil();
        if (perfil != null
                && perfil.getNombres() != null && !perfil.getNombres().isBlank()
                && perfil.getApellidos() != null && !perfil.getApellidos().isBlank()) {
            dto.setNombreCompleto(perfil.getNombres().trim() + " " + perfil.getApellidos().trim());
            dto.setTienePerfil(true);
        } else {
            dto.setNombreCompleto("Sin perfil completado");
            dto.setTienePerfil(false);
        }

        UsuarioRol asignacion = obtenerAsignacionPrincipal(usuario);
        if (asignacion != null && asignacion.getRol() != null) {
            dto.setIdRol(asignacion.getRol().getIdRol());
            dto.setNombreRolActual(asignacion.getRol().getNombre());
            dto.setFechaAsignacionRol(asignacion.getFechaAsignacion());

            Map<String, Object> aud = asignacion.getAuditoria();
            if (aud != null) {
                Object asignadoPorObj = aud.get("asignado_por");
                if (asignadoPorObj != null && !asignadoPorObj.toString().isBlank()) {
                    dto.setAsignadoPor(asignadoPorObj.toString());
                } else if ("registro_usuario_web".equalsIgnoreCase(String.valueOf(aud.get("motivo")))) {
                    dto.setAsignadoPor("Registro Web");
                } else {
                    dto.setAsignadoPor("Sistema");
                }

                Object rolAntObj = aud.get("rol_anterior");
                if (rolAntObj != null && !rolAntObj.toString().isBlank()) {
                    dto.setRolAnteriorAuditoria(rolAntObj.toString());
                }

                Object motivoObj = aud.get("motivo");
                if (motivoObj != null && !motivoObj.toString().isBlank()) {
                    String mot = motivoObj.toString();
                    dto.setMotivoUltimaAuditoria(
                            "registro_usuario_web".equalsIgnoreCase(mot)
                                    ? "Asignación automática al registrarse en tienda"
                                    : mot
                    );
                } else {
                    dto.setMotivoUltimaAuditoria("Asignación inicial del sistema");
                }
            } else {
                dto.setAsignadoPor("Sistema");
                dto.setMotivoUltimaAuditoria("Asignación inicial del sistema");
            }
        } else {
            dto.setNombreRolActual("SIN_ROL");
            dto.setAsignadoPor("—");
            dto.setMotivoUltimaAuditoria("Sin rol asignado");
        }

        return dto;
    }
}
