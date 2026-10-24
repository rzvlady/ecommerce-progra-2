package com.sv.grupo1.ecommerce.services.impl;

import com.sv.grupo1.ecommerce.dao.CategoriaRepository;
import com.sv.grupo1.ecommerce.dao.MarcaRepository;
import com.sv.grupo1.ecommerce.dao.ProductoRepository;
import com.sv.grupo1.ecommerce.entities.catalogo.Marca;
import com.sv.grupo1.ecommerce.entities.catalogo.Categoria;
import com.sv.grupo1.ecommerce.entities.core.Producto;
import com.sv.grupo1.ecommerce.services.ProductoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MarcaRepository marcaRepository;

    public ProductoServiceImpl(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository,
            MarcaRepository marcaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.marcaRepository = marcaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listarProductos() {
        return productoRepository.findAllConRelaciones();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> filtrarProductos(String busqueda, Integer idCategoria, Integer idMarca) {
        List<Producto> todos = productoRepository.findAllConRelaciones();
        String termino = (busqueda != null && !busqueda.isBlank()) ? busqueda.trim().toLowerCase(Locale.ROOT) : null;

        return todos.stream()
                .filter(p -> {
                    if (termino == null) {
                        return true;
                    }
                    boolean coincideSku = p.getCodigoSku() != null && p.getCodigoSku().toLowerCase(Locale.ROOT).contains(termino);
                    boolean coincideNombre = p.getNombreProducto() != null && p.getNombreProducto().toLowerCase(Locale.ROOT).contains(termino);
                    boolean coincideDesc = p.getDescripcionProducto() != null && p.getDescripcionProducto().toLowerCase(Locale.ROOT).contains(termino);
                    return coincideSku || coincideNombre || coincideDesc;
                })
                .filter(p -> idCategoria == null || (p.getCategoria() != null && idCategoria.equals(p.getCategoria().getIdCategoria())))
                .filter(p -> idMarca == null || (p.getMarca() != null && idMarca.equals(p.getMarca().getIdMarca())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Producto> obtenerPorId(Integer idProducto) {
        return productoRepository.findByIdConRelaciones(idProducto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> listarCategorias() {
        return categoriaRepository.findAllByOrderByNombreCategoriaAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Marca> listarMarcas() {
        return marcaRepository.findAllByOrderByNombreMarcaAsc();
    }
}
