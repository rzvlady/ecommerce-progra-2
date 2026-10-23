package com.sv.grupo1.ecommerce;

import com.sv.grupo1.backoffice.dto.MovimientoInventarioDTO;
import com.sv.grupo1.backoffice.dto.ProductoRegistroDTO;
import com.sv.grupo1.backoffice.service.BackofficeProductoService;
import com.sv.grupo1.backoffice.service.ProductoImagenStorageService;
import com.sv.grupo1.ecommerce.entities.catalogo.Categoria;
import com.sv.grupo1.ecommerce.entities.catalogo.Marca;
import com.sv.grupo1.ecommerce.entities.core.MovimientoInventario;
import com.sv.grupo1.ecommerce.entities.core.Producto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ECommerceApplicationTests {

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private BackofficeProductoService backofficeProductoService;

    @Autowired
    private ProductoImagenStorageService imagenStorageService;

    @Test
    void testParseInventarioTemplateWithRealData() {
        MockServletContext servletContext = new MockServletContext();
        MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
        MockHttpServletResponse response = new MockHttpServletResponse();

        var application = JakartaServletWebApplication.buildApplication(servletContext);
        var webExchange = application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);

        Categoria depto = new Categoria();
        depto.setIdCategoria(100);
        depto.setNombreCategoria("Tecnología");

        Categoria subcat = new Categoria();
        subcat.setIdCategoria(10);
        subcat.setNombreCategoria("Computación");
        subcat.setCategoriaPadre(depto);

        Categoria cat = new Categoria();
        cat.setIdCategoria(1);
        cat.setNombreCategoria("Laptops");
        cat.setCategoriaPadre(subcat);

        Marca marca = new Marca();
        marca.setIdMarca(1);
        marca.setNombreMarca("Apple");

        Producto p1 = new Producto();
        p1.setIdProducto(1);
        p1.setNombreProducto("MacBook Air");
        p1.setCodigoSku("COM-APP-0001");
        p1.setDescripcionProducto("Laptop ultradelgada M2");
        p1.setStockDisponible(0);
        p1.setStockMinimo(5);
        p1.setPrecioCosto(BigDecimal.valueOf(800));
        p1.setPrecioVenta(BigDecimal.valueOf(1100));
        p1.setCategoria(cat);
        p1.setMarca(marca);
        p1.setImagenPrincipalUrl("/uploads/productos/tecnologia/computacion/laptops/COM-APP-0001/img-1.webp");
        p1.setTotalImagenes(3);
        p1.setMetadata("{\"RAM\":\"16GB\",\"Color\":\"Space Gray\",\"Almacenamiento\":\"512GB SSD\"}");

        Producto p2 = new Producto();
        p2.setIdProducto(2);
        p2.setNombreProducto("MacBook Pro");
        p2.setCodigoSku("COM-APP-0002");
        p2.setDescripcionProducto("Laptop profesional M3 Pro");
        p2.setStockDisponible(3);
        p2.setStockMinimo(5);
        p2.setPrecioCosto(BigDecimal.valueOf(1500));
        p2.setPrecioVenta(BigDecimal.valueOf(2000));
        p2.setCategoria(cat);
        p2.setMarca(marca);
        p2.setTotalImagenes(0);
        p2.setMetadata("{\"RAM\":\"32GB\",\"Pantalla\":\"16 pulgadas\"}");

        Producto p3 = new Producto();
        p3.setIdProducto(3);
        p3.setNombreProducto("iPad Air");
        p3.setCodigoSku("COM-APP-0003");
        p3.setDescripcionProducto("Tablet potente M2");
        p3.setStockDisponible(20);
        p3.setStockMinimo(5);
        p3.setPrecioCosto(BigDecimal.valueOf(500));
        p3.setPrecioVenta(BigDecimal.valueOf(700));
        p3.setCategoria(cat);
        p3.setMarca(marca);
        p3.setTotalImagenes(1);
        p3.setImagenPrincipalUrl("/uploads/productos/tecnologia/computacion/laptops/COM-APP-0003/img-1.webp");

        MovimientoInventario m1 = new MovimientoInventario();
        m1.setIdMovimiento(1);
        m1.setProducto(p1);
        m1.setTipoMovimiento("ENTRADA");
        m1.setCantidad(10);
        m1.setMotivo("COMPRA_PROVEEDOR");
        m1.setFechaMovimiento(OffsetDateTime.now());

        MovimientoInventario m2 = new MovimientoInventario();
        m2.setIdMovimiento(2);
        m2.setProducto(p2);
        m2.setTipoMovimiento("SALIDA");
        m2.setCantidad(2);
        m2.setMotivo("AJUSTE");
        m2.setFechaMovimiento(OffsetDateTime.now());

        context.setVariable("valorTotalInventario", BigDecimal.valueOf(14500));
        context.setVariable("unidadesTotalesBodega", 23L);
        context.setVariable("totalAlertasCriticas", 2L);
        context.setVariable("productosStockBajo", 1L);
        context.setVariable("productosAgotados", 1L);
        context.setVariable("totalMovimientos", 2L);
        context.setVariable("totalEntradas", 1L);
        context.setVariable("totalSalidas", 1L);
        context.setVariable("productos", List.of(p1, p2, p3));
        context.setVariable("movimientos", List.of(m1, m2));
        context.setVariable("alertasMonitor", List.of(p1, p2));
        context.setVariable("categorias", List.of(cat));
        context.setVariable("marcas", List.of(marca));
        context.setVariable("movimientoDTO", new MovimientoInventarioDTO());
        context.setVariable("tabActiva", "existencias");
        context.setVariable("abrirModalMovimiento", false);

        String result = templateEngine.process("backoffice/inventario/lista", context);
        assertNotNull(result);
        assertTrue(result.contains("MacBook Air"));
        assertTrue(result.contains("COM-APP-0001"));
        assertTrue(result.contains("N3"));
        assertTrue(result.contains("Tecnología › Computación"));
        assertTrue(result.contains("img-1.webp"));
        assertTrue(result.contains("+2"));
        assertTrue(result.contains("Space Gray"));
        System.out.println("INVENTARIO TEMPLATE FULLY VERIFIED WITH TAXONOMY, IMAGES AND METADATA! Length: " + result.length());
    }

    @Test
    void testProductosTemplateAndImageStorageSegmentation() {
        List<Producto> productos = backofficeProductoService.listarProductos();
        List<Categoria> categorias = backofficeProductoService.listarCategorias();
        List<Marca> marcas = backofficeProductoService.listarMarcas();

        assertTrue(!productos.isEmpty());
        assertTrue(!categorias.isEmpty());
        assertTrue(!marcas.isEmpty());
        assertTrue(productos.stream().anyMatch(p -> "ELC-COM-LAP-HP-0001".equals(p.getCodigoSku())));

        // Verificar ruta taxonómica segmentada para imágenes (3 niveles: Departamento -> Categoría -> Subcategoría -> SKU)
        Categoria dep = new Categoria();
        dep.setNombreCategoria("ROPA");
        Categoria cat = new Categoria();
        cat.setNombreCategoria("HOMBRE");
        cat.setCategoriaPadre(dep);
        Categoria sub = new Categoria();
        sub.setNombreCategoria("CAMISAS");
        sub.setCategoriaPadre(cat);

        String rutaSegmentada = imagenStorageService.construirRutaRelativaTaxonomia(sub, "ROP-HOM-CAM-NIK-0047");
        assertTrue(rutaSegmentada.equals("ropa/hombre/camisas/ROP-HOM-CAM-NIK-0047"));

        // Verificar guardado físico y carga de URL en un slot de prueba y posterior limpieza
        Producto prodPrueba = new Producto();
        prodPrueba.setCategoria(sub);
        prodPrueba.setCodigoSku("ROP-HOM-CAM-TEST-9999");

        ProductoRegistroDTO dtoPrueba = new ProductoRegistroDTO();
        dtoPrueba.setImagenSlot1(new org.springframework.mock.web.MockMultipartFile(
                "imagenSlot1", "camisa-principal.png", "image/png", new byte[]{(byte) 137, 80, 78, 71}));
        dtoPrueba.setImagenSlot2(new org.springframework.mock.web.MockMultipartFile(
                "imagenSlot2", "camisa-detalle.jpg", "image/jpeg", new byte[]{(byte) 255, (byte) 216, (byte) 255}));

        imagenStorageService.procesarImagenesProducto(prodPrueba, dtoPrueba);
        imagenStorageService.cargarImagenesEnProducto(prodPrueba);

        assertNotNull(prodPrueba.getImagenPrincipalUrl());
        assertTrue(prodPrueba.getImagenPrincipalUrl().startsWith("/uploads/productos/ropa/hombre/camisas/ROP-HOM-CAM-TEST-9999/img-1.png"));
        assertTrue(prodPrueba.getTotalImagenes() == 2);
        assertTrue(prodPrueba.getImagenesSlotsJson().contains("\"1\":"));
        assertTrue(prodPrueba.getImagenesSlotsJson().contains("\"2\":"));

        // Limpiar carpeta del producto de prueba
        imagenStorageService.eliminarCarpetaProducto(sub, "ROP-HOM-CAM-TEST-9999");
    }
}
