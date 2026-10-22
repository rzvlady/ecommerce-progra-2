package com.sv.grupo1.ecommerce.services.impl;

import com.sv.grupo1.ecommerce.dao.CategoriaRepository;
import com.sv.grupo1.ecommerce.dao.MarcaRepository;
import com.sv.grupo1.ecommerce.dao.ProductoRepository;
import com.sv.grupo1.ecommerce.dto.ProductoRegistroDTO;
import com.sv.grupo1.ecommerce.entities.catalogo.Marca;
import com.sv.grupo1.ecommerce.entities.catalogo.Categoria;
import com.sv.grupo1.ecommerce.entities.core.Producto;
import com.sv.grupo1.ecommerce.services.ProductoService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MarcaRepository marcaRepository;
    private final StockMonitorTaskImpl stockMonitorTaskImpl;

    public ProductoServiceImpl(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository,
            MarcaRepository marcaRepository,
            StockMonitorTaskImpl stockMonitorTaskImpl) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.marcaRepository = marcaRepository;
        this.stockMonitorTaskImpl = stockMonitorTaskImpl;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listarProductos() {
        return productoRepository.findAllConRelaciones();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> filtrarProductos(String busqueda, Integer idCategoria, Integer idMarca, String filtroEstado) {
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
                .filter(p -> {
                    if (filtroEstado == null || filtroEstado.isBlank()) {
                        return true;
                    }
                    return switch (filtroEstado.toUpperCase(Locale.ROOT)) {
                        case "A" -> Character.valueOf('A').equals(p.getEstado());
                        case "I" -> Character.valueOf('I').equals(p.getEstado());
                        case "BAJO" -> p.getStockDisponible() != null && p.getStockMinimo() != null
                                && p.getStockDisponible() > 0 && p.getStockDisponible() <= p.getStockMinimo();
                        case "AGOTADO" -> p.getStockDisponible() != null && p.getStockDisponible() <= 0;
                        default -> true;
                    };
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Producto> obtenerPorId(Integer idProducto) {
        return productoRepository.findByIdConRelaciones(idProducto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoRegistroDTO obtenerDtoPorId(Integer idProducto) {
        Producto producto = productoRepository.findByIdConRelaciones(idProducto)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + idProducto));

        ProductoRegistroDTO dto = new ProductoRegistroDTO();
        dto.setIdProducto(producto.getIdProducto());
        dto.setSku(producto.getCodigoSku());
        dto.setNombreProducto(producto.getNombreProducto());
        dto.setDescripcionProducto(producto.getDescripcionProducto());
        dto.setPrecioVenta(producto.getPrecioVenta());
        dto.setPrecioCosto(producto.getPrecioCosto());
        dto.setStockDisponible(producto.getStockDisponible());
        dto.setStockMinimo(producto.getStockMinimo());
        dto.setIdCategoria(producto.getCategoria() != null ? producto.getCategoria().getIdCategoria() : null);
        dto.setIdMarca(producto.getMarca() != null ? producto.getMarca().getIdMarca() : null);
        dto.setEstado(producto.getEstado() != null ? producto.getEstado() : 'A');
        return dto;
    }

    @Override
    @Transactional
    public void registrarProducto(ProductoRegistroDTO dto) {
        var categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));
        var marca = marcaRepository.findById(dto.getIdMarca())
                .orElseThrow(() -> new IllegalArgumentException("Marca no encontrada"));

        String skuGenerado = generarCodigoSku(categoria, marca, null);
        dto.setSku(skuGenerado);
        dto.setEstado('A');

        Producto producto = new Producto();
        mapearDatosProducto(producto, dto, categoria, marca, skuGenerado);
        producto.setEstado('A');
        productoRepository.save(producto);
    }

    @Override
    @Transactional
    public void actualizarProducto(Integer idProducto, ProductoRegistroDTO dto) {
        Producto producto = productoRepository.findByIdConRelaciones(idProducto)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + idProducto));

        var categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));
        var marca = marcaRepository.findById(dto.getIdMarca())
                .orElseThrow(() -> new IllegalArgumentException("Marca no encontrada"));

        // Si cambió la categoría o la marca (o si el SKU aún tenía el formato antiguo), se regenera el SKU
        String prefijoEsperado = obtenerPrefijoCategoria(categoria.getNombreCategoria())
                + "-" + obtenerPrefijoMarca(marca.getNombreMarca()) + "-";

        String skuFinal = producto.getCodigoSku();
        if (skuFinal == null || !skuFinal.startsWith(prefijoEsperado)) {
            skuFinal = generarCodigoSku(categoria, marca, idProducto);
        }
        dto.setSku(skuFinal);

        Character estadoActual = producto.getEstado() != null ? producto.getEstado() : 'A';
        mapearDatosProducto(producto, dto, categoria, marca, skuFinal);
        producto.setEstado(estadoActual);
        productoRepository.save(producto);
    }

    @Override
    @Transactional
    public void cambiarEstado(Integer idProducto) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + idProducto));

        char estadoActual = producto.getEstado() != null ? producto.getEstado() : 'A';
        producto.setEstado(estadoActual == 'A' ? 'I' : 'A');
        productoRepository.save(producto);
    }

    @Override
    @Transactional
    public void eliminarProducto(Integer idProducto) {
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + idProducto));

        try {
            productoRepository.delete(producto);
            productoRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException(
                    "No se puede eliminar físicamente el producto '" + producto.getNombreProducto()
                            + "' porque tiene pedidos o movimientos de inventario asociados. Puedes desactivarlo en su lugar.");
        }
    }

    @Override
    public List<Producto> listarProductosConStockBajo() {
        return stockMonitorTaskImpl.getProductosBajoStockAlertados();
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

    @Override
    @Transactional(readOnly = true)
    public long contarTotalProductos() {
        return productoRepository.count();
    }

    private void mapearDatosProducto(
            Producto producto,
            ProductoRegistroDTO dto,
            Categoria categoria,
            Marca marca,
            String codigoSku) {

        String nombreEnMayusculas = dto.getNombreProducto() != null
                ? dto.getNombreProducto().trim().toUpperCase(Locale.ROOT)
                : "";

        producto.setCodigoSku(codigoSku);
        producto.setNombreProducto(nombreEnMayusculas);
        producto.setDescripcionProducto(dto.getDescripcionProducto().trim());
        producto.setPrecioCosto(dto.getPrecioCosto());
        producto.setPrecioVenta(dto.getPrecioVenta() != null ? dto.getPrecioVenta() : dto.getPrecioCosto());
        producto.setStockDisponible(dto.getStockDisponible());
        producto.setStockMinimo(dto.getStockMinimo());
        producto.setCategoria(categoria);
        producto.setMarca(marca);
    }

    private String generarCodigoSku(Categoria categoria, Marca marca, Integer idProductoExistente) {
        String prefijoCat = obtenerPrefijoCategoria(categoria.getNombreCategoria());
        String prefijoMarca = obtenerPrefijoMarca(marca.getNombreMarca());

        int correlativo = (idProductoExistente != null)
                ? idProductoExistente
                : (Optional.ofNullable(productoRepository.obtenerMaxIdProducto()).orElse(0) + 1);

        String candidato = String.format("%s-%s-%04d", prefijoCat, prefijoMarca, correlativo);
        while (idProductoExistente == null
                ? productoRepository.existsByCodigoSku(candidato)
                : productoRepository.existsByCodigoSkuAndIdProductoNot(candidato, idProductoExistente)) {
            correlativo++;
            candidato = String.format("%s-%s-%04d", prefijoCat, prefijoMarca, correlativo);
        }
        return candidato;
    }

    private String obtenerPrefijoCategoria(String nombreCategoria) {
        String limpio = limpiarTextoAscii(nombreCategoria);
        if (limpio.startsWith("ELECTRODOM")) {
            return "ELD";
        }
        if (limpio.startsWith("ELECTRON")) {
            return "ELC";
        }
        return limpio.length() >= 3 ? limpio.substring(0, 3) : String.format("%-3s", limpio).replace(' ', 'X');
    }

    private String obtenerPrefijoMarca(String nombreMarca) {
        String limpio = limpiarTextoAscii(nombreMarca);
        return limpio.length() >= 3 ? limpio.substring(0, 3) : limpio;
    }

    private String limpiarTextoAscii(String texto) {
        if (texto == null) {
            return "GEN";
        }
        String sinAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String soloAlfanumerico = sinAcentos.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
        return soloAlfanumerico.isEmpty() ? "GEN" : soloAlfanumerico;
    }
}
