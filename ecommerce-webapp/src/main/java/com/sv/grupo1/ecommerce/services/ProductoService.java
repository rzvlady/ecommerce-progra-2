package com.sv.grupo1.ecommerce.services;

import com.sv.grupo1.ecommerce.dto.ProductoRegistroDTO;
import com.sv.grupo1.ecommerce.entities.catalogo.Marca;
import com.sv.grupo1.ecommerce.entities.catalogo.Categoria;
import com.sv.grupo1.ecommerce.entities.core.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoService {

    List<Producto> listarProductos();

    List<Producto> filtrarProductos(String busqueda, Integer idCategoria, Integer idMarca, String filtroEstado);

    Optional<Producto> obtenerPorId(Integer idProducto);

    ProductoRegistroDTO obtenerDtoPorId(Integer idProducto);

    void registrarProducto(ProductoRegistroDTO dto);

    void actualizarProducto(Integer idProducto, ProductoRegistroDTO dto);

    void cambiarEstado(Integer idProducto);

    void eliminarProducto(Integer idProducto);

    List<Producto> listarProductosConStockBajo();

    List<Categoria> listarCategorias();

    List<Marca> listarMarcas();

    long contarTotalProductos();

}
