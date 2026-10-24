package com.sv.grupo1.backoffice.service.impl;

import com.sv.grupo1.backoffice.dto.ProductoRegistroDTO;
import com.sv.grupo1.backoffice.service.ProductoImagenStorageService;
import com.sv.grupo1.ecommerce.entities.catalogo.Categoria;
import com.sv.grupo1.ecommerce.entities.core.Producto;
import jakarta.annotation.PostConstruct; // Importante
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service; // Importante
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Stream;

@Service // Marca la clase como un componente de Spring
public class ProductoImagenStorageServiceImpl implements ProductoImagenStorageService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductoImagenStorageServiceImpl.class);

    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of("jpg", "jpeg", "png", "webp", "gif", "avif");

    private final Path baseStorageDir;

    // 1. El constructor ahora solo asigna valores (Inyección limpia)
    public ProductoImagenStorageServiceImpl(
            @Value("${app.storage.productos-dir:uploads/productos}") String configuredDir) {
        this.baseStorageDir = resolverDirectorioBase(configuredDir);
    }

    // 2. Spring llama a este método automáticamente después del constructor
    @PostConstruct
    public void inicializarDirectorioAlmacenamiento() {
        try {
            Files.createDirectories(this.baseStorageDir);
            LOG.info("Directorio dedicado de fotografías de productos inicializado en: {}", this.baseStorageDir);
        } catch (IOException e) {
            LOG.error("No se pudo inicializar el directorio de imágenes de productos: {}", e.getMessage());
            // Opcional: throw new IllegalStateException("Fallo al crear directorio base", e);
        }
    }

    @Override
    public Path getBaseStorageDir() {
        return baseStorageDir;
    }

    @Override
    public void procesarImagenesProducto(Producto producto, ProductoRegistroDTO dto) {
        // ... (resto del código sin cambios)
        if (producto == null || producto.getCodigoSku() == null) {
            return;
        }

        Path carpetaProducto = resolverCarpetaProducto(producto.getCategoria(), producto.getCodigoSku());

        if (dto.getSlotsEliminar() != null && !dto.getSlotsEliminar().isBlank()) {
            String[] slots = dto.getSlotsEliminar().split(",");
            for (String s : slots) {
                try {
                    int slotNum = Integer.parseInt(s.trim());
                    if (slotNum >= 1 && slotNum <= 4) {
                        eliminarArchivoSlot(carpetaProducto, slotNum);
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        guardarArchivoSlot(carpetaProducto, 1, dto.getImagenSlot1());
        guardarArchivoSlot(carpetaProducto, 2, dto.getImagenSlot2());
        guardarArchivoSlot(carpetaProducto, 3, dto.getImagenSlot3());
        guardarArchivoSlot(carpetaProducto, 4, dto.getImagenSlot4());
    }

    /**
     * Si al editar un producto cambia su categoría (taxonomía) o su SKU, migra sus fotografías
     * a la nueva carpeta segmentada para no perder ninguna imagen.
     */
    public void migrarCarpetaSiCambioUbicacion(
            Categoria categoriaAnterior,
            String skuAnterior,
            Categoria categoriaNueva,
            String skuNuevo) {

        if (skuAnterior == null || skuNuevo == null) {
            return;
        }

        Path origen = resolverCarpetaProducto(categoriaAnterior, skuAnterior);
        Path destino = resolverCarpetaProducto(categoriaNueva, skuNuevo);

        if (origen.equals(destino) || !Files.exists(origen)) {
            return;
        }

        try {
            Files.createDirectories(destino);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(origen)) {
                for (Path file : stream) {
                    if (Files.isRegularFile(file)) {
                        Files.move(file, destino.resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
            eliminarCarpetaRecursiva(origen);
            limpiarDirectoriosPadresVacios(origen.getParent());
            LOG.info("Fotografías migradas de {} hacia {}", origen, destino);
        } catch (IOException e) {
            LOG.warn("No se pudieron migrar las fotografías de {} a {}: {}", origen, destino, e.getMessage());
        }
    }

    /**
     * Elimina la carpeta de fotografías asociada a un producto cuando este se elimina del catálogo.
     */
    public void eliminarCarpetaProducto(Categoria categoria, String codigoSku) {
        if (codigoSku == null || codigoSku.isBlank()) {
            return;
        }
        Path carpeta = resolverCarpetaProducto(categoria, codigoSku);
        if (Files.exists(carpeta)) {
            eliminarCarpetaRecursiva(carpeta);
            limpiarDirectoriosPadresVacios(carpeta.getParent());
        }
    }

    /**
     * Retorna un mapa con los slots y URLs relativas de las fotografías del producto (sin parámetro ?v=).
     * Ejemplo: {1: "/uploads/productos/electronica/computacion/laptops/ELC-COM-LAP-HP-0001/img-1.webp", ...}
     */
    public Map<Integer, String> obtenerMapaUrlsImagenes(Categoria categoria, String codigoSku) {
        Map<Integer, String> mapaSlots = new LinkedHashMap<>();
        if (codigoSku == null || codigoSku.isBlank()) {
            return mapaSlots;
        }

        String rutaRelativaSegmentada = construirRutaRelativaTaxonomia(categoria, codigoSku);
        Path carpetaProducto = baseStorageDir.resolve(rutaRelativaSegmentada).normalize();

        if (Files.isDirectory(carpetaProducto)) {
            for (int slot = 1; slot <= 4; slot++) {
                Path archivoSlot = buscarArchivoSlot(carpetaProducto, slot);
                if (archivoSlot != null) {
                    String urlPublica = "/uploads/productos/"
                            + rutaRelativaSegmentada.replace('\\', '/')
                            + "/" + archivoSlot.getFileName().toString();
                    mapaSlots.put(slot, urlPublica);
                }
            }
        }
        return mapaSlots;
    }

    /**
     * Carga en el objeto Producto las URLs públicas de sus fotografías (slots 1..4) para la vista.
     */
    public void cargarImagenesEnProducto(Producto producto) {
        if (producto == null || producto.getCodigoSku() == null) {
            return;
        }

        String rutaRelativaSegmentada = construirRutaRelativaTaxonomia(producto.getCategoria(), producto.getCodigoSku());
        Path carpetaProducto = baseStorageDir.resolve(rutaRelativaSegmentada).normalize();

        Map<Integer, String> mapaSlots = new LinkedHashMap<>();
        if (Files.isDirectory(carpetaProducto)) {
            for (int slot = 1; slot <= 4; slot++) {
                Path archivoSlot = buscarArchivoSlot(carpetaProducto, slot);
                if (archivoSlot != null) {
                    long lastMod = obtenerTimestampModificacion(archivoSlot);
                    String urlPublica = "/uploads/productos/"
                            + rutaRelativaSegmentada.replace('\\', '/')
                            + "/" + archivoSlot.getFileName().toString()
                            + "?v=" + lastMod;
                    mapaSlots.put(slot, urlPublica);
                }
            }
        }

        String urlPrincipal = mapaSlots.get(1);
        if (urlPrincipal == null && !mapaSlots.isEmpty()) {
            urlPrincipal = mapaSlots.values().iterator().next();
        }

        producto.setImagenPrincipalUrl(urlPrincipal);
        producto.setTotalImagenes(mapaSlots.size());
        producto.setImagenesSlotsJson(serializarMapaSlotsJson(mapaSlots));
    }

    private void guardarArchivoSlot(Path carpetaProducto, int slot, MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            return;
        }

        String extension = extraerExtensionValida(archivo);
        try {
            Files.createDirectories(carpetaProducto);
            // Eliminar imagen previa en este mismo slot (por si antes era .png y ahora sube .jpg)
            eliminarArchivoSlot(carpetaProducto, slot);

            String nombreArchivo = "img-" + slot + "." + extension;
            Path destino = carpetaProducto.resolve(nombreArchivo).normalize();
            if (!destino.startsWith(baseStorageDir)) {
                throw new IllegalArgumentException("Ruta de almacenamiento inválida.");
            }

            try (InputStream in = archivo.getInputStream()) {
                Files.copy(in, destino, StandardCopyOption.REPLACE_EXISTING);
            }
            LOG.info("Fotografía guardada en slot {} -> {}", slot, destino);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error al guardar la fotografía del Bloque " + slot + ": " + e.getMessage());
        }
    }

    private void eliminarArchivoSlot(Path carpetaProducto, int slot) {
        if (!Files.isDirectory(carpetaProducto)) {
            return;
        }
        String prefijo = "img-" + slot + ".";
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(carpetaProducto, prefijo + "*")) {
            for (Path p : stream) {
                Files.deleteIfExists(p);
            }
        } catch (IOException e) {
            LOG.warn("No se pudo eliminar archivo previo del slot {}: {}", slot, e.getMessage());
        }
    }

    private Path buscarArchivoSlot(Path carpetaProducto, int slot) {
        String prefijo = "img-" + slot + ".";
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(carpetaProducto, prefijo + "*")) {
            for (Path p : stream) {
                if (Files.isRegularFile(p)) {
                    return p;
                }
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    /**
     * Construye la ruta relativa segmentada por la taxonomía de categorías y el SKU:
     * Ejemplo: "electronica/computacion/laptops/ELC-COM-LAP-HP-0001"
     * Ejemplo: "ropa/hombre/camisas/ROP-HOM-CAM-NIK-0047"
     */
    public String construirRutaRelativaTaxonomia(Categoria categoria, String codigoSku) {
        LinkedList<String> segmentos = new LinkedList<>();
        Categoria actual = categoria;
        int guard = 0;
        while (actual != null && guard < 3) {
            segmentos.addFirst(slugifySegmento(actual.getNombreCategoria()));
            actual = actual.getCategoriaPadre();
            guard++;
        }
        if (segmentos.isEmpty()) {
            segmentos.add("sin-categoria");
        }
        String skuSanitizado = codigoSku.trim().replaceAll("[^A-Za-z0-9_-]", "");
        segmentos.add(skuSanitizado);
        return String.join("/", segmentos);
    }

    private Path resolverCarpetaProducto(Categoria categoria, String codigoSku) {
        String rutaRelativa = construirRutaRelativaTaxonomia(categoria, codigoSku);
        Path carpeta = baseStorageDir.resolve(rutaRelativa).normalize();
        if (!carpeta.startsWith(baseStorageDir)) {
            throw new IllegalArgumentException("Ruta de carpeta de producto fuera del directorio permitido.");
        }
        return carpeta;
    }

    private String extraerExtensionValida(MultipartFile archivo) {
        String original = archivo.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf('.') + 1).trim().toLowerCase(Locale.ROOT);
        }
        if (ext.isEmpty() || !EXTENSIONES_PERMITIDAS.contains(ext)) {
            String contentType = archivo.getContentType();
            if (contentType != null) {
                if (contentType.contains("png")) ext = "png";
                else if (contentType.contains("webp")) ext = "webp";
                else if (contentType.contains("gif")) ext = "gif";
                else if (contentType.contains("jpeg") || contentType.contains("jpg")) ext = "jpg";
            }
        }
        if (ext.isEmpty() || !EXTENSIONES_PERMITIDAS.contains(ext)) {
            throw new IllegalArgumentException("Formato de imagen no soportado. Use PNG, JPG o WEBP.");
        }
        return ext.equals("jpeg") ? "jpg" : ext;
    }

    private String slugifySegmento(String texto) {
        if (texto == null || texto.isBlank()) {
            return "general";
        }
        String sinAcentos = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String slug = sinAcentos.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return slug.isEmpty() ? "general" : slug;
    }

    private String serializarMapaSlotsJson(Map<Integer, String> mapaSlots) {
        if (mapaSlots.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        boolean primero = true;
        for (Map.Entry<Integer, String> entry : mapaSlots.entrySet()) {
            if (!primero) {
                sb.append(",");
            }
            sb.append("\"").append(entry.getKey()).append("\":\"")
                    .append(entry.getValue().replace("\\", "\\\\").replace("\"", "\\\""))
                    .append("\"");
            primero = false;
        }
        sb.append("}");
        return sb.toString();
    }

    private long obtenerTimestampModificacion(Path archivo) {
        try {
            return Files.getLastModifiedTime(archivo).toMillis();
        } catch (IOException e) {
            return System.currentTimeMillis();
        }
    }

    private void eliminarCarpetaRecursiva(Path carpeta) {
        try (Stream<Path> walk = Files.walk(carpeta)) {
            walk.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                }
            });
        } catch (IOException ignored) {
        }
    }

    private void limpiarDirectoriosPadresVacios(Path dir) {
        Path actual = dir;
        while (actual != null && !actual.equals(baseStorageDir) && actual.startsWith(baseStorageDir)) {
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(actual)) {
                if (entries.iterator().hasNext()) {
                    break;
                }
                Files.deleteIfExists(actual);
                actual = actual.getParent();
            } catch (IOException e) {
                break;
            }
        }
    }

    private Path resolverDirectorioBase(String configuredDir) {
        Path cwd = Paths.get("").toAbsolutePath().normalize();
        // Si la aplicación se ejecuta desde la raíz del monorepo (ecommerce-progra-2),
        // ubicar dentro de ecommerce-webapp/uploads/productos para mantener consistencia
        if (cwd.getFileName() != null
                && !cwd.getFileName().toString().equalsIgnoreCase("ecommerce-webapp")
                && Files.isDirectory(cwd.resolve("ecommerce-webapp").resolve("src"))) {
            return cwd.resolve("ecommerce-webapp").resolve(configuredDir).toAbsolutePath().normalize();
        }
        return cwd.resolve(configuredDir).toAbsolutePath().normalize();
    }
}
