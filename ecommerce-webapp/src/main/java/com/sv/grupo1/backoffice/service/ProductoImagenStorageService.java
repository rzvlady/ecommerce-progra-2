package com.sv.grupo1.backoffice.service;

import com.sv.grupo1.backoffice.dto.ProductoRegistroDTO;
import com.sv.grupo1.ecommerce.entities.catalogo.Categoria;
import com.sv.grupo1.ecommerce.entities.core.Producto;

import java.nio.file.Path;
import java.util.Map;

public interface ProductoImagenStorageService {

    Path getBaseStorageDir();

    void procesarImagenesProducto(Producto producto, ProductoRegistroDTO dto);

    void migrarCarpetaSiCambioUbicacion(Categoria categoriaAnterior, String skuAnterior, Categoria categoriaNueva, String skuNuevo);

    void eliminarCarpetaProducto(Categoria categoria, String codigoSku);

    Map<Integer, String> obtenerMapaUrlsImagenes(Categoria categoria, String codigoSku);

    void cargarImagenesEnProducto(Producto producto);

    String construirRutaRelativaTaxonomia(Categoria categoria, String codigoSku);
}