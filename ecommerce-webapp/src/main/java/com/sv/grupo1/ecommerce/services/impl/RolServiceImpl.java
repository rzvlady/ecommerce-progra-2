package com.sv.grupo1.ecommerce.services.impl;

import com.sv.grupo1.backoffice.repository.RolRepository;
import com.sv.grupo1.ecommerce.dto.RolDTO;
import com.sv.grupo1.ecommerce.entities.seguridad.Rol;
import com.sv.grupo1.ecommerce.services.RolService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
public class RolServiceImpl implements RolService {

    private static final String PREFIJO_ROL = "ROLE_";
    private static final int LONGITUD_MAXIMA_NOMBRE = 50;

    private static final Set<String> ROLES_SISTEMA = Set.of(
            "ROLE_ADMIN",
            "ROLE_CLIENTE",
            "ROLE_VENDEDOR",
            "ROLE_SOPORTE"
    );

    private final RolRepository rolRepository;

    public RolServiceImpl(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolDTO> listarTodos() {
        return rolRepository.findAllByOrderByIdRolAsc()
                .stream()
                .map(this::convertirADto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RolDTO obtenerDtoPorId(Integer idRol) {
        if (idRol == null) {
            throw new IllegalArgumentException("El ID del rol es obligatorio.");
        }
        Rol rol = rolRepository.findById(idRol)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el rol con ID: " + idRol));
        return convertirADto(rol);
    }

    @Override
    public String normalizarNombreRol(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del rol es obligatorio.");
        }

        // 1. Trim y convertir a MAYÚSCULAS
        String normalizado = nombre.trim().toUpperCase(Locale.ROOT);

        // 2. Reemplazar letras con acentos o diacríticos (ej. ÁÉÍÓÚÜÑ -> AEIOUUN)
        normalizado = Normalizer.normalize(normalizado, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

        // 3. Reemplazar espacios y guiones medios por guion bajo
        normalizado = normalizado.replaceAll("[\\s-]+", "_");

        // 4. Eliminar cualquier carácter especial (conservar solo A-Z, 0-9 y _)
        normalizado = normalizado.replaceAll("[^A-Z0-9_]", "");

        // 5. Colapsar guiones bajos múltiples y limpiar guiones bajos iniciales
        normalizado = normalizado.replaceAll("_+", "_")
                .replaceAll("^_+", "");

        // 6. Identificar si ya incluye el prefijo ROLE_ para no duplicarlo
        if (normalizado.equals("ROLE") || normalizado.startsWith(PREFIJO_ROL)) {
            normalizado = normalizado.length() > PREFIJO_ROL.length()
                    ? normalizado.substring(PREFIJO_ROL.length())
                    : "";
        }

        // Limpiar guiones bajos remanentes al inicio o al final del identificador base
        normalizado = normalizado.replaceAll("^_+|_+$", "");

        if (normalizado.isEmpty()) {
            throw new IllegalArgumentException(
                    "El nombre del rol debe contener al menos una letra o número válido (ej. AUDITORIA)."
            );
        }

        String resultado = PREFIJO_ROL + normalizado;

        if (resultado.length() > LONGITUD_MAXIMA_NOMBRE) {
            throw new IllegalArgumentException(
                    "El nombre normalizado del rol ('" + resultado + "') excede el límite máximo de "
                            + LONGITUD_MAXIMA_NOMBRE + " caracteres."
            );
        }

        return resultado;
    }

    @Override
    public boolean esRolSistema(String nombreRol) {
        if (nombreRol == null) {
            return false;
        }
        return ROLES_SISTEMA.contains(nombreRol.trim().toUpperCase(Locale.ROOT));
    }

    @Override
    @Transactional
    public String guardarRol(RolDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Los datos del rol no pueden ser nulos.");
        }

        String nombreNormalizado = normalizarNombreRol(dto.getNombre());
        dto.setNombre(nombreNormalizado);

        String descripcionLimpia = (dto.getDescripcion() != null && !dto.getDescripcion().isBlank())
                ? dto.getDescripcion().trim()
                : null;

        boolean esEdicion = dto.getIdRol() != null;

        if (esEdicion) {
            Rol rolExistente = rolRepository.findById(dto.getIdRol())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No se encontró el rol que intenta editar (ID: " + dto.getIdRol() + ")."
                    ));

            // Proteger el identificador técnico de los roles base del sistema
            if (esRolSistema(rolExistente.getNombre())
                    && !rolExistente.getNombre().equalsIgnoreCase(nombreNormalizado)) {
                throw new IllegalArgumentException(
                        "El rol '" + rolExistente.getNombre() + "' es un rol base del sistema y no se permite cambiar su identificador. Solo puede actualizar su descripción."
                );
            }

            Optional<Rol> colision = rolRepository.findByNombreIgnorandoEliminado(nombreNormalizado);
            if (colision.isPresent() && !colision.get().getIdRol().equals(dto.getIdRol())) {
                throw new IllegalArgumentException(
                        "Ya existe otro rol registrado con el identificador '" + nombreNormalizado + "'."
                );
            }

            rolExistente.setNombre(nombreNormalizado);
            rolExistente.setDescripcion(descripcionLimpia);
            rolRepository.save(rolExistente);
        } else {
            Optional<Rol> existente = rolRepository.findByNombreIgnorandoEliminado(nombreNormalizado);
            if (existente.isPresent()) {
                Rol registroPrevio = existente.get();
                if (Boolean.TRUE.equals(registroPrevio.getEliminado())) {
                    // Reactivar rol previamente eliminado de forma lógica
                    rolRepository.reactivarRol(registroPrevio.getIdRol(), descripcionLimpia);
                    return nombreNormalizado;
                }
                throw new IllegalArgumentException(
                        "Ya existe un rol registrado con el identificador '" + nombreNormalizado + "'."
                );
            }

            Rol nuevoRol = new Rol();
            nuevoRol.setNombre(nombreNormalizado);
            nuevoRol.setDescripcion(descripcionLimpia);
            nuevoRol.setEliminado(false);
            rolRepository.save(nuevoRol);
        }

        return nombreNormalizado;
    }

    @Override
    @Transactional
    public void eliminarRol(Integer idRol) {
        if (idRol == null) {
            throw new IllegalArgumentException("El ID del rol es obligatorio.");
        }

        Rol rol = rolRepository.findById(idRol)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el rol con ID: " + idRol));

        if (esRolSistema(rol.getNombre())) {
            throw new IllegalStateException(
                    "No se puede eliminar el rol '" + rol.getNombre() + "' porque es un rol base del sistema."
            );
        }

        long usuariosAsignados = rolRepository.contarUsuariosPorRol(idRol);
        if (usuariosAsignados > 0) {
            throw new IllegalStateException(
                    "No se puede eliminar el rol '" + rol.getNombre() + "' porque está asignado a "
                            + usuariosAsignados + " usuario(s)."
            );
        }

        try {
            rolRepository.delete(rol);
            rolRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException(
                    "No es posible eliminar el rol '" + rol.getNombre() + "' debido a registros dependientes en el sistema."
            );
        }
    }

    private RolDTO convertirADto(Rol entidad) {
        RolDTO dto = new RolDTO();
        dto.setIdRol(entidad.getIdRol());
        dto.setNombre(entidad.getNombre());
        dto.setDescripcion(entidad.getDescripcion());
        dto.setFechaCreacion(entidad.getFechaCreacion());
        dto.setRolSistema(esRolSistema(entidad.getNombre()));
        if (entidad.getIdRol() != null) {
            dto.setCantidadUsuarios(rolRepository.contarUsuariosPorRol(entidad.getIdRol()));
        }
        return dto;
    }
}
