package com.sv.grupo1.ecommerce.services.impl;

import com.sv.grupo1.ecommerce.dao.CategoriaRepository;
import com.sv.grupo1.ecommerce.dao.ProductoRepository;
import com.sv.grupo1.ecommerce.dto.CategoriaDTO;
import com.sv.grupo1.ecommerce.entities.catalogo.Categoria;
import com.sv.grupo1.ecommerce.services.CategoriaService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Implementación de la lógica de negocio para la gestión de categorías con jerarquía
 * reflexiva limitada estrictamente a 3 niveles de profundidad.
 */
@Service
public class CategoriaServiceImpl implements CategoriaService {

    private static final int NIVEL_MAXIMO_PERMITIDO = 3;
    private static final int NIVEL_MAXIMO_PADRE = 2;

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public CategoriaServiceImpl(
            CategoriaRepository categoriaRepository,
            ProductoRepository productoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaDTO> listarTodasConNivel() {
        List<Categoria> entidades = categoriaRepository.findAllConJerarquia();

        // Mapa de hijos directos para calcular subcategorías y profundidad de descendencia en memoria
        Map<Integer, List<Categoria>> hijosPorPadre = new HashMap<>();
        for (Categoria cat : entidades) {
            if (cat.getCategoriaPadre() != null && cat.getCategoriaPadre().getIdCategoria() != null) {
                hijosPorPadre
                        .computeIfAbsent(cat.getCategoriaPadre().getIdCategoria(), k -> new ArrayList<>())
                        .add(cat);
            }
        }

        List<CategoriaDTO> resultado = new ArrayList<>(entidades.size());
        for (Categoria cat : entidades) {
            CategoriaDTO dto = convertirADto(cat, hijosPorPadre);
            resultado.add(dto);
        }

        // Ordenar por ruta jerárquica para agrupar visualmente departamentos con sus categorías e hijos
        resultado.sort(Comparator.comparing(
                c -> c.getRutaJerarquica() != null ? c.getRutaJerarquica().toLowerCase(Locale.ROOT) : ""
        ));

        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaDTO> listarCandidatasPadre() {
        return listarTodasConNivel().stream()
                .filter(dto -> dto.getNivel() != null && dto.getNivel() <= NIVEL_MAXIMO_PADRE)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaDTO obtenerDtoPorId(Integer idCategoria) {
        if (idCategoria == null) {
            throw new IllegalArgumentException("El ID de la categoría es obligatorio.");
        }
        Categoria categoria = categoriaRepository.findByIdConJerarquia(idCategoria)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la categoría con ID: " + idCategoria));

        List<Categoria> todas = categoriaRepository.findAllConJerarquia();
        Map<Integer, List<Categoria>> hijosPorPadre = new HashMap<>();
        for (Categoria c : todas) {
            if (c.getCategoriaPadre() != null && c.getCategoriaPadre().getIdCategoria() != null) {
                hijosPorPadre
                        .computeIfAbsent(c.getCategoriaPadre().getIdCategoria(), k -> new ArrayList<>())
                        .add(c);
            }
        }
        return convertirADto(categoria, hijosPorPadre);
    }

    @Override
    @Transactional(readOnly = true)
    public int calcularNivel(Categoria categoria) {
        if (categoria == null) {
            return 0;
        }
        int nivel = 1;
        Categoria actual = categoria.getCategoriaPadre();
        Set<Integer> visitados = new HashSet<>();
        if (categoria.getIdCategoria() != null) {
            visitados.add(categoria.getIdCategoria());
        }

        while (actual != null) {
            if (actual.getIdCategoria() != null && !visitados.add(actual.getIdCategoria())) {
                // Protección de seguridad ante ciclos preexistentes en base de datos
                break;
            }
            nivel++;
            actual = actual.getCategoriaPadre();
        }
        return nivel;
    }

    @Override
    @Transactional
    public void guardarCategoria(CategoriaDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Los datos de la categoría no pueden ser nulos.");
        }

        String nombreLimpio = dto.getNombreCategoria() != null ? dto.getNombreCategoria().trim() : "";
        if (nombreLimpio.isEmpty()) {
            throw new IllegalArgumentException("El nombre de la categoría es obligatorio.");
        }

        String descripcionLimpia = (dto.getDescripcion() != null && !dto.getDescripcion().isBlank())
                ? dto.getDescripcion().trim()
                : null;

        boolean esEdicion = dto.getIdCategoria() != null;
        Categoria categoria;

        if (esEdicion) {
            categoria = categoriaRepository.findByIdConJerarquia(dto.getIdCategoria())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No se encontró la categoría que intenta editar (ID: " + dto.getIdCategoria() + ")."
                    ));

            if (categoriaRepository.existsByNombreCategoriaIgnoreCaseAndIdCategoriaNot(nombreLimpio, dto.getIdCategoria())) {
                throw new IllegalArgumentException("Ya existe otra categoría registrada con el nombre '" + nombreLimpio + "'.");
            }
        } else {
            categoria = new Categoria();
            if (categoriaRepository.existsByNombreCategoriaIgnoreCase(nombreLimpio)) {
                throw new IllegalArgumentException("Ya existe una categoría registrada con el nombre '" + nombreLimpio + "'.");
            }
        }

        // Validación de Categoría Padre, Prevención de Ciclos y Límite de 3 Niveles
        Categoria nuevoPadre = null;
        int nuevoNivel = 1;

        if (dto.getIdCategoriaPadre() != null) {
            // 1. No puede ser padre de sí misma
            if (esEdicion && dto.getIdCategoria().equals(dto.getIdCategoriaPadre())) {
                throw new IllegalArgumentException("Una categoría no puede asignarse a sí misma como categoría padre.");
            }

            nuevoPadre = categoriaRepository.findByIdConJerarquia(dto.getIdCategoriaPadre())
                    .orElseThrow(() -> new IllegalArgumentException("La categoría padre seleccionada no existe."));

            // 2. Prevención de ciclos: el nuevo padre no puede ser descendiente de la categoría editada
            if (esEdicion && esDescendienteDe(nuevoPadre, dto.getIdCategoria())) {
                throw new IllegalArgumentException(
                        "Referencia circular detectada: no se puede asignar como padre a una subcategoría descendiente de sí misma."
                );
            }

            // 3. Validar que el padre sea de Nivel 1 o Nivel 2 (nunca Nivel 3)
            int nivelPadre = calcularNivel(nuevoPadre);
            if (nivelPadre >= NIVEL_MAXIMO_PERMITIDO) {
                throw new IllegalArgumentException(
                        "La categoría '" + nuevoPadre.getNombreCategoria() + "' es de Nivel 3 (Subcategoría final) " +
                        "y no puede tener subcategorías hijas. El límite máximo permitido es de 3 niveles."
                );
            }

            nuevoNivel = nivelPadre + 1;
        }

        // 4. Si es edición y la categoría ya tiene subcategorías descendientes, verificar que no desborden el nivel 3
        if (esEdicion) {
            List<Categoria> todas = categoriaRepository.findAllConJerarquia();
            Map<Integer, List<Categoria>> hijosPorPadre = new HashMap<>();
            for (Categoria c : todas) {
                if (c.getCategoriaPadre() != null && c.getCategoriaPadre().getIdCategoria() != null) {
                    hijosPorPadre
                            .computeIfAbsent(c.getCategoriaPadre().getIdCategoria(), k -> new ArrayList<>())
                            .add(c);
                }
            }
            int profundidadHijos = calcularProfundidadDescendientes(dto.getIdCategoria(), hijosPorPadre, new HashSet<>());
            if (nuevoNivel + profundidadHijos > NIVEL_MAXIMO_PERMITIDO) {
                throw new IllegalArgumentException(
                        "No se puede mover esta categoría al Nivel " + nuevoNivel + " porque ya posee subcategorías hijas " +
                        "que superarían el límite máximo de 3 niveles de profundidad."
                );
            }
        }

        categoria.setNombreCategoria(nombreLimpio);
        categoria.setDescripcion(descripcionLimpia);
        categoria.setCategoriaPadre(nuevoPadre);
        categoria.setEstado(dto.getEstado() != null ? dto.getEstado() : 'A');

        categoriaRepository.save(categoria);
    }

    @Override
    @Transactional
    public void eliminarCategoria(Integer idCategoria) {
        if (idCategoria == null) {
            throw new IllegalArgumentException("El ID de la categoría es obligatorio.");
        }

        Categoria categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la categoría con ID: " + idCategoria));

        // 1. Validar que no tenga subcategorías dependientes
        long subcategorias = categoriaRepository.countByCategoriaPadre_IdCategoria(idCategoria);
        if (subcategorias > 0) {
            throw new IllegalStateException(
                    "No se puede eliminar la categoría '" + categoria.getNombreCategoria() + "' porque tiene " +
                    subcategorias + " subcategoría(s) dependiente(s). Reasigne o elimine primero sus subcategorías."
            );
        }

        // 2. Validar que no tenga productos asociados
        long productos = productoRepository.countByCategoria_IdCategoria(idCategoria);
        if (productos > 0) {
            throw new IllegalStateException(
                    "No se puede eliminar la categoría '" + categoria.getNombreCategoria() + "' porque tiene " +
                    productos + " producto(s) asociado(s) en el catálogo."
            );
        }

        try {
            categoriaRepository.delete(categoria);
            categoriaRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException(
                    "No es posible eliminar la categoría '" + categoria.getNombreCategoria() + "' debido a registros dependientes en el sistema."
            );
        }
    }

    // ==================== Métodos utilitarios privados ====================

    /**
     * Verifica si {@code candidatoPadre} es descendiente de la categoría con ID {@code idAncestro}.
     */
    private boolean esDescendienteDe(Categoria candidatoPadre, Integer idAncestro) {
        Categoria actual = candidatoPadre;
        Set<Integer> visitados = new HashSet<>();

        while (actual != null) {
            Integer idActual = actual.getIdCategoria();
            if (idActual != null) {
                if (idActual.equals(idAncestro)) {
                    return true;
                }
                if (!visitados.add(idActual)) {
                    break;
                }
            }
            actual = actual.getCategoriaPadre();
        }
        return false;
    }

    /**
     * Calcula cuántos niveles de descendencia existen debajo de una categoría:
     * 0 = sin hijos, 1 = tiene hijos directos, 2 = tiene hijos y nietos.
     */
    private int calcularProfundidadDescendientes(
            Integer idCategoria,
            Map<Integer, List<Categoria>> hijosPorPadre,
            Set<Integer> visitados) {

        if (idCategoria == null || !visitados.add(idCategoria)) {
            return 0;
        }
        List<Categoria> hijos = hijosPorPadre.getOrDefault(idCategoria, Collections.emptyList());
        if (hijos.isEmpty()) {
            return 0;
        }
        int maxSub = 0;
        for (Categoria hijo : hijos) {
            int profHijo = calcularProfundidadDescendientes(hijo.getIdCategoria(), hijosPorPadre, new HashSet<>(visitados));
            if (profHijo > maxSub) {
                maxSub = profHijo;
            }
        }
        return 1 + maxSub;
    }

    private String construirRutaJerarquica(Categoria categoria) {
        LinkedList<String> partes = new LinkedList<>();
        Categoria actual = categoria;
        Set<Integer> visitados = new HashSet<>();

        while (actual != null) {
            if (actual.getIdCategoria() != null && !visitados.add(actual.getIdCategoria())) {
                break;
            }
            partes.addFirst(actual.getNombreCategoria());
            actual = actual.getCategoriaPadre();
        }
        return String.join(" › ", partes);
    }

    private CategoriaDTO convertirADto(Categoria entidad, Map<Integer, List<Categoria>> hijosPorPadre) {
        CategoriaDTO dto = new CategoriaDTO();
        dto.setIdCategoria(entidad.getIdCategoria());
        dto.setNombreCategoria(entidad.getNombreCategoria());
        dto.setDescripcion(entidad.getDescripcion());
        dto.setEstado(entidad.getEstado() != null ? entidad.getEstado() : 'A');
        dto.setFechaCreacion(entidad.getFechaCreacion());

        if (entidad.getCategoriaPadre() != null) {
            dto.setIdCategoriaPadre(entidad.getCategoriaPadre().getIdCategoria());
            dto.setNombreCategoriaPadre(entidad.getCategoriaPadre().getNombreCategoria());
        }

        int nivel = calcularNivel(entidad);
        dto.setNivel(nivel);
        dto.setRutaJerarquica(construirRutaJerarquica(entidad));

        List<Categoria> hijosDirectos = hijosPorPadre.getOrDefault(entidad.getIdCategoria(), Collections.emptyList());
        dto.setCantidadSubcategorias(hijosDirectos.size());
        dto.setProfundidadDescendientes(
                calcularProfundidadDescendientes(entidad.getIdCategoria(), hijosPorPadre, new HashSet<>())
        );

        if (entidad.getIdCategoria() != null) {
            dto.setCantidadProductos(productoRepository.countByCategoria_IdCategoria(entidad.getIdCategoria()));
        }

        return dto;
    }
}
