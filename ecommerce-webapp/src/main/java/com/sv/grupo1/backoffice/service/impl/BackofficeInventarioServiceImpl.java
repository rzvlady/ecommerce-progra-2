package com.sv.grupo1.backoffice.service.impl;

import com.sv.grupo1.backoffice.dto.MovimientoInventarioDTO;
import com.sv.grupo1.backoffice.service.BackofficeInventarioService;
import com.sv.grupo1.backoffice.service.ProductoImagenStorageService;
import com.sv.grupo1.backoffice.task.StockMonitorTaskImpl;
import com.sv.grupo1.ecommerce.dao.CategoriaRepository;
import com.sv.grupo1.ecommerce.dao.MarcaRepository;
import com.sv.grupo1.ecommerce.dao.MovimientoInventarioRepository;
import com.sv.grupo1.ecommerce.dao.ProductoRepository;
import com.sv.grupo1.ecommerce.entities.catalogo.Categoria;
import com.sv.grupo1.ecommerce.entities.catalogo.Marca;
import com.sv.grupo1.ecommerce.entities.core.MovimientoInventario;
import com.sv.grupo1.ecommerce.entities.core.Producto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class BackofficeInventarioServiceImpl implements BackofficeInventarioService {

    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MarcaRepository marcaRepository;
    private final StockMonitorTaskImpl stockMonitorTaskImpl;
    private final ProductoImagenStorageService imagenStorageService;

    public BackofficeInventarioServiceImpl(
            ProductoRepository productoRepository,
            MovimientoInventarioRepository movimientoRepository,
            CategoriaRepository categoriaRepository,
            MarcaRepository marcaRepository,
            StockMonitorTaskImpl stockMonitorTaskImpl,
            ProductoImagenStorageService imagenStorageService) {
        this.productoRepository = productoRepository;
        this.movimientoRepository = movimientoRepository;
        this.categoriaRepository = categoriaRepository;
        this.marcaRepository = marcaRepository;
        this.stockMonitorTaskImpl = stockMonitorTaskImpl;
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
    public List<MovimientoInventario> listarMovimientos() {
        List<MovimientoInventario> movimientos = movimientoRepository.findAllConProducto();
        movimientos.forEach(m -> {
            if (m.getProducto() != null) {
                imagenStorageService.cargarImagenesEnProducto(m.getProducto());
            }
        });
        return movimientos;
    }

    @Override
    public List<Producto> obtenerAlertasMonitor() {
        List<Producto> alertados = stockMonitorTaskImpl.getProductosBajoStockAlertados();
        if (alertados == null || alertados.isEmpty()) {
            stockMonitorTaskImpl.escanearInventario();
            alertados = stockMonitorTaskImpl.getProductosBajoStockAlertados();
        }
        if (alertados != null) {
            alertados.forEach(imagenStorageService::cargarImagenesEnProducto);
        }
        return alertados;
    }

    @Override
    public void ejecutarEscaneoMonitor() {
        stockMonitorTaskImpl.escanearInventario();
    }

    @Override
    @Transactional
    public Producto registrarMovimiento(MovimientoInventarioDTO dto) {
        Producto producto = productoRepository.findByIdConRelaciones(dto.getIdProducto())
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el producto seleccionado."));

        String tipo = dto.getTipoMovimiento() != null
                ? dto.getTipoMovimiento().trim().toUpperCase(Locale.ROOT)
                : "";

        if (!"ENTRADA".equals(tipo) && !"SALIDA".equals(tipo)) {
            throw new IllegalArgumentException("El tipo de movimiento debe ser ENTRADA o SALIDA.");
        }

        int cantidad = dto.getCantidad() != null ? dto.getCantidad() : 0;
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad del movimiento debe ser mayor a 0.");
        }

        int stockActual = producto.getStockDisponible() != null ? producto.getStockDisponible() : 0;
        int nuevoStock;

        if ("ENTRADA".equals(tipo)) {
            nuevoStock = stockActual + cantidad;
        } else {
            if (cantidad > stockActual) {
                throw new IllegalArgumentException(
                        "Stock insuficiente para realizar la salida. El producto '"
                                + producto.getNombreProducto() + "' (" + producto.getCodigoSku()
                                + ") solo tiene " + stockActual + " unidades disponibles.");
            }
            nuevoStock = stockActual - cantidad;
        }

        String motivoBase = dto.getMotivo() != null && !dto.getMotivo().isBlank()
                ? dto.getMotivo().trim().toUpperCase(Locale.ROOT)
                : "AJUSTE";

        String observacion = dto.getObservacion() != null ? dto.getObservacion().trim() : "";
        String motivoFinal = observacion.isEmpty() ? motivoBase : motivoBase + " - " + observacion;

        producto.setStockDisponible(nuevoStock);
        productoRepository.save(producto);

        MovimientoInventario movimiento = new MovimientoInventario();
        movimiento.setProducto(producto);
        movimiento.setTipoMovimiento(tipo);
        movimiento.setCantidad(cantidad);
        movimiento.setMotivo(motivoFinal);
        movimientoRepository.save(movimiento);

        // Actualizar de inmediato el monitor concurrente de alertas de stock
        stockMonitorTaskImpl.escanearInventario();
        imagenStorageService.cargarImagenesEnProducto(producto);

        return producto;
    }

    @Override
    @Transactional
    public Producto actualizarStockMinimo(Integer idProducto, Integer nuevoStockMinimo) {
        if (nuevoStockMinimo == null || nuevoStockMinimo < 0) {
            throw new IllegalArgumentException("El stock mínimo no puede ser negativo.");
        }

        Producto producto = productoRepository.findByIdConRelaciones(idProducto)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + idProducto));

        producto.setStockMinimo(nuevoStockMinimo);
        productoRepository.save(producto);

        // Actualizar de inmediato el monitor concurrente de alertas de stock
        stockMonitorTaskImpl.escanearInventario();
        imagenStorageService.cargarImagenesEnProducto(producto);

        return producto;
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
}
