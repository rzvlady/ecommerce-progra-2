package com.sv.grupo1.ecommerce.services;

import com.sv.grupo1.ecommerce.dto.ProductoRegistroDTO;
import com.sv.grupo1.ecommerce.entities.core.Producto;

import java.util.List;

public interface ProductoService {

    void registrarProducto(ProductoRegistroDTO dto);

    List<Producto> listarProductosConStockBajo();

}
