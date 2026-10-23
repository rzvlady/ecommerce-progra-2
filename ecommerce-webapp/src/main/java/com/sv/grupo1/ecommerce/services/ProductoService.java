package com.sv.grupo1.ecommerce.services;

import com.sv.grupo1.ecommerce.entities.catalogo.Marca;
import com.sv.grupo1.ecommerce.entities.catalogo.Categoria;
import com.sv.grupo1.ecommerce.entities.core.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoService {

    List<Producto> listarProductos();

    List<Producto> filtrarProductos(String busqueda, Integer idCategoria, Integer idMarca);

    Optional<Producto> obtenerPorId(Integer idProducto);

    List<Categoria> listarCategorias();

    List<Marca> listarMarcas();
}
