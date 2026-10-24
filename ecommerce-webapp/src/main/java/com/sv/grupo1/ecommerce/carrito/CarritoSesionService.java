package com.sv.grupo1.ecommerce.carrito;

import com.sv.grupo1.ecommerce.dao.ProductoRepository;
import com.sv.grupo1.ecommerce.entities.core.Producto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;

@Service
public class CarritoSesionService {

    private final ProductoRepository productoRepository;
    private final CarritoSesion carrito;
    private final BigDecimal envioFijo;
    private final BigDecimal envioGratisDesde;

    public CarritoSesionService(ProductoRepository productoRepository,
                          CarritoSesion carrito,
                          @Value("${ecommerce.carrito.envio-fijo:3.50}") BigDecimal envioFijo,
                          @Value("${ecommerce.carrito.envio-gratis-desde:50.00}") BigDecimal envioGratisDesde) {
        this.productoRepository = productoRepository;
        this.carrito = carrito;
        this.envioFijo = envioFijo;
        this.envioGratisDesde = envioGratisDesde;
    }

    public Collection<CarritoItem> items() {
        return new ArrayList<>(carrito.getItems());
    }

    /** Agrega (o suma) unidades de un producto, respetando el stock disponible. */
    public void agregar(Integer idProducto, int cantidad) {
        if (cantidad < 1) {
            throw new IllegalArgumentException("La cantidad debe ser al menos 1.");
        }
        Producto p = buscar(idProducto);
        CarritoItem item = carrito.get(idProducto);
        int nueva = (item == null ? 0 : item.getCantidad()) + cantidad;
        validarStock(p, nueva);

        if (item == null) {
            carrito.poner(new CarritoItem(idProducto, p.getCodigoSku(), p.getNombreProducto(),
                    precio(p), nueva, stock(p)));
        } else {
            item.setCantidad(nueva);
            item.setPrecioUnitario(precio(p));
            item.setStockDisponible(stock(p));
        }
    }

    /** Cambia la cantidad de un artículo; con 0 o menos lo elimina. */
    public void actualizar(Integer idProducto, int cantidad) {
        CarritoItem item = carrito.get(idProducto);
        if (item == null) {
            return;
        }
        if (cantidad < 1) {
            carrito.quitar(idProducto);
            return;
        }
        Producto p = buscar(idProducto);
        validarStock(p, cantidad);
        item.setCantidad(cantidad);
        item.setPrecioUnitario(precio(p));
        item.setStockDisponible(stock(p));
    }

    public void eliminar(Integer idProducto) {
        carrito.quitar(idProducto);
    }

    public void vaciar() {
        carrito.vaciar();
    }

    /** Subtotal, envío y total. Envío fijo, gratis al superar el monto configurado. */
    public CarritoResumen resumen() {
        BigDecimal subtotal = BigDecimal.ZERO;
        int unidades = 0;
        for (CarritoItem i : carrito.getItems()) {
            subtotal = subtotal.add(i.getSubtotal());
            unidades += i.getCantidad();
        }
        BigDecimal envio = (carrito.estaVacio() || subtotal.compareTo(envioGratisDesde) >= 0)
                ? BigDecimal.ZERO : envioFijo;
        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        envio = envio.setScale(2, RoundingMode.HALF_UP);
        return new CarritoResumen(subtotal, envio, subtotal.add(envio), unidades);
    }

    // ---- helpers -------------------------------------------------------

    private Producto buscar(Integer idProducto) {
        return productoRepository.findById(idProducto)
                .orElseThrow(() -> new NoSuchElementException("El producto no existe."));
    }

    private int stock(Producto p) {
        Integer s = p.getStockDisponible();
        return s == null ? 0 : s;
    }

    private void validarStock(Producto p, int solicitado) {
        if (p.getEstado() != null && p.getEstado() != 'A') {
            throw new IllegalStateException("\"" + p.getNombreProducto() + "\" ya no esta disponible.");
        }
        int disponible = stock(p);
        if (disponible < 1) {
            throw new IllegalStateException("\"" + p.getNombreProducto() + "\" no tiene stock disponible.");
        }
        if (solicitado > disponible) {
            throw new IllegalArgumentException("Solo hay " + disponible + " unidad(es) de \""
                    + p.getNombreProducto() + "\".");
        }
    }

    private BigDecimal precio(Producto p) {
        BigDecimal pv = p.getPrecioVenta();
        if (pv == null) {
            throw new IllegalStateException("El producto no tiene precio de venta.");
        }
        return pv.setScale(2, RoundingMode.HALF_UP);
    }
}
