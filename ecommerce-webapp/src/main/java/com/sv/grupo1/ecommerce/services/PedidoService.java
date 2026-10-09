package com.sv.grupo1.ecommerce.services;

import com.sv.grupo1.ecommerce.dao.*;
import com.sv.grupo1.ecommerce.entities.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

//@Service
public class PedidoService {

    //@Autowired
    private PedidoRepository pedidoRepository;

    //@Autowired
    private DetallePedidoRepository detallePedidoRepository;

    //@Autowired
    private EnvioRepository envioRepository;

    //@Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

    //@Autowired
    private ProductoRepository productoRepository;

    /**
     * Reduce el stock, registra el movimientos de inventario, crea el envío
     * y actualiza el estado del pedido a DESPACHADO.
     */
    @Transactional
    public Envio despacharPedido(Integer idPedido, String empresaTransporte, String numeroSeguimiento) {

        //Pedido pedido = pedidoRepository.findById(idPedido).orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado: " + idPedido));
        Pedido pedido = new Pedido();

        if (!"PAGADO".equalsIgnoreCase(pedido.getEstadoPedido())) {
            throw new IllegalStateException(
                    "Solo se pueden despachar pedidos en estado PAGADO. Estado actual: " + pedido.getEstadoPedido());
        }

        List<DetallePedido> detalles = detallePedidoRepository.findByPedido_IdPedido(idPedido);
        if (detalles.isEmpty()) {
            throw new IllegalStateException("El pedido no tiene detalles registrados.");
        }

        // Reduce el stock y registrar movimiento de inventario por cada producto
        for (DetallePedido detalle : detalles) {
            Producto producto = detalle.getProducto();

            if (producto.getStockDisponible() < detalle.getCantidad()) {
                throw new IllegalStateException(
                        "Stock insuficiente para despachar el producto: " + producto.getNombreProducto());
            }

            producto.setStockDisponible(producto.getStockDisponible() - detalle.getCantidad());
            //productoRepository.save(producto);

            MovimientoInventario movimiento = new MovimientoInventario();
            movimiento.setProducto(producto);
            movimiento.setTipoMovimiento("SALIDA");
            movimiento.setCantidad(detalle.getCantidad());
            movimiento.setMotivo("Despacho de pedido #" + idPedido);
            movimiento.setPedido(pedido);
            //movimientoInventarioRepository.save(movimiento);
        }

        // Crear el registro de envío
        Envio envio = new Envio();
        envio.setPedido(pedido);
        envio.setEmpresaTransporte(empresaTransporte);
        envio.setNumeroSeguimiento(numeroSeguimiento);
        envio.setEstadoEnvio("DESPACHADO");
        envio.setFechaDespacho(OffsetDateTime.now());
        //envioRepository.save(envio);

        // Actualizar estado del pedido
        pedido.setEstadoPedido("DESPACHADO");
        //pedidoRepository.save(pedido);

        return envio;
    }
}