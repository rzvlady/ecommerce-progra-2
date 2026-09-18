package com.sv.grupo1.ecommerce.services;

import com.sv.grupo1.ecommerce.dao.CategoriaRepository;
import com.sv.grupo1.ecommerce.dao.MarcaRepository;
import com.sv.grupo1.ecommerce.dao.ProductoRepository;
import com.sv.grupo1.ecommerce.dto.ProductoRegistroDTO;
import com.sv.grupo1.ecommerce.entities.core.Producto;
import org.springframework.stereotype.Service;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MarcaRepository marcaRepository;

    public ProductoService(ProductoRepository productoRepository, CategoriaRepository categoriaRepository, MarcaRepository marcaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.marcaRepository = marcaRepository;
    }

    public void registrarProducto(ProductoRegistroDTO dto) {
        if (productoRepository.existsByCodigoSku(dto.getSku())) {
            throw new IllegalArgumentException("El código SKU ya está registrado.");
        }

        Producto producto = new Producto();
        producto.setCodigoSku(dto.getSku());
        producto.setNombreProducto(dto.getNombreProducto());
        producto.setPrecioVenta(dto.getPrecioCosto());
        producto.setStockDisponible(dto.getStockDisponible());

        //Se buscan las entidades para asociarlas
        var categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));
        var marca = marcaRepository.findById(dto.getIdMarca())
                .orElseThrow(() -> new IllegalArgumentException("Marca no encontrada"));

        producto.setCategoria(categoria);
        producto.setMarca(marca);

        productoRepository.save(producto);
    }
}