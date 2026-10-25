package com.sv.grupo1.ecommerce.controllers;

import com.sv.grupo1.ecommerce.dto.EspecificacionItemDTO;
import com.sv.grupo1.ecommerce.services.ProductoService;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * @author Miguel-A-Serrano
 */
@Controller
@RequestMapping("/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    public String catalogo(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) Integer idCategoria,
            @RequestParam(required = false) Integer idMarca,
            Model model) {

        model.addAttribute("productos", productoService.filtrarProductos(busqueda, idCategoria, idMarca));
        model.addAttribute("categorias", productoService.listarCategorias());
        model.addAttribute("marcas", productoService.listarMarcas());
        model.addAttribute("busqueda", busqueda);
        model.addAttribute("idCategoriaSeleccionada", idCategoria);
        model.addAttribute("idMarcaSeleccionada", idMarca);

        return "ecommerce/pages/productos";
    }

    @GetMapping("/{id}")
    public String detalleProducto(@PathVariable("id") Integer id, Model model) {
        var productoOpt = productoService.obtenerPorId(id);
        if (productoOpt.isEmpty()) {
            return "redirect:/productos";
        }
        var producto = productoOpt.get();
        model.addAttribute("producto", producto);
        model.addAttribute("tieneImagenes", producto.isTieneImagenes());
        model.addAttribute("imagenesSlots", producto.getImagenesSlots());

        List<EspecificacionItemDTO> especificaciones = procesarEspecificaciones(producto.getMetadata());
        model.addAttribute("especificaciones", especificaciones);

        return "ecommerce/pages/producto-detalle";
    }

    private List<EspecificacionItemDTO> procesarEspecificaciones(String metadata) {
        List<EspecificacionItemDTO> lista = new ArrayList<>();
        if (metadata == null || metadata.isBlank()) {
            return lista;
        }

        Map<String, String> rawMap = new LinkedHashMap<>();
        try {
            JsonParser parser = JsonParserFactory.getJsonParser();
            Map<String, Object> parsed = parser.parseMap(metadata);
            if (parsed != null) {
                for (Map.Entry<String, Object> entry : parsed.entrySet()) {
                    if (!"imagenes".equalsIgnoreCase(entry.getKey()) && entry.getValue() != null) {
                        rawMap.put(entry.getKey(), String.valueOf(entry.getValue()));
                    }
                }
            }
        } catch (Exception e) {
            Matcher m = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]+)\"").matcher(metadata);
            while (m.find()) {
                if (!"imagenes".equalsIgnoreCase(m.group(1))) {
                    rawMap.put(m.group(1), m.group(2));
                }
            }
        }

        for (Map.Entry<String, String> entry : rawMap.entrySet()) {
            String etiqueta = formatearEtiquetaEspecificacion(entry.getKey());
            String icono = resolverIconoEspecificacion(entry.getKey());
            lista.add(new EspecificacionItemDTO(entry.getKey(), etiqueta, entry.getValue(), icono));
        }

        return lista;
    }

    private String formatearEtiquetaEspecificacion(String clave) {
        if (clave == null || clave.isBlank()) return "";
        String limpia = clave.replace('_', ' ').replace('-', ' ').trim();
        String[] palabras = limpia.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String p : palabras) {
            if (!p.isEmpty()) {
                if (!sb.isEmpty()) sb.append(" ");
                sb.append(Character.toUpperCase(p.charAt(0)))
                  .append(p.substring(1).toLowerCase());
            }
        }
        return sb.toString();
    }

    private String resolverIconoEspecificacion(String clave) {
        if (clave == null) return "bi-sliders";
        String k = clave.toLowerCase();
        if (k.contains("procesador") || k.contains("chip") || k.contains("cpu")) return "bi-cpu";
        if (k.contains("ram") || k.contains("memoria")) return "bi-memory";
        if (k.contains("almacenamiento") || k.contains("disco") || k.contains("ssd") || k.contains("capacidad")) return "bi-device-ssd";
        if (k.contains("pantalla") || k.contains("display") || k.contains("resolucion") || k.contains("panel") || k.contains("tamano")) return "bi-display";
        if (k.contains("bateria") || k.contains("potencia") || k.contains("carga") || k.contains("voltaje") || k.contains("salidas")) return "bi-lightning-charge";
        if (k.contains("conectividad") || k.contains("red") || k.contains("wifi") || k.contains("bluetooth") || k.contains("puerto") || k.contains("salida")) return "bi-wifi";
        if (k.contains("camara") || k.contains("sensor") || k.contains("lente")) return "bi-camera";
        if (k.contains("audio") || k.contains("sonido") || k.contains("parlante") || k.contains("microfono")) return "bi-soundwave";
        if (k.contains("enfriamiento") || k.contains("ventilador")) return "bi-fan";
        if (k.contains("compatibilidad") || k.contains("compatible")) return "bi-check2-square";
        if (k.contains("peso") || k.contains("dimensiones")) return "bi-box-seam";
        if (k.contains("material") || k.contains("estructura") || k.contains("resistencia")) return "bi-shield-check";
        if (k.contains("tipo_switch") || k.contains("switch")) return "bi-keyboard";
        if (k.contains("garantia")) return "bi-patch-check";
        if (k.contains("color")) return "bi-palette";
        return "bi-sliders";
    }
}
