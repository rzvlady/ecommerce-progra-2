package com.sv.grupo1.ecommerce.services;

import com.sv.grupo1.ecommerce.dao.ProductoRepository;
import com.sv.grupo1.ecommerce.entities.core.DetallePedido;
import com.sv.grupo1.ecommerce.entities.core.Producto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CarritoService {

    @Autowired
    private ProductoRepository productoRepository;

    // Colección en memoria para manejar carritos por "id de sesión o cliente"
    // Justificación Punto 7 (Uso de colecciones)
    private Map<Integer, Map<Integer, DetallePedido>> carritosActivos = new HashMap<>();

    public void agregarProductoAlCarrito(Integer idCliente, Integer idProducto, Integer cantidad) {
        // Obtenemos o creamos el carrito del cliente
        Map<Integer, DetallePedido> carrito = carritosActivos.computeIfAbsent(idCliente, k -> new HashMap<>());

        Optional<Producto> optProducto = productoRepository.findById(idProducto);
        if (optProducto.isPresent()) {
            Producto producto = optProducto.get();

            // Verificamos si la cantidad solicitada no supera el stock
            if (producto.getStockDisponible() < cantidad) {
                throw new IllegalArgumentException("Stock insuficiente para el producto: " + producto.getNombreProducto());
            }

            if (carrito.containsKey(idProducto)) {
                // Actualizar detalle existente
                DetallePedido detalleExistente = carrito.get(idProducto);
                detalleExistente.setCantidad(detalleExistente.getCantidad() + cantidad);
                detalleExistente.setSubtotal(detalleExistente.getPrecioUnitario().multiply(new BigDecimal(detalleExistente.getCantidad())));
            } else {
                // Crear nuevo detalle
                DetallePedido nuevoDetalle = new DetallePedido();
                nuevoDetalle.setProducto(producto);
                nuevoDetalle.setCantidad(cantidad);
                nuevoDetalle.setPrecioUnitario(producto.getPrecioVenta());
                nuevoDetalle.setSubtotal(producto.getPrecioVenta().multiply(new BigDecimal(cantidad)));
                carrito.put(idProducto, nuevoDetalle);
            }
        } else {
            throw new IllegalArgumentException("Producto no encontrado");
        }
    }

    public List<DetallePedido> obtenerCarrito(Integer idCliente) {
        Map<Integer, DetallePedido> carrito = carritosActivos.getOrDefault(idCliente, new HashMap<>());
        return new ArrayList<>(carrito.values());
    }

    public void eliminarProducto(Integer idCliente, Integer idProducto) {
        Map<Integer, DetallePedido> carrito = carritosActivos.get(idCliente);
        if (carrito != null) {
            carrito.remove(idProducto);
        }
    }

    public void vaciarCarrito(Integer idCliente) {
        carritosActivos.remove(idCliente);
    }

    public BigDecimal calcularTotal(Integer idCliente) {
        Map<Integer, DetallePedido> carrito = carritosActivos.get(idCliente);
        if (carrito == null) return BigDecimal.ZERO;

        return carrito.values().stream()
                .map(DetallePedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
