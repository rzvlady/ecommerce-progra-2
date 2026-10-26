package com.sv.grupo1.ecommerce.services.impl;

import com.sv.grupo1.ecommerce.dao.MarcaRepository;
import com.sv.grupo1.ecommerce.dao.ProductoRepository;
import com.sv.grupo1.ecommerce.dto.MarcaDTO;
import com.sv.grupo1.ecommerce.entities.catalogo.Marca;
import com.sv.grupo1.ecommerce.services.MarcaService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class MarcaServiceImpl implements MarcaService {

    private final MarcaRepository marcaRepository;
    private final ProductoRepository productoRepository;

    public MarcaServiceImpl(
            MarcaRepository marcaRepository,
            ProductoRepository productoRepository) {
        this.marcaRepository = marcaRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarcaDTO> listarTodasConConteo() {
        List<Marca> entidades = marcaRepository.findAllByOrderByNombreMarcaAsc();
        List<MarcaDTO> resultado = new ArrayList<>(entidades.size());

        for (Marca marca : entidades) {
            resultado.add(convertirADto(marca));
        }
        return resultado;
    }

    @Override
    @Transactional(readOnly = true)
    public MarcaDTO obtenerDtoPorId(Integer idMarca) {
        if (idMarca == null) {
            throw new IllegalArgumentException("El ID de la marca es obligatorio.");
        }
        Marca marca = marcaRepository.findById(idMarca)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la marca con ID: " + idMarca));

        return convertirADto(marca);
    }

    @Override
    @Transactional
    public void guardarMarca(MarcaDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Los datos de la marca no pueden ser nulos.");
        }

        String nombreLimpio = dto.getNombreMarca() != null ? dto.getNombreMarca().trim() : "";
        if (nombreLimpio.isEmpty()) {
            throw new IllegalArgumentException("El nombre de la marca es obligatorio.");
        }

        String descripcionLimpia = (dto.getDescripcion() != null && !dto.getDescripcion().isBlank())
                ? dto.getDescripcion().trim()
                : null;

        boolean esEdicion = dto.getIdMarca() != null;
        Marca marca;

        if (esEdicion) {
            marca = marcaRepository.findById(dto.getIdMarca())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No se encontró la marca que intenta editar (ID: " + dto.getIdMarca() + ")."
                    ));

            if (marcaRepository.existsByNombreMarcaIgnoreCaseAndIdMarcaNot(nombreLimpio, dto.getIdMarca())) {
                throw new IllegalArgumentException("Ya existe otra marca registrada con el nombre '" + nombreLimpio + "'.");
            }
        } else {
            marca = new Marca();
            if (marcaRepository.existsByNombreMarcaIgnoreCase(nombreLimpio)) {
                throw new IllegalArgumentException("Ya existe una marca registrada con el nombre '" + nombreLimpio + "'.");
            }
            marca.setEstado('A');
        }

        marca.setNombreMarca(nombreLimpio);
        marca.setDescripcion(descripcionLimpia);
        if (marca.getEstado() == null) {
            marca.setEstado('A');
        }

        marcaRepository.save(marca);
    }

    @Override
    @Transactional
    public void cambiarEstado(Integer idMarca) {
        if (idMarca == null) {
            throw new IllegalArgumentException("El ID de la marca es obligatorio.");
        }
        Marca marca = marcaRepository.findById(idMarca)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la marca con ID: " + idMarca));

        char estadoActual = marca.getEstado() != null ? marca.getEstado() : 'A';
        marca.setEstado(estadoActual == 'A' ? 'I' : 'A');
        marcaRepository.save(marca);
    }

    @Override
    @Transactional
    public void eliminarMarca(Integer idMarca) {
        if (idMarca == null) {
            throw new IllegalArgumentException("El ID de la marca es obligatorio.");
        }

        Marca marca = marcaRepository.findById(idMarca)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró la marca con ID: " + idMarca));

        long productosVinculados = productoRepository.countByMarca_IdMarca(idMarca);
        if (productosVinculados > 0) {
            throw new IllegalStateException(
                    "No se puede eliminar la marca '" + marca.getNombreMarca() + "' porque tiene " +
                    productosVinculados + " producto(s) vinculado(s) en el catálogo. Puedes desactivarla en su lugar."
            );
        }

        try {
            marcaRepository.delete(marca);
            marcaRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException(
                    "No es posible eliminar la marca '" + marca.getNombreMarca() + "' debido a registros asociados en el sistema."
            );
        }
    }

    private MarcaDTO convertirADto(Marca entidad) {
        MarcaDTO dto = new MarcaDTO();
        dto.setIdMarca(entidad.getIdMarca());
        dto.setNombreMarca(entidad.getNombreMarca());
        dto.setDescripcion(entidad.getDescripcion());
        dto.setEstado(entidad.getEstado() != null ? entidad.getEstado() : 'A');
        dto.setFechaCreacion(entidad.getFechaCreacion());

        if (entidad.getIdMarca() != null) {
            dto.setCantidadProductos(productoRepository.countByMarca_IdMarca(entidad.getIdMarca()));
        }
        return dto;
    }
}
