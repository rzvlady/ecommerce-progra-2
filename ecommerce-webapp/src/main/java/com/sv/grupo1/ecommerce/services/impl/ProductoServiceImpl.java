package com.sv.grupo1.ecommerce.services.impl;

import com.sv.grupo1.backoffice.service.ProductoImagenStorageService;
import com.sv.grupo1.ecommerce.dao.CategoriaRepository;
import com.sv.grupo1.ecommerce.dao.MarcaRepository;
import com.sv.grupo1.ecommerce.dao.ProductoRepository;
import com.sv.grupo1.ecommerce.dto.CategoriaHomeDTO;
import com.sv.grupo1.ecommerce.dto.CategoriaNodoDTO;
import com.sv.grupo1.ecommerce.dto.ProductoHomeDTO;
import com.sv.grupo1.ecommerce.dto.SugerenciaBusquedaDTO;
import com.sv.grupo1.ecommerce.entities.catalogo.Marca;
import com.sv.grupo1.ecommerce.entities.catalogo.Categoria;
import com.sv.grupo1.ecommerce.entities.core.Producto;
import com.sv.grupo1.ecommerce.services.ProductoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MarcaRepository marcaRepository;
    private final ProductoImagenStorageService imagenStorageService;

    private static final String[] IMAGENES_FALLBACK = {
            "https://images.unsplash.com/photo-1572569511254-d8f925fe2cbb?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=500&q=80",
            "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=500&q=80",
            "https://images.unsplash.com/photo-1502920917128-1aa500764cbd?auto=format&fit=crop&w=800&q=80"
    };

    private static final String[] BADGES_DESTACADOS = {
            "Destacado", "Popular", "Colección", "Nuevo"
    };

    public ProductoServiceImpl(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository,
            MarcaRepository marcaRepository,
            ProductoImagenStorageService imagenStorageService) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.marcaRepository = marcaRepository;
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
    public List<Producto> filtrarProductos(String busqueda, Integer idCategoria, Integer idMarca) {

        List<Producto> todos = productoRepository.findAllConRelaciones();
        todos.forEach(imagenStorageService::cargarImagenesEnProducto);
        String termino = (busqueda != null && !busqueda.isBlank())
                ? normalizarTexto(busqueda)
                : null;

        List<Producto> filtrados = todos.stream()
                .filter(p -> Character.valueOf('A').equals(p.getEstado()))
                .filter(p -> {
                    if (termino == null) {
                        return true;
                    }

                    boolean coincideSku = normalizarTexto(p.getCodigoSku()).contains(termino);
                    boolean coincideNombre = normalizarTexto(p.getNombreProducto()).contains(termino);
                    boolean coincideDescripcion = normalizarTexto(p.getDescripcionProducto()).contains(termino);
                    boolean coincideMarca = p.getMarca() != null
                            && normalizarTexto(p.getMarca().getNombreMarca()).contains(termino);

                    boolean coincideCategoria = false;
                    Categoria categoriaActual = p.getCategoria();

                    while (categoriaActual != null) {
                        if (normalizarTexto(categoriaActual.getNombreCategoria()).contains(termino)) {
                            coincideCategoria = true;
                            break;
                        }
                        categoriaActual = categoriaActual.getCategoriaPadre();
                    }

                    return coincideSku
                            || coincideNombre
                            || coincideDescripcion
                            || coincideMarca
                            || coincideCategoria;
                })
                .filter(p -> idCategoria == null
                        || (p.getCategoria() != null
                        && p.getCategoria().perteneceAJerarquia(idCategoria)))
                .filter(p -> idMarca == null
                        || (p.getMarca() != null
                        && idMarca.equals(p.getMarca().getIdMarca())))
                .toList();

        filtrados.forEach(imagenStorageService::cargarImagenesEnProducto);

        return filtrados;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Producto> obtenerPorId(Integer idProducto) {
        Optional<Producto> opt = productoRepository.findByIdConRelaciones(idProducto);
        opt.ifPresent(imagenStorageService::cargarImagenesEnProducto);
        return opt;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> listarCategorias() {
        List<Categoria> cats = categoriaRepository.findAllByOrderByNombreCategoriaAsc();
        if (cats == null || cats.isEmpty()) {
            cats = new ArrayList<>();
            Categoria tec = crearCategoriaMock(1, "Electrónica", null);
            cats.add(tec);
            cats.add(crearCategoriaMock(2, "Computación", tec));
            cats.add(crearCategoriaMock(3, "Celulares", tec));
            cats.add(crearCategoriaMock(4, "Audio", tec));
            cats.add(crearCategoriaMock(5, "Televisores", tec));
            cats.add(crearCategoriaMock(6, "Gaming", tec));
            Categoria hog = crearCategoriaMock(7, "Hogar", null);
            cats.add(hog);
            cats.add(crearCategoriaMock(8, "Electrodomésticos", hog));
            cats.add(crearCategoriaMock(9, "Cuidado Personal", hog));
            cats.add(crearCategoriaMock(10, "Accesorios", null));
        }
        return cats;
    }

    private Categoria crearCategoriaMock(Integer id, String nombre, Categoria padre) {
        Categoria c = new Categoria();
        c.setIdCategoria(id);
        c.setNombreCategoria(nombre);
        c.setCategoriaPadre(padre);
        c.setEstado('A');
        return c;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaNodoDTO> obtenerArbolCategorias() {
        List<Categoria> todas;
        try {
            todas = categoriaRepository.findAllConJerarquia();
        } catch (Exception e) {
            todas = listarCategorias();
        }
        if (todas == null || todas.isEmpty()) {
            todas = listarCategorias();
        }

        Map<Integer, CategoriaNodoDTO> nodoMap = new LinkedHashMap<>();
        for (Categoria c : todas) {
            if (c.getEstado() != null && !Character.valueOf('A').equals(c.getEstado())) {
                continue;
            }
            CategoriaNodoDTO nodo = new CategoriaNodoDTO(c.getIdCategoria(), c.getNombreCategoria());
            if (c.getCategoriaPadre() != null) {
                nodo.setIdCategoriaPadre(c.getCategoriaPadre().getIdCategoria());
                nodo.setNombreCategoriaPadre(c.getCategoriaPadre().getNombreCategoria());
            }
            nodoMap.put(c.getIdCategoria(), nodo);
        }

        List<CategoriaNodoDTO> raices = new ArrayList<>();
        for (Categoria c : todas) {
            if (c.getEstado() != null && !Character.valueOf('A').equals(c.getEstado())) {
                continue;
            }
            CategoriaNodoDTO nodo = nodoMap.get(c.getIdCategoria());
            if (c.getCategoriaPadre() != null && nodoMap.containsKey(c.getCategoriaPadre().getIdCategoria())) {
                CategoriaNodoDTO padre = nodoMap.get(c.getCategoriaPadre().getIdCategoria());
                if (!padre.getSubcategorias().contains(nodo)) {
                    padre.getSubcategorias().add(nodo);
                }
            } else {
                if (!raices.contains(nodo)) {
                    raices.add(nodo);
                }
            }
        }

        raices.sort((a, b) -> a.getNombreCategoria().compareToIgnoreCase(b.getNombreCategoria()));
        for (CategoriaNodoDTO r : raices) {
            r.getSubcategorias().sort((a, b) -> a.getNombreCategoria().compareToIgnoreCase(b.getNombreCategoria()));
        }

        return raices;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Marca> listarMarcas() {
        return marcaRepository.findAllByOrderByNombreMarcaAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoHomeDTO> obtenerProductosDestacadosHome() {
        List<Producto> productosEntidad = productoRepository.findProductosActivosHome();
        if (productosEntidad.isEmpty()) {
            productosEntidad = productoRepository.findAllConRelaciones();
        }

        List<ProductoHomeDTO> dtoList = new ArrayList<>();
        int index = 0;
        for (Producto p : productosEntidad) {
            if (index >= 4) {
                break;
            }
            imagenStorageService.cargarImagenesEnProducto(p);

            ProductoHomeDTO dto = new ProductoHomeDTO();
            dto.setIdProducto(p.getIdProducto());
            dto.setCodigoSku(p.getCodigoSku());
            dto.setNombreProducto(p.getNombreProducto());
            dto.setDescripcionProducto(p.getDescripcionProducto());
            dto.setPrecioVenta(p.getPrecioVenta());
            dto.setStockDisponible(p.getStockDisponible());

            if (p.getCategoria() != null) {
                dto.setIdCategoria(p.getCategoria().getIdCategoria());
                dto.setNombreCategoria(p.getCategoria().getNombreCategoria());
            }

            if (p.getMarca() != null) {
                dto.setNombreMarca(p.getMarca().getNombreMarca());
            }

            if (p.getImagenPrincipalUrl() != null && !p.getImagenPrincipalUrl().isBlank()) {
                dto.setImagenPrincipalUrl(p.getImagenPrincipalUrl());
            } else {
                dto.setImagenPrincipalUrl(IMAGENES_FALLBACK[index % IMAGENES_FALLBACK.length]);
            }

            dto.setBadge(BADGES_DESTACADOS[index % BADGES_DESTACADOS.length]);

            dtoList.add(dto);
            index++;
        }

        if (dtoList.isEmpty()) {
            dtoList.add(crearMockProducto(1, "SKU-0001", "Auriculares inalámbricos", "Auriculares con cancelación activa de ruido", new BigDecimal("39.99"), IMAGENES_FALLBACK[0], "Destacado"));
            dtoList.add(crearMockProducto(2, "SKU-0002", "Reloj de pulsera", "Reloj de pulsera clásico y elegante", new BigDecimal("59.99"), IMAGENES_FALLBACK[1], "Popular"));
            dtoList.add(crearMockProducto(3, "SKU-0003", "Zapatillas deportivas", "Zapatillas deportivas confort para entrenamiento", new BigDecimal("49.99"), IMAGENES_FALLBACK[2], "Colección"));
            dtoList.add(crearMockProducto(4, "SKU-0004", "Cámara fotográfica", "Cámara fotográfica réflex de alta resolución", new BigDecimal("89.99"), IMAGENES_FALLBACK[3], "Nuevo"));
        }

        return dtoList;
    }

    private ProductoHomeDTO crearMockProducto(Integer id, String sku, String nombre, String desc, BigDecimal precio, String imagen, String badge) {
        ProductoHomeDTO dto = new ProductoHomeDTO();
        dto.setIdProducto(id);
        dto.setCodigoSku(sku);
        dto.setNombreProducto(nombre);
        dto.setDescripcionProducto(desc);
        dto.setPrecioVenta(precio);
        dto.setImagenPrincipalUrl(imagen);
        dto.setBadge(badge);
        dto.setStockDisponible(15);
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaHomeDTO> obtenerCategoriasHome() {
        List<CategoriaHomeDTO> categorias = new ArrayList<>();
        categorias.add(new CategoriaHomeDTO(1, "Tecnología", "Audio, computación y gadgets", "https://images.unsplash.com/photo-1519389950473-47ba0277781c?auto=format&fit=crop&w=450&q=80", "/productos?busqueda=tecnologia"));
        categorias.add(new CategoriaHomeDTO(7, "Hogar", "Muebles, confort y decoración", "https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&w=450&q=80", "/productos?idCategoria=7"));
        categorias.add(new CategoriaHomeDTO(3, "Moda", "Tendencias, ropa y calzado", "https://images.unsplash.com/photo-1445205170230-053b83016050?auto=format&fit=crop&w=450&q=80", "/productos?busqueda=moda"));
        categorias.add(new CategoriaHomeDTO(10, "Accesorios", "Relojes, bolsos y complementos", "https://images.unsplash.com/photo-1523293182086-7651a899d37f?auto=format&fit=crop&w=450&q=80", "/productos?idCategoria=10"));
        categorias.add(new CategoriaHomeDTO(4, "Audio & Sonido", "Auriculares y altavoces", "https://images.unsplash.com/photo-1546435770-a3e426bf472b?auto=format&fit=crop&w=450&q=80", "/productos?idCategoria=4"));
        categorias.add(new CategoriaHomeDTO(2, "Computación", "Laptops y accesorios de cómputo", "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?auto=format&fit=crop&w=450&q=80", "/productos?idCategoria=2"));
        categorias.add(new CategoriaHomeDTO(6, "Gaming", "Consolas, controles y accesorios", "https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?auto=format&fit=crop&w=450&q=80", "/productos?idCategoria=6"));
        categorias.add(new CategoriaHomeDTO(9, "Novedades", "Lo más reciente en catálogo", "https://images.unsplash.com/photo-1511556532299-8f662fc26c06?auto=format&fit=crop&w=450&q=80", "/productos"));
        return categorias;
    }

    @Override
    @Transactional(readOnly = true)
    public SugerenciaBusquedaDTO buscarSugerencias(String termino) {
        SugerenciaBusquedaDTO dto = new SugerenciaBusquedaDTO();
        if (termino == null || termino.trim().isBlank()) {
            return dto;
        }

        final String normTerm = normalizarTexto(termino);
        if (normTerm.isEmpty()) {
            return dto;
        }

        // 1. Categorías sugeridas
        List<Categoria> todasCategorias = listarCategorias();
        List<CategoriaScore> catScores = new ArrayList<>();
        for (Categoria cat : todasCategorias) {
            String normCat = normalizarTexto(cat.getNombreCategoria());
            int score = calcularScoreCoincidencia(normCat, normTerm);
            if (score > 0) {
                catScores.add(new CategoriaScore(cat, score));
            }
        }
        catScores.sort((a, b) -> {
            int cmp = Integer.compare(b.score(), a.score());
            if (cmp != 0) return cmp;
            return a.cat().getNombreCategoria().compareToIgnoreCase(b.cat().getNombreCategoria());
        });

        List<SugerenciaBusquedaDTO.SugerenciaCategoriaDTO> sugerenciasCategorias = catScores.stream()
                .limit(5)
                .map(cs -> new SugerenciaBusquedaDTO.SugerenciaCategoriaDTO(
                        cs.cat().getIdCategoria(),
                        cs.cat().getNombreCategoria(),
                        "/productos?idCategoria=" + cs.cat().getIdCategoria()))
                .toList();
        dto.setCategorias(sugerenciasCategorias);

        // 2. Marcas sugeridas
        List<Marca> todasMarcas = listarMarcas();
        List<MarcaScore> marcaScores = new ArrayList<>();
        for (Marca marca : todasMarcas) {
            String normMarca = normalizarTexto(marca.getNombreMarca());
            int score = calcularScoreCoincidencia(normMarca, normTerm);
            if (score > 0) {
                marcaScores.add(new MarcaScore(marca, score));
            }
        }
        marcaScores.sort((a, b) -> {
            int cmp = Integer.compare(b.score(), a.score());
            if (cmp != 0) return cmp;
            return a.marca().getNombreMarca().compareToIgnoreCase(b.marca().getNombreMarca());
        });

        List<SugerenciaBusquedaDTO.SugerenciaMarcaDTO> sugerenciasMarcas = marcaScores.stream()
                .limit(5)
                .map(ms -> new SugerenciaBusquedaDTO.SugerenciaMarcaDTO(
                        ms.marca().getIdMarca(),
                        ms.marca().getNombreMarca(),
                        "/productos?idMarca=" + ms.marca().getIdMarca()))
                .toList();
        dto.setMarcas(sugerenciasMarcas);

        // 3. Productos sugeridos (solo activos 'A')
        List<Producto> todosProductos = productoRepository.findAllConRelaciones();
        List<ProductoScore> prodScores = new ArrayList<>();
        for (Producto prod : todosProductos) {
            if (!Character.valueOf('A').equals(prod.getEstado())) {
                continue;
            }
            String normNombre = normalizarTexto(prod.getNombreProducto());
            String normSku = normalizarTexto(prod.getCodigoSku());
            String normMarca = prod.getMarca() != null ? normalizarTexto(prod.getMarca().getNombreMarca()) : "";
            String normCat = prod.getCategoria() != null ? normalizarTexto(prod.getCategoria().getNombreCategoria()) : "";

            int scoreNombre = calcularScoreCoincidencia(normNombre, normTerm);
            int scoreSku = calcularScoreCoincidencia(normSku, normTerm);
            int scoreMarca = calcularScoreCoincidencia(normMarca, normTerm);
            int scoreCat = calcularScoreCoincidencia(normCat, normTerm);

            int maxScore = Math.max(scoreNombre * 3, Math.max(scoreSku * 2, Math.max(scoreMarca, scoreCat)));
            if (maxScore > 0) {
                prodScores.add(new ProductoScore(prod, maxScore));
            }
        }
        prodScores.sort((a, b) -> {
            int cmp = Integer.compare(b.score(), a.score());
            if (cmp != 0) return cmp;
            return a.prod().getNombreProducto().compareToIgnoreCase(b.prod().getNombreProducto());
        });

        List<SugerenciaBusquedaDTO.SugerenciaProductoDTO> sugerenciasProductos = new ArrayList<>();
        int pIndex = 0;
        for (ProductoScore ps : prodScores) {
            if (pIndex >= 6) break;
            Producto p = ps.prod();
            imagenStorageService.cargarImagenesEnProducto(p);

            String imgUrl = (p.getImagenPrincipalUrl() != null && !p.getImagenPrincipalUrl().isBlank())
                    ? p.getImagenPrincipalUrl()
                    : IMAGENES_FALLBACK[pIndex % IMAGENES_FALLBACK.length];

            String catNombre = p.getCategoria() != null ? p.getCategoria().getNombreCategoria() : null;
            String marcaNombre = p.getMarca() != null ? p.getMarca().getNombreMarca() : null;

            sugerenciasProductos.add(new SugerenciaBusquedaDTO.SugerenciaProductoDTO(
                    p.getIdProducto(),
                    p.getCodigoSku(),
                    p.getNombreProducto(),
                    p.getPrecioVenta(),
                    imgUrl,
                    catNombre,
                    marcaNombre,
                    "/productos/" + p.getIdProducto()
            ));
            pIndex++;
        }
        dto.setProductos(sugerenciasProductos);

        dto.setTotalResultados(catScores.size() + marcaScores.size() + prodScores.size());
        return dto;
    }

    private int calcularScoreCoincidencia(String texto, String termino) {
        if (texto == null || texto.isEmpty() || termino == null || termino.isEmpty()) {
            return 0;
        }
        if (texto.startsWith(termino)) {
            return 100;
        }
        String[] palabras = texto.split("\\s+");
        for (String palabra : palabras) {
            if (palabra.startsWith(termino)) {
                return 75;
            }
        }
        if (texto.contains(termino)) {
            return 50;
        }
        return 0;
    }

    private record CategoriaScore(Categoria cat, int score) {}
    private record MarcaScore(Marca marca, int score) {}
    private record ProductoScore(Producto prod, int score) {}

    private String normalizarTexto(String texto) {
        if (texto == null) {
            return "";
        }
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }
}
