package com.sv.grupo1.backoffice.service.impl;

import com.sv.grupo1.backoffice.dto.ProductoRegistroDTO;
import com.sv.grupo1.backoffice.service.BackofficeProductoService;
import com.sv.grupo1.backoffice.service.ProductoImagenStorageService;
import com.sv.grupo1.backoffice.task.StockMonitorTask;
import com.sv.grupo1.ecommerce.dao.CategoriaRepository;
import com.sv.grupo1.ecommerce.dao.MarcaRepository;
import com.sv.grupo1.ecommerce.dao.MovimientoInventarioRepository;
import com.sv.grupo1.ecommerce.dao.ProductoRepository;
import com.sv.grupo1.ecommerce.entities.catalogo.Marca;
import com.sv.grupo1.ecommerce.entities.catalogo.Categoria;
import com.sv.grupo1.ecommerce.entities.core.MovimientoInventario;
import com.sv.grupo1.ecommerce.entities.core.Producto;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class BackofficeProductoServiceImpl implements BackofficeProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MarcaRepository marcaRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final StockMonitorTask stockMonitorTask;
    private final ProductoImagenStorageService imagenStorageService;

    public BackofficeProductoServiceImpl(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository,
            MarcaRepository marcaRepository,
            MovimientoInventarioRepository movimientoInventarioRepository,
            StockMonitorTask stockMonitorTask,
            ProductoImagenStorageService imagenStorageService) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.marcaRepository = marcaRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
        this.stockMonitorTask = stockMonitorTask;
        this.imagenStorageService = imagenStorageService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listarProductos() {
        List<Producto> productos = productoRepository.findAllConRelaciones();
        productos.forEach(imagenStorageService::cargarImagenesEnProducto);
        return productos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> filtrarProductos(String busqueda, Integer idCategoria, Integer idMarca, String filtroEstado) {
        List<Producto> todos = productoRepository.findAllConRelaciones();
        todos.forEach(imagenStorageService::cargarImagenesEnProducto);
        String termino = (busqueda != null && !busqueda.isBlank()) ? busqueda.trim().toLowerCase(Locale.ROOT) : null;

        return todos.stream()
                .filter(p -> {
                    if (termino == null) {
                        return true;
                    }
                    boolean coincideSku = p.getCodigoSku() != null && p.getCodigoSku().toLowerCase(Locale.ROOT).contains(termino);
                    boolean coincideNombre = p.getNombreProducto() != null && p.getNombreProducto().toLowerCase(Locale.ROOT).contains(termino);
                    boolean coincideDesc = p.getDescripcionProducto() != null && p.getDescripcionProducto().toLowerCase(Locale.ROOT).contains(termino);
                    boolean coincideMeta = p.getMetadata() != null && p.getMetadata().toLowerCase(Locale.ROOT).contains(termino);
                    return coincideSku || coincideNombre || coincideDesc || coincideMeta;
                })
                .filter(p -> idCategoria == null || (p.getCategoria() != null && p.getCategoria().perteneceAJerarquia(idCategoria)))
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
        Optional<Producto> prodOpt = productoRepository.findByIdConRelaciones(idProducto);
        prodOpt.ifPresent(imagenStorageService::cargarImagenesEnProducto);
        return prodOpt;
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
        dto.setMetadata(filtrarMetadataSinImagenes(producto.getMetadata()));
        return dto;
    }

    @Override
    @Transactional
    public void registrarProducto(ProductoRegistroDTO dto) {
        var categoria = categoriaRepository.findByIdConJerarquia(dto.getIdCategoria())
                .orElseThrow(() -> new IllegalArgumentException("CategorÃ­a no encontrada"));
        var marca = marcaRepository.findById(dto.getIdMarca())
                .orElseThrow(() -> new IllegalArgumentException("Marca no encontrada"));

        String skuGenerado = generarCodigoSku(categoria, marca, null);
        dto.setSku(skuGenerado);
        dto.setEstado('A');

        Producto producto = new Producto();
        mapearDatosProducto(producto, dto, categoria, marca, skuGenerado);
        producto.setEstado('A');
        Producto guardado = productoRepository.save(producto);

        imagenStorageService.procesarImagenesProducto(guardado, dto);
        actualizarImagenesEnMetadata(guardado, categoria, skuGenerado);

        if (guardado.getStockDisponible() != null && guardado.getStockDisponible() > 0) {
            MovimientoInventario movInicial = new MovimientoInventario();
            movInicial.setProducto(guardado);
            movInicial.setTipoMovimiento("ENTRADA");
            movInicial.setCantidad(guardado.getStockDisponible());
            movInicial.setMotivo("COMPRA_PROVEEDOR - Stock inicial de registro");
            movimientoInventarioRepository.save(movInicial);
        }

        stockMonitorTask.escanearInventario();
    }

    @Override
    @Transactional
    public void actualizarProducto(Integer idProducto, ProductoRegistroDTO dto) {
        Producto producto = productoRepository.findByIdConRelaciones(idProducto)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + idProducto));

        var categoria = categoriaRepository.findByIdConJerarquia(dto.getIdCategoria())
                .orElseThrow(() -> new IllegalArgumentException("CategorÃ­a no encontrada"));
        var marca = marcaRepository.findById(dto.getIdMarca())
                .orElseThrow(() -> new IllegalArgumentException("Marca no encontrada"));

        // Las existencias en bodega se preservan de forma Ã­ntegra al editar el producto;
        // para modificarlas se registran entradas/salidas auditadas en el mÃ³dulo de Inventario.
        dto.setStockDisponible(producto.getStockDisponible());

        Categoria categoriaAnterior = producto.getCategoria();
        String skuAnterior = producto.getCodigoSku();

        // Si cambiÃ³ la taxonomÃ­a de categorÃ­a o la marca (o si el SKU aÃºn tenÃ­a el formato antiguo), se regenera el SKU
        String prefijoEsperado = obtenerPrefijoTaxonomiaCategoria(categoria)
                + "-" + obtenerPrefijoMarca(marca.getNombreMarca()) + "-";

        String skuFinal = producto.getCodigoSku();
        if (skuFinal == null || !skuFinal.startsWith(prefijoEsperado)) {
            skuFinal = generarCodigoSku(categoria, marca, idProducto);
        }
        dto.setSku(skuFinal);

        Character estadoActual = producto.getEstado() != null ? producto.getEstado() : 'A';
        mapearDatosProducto(producto, dto, categoria, marca, skuFinal);
        producto.setStockDisponible(producto.getStockDisponible());
        producto.setEstado(estadoActual);
        Producto actualizado = productoRepository.save(producto);

        imagenStorageService.migrarCarpetaSiCambioUbicacion(categoriaAnterior, skuAnterior, categoria, skuFinal);
        imagenStorageService.procesarImagenesProducto(actualizado, dto);
        actualizarImagenesEnMetadata(actualizado, categoria, skuFinal);

        stockMonitorTask.escanearInventario();
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
        Producto producto = productoRepository.findByIdConRelaciones(idProducto)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + idProducto));

        Categoria categoria = producto.getCategoria();
        String codigoSku = producto.getCodigoSku();

        try {
            productoRepository.delete(producto);
            productoRepository.flush();
            imagenStorageService.eliminarCarpetaProducto(categoria, codigoSku);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalStateException(
                    "No se puede eliminar fÃ­sicamente el producto '" + producto.getNombreProducto()
                            + "' porque tiene pedidos o movimientos de inventario asociados. Puedes desactivarlo en su lugar.");
        }
    }

    @Override
    public List<Producto> listarProductosConStockBajo() {
        return stockMonitorTask.getProductosBajoStockAlertados();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> listarCategorias() {
        List<Categoria> categorias = categoriaRepository.findAllConJerarquia();
        categorias.sort(Comparator.comparing(Categoria::getRutaJerarquica, String.CASE_INSENSITIVE_ORDER));
        return categorias;
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
        producto.setMetadata(normalizarMetadataJson(dto.getMetadata()));
    }

    private void actualizarImagenesEnMetadata(Producto producto, Categoria categoria, String codigoSku) {
        Map<Integer, String> fotos = imagenStorageService.obtenerMapaUrlsImagenes(categoria, codigoSku);
        Map<String, Object> mapaActual = new LinkedHashMap<>();

        if (producto.getMetadata() != null && !producto.getMetadata().isBlank()) {
            try {
                JsonParser parser = JsonParserFactory.getJsonParser();
                mapaActual.putAll(parser.parseMap(producto.getMetadata()));
            } catch (Exception ignored) {
            }
        }

        if (fotos.isEmpty()) {
            mapaActual.remove("imagenes");
        } else {
            List<String> listaUrls = new LinkedList<>(fotos.values());
            mapaActual.put("imagenes", listaUrls);
        }

        if (mapaActual.isEmpty()) {
            producto.setMetadata(null);
        } else {
            producto.setMetadata(serializarMapAJson(mapaActual));
        }
        productoRepository.save(producto);
    }

    private String serializarMapAJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean primero = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!primero) sb.append(",");
            sb.append("\"").append(escapeJson(entry.getKey())).append("\":");
            Object val = entry.getValue();
            if (val instanceof List<?> list) {
                sb.append("[");
                boolean primerItem = true;
                for (Object item : list) {
                    if (!primerItem) sb.append(",");
                    sb.append("\"").append(escapeJson(String.valueOf(item))).append("\"");
                    primerItem = false;
                }
                sb.append("]");
            } else {
                sb.append("\"").append(escapeJson(String.valueOf(val))).append("\"");
            }
            primero = false;
        }
        sb.append("}");
        return sb.toString();
    }

    private String filtrarMetadataSinImagenes(String rawMetadata) {
        if (rawMetadata == null || rawMetadata.isBlank()) {
            return null;
        }
        try {
            JsonParser parser = JsonParserFactory.getJsonParser();
            Map<String, Object> mapa = new LinkedHashMap<>(parser.parseMap(rawMetadata));
            mapa.remove("imagenes");
            if (mapa.isEmpty()) {
                return null;
            }
            return serializarMapAJson(mapa);
        } catch (Exception e) {
            return rawMetadata;
        }
    }

    private String escapeJson(String texto) {
        if (texto == null) return "";
        return texto.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\b", "\\b")
                    .replace("\f", "\\f")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }

    private String normalizarMetadataJson(String rawMetadata) {
        if (rawMetadata == null || rawMetadata.isBlank()) {
            return null;
        }
        String trimmed = rawMetadata.trim();
        if (trimmed.equals("{}") || trimmed.equals("[]") || trimmed.equalsIgnoreCase("null")) {
            return null;
        }
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            throw new IllegalArgumentException("El formato de metadata debe ser un objeto JSON vÃ¡lido (Clave -> Valor).");
        }
        return trimmed;
    }

    private String generarCodigoSku(Categoria categoria, Marca marca, Integer idProductoExistente) {
        String prefijoTaxonomia = obtenerPrefijoTaxonomiaCategoria(categoria);
        String prefijoMarca = obtenerPrefijoMarca(marca.getNombreMarca());

        int correlativo = (idProductoExistente != null)
                ? idProductoExistente
                : (Optional.ofNullable(productoRepository.obtenerMaxIdProducto()).orElse(0) + 1);

        String candidato = String.format("%s-%s-%04d", prefijoTaxonomia, prefijoMarca, correlativo);
        while (idProductoExistente == null
                ? productoRepository.existsByCodigoSku(candidato)
                : productoRepository.existsByCodigoSkuAndIdProductoNot(candidato, idProductoExistente)) {
            correlativo++;
            candidato = String.format("%s-%s-%04d", prefijoTaxonomia, prefijoMarca, correlativo);
        }
        return candidato;
    }

    private String obtenerPrefijoTaxonomiaCategoria(Categoria categoria) {
        if (categoria == null) {
            return "GEN";
        }
        LinkedList<String> segmentos = new LinkedList<>();
        Categoria actual = categoria;
        int guard = 0;
        while (actual != null && guard < 3) {
            segmentos.addFirst(obtenerPrefijoCategoria(actual.getNombreCategoria()));
            actual = actual.getCategoriaPadre();
            guard++;
        }
        return String.join("-", segmentos);
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
